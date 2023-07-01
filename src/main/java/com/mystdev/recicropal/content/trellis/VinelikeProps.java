package com.mystdev.recicropal.content.trellis;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VinelikeProps {

    // Heavily inspired from Vanilla vines
    public static final BooleanProperty UP = PipeBlock.UP;
    public static final BooleanProperty DOWN = PipeBlock.DOWN;
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;
    private final VoxelShape UP_AABB;
    private final VoxelShape DOWN_AABB;
    private final VoxelShape WEST_AABB;
    private final VoxelShape EAST_AABB;
    private final VoxelShape NORTH_AABB;
    private final VoxelShape SOUTH_AABB;

    private final Predicate<Map.Entry<Direction, BooleanProperty>> sideFilter;

    private VoxelShape calculateShape(BlockState state) {
        VoxelShape voxelshape = Shapes.empty();
        if (state.hasProperty(UP) && state.getValue(UP)) {
            voxelshape = UP_AABB;
        }

        if (state.hasProperty(DOWN) && state.getValue(DOWN)) {
            voxelshape = Shapes.or(voxelshape, DOWN_AABB);
        }

        if (state.hasProperty(NORTH) && state.getValue(NORTH)) {
            voxelshape = Shapes.or(voxelshape, NORTH_AABB);
        }

        if (state.hasProperty(SOUTH) && state.getValue(SOUTH)) {
            voxelshape = Shapes.or(voxelshape, SOUTH_AABB);
        }

        if (state.hasProperty(EAST) && state.getValue(EAST)) {
            voxelshape = Shapes.or(voxelshape, EAST_AABB);
        }

        if (state.hasProperty(WEST) && state.getValue(WEST)) {
            voxelshape = Shapes.or(voxelshape, WEST_AABB);
        }

        return voxelshape.isEmpty() ? Shapes.block() : voxelshape;
    }

    // These two will break if state does not have all the directions
    public BlockState rotate(BlockState state, Rotation rot) {
        return switch (rot) {
            case CLOCKWISE_180 -> state
                    .setValue(NORTH, state.getValue(SOUTH))
                    .setValue(EAST, state.getValue(WEST))
                    .setValue(SOUTH, state.getValue(NORTH))
                    .setValue(WEST, state.getValue(EAST));
            case COUNTERCLOCKWISE_90 -> state
                    .setValue(NORTH, state.getValue(EAST))
                    .setValue(EAST, state.getValue(SOUTH))
                    .setValue(SOUTH, state.getValue(WEST))
                    .setValue(WEST, state.getValue(NORTH));
            case CLOCKWISE_90 -> state
                    .setValue(NORTH, state.getValue(WEST))
                    .setValue(EAST, state.getValue(NORTH))
                    .setValue(SOUTH, state.getValue(EAST))
                    .setValue(WEST, state.getValue(SOUTH));
            default -> state;
        };
    }

    public BlockState mirror(BlockState state, Mirror mirror, BiFunction<BlockState, Mirror, BlockState> superCall) {
        return switch (mirror) {
            case LEFT_RIGHT -> state.setValue(NORTH, state.getValue(SOUTH)).setValue(SOUTH, state.getValue(NORTH));
            case FRONT_BACK -> state.setValue(EAST, state.getValue(WEST)).setValue(WEST, state.getValue(EAST));
            default -> superCall.apply(state, mirror);
        };
    }


    public VinelikeProps(double modelThickness, Predicate<Map.Entry<Direction, BooleanProperty>> sideFilter) {
        this.UP_AABB = Block.box(0.0D, 16.0D - modelThickness, 0.0D, 16.0D, 16.0D, 16.0D);
        this.DOWN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, modelThickness, 16.0D);
        this.WEST_AABB = Block.box(0.0D, 0.0D, 0.0D, modelThickness, 16.0D, 16.0D);
        this.EAST_AABB = Block.box(16.0D - modelThickness, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        this.NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, modelThickness);
        this.SOUTH_AABB = Block.box(0.0D, 0.0D, 16.0D - modelThickness, 16.0D, 16.0D, 16.0D);
        this.sideFilter = sideFilter;
    }

    public StateDefinition.Builder<Block, BlockState> generateStateDefinition(
            StateDefinition.Builder<Block, BlockState> stateBuilder) {
        var builder = new Object() {
            StateDefinition.Builder<Block, BlockState> builder = stateBuilder;
        };
        streamProps().forEach((e) -> builder.builder = builder.builder.add(e.getValue()));
        return builder.builder;
    }

    public BlockState generateDefaultState(BlockState baseState) {
        var state = new Object() {
            BlockState state = baseState;
        };
        streamProps().forEach((e) -> state.state = state.state.setValue(e.getValue(), Boolean.FALSE));
        return state.state;
    }

    public Map<BlockState, VoxelShape> generateShapesCache(StateDefinition<Block, BlockState> stateDefinition) {
        return ImmutableMap.copyOf(
                stateDefinition
                        .getPossibleStates()
                        .stream()
                        .collect(Collectors.toMap(Function.identity(), this::calculateShape)));
    }

    public Stream<Map.Entry<Direction, BooleanProperty>> streamProps() {
        return PROPERTY_BY_DIRECTION.entrySet()
                                    .stream()
                                    .filter(this.sideFilter);
    }

    public boolean hasFaces(BlockState state) {
        return this.countFaces(state) > 0;
    }

    private int countFaces(BlockState state) {
        return (int) streamProps().filter(e -> state.getValue(e.getValue())).count();
    }
}
