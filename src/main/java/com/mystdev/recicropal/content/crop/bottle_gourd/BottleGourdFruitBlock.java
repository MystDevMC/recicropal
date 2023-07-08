package com.mystdev.recicropal.content.crop.bottle_gourd;

import com.mystdev.recicropal.ModBlocks;
import com.mystdev.recicropal.ModPotions;
import com.mystdev.recicropal.content.mixing.EffectProvider;
import com.mystdev.recicropal.content.mixing.Mixture;
import com.mystdev.recicropal.content.mixing.MixturePool;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BottleGourdFruitBlock extends Block {
    public static final BooleanProperty PERSISTENT = LeavesBlock.PERSISTENT;
    public static final MixturePool ROTTING_POOL = new MixturePool()
            .withEntry(new EffectProvider().of(() -> Potions.STRONG_POISON), 2)
            .withEntry(new EffectProvider().of(ModPotions.STRONG_REVITALIZATION), 1)
            .withEntry(new EffectProvider().of(List.of(new MobEffectInstance(MobEffects.CONFUSION, 3600, 0),
                                                       new MobEffectInstance(MobEffects.WEAKNESS, 1800, 2))), 2)
            .withEntry(new EffectProvider().of(List.of(new MobEffectInstance(MobEffects.WITHER, 1800, 0))), 1)
            .withEntry(new EffectProvider().of(List.of(new MobEffectInstance(MobEffects.SATURATION, 1800, 1),
                                                       new MobEffectInstance(MobEffects.ABSORPTION, 1800))), 1);

    public BottleGourdFruitBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(PERSISTENT, Boolean.FALSE));
    }

    public VoxelShape makeShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.125, 0, 0.125, 0.875, 0.5625, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.28125, 0.5625, 0.28125, 0.71875, 0.9375, 0.71875), BooleanOp.OR);

        return shape;
    }

    @Override
    public VoxelShape getShape(BlockState state,
                               BlockGetter blockGetter,
                               BlockPos pos,
                               CollisionContext collisionContext) {
        return makeShape();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(PERSISTENT, Boolean.TRUE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(PERSISTENT);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(PERSISTENT);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        if (randomSource.nextInt(10) == 0) {
            var state2Place = ModBlocks.BOTTLE_GOURD
                    .getDefaultState()
                    .setValue(BottleGourdBlock.DROP_SEEDS, Boolean.TRUE);
            level.setBlock(pos, state2Place, 2);
            var be = level.getBlockEntity(pos);
            if (be instanceof BottleGourdBlockEntity bottle) {
                var amount = randomSource.nextInt(BottleGourdTank.CAPACITY);
                var splits = randomSource.nextInt(1, 4);
                var cursor = 0;
                Mixture mixture = null;
                for (var i = 0; i <= splits; i++) {
                    var split = randomSource.nextInt(cursor, amount);
                    if (split == amount) break;
                    var rationedAmount = amount - split;
                    var newMix = ROTTING_POOL.pullMixture("gourd_juice." + i, null, rationedAmount, randomSource);
                    if (mixture == null) {
                        mixture = newMix;
                    }
                    else {
                        mixture = Mixture.mix(mixture, cursor, newMix, rationedAmount);
                    }
                    cursor = split;
                }
                if (mixture != null) {
                    bottle.tank.fill(Mixture.asFluid(mixture, cursor), IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }
    }
}
