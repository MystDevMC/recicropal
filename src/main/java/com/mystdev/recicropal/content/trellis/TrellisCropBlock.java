package com.mystdev.recicropal.content.trellis;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.Map;

public abstract class TrellisCropBlock extends CropBlock {
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final VinelikeProps PROPS =
            new VinelikeProps(3D, (e) -> e.getKey() != Direction.DOWN && e.getKey() != Direction.UP);
    private final Map<BlockState, VoxelShape> shapesCache;

    private final Block vineBlock;

    public TrellisCropBlock(Properties props, Block vineBlock) {
        super(props);
        this.vineBlock = vineBlock;
        this.registerDefaultState(PROPS.generateDefaultState(this.stateDefinition.any()).setValue(AGE, 0));
        this.shapesCache = PROPS.generateShapesCache(this.stateDefinition);
    }

    public @NotNull IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    protected abstract ItemLike getBaseSeedId();

    public int getMaxAge() {
        return TrellisCropBlock.MAX_AGE;
    }

    @Override
    public VoxelShape getShape(BlockState state,
                               BlockGetter blockGetter,
                               BlockPos pos,
                               CollisionContext ctx) {
        return shapesCache.get(state);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        PROPS.generateStateDefinition(stateBuilder).add(AGE);
    }

    protected boolean mayPlaceOn(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return state.is(BlockTags.DIRT);
    }


    public BlockState updateShape(BlockState state,
                                  Direction direction,
                                  BlockState otherState,
                                  LevelAccessor levelAccessor,
                                  BlockPos pos,
                                  BlockPos otherPos) {
        if (direction == Direction.DOWN || direction == Direction.UP) {
            return super.updateShape(state, direction, otherState, levelAccessor, pos, otherPos);
        }
        else {
            var blockstate = state;
            var sides = PROPS.streamProps().filter(e -> state.getValue(e.getValue())).toList();
            for (var side : sides) {
                var oldValue = state.getValue(side.getValue());
                if (oldValue) {
                    var attached = pos.relative(side.getKey());
                    var newValue = MultifaceBlock.canAttachTo(levelAccessor,
                                                              side.getKey(),
                                                              attached,
                                                              levelAccessor.getBlockState(attached));
                    if (!newValue) blockstate = state.setValue(VinelikeProps.PROPERTY_BY_DIRECTION.get(side.getKey()),
                                                               Boolean.FALSE);
                }
            }
            return blockstate;
        }
    }

    public boolean isRandomlyTicking(BlockState p_52288_) {
        return true;
    }

    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        if (!level.isAreaLoaded(pos, 1)) return;
        if (level.getRawBrightness(pos, 0) < 9) return;

        var random = randomSource.nextInt(2);
        if (!this.isMaxAge(state)) this.age(state, level, pos, randomSource);
        else if (this.isMaxAge(state)) {
            if (random == 0) this.spread(state, level, pos, randomSource, true);
            else this.attach(state, level, pos, randomSource, true);
        }
    }

    public void age(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        int i = this.getAge(state);
        float f = getGrowthSpeed(this, level, pos);
        if (ForgeHooks.onCropsGrowPre(level, pos, state, randomSource.nextInt((int) (25.0F / f) + 1) == 0)) {
            level.setBlock(pos, this.getStateForAge(i + 1), 2);
            ForgeHooks.onCropsGrowPost(level, pos, state);
        }
    }

    public void spread(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource, boolean gacha) {
        var sides = PROPS.streamProps().toList();
        var dirs = new ArrayList<Pair<BlockPos, Direction>>();
        sides.forEach(e -> {
            var side = e.getKey();
            var sideHasVines = state.getValue(e.getValue());

            var adj = pos.relative(side);
            var topAdj = adj.relative(Direction.UP);
            var cSide = side.getClockWise(Direction.Axis.Y);
            var cAdj = adj.relative(cSide);
            var ccSide = side.getCounterClockWise(Direction.Axis.Y);
            var ccAdj = adj.relative(ccSide);
            var top = pos.relative(Direction.UP);

            if (!level.isEmptyBlock(adj) && sideHasVines) {
                if (level.isEmptyBlock(top) &&
                        MultifaceBlock.canAttachTo(level, side, topAdj, level.getBlockState(topAdj))) {
                    dirs.add(new Pair<>(top, side));
                }
                else if (level.isEmptyBlock(topAdj) &&
                        MultifaceBlock.canAttachTo(level, Direction.DOWN, adj, level.getBlockState(adj))) {
                    dirs.add(new Pair<>(topAdj, Direction.DOWN));
                }
                if (level.isEmptyBlock(cAdj) &&
                        MultifaceBlock.canAttachTo(level, cSide.getOpposite(), adj, level.getBlockState(adj))) {
                    dirs.add(new Pair<>(cAdj, cSide.getOpposite()));
                }
                if (level.isEmptyBlock(ccAdj) &&
                        MultifaceBlock.canAttachTo(level, ccSide.getOpposite(), adj, level.getBlockState(adj))) {
                    dirs.add(new Pair<>(ccAdj, ccSide.getOpposite()));
                }
            }
            else if (level.isEmptyBlock(adj)) {
                var botAdj = adj.relative(Direction.DOWN);
                var bot = pos.relative(Direction.DOWN);
                if (MultifaceBlock.canAttachTo(level, Direction.DOWN, botAdj, level.getBlockState(botAdj))) {
                    dirs.add(new Pair<>(adj, Direction.DOWN));
                }
                else if (level.isEmptyBlock(botAdj) &&
                        MultifaceBlock.canAttachTo(level, side.getOpposite(), bot, level.getBlockState(bot))) {
                    dirs.add(new Pair<>(botAdj, side.getOpposite()));
                }
            }
        });
        float f = getGrowthSpeed(this, level, pos);

        if (dirs.size() == 0) return;
        var randomChoice = dirs.get(randomSource.nextInt(dirs.size()));
        if (gacha) {
            if (ForgeHooks.onCropsGrowPre(level,
                                          pos,
                                          state,
                                          randomSource.nextInt((int) (10F / f) + 1) == 0)) {
                level.setBlock(randomChoice.getA(),
                               vineBlock.defaultBlockState()
                                        .setValue(VinelikeProps.PROPERTY_BY_DIRECTION.get(randomChoice.getB()),
                                                  Boolean.TRUE)
                        , 2);
                ForgeHooks.onCropsGrowPost(level, pos, state);
            }
        }

        else {
            level.setBlock(randomChoice.getA(),
                           vineBlock.defaultBlockState()
                                    .setValue(VinelikeProps.PROPERTY_BY_DIRECTION.get(randomChoice.getB()),
                                              Boolean.TRUE)
                    , 2);
        }
    }

    public void attach(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource, boolean gacha) {
        var sides = PROPS.streamProps()
                         .filter(e -> state.getValue(e.getValue()))
                         .toList();
        if (sides.size() < PROPS.streamProps().count()) {
            // Find first supporting block adjacent to this
            var attachableDir =
                    Direction.allShuffled(randomSource)
                             .stream()
                             .filter(d -> d != Direction.UP && d != Direction.DOWN)
                             .filter(d -> {
                                 var adj = pos.relative(d);
                                 return MultifaceBlock.canAttachTo(level, d, adj, level.getBlockState(adj));
                             }).findFirst();

            if (attachableDir.isPresent()) {
                float f = getGrowthSpeed(this, level, pos);
                if (gacha) {
                    if (ForgeHooks.onCropsGrowPre(level,
                                                  pos,
                                                  state,
                                                  randomSource.nextInt((int) (25.0F / f) + 1) == 0)) {
                        level.setBlock(pos,
                                       state.setValue(VinelikeProps.PROPERTY_BY_DIRECTION.get(attachableDir.get()),
                                                      Boolean.TRUE),
                                       2);
                        ForgeHooks.onCropsGrowPost(level, pos, state);
                    }

                }
                else {
                    level.setBlock(pos,
                                   state.setValue(VinelikeProps.PROPERTY_BY_DIRECTION.get(attachableDir.get()),
                                                  Boolean.TRUE),
                                   2);
                }
            }
        }
    }

    protected int getBonemealAgeIncrease(Level level) {
        return super.getBonemealAgeIncrease(level) / 3;
    }

    public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos pos, BlockState state, boolean def) {
        return true;
    }

    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos pos, BlockState state) {
        return true;
    }

    public void performBonemeal(ServerLevel level, RandomSource randomSource, BlockPos pos, BlockState state) {
        if (!this.isMaxAge(state)) {
            int i = this.getAge(state) + this.getBonemealAgeIncrease(level);
            int j = this.getMaxAge();
            if (i > j) {
                i = j;
            }
            level.setBlock(pos, state.setValue(this.getAgeProperty(), i), 2);
        }
        else {
            if (randomSource.nextBoolean()) this.spread(state, level, pos, randomSource, false);
            else this.attach(state, level, pos, randomSource, false);
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
        return false;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return PROPS.rotate(state, rotation);
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return PROPS.mirror(state, mirror, super::mirror);
    }
}
