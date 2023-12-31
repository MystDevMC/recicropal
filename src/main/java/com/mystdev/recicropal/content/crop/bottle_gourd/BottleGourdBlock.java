package com.mystdev.recicropal.content.crop.bottle_gourd;

import com.mystdev.recicropal.ModBlockEntities;
import com.mystdev.recicropal.ModRecipes;
import com.mystdev.recicropal.content.mixing.BottleInteractionContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BottleGourdBlock extends Block implements EntityBlock {
    public static final BooleanProperty DROP_SEEDS = BooleanProperty.create("drop_seeds");

    public BottleGourdBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(DROP_SEEDS, Boolean.FALSE));
    }

    /**
     * By default, the way things happen are like so:
     * Filling > Emptying > {@code FluidHandlerItem} interaction
     * <p>
     * When one happens, the others should not be triggered. This is
     * useful if someone wants to alter the interaction of common
     * {@code FluidHandlerItem}-attached items
     * (like milk buckets, honey bottles, potions, etc.).
     * </p>
     */
    @SubscribeEvent
    public static void interact(PlayerInteractEvent.RightClickBlock event) {
        var pos = event.getPos();
        var be = event.getLevel().getBlockEntity(event.getPos());
        var player = event.getEntity();
        var hand = event.getHand();

        if (be instanceof BottleGourdBlockEntity bottle) {
            var stack = player.getItemInHand(hand).copy();

            var isSneaking = player.isCrouching();
            var level = event.getLevel();

            var usedStack = ItemHandlerHelper.copyStackWithSize(stack, 1);

            // Filling & Mixing
            var container = new BottleInteractionContainer(usedStack, bottle);
            if (!isSneaking) {
                filling(event, pos, player, hand, stack, level, container);
                if (event.isCanceled()) return;
            }
            // Pouring
            pouring(event, pos, player, hand, stack, level, container);
            if (event.isCanceled()) return;

            // FluidHandlerItem interactions
            interactCapability(event, pos, player, hand, bottle, stack, isSneaking);
        }
    }

    private static void interactCapability(PlayerInteractEvent.RightClickBlock event,
                                           BlockPos pos,
                                           Player player,
                                           InteractionHand hand,
                                           BottleGourdBlockEntity bottle,
                                           ItemStack stack,
                                           boolean isSneaking) {
        var fluidTankItem = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        fluidTankItem.ifPresent((tank) -> {
            if (isSneaking) {
                var drainable = Math.min(BottleGourdTank.configuredTransferAmount(), bottle.tank.getFluidAmount());
                var result = FluidUtil.tryFillContainerAndStow(stack,
                                                               bottle.tank,
                                                               new PlayerArmorInvWrapper(player.getInventory()),
                                                               drainable,
                                                               player,
                                                               false);
                if (!result.isSuccess()) {
                    event.setCancellationResult(InteractionResult.FAIL);
                    return;
                }
                result = FluidUtil.tryFillContainerAndStow(stack,
                                                           bottle.tank,
                                                           new PlayerArmorInvWrapper(player.getInventory()),
                                                           drainable,
                                                           player,
                                                           true);
                playSound(event, pos, SoundEvents.BOTTLE_EMPTY);
                player.setItemInHand(hand, result.result);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
            else {
                // This must try to get the amounts first
                var drainable = Math.min(BottleGourdTank.configuredTransferAmount(), bottle.tank.getSpace());
                var result = FluidUtil.tryEmptyContainerAndStow(stack,
                                                                bottle.tank,
                                                                new PlayerArmorInvWrapper(player.getInventory()),
                                                                drainable,
                                                                player,
                                                                false);
                if (!result.isSuccess()) {
                    event.setCancellationResult(InteractionResult.FAIL);
                    return;
                }
                result = FluidUtil.tryEmptyContainerAndStow(stack,
                                                            bottle.tank,
                                                            new PlayerArmorInvWrapper(player.getInventory()),
                                                            drainable,
                                                            player,
                                                            true);
                playSound(event, pos, SoundEvents.BOTTLE_FILL);
                player.setItemInHand(hand, result.result);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);

            }
        });
    }

    private static void pouring(PlayerInteractEvent.RightClickBlock event,
                                BlockPos pos,
                                Player player,
                                InteractionHand hand,
                                ItemStack stack,
                                Level level,
                                BottleInteractionContainer container) {
        var pouringRecipeOpt = level.getRecipeManager().getRecipeFor(ModRecipes.POURING_RECIPE.get(), container, level);
        pouringRecipeOpt.ifPresent(pouringRecipe -> {
            var res = pouringRecipe.assemble(container);
            player.setItemInHand(hand, ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1));
            ItemHandlerHelper.giveItemToPlayer(player, res);
            playSound(event, pos, SoundEvents.BOTTLE_FILL);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        });
    }

    private static void filling(PlayerInteractEvent.RightClickBlock event,
                                BlockPos pos,
                                Player player,
                                InteractionHand hand,
                                ItemStack stack,
                                Level level,
                                BottleInteractionContainer container) {
        var fillingRecipeOpt = level.getRecipeManager().getRecipeFor(ModRecipes.FILLING_RECIPE.get(), container, level);
        fillingRecipeOpt.ifPresent(fillingRecipe -> {
            var res = fillingRecipe.assemble(container);
            player.setItemInHand(hand, ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1));
            ItemHandlerHelper.giveItemToPlayer(player, res);
            playSound(event, pos, SoundEvents.BOTTLE_EMPTY);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        });
    }

    private static void playSound(PlayerInteractEvent.RightClickBlock event, BlockPos pos, SoundEvent soundEvent) {
        event
                .getLevel()
                .playLocalSound(pos.getX(), pos.getY(), pos.getZ(), soundEvent, SoundSource.BLOCKS, 1, 1, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(DROP_SEEDS);
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.BOTTLE_GOURD.create(pos, state);
    }

    @Override
    public void setPlacedBy(Level level,
                            BlockPos pos,
                            BlockState state,
                            @Nullable LivingEntity entity,
                            ItemStack stack) {
        var tank = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        tank.ifPresent(cap -> {
            var be = level.getBlockEntity(pos);
            if (be instanceof BottleGourdBlockEntity bottle) {
                bottle.updateTank(cap);
            }
        });
        super.setPlacedBy(level, pos, state, entity, stack);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        var be = level.getBlockEntity(pos);
        if (be instanceof BottleGourdBlockEntity bottle) {
            var tank = bottle.tank;
            return Math.round((float) tank.getFluidAmount() / BottleGourdTank.configuredCapacity() * 16);
        }
        return super.getAnalogOutputSignal(state, level, pos);
    }
}
