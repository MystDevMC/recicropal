package com.mystdev.recicropal.content.trellis;

import com.mystdev.recicropal.Recicropal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.NonNullLazy;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.Map;

public abstract class TrellisVineBlock extends CropBlock {
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    public static final BooleanProperty ATTACHED = BooleanProperty.create("attached");

    public static final VinelikeProps PROPS = new VinelikeProps(3D, (p) -> true);
    private final Map<BlockState, VoxelShape> shapesCache;
    private final Block fruitBlock;
    private final NonNullLazy<TrellisCropBlock> lazyCropBlock;

    public TrellisVineBlock(Properties props, Block fruitBlock, NonNullLazy<TrellisCropBlock> lazyCropBlock) {
        super(props);
        this.fruitBlock = fruitBlock;
        this.lazyCropBlock = lazyCropBlock;
        this.registerDefaultState(PROPS
                                          .generateDefaultState(this.stateDefinition.any())
                                          .setValue(AGE, 0)
                                          .setValue(ATTACHED, false));
        this.shapesCache = PROPS.generateShapesCache(this.stateDefinition);
    }

    public @NotNull IntegerProperty getAgeProperty() {
        return AGE;
    }

    public int getMaxAge() {
        return TrellisVineBlock.MAX_AGE;
    }

    @Override
    protected abstract ItemLike getBaseSeedId();

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext ctx) {
        return shapesCache.get(state);
    }

    public BlockState updateShape(BlockState state,
                                  Direction direction,
                                  BlockState otherState,
                                  LevelAccessor levelAccessor,
                                  BlockPos pos,
                                  BlockPos otherPos) {

        var blockstate = state;
        var sides = PROPS.streamProps().filter(e -> state.getValue(e.getValue())).toList();
        for (var side : sides) {
            var attached = pos.relative(side.getKey());
            var newValue = MultifaceBlock.canAttachTo(levelAccessor,
                                                      side.getKey(),
                                                      attached,
                                                      levelAccessor.getBlockState(attached));

            if (side.getKey().getAxis().isHorizontal()) {
                var aboveState = levelAccessor.getBlockState(pos.above());
                newValue = newValue || (aboveState.is(this) && aboveState.getValue(VinelikeProps.PROPERTY_BY_DIRECTION.get(
                        side.getKey())));

                var adjState = levelAccessor.getBlockState(pos.above().relative(side.getKey()));
                newValue = newValue || (adjState.is(this) && adjState.getValue(VinelikeProps.DOWN) &&
                        MultifaceBlock.canAttachTo(levelAccessor,
                                                   Direction.DOWN,
                                                   attached,
                                                   levelAccessor.getBlockState(attached)));

            }
            if (!newValue)
                blockstate = blockstate.setValue(VinelikeProps.PROPERTY_BY_DIRECTION.get(side.getKey()), Boolean.FALSE);
        }
        return !PROPS.hasFaces(blockstate) ? Blocks.AIR.defaultBlockState() : blockstate;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        PROPS.generateStateDefinition(stateBuilder).add(AGE, ATTACHED);
    }

    public boolean mayPlaceOn(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return MultifaceBlock.canAttachTo(blockGetter, Direction.DOWN, pos, blockGetter.getBlockState(pos));
    }

    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        if (!level.isAreaLoaded(pos, 1)) return;
        if (level.getRawBrightness(pos, 0) < 9) return;

        if (state.getValue(ATTACHED) && !level.getBlockState(pos.below(2)).is(this.fruitBlock)) {
            level.setBlock(pos, state.setValue(ATTACHED, Boolean.FALSE), 2);
        }

        var random = randomSource.nextInt(3);
        if (random == 0) {
            var hasValidCrop = this.isCropInReasonableDistance(level, pos);
            if (hasValidCrop == CropSearchResult.SHOULD_DIE) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }
            if (!this.isMaxAge(state)) {
                this.age(state, level, pos, randomSource);
            }
            else if (this.isMaxAge(state) && hasValidCrop == CropSearchResult.CAN_FRUIT && !state.getValue(ATTACHED)) {
                this.fruit(state, level, pos);
            }
            else if (this.isMaxAge(state)) {
                this.removeFlower(state, level, pos, randomSource);
            }
        }
        else if (random == 1) this.attach(state, level, pos, randomSource, true);
        else this.spread(state, level, pos, randomSource, true);
    }

    private void fruit(BlockState state, ServerLevel level, BlockPos pos) {
        var below = level.getBlockState(pos.below());
        var hasSpace = level.isEmptyBlock(pos.below(2));
        if (!below.isCollisionShapeFullBlock(level, pos.below()) && hasSpace && state.getValue(VinelikeProps.DOWN)) {
            level.setBlock(pos, state.setValue(this.getAgeProperty(), 2).setValue(ATTACHED, Boolean.TRUE), 2);
            level.setBlock(pos.below(2), this.fruitBlock.defaultBlockState(), 2);
        }
        else {
            level.setBlock(pos, this.fruitBlock.defaultBlockState(), 2);
        }
    }

    private enum CropSearchResult {
        CANNOT_FRUIT,
        CAN_FRUIT,
        SHOULD_DIE
    }

    private CropSearchResult isCropInReasonableDistance(ServerLevel level, BlockPos thisPos) {
        var innerSize = 2;
        var innerPoss = BlockPos.withinManhattan(thisPos, innerSize, innerSize, innerSize);
        var outerSize = 8;
        var outerPoss = BlockPos.withinManhattan(thisPos, outerSize, outerSize, outerSize);

        for (var pos : innerPoss) {
            // Without the distance check, this would just return a cube somehow
            // Lets see...
            if (Math.round(thisPos.distSqr(pos)) > innerSize) continue;
            if (level.getBlockState(pos).is(lazyCropBlock.get())) {
                return CropSearchResult.CANNOT_FRUIT;
            }
        }
        for (BlockPos pos : outerPoss) {
            if (Math.round(thisPos.distSqr(pos)) > outerSize) continue;
            if (level.getBlockState(pos).is(lazyCropBlock.get())) {
                return CropSearchResult.CAN_FRUIT;
            }
        }
        return CropSearchResult.SHOULD_DIE;
    }

    public void age(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        int i = this.getAge(state);
        float f = getGrowthSpeed(this, level, pos);
        if (ForgeHooks.onCropsGrowPre(level, pos, state, randomSource.nextInt((int) (25.0F / f) + 1) == 0)) {
            level.setBlock(pos, state.setValue(this.getAgeProperty(), i + 1), 2);
            ForgeHooks.onCropsGrowPost(level, pos, state);
        }
    }

    public void removeFlower(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        float f = getGrowthSpeed(this, level, pos);
        if (ForgeHooks.onCropsGrowPre(level, pos, state, randomSource.nextInt((int) (25.0F / f) + 1) == 0)) {
            level.setBlock(pos, state.setValue(this.getAgeProperty(), this.getMaxAge() - 1), 2);
            ForgeHooks.onCropsGrowPost(level, pos, state);
        }
    }

    public void attach(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource, boolean gacha) {
        var sides = PROPS.streamProps().filter(e -> state.getValue(e.getValue())).toList();
        if (sides.size() < PROPS.streamProps().count()) {
            // Find first supporting block adjacent to this or vines above this
            // Why don't I make it filter the sides that haven't got any vines...
            var attachableDir = Direction.allShuffled(randomSource).stream().filter(d -> {
                var adj = pos.relative(d);
                var floatCheck = false;
                if (d.getAxis().isHorizontal()) {
                    var aboveState = level.getBlockState(pos.above());
                    floatCheck = aboveState.is(this) && aboveState.getValue(VinelikeProps.PROPERTY_BY_DIRECTION.get(d));
                    if (floatCheck) return true;
                    var adjState = level.getBlockState(pos.above().relative(d));
                    floatCheck = adjState.is(this) && adjState.getValue(VinelikeProps.DOWN);
                    if (floatCheck) return true;
                }
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
        return !state.getValue(ATTACHED);
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

    public void spread(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource, boolean gacha) {
        var sides = PROPS.streamProps().toList();
        var dirs = new ArrayList<Pair<BlockPos, Direction>>();
        sides.forEach(e -> {
            var side = e.getKey();
            boolean sideHasVines = state.getValue(e.getValue());

            var adj = pos.relative(side);
            var topAdj = adj.above();
            var cSide = side.getClockWise(Direction.Axis.Y);
            var c = pos.relative(cSide);
            var cAdj = adj.relative(cSide);
            var ccSide = side.getCounterClockWise(Direction.Axis.Y);
            var cc = pos.relative(ccSide);
            var ccAdj = adj.relative(ccSide);
            var top = pos.relative(Direction.UP);

            var bot = pos.relative(Direction.DOWN);

            if (side.getAxis().isHorizontal() && sideHasVines) {
                if (level.isEmptyBlock(bot)) {
                    dirs.add(new Pair<>(bot, side));
                }
                if (level.isEmptyBlock(top)
                        && MultifaceBlock.canAttachTo(level, side, topAdj, level.getBlockState(topAdj))) {
                    dirs.add(new Pair<>(top, side));
                }
                else if (level.isEmptyBlock(topAdj)
                        && MultifaceBlock.canAttachTo(level, Direction.DOWN, adj, level.getBlockState(adj))) {
                    dirs.add(new Pair<>(topAdj, Direction.DOWN));
                }
                if (level.isEmptyBlock(cAdj)
                        && MultifaceBlock.canAttachTo(level, cSide.getOpposite(), adj, level.getBlockState(adj))) {
                    dirs.add(new Pair<>(cAdj, cSide.getOpposite()));
                }
                else if (level.isEmptyBlock(c)
                        && MultifaceBlock.canAttachTo(level, side, cAdj, level.getBlockState(cAdj))) {
                    dirs.add(new Pair<>(c, side));
                }
                if (level.isEmptyBlock(ccAdj)
                        && MultifaceBlock.canAttachTo(level, ccSide.getOpposite(), adj, level.getBlockState(adj))) {
                    dirs.add(new Pair<>(ccAdj, ccSide.getOpposite()));
                }
                else if (level.isEmptyBlock(cc)
                        && MultifaceBlock.canAttachTo(level, side, ccAdj, level.getBlockState(ccAdj))) {
                    dirs.add(new Pair<>(cc, side));
                }
            }
            if (side == Direction.DOWN && sideHasVines) {
                for (Direction horizontalSide : Direction.Plane.HORIZONTAL) {
                    var targetPos = pos.below().relative(horizontalSide);
                    if (level.isEmptyBlock(targetPos)) dirs.add(new Pair<>(targetPos, horizontalSide.getOpposite()));
                }
                for (Direction horizontalSide : Direction.Plane.HORIZONTAL) {
                    var targetPos = pos.relative(horizontalSide);
                    if (level.isEmptyBlock(targetPos)
                            && MultifaceBlock.canAttachTo(level,
                                                          Direction.DOWN,
                                                          targetPos.below(),
                                                          level.getBlockState(targetPos.below())))
                        dirs.add(new Pair<>(targetPos, Direction.DOWN));
                }
            }
        });
        float f = getGrowthSpeed(this, level, pos);
        if (dirs.size() == 0) return;
        var randomChoice = dirs.get(randomSource.nextInt(dirs.size()));
        if (gacha) {
            if (ForgeHooks.onCropsGrowPre(level, pos, state, randomSource.nextInt((int) (10F / f) + 1) == 0)) {
                level.setBlock(randomChoice.getA(),
                               this
                                       .defaultBlockState()
                                       .setValue(VinelikeProps.PROPERTY_BY_DIRECTION.get(randomChoice.getB()),
                                                 Boolean.TRUE),
                               2);
                ForgeHooks.onCropsGrowPost(level, pos, state);
            }
        }
        else {
            level.setBlock(randomChoice.getA(),
                           this
                                   .defaultBlockState()
                                   .setValue(VinelikeProps.PROPERTY_BY_DIRECTION.get(randomChoice.getB()),
                                             Boolean.TRUE),
                           2);
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
