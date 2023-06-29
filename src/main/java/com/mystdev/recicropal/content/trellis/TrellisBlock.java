package com.mystdev.recicropal.content.trellis;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Map;

public class TrellisBlock extends Block {
    public static final VinelikeProps PROPS = new VinelikeProps(2D, (p) -> true);
    private final Map<BlockState, VoxelShape> shapesCache;

    public TrellisBlock(Properties props) {
        super(props);
        this.registerDefaultState(PROPS.generateDefaultState(this.stateDefinition.any()));
        this.shapesCache = PROPS.generateShapesCache(this.stateDefinition);
    }

    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext ctx) {
        return this.shapesCache.get(state);
    }

    public boolean propagatesSkylightDown(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return true;
    }

    public boolean canSurvive(BlockState state, LevelReader levelReader, BlockPos pos) {
        return this.hasFaces(state);
    }

    private boolean hasFaces(BlockState state) {
        return this.countFaces(state) > 0;
    }

    private int countFaces(BlockState state) {
        int i = 0;
        for (BooleanProperty booleanproperty : VinelikeProps.PROPERTY_BY_DIRECTION.values()) {
            if (state.getValue(booleanproperty)) ++i;
        }
        return i;
    }

    public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
        var blockstate = ctx.getLevel().getBlockState(ctx.getClickedPos());
        var heldItem = ctx.getItemInHand();
        var playerSneak = ctx.getPlayer() != null && ctx.getPlayer().isShiftKeyDown();
        if (blockstate.is(this) && heldItem.is(this.asItem()) && !playerSneak) {
            return this.countFaces(blockstate) < VinelikeProps.PROPERTY_BY_DIRECTION.size();
        }
        else return false;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState clickedState = ctx.getLevel().getBlockState(ctx.getClickedPos());

        boolean isTrellis = clickedState.is(this);
        BlockState resultState = isTrellis ? clickedState : this.defaultBlockState();

//        if (ctx.getPlayer() != null && ctx.getPlayer().isShiftKeyDown()) {
//            var loc = ctx.getClickLocation();
//            var pos = ctx.getClickedPos();
//            var relativeLoc = loc.subtract(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
//
//            Recicropal.LOGGER.debug(loc.toString());
//        }

        for (Direction direction : ctx.getNearestLookingDirections()) {
            var sideHasTrellis = getPropertyForFace(direction);
            boolean canAddTrellis = isTrellis && clickedState.getValue(sideHasTrellis);
            if (!canAddTrellis) {
                return resultState.setValue(sideHasTrellis, Boolean.TRUE);
            }
        }

        return isTrellis ? resultState : null;
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return PROPS.rotate(state, rot);
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return PROPS.mirror(state, mirror, super::mirror);
    }

    public static BooleanProperty getPropertyForFace(Direction direction) {
        return VinelikeProps.PROPERTY_BY_DIRECTION.get(direction);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder = PROPS.generateStateDefinition(stateBuilder);
    }
}
