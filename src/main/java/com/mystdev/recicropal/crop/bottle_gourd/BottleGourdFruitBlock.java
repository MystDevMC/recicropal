package com.mystdev.recicropal.crop.bottle_gourd;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BottleGourdFruitBlock extends Block {

    public BottleGourdFruitBlock(Properties props) {
        super(props);
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
}
