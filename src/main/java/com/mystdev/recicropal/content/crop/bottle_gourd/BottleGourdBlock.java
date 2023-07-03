package com.mystdev.recicropal.content.crop.bottle_gourd;

import com.mystdev.recicropal.ModBlockEntities;
import com.mystdev.recicropal.ModRecipes;
import com.mystdev.recicropal.content.mixing.BottleInteractionContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
    public BottleGourdBlock(Properties props) {
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

            var container = new BottleInteractionContainer(usedStack, bottle);

            var recipe = level.getRecipeManager()
                              .getRecipeFor(
                                      ModRecipes.FILLING_RECIPE.get(),
                                      container,
                                      level
                              );
            recipe.ifPresent(fillingRecipe -> {
                var res = fillingRecipe.assemble(container);
                player.setItemInHand(hand, ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1));
                ItemHandlerHelper.giveItemToPlayer(player, res);
                playSound(event, pos, SoundEvents.BOTTLE_EMPTY);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            });
            if (event.isCanceled()) return;

            var fluidTankItem = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
            fluidTankItem.ifPresent((tank) -> {
                if (isSneaking) {
                    var result = FluidUtil
                            .tryFillContainerAndStow(stack,
                                                     bottle.tank,
                                                     new PlayerArmorInvWrapper(player.getInventory()),
                                                     1000,
                                                     player,
                                                     false);
                    if (!result.isSuccess()) {
                        event.setCancellationResult(InteractionResult.FAIL);
                        return;
                    }
                    result = FluidUtil
                            .tryFillContainerAndStow(stack,
                                                     bottle.tank,
                                                     new PlayerArmorInvWrapper(player.getInventory()),
                                                     1000,
                                                     player,
                                                     true);
                    playSound(event, pos, SoundEvents.BOTTLE_EMPTY);
                    player.setItemInHand(hand, result.result);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
                else {
                    var result = FluidUtil
                            .tryEmptyContainerAndStow(stack,
                                                      bottle.tank,
                                                      new PlayerArmorInvWrapper(player.getInventory()),
                                                      1000,
                                                      player,
                                                      false);
                    if (!result.isSuccess()) {
                        event.setCancellationResult(InteractionResult.FAIL);
                        return;
                    }
                    result = FluidUtil
                            .tryEmptyContainerAndStow(stack,
                                                      bottle.tank,
                                                      new PlayerArmorInvWrapper(player.getInventory()),
                                                      1000,
                                                      player,
                                                      true);
                    playSound(event, pos, SoundEvents.BOTTLE_FILL);

                    player.setItemInHand(hand, result.result);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);

                }
            });
        }
    }

    private static void playSound(PlayerInteractEvent.RightClickBlock event, BlockPos pos, SoundEvent soundEvent) {
        event.getLevel()
             .playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                             soundEvent,
                             SoundSource.BLOCKS,
                             1, 1, false);
    }
}
