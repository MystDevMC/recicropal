package com.mystdev.recicropal.content.crop.bottle_gourd;

import com.mystdev.recicropal.ModBlocks;
import com.mystdev.recicropal.Recicropal;
import com.mystdev.recicropal.content.drinking.DrinkManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public class BottleGourdItem extends BlockItem {
    public BottleGourdItem(Properties props) {
        super(ModBlocks.BOTTLE_GOURD.get(), props);
    }

    @Override
    public InteractionResult useOn(UseOnContext p_40581_) {
        return InteractionResult.PASS;
    }

    // TODO: This is getting out of hand XD
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        var usedStack = ItemHandlerHelper.copyStackWithSize(stack, 1);

        var blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);

        var isSneaking = player.isCrouching();
        if (isSneaking) {
            blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        }

        var pos = blockhitresult.getBlockPos();
        var fluid = level.getFluidState(pos);

        var clickedState = level.getBlockState(blockhitresult.getBlockPos());

        if (!isSneaking) {
            if (!fluid.isEmpty() && fluid.isSource()) {
                var result = FluidUtil.tryPickUpFluid(usedStack,
                                                      player,
                                                      level,
                                                      pos,
                                                      blockhitresult.getDirection());
                if (result.isSuccess()) {
                    level.playLocalSound(pos.getX(),
                                         pos.getY(),
                                         pos.getZ(),
                                         SoundEvents.BOTTLE_FILL,
                                         SoundSource.BLOCKS,
                                         1,
                                         1,
                                         false);
                    if (!player.getAbilities().instabuild) {
                        if (stack.getCount() != 1) {
                            if (!player.addItem(result.result))
                                ItemHandlerHelper.giveItemToPlayer(player, result.result);
                            player.setItemInHand(hand, ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1));
                        }
                        else {
                            player.setItemInHand(hand, result.result);
                        }
                    }
                    return InteractionResultHolder.success(player.getItemInHand(hand));
                }
                return InteractionResultHolder.fail(player.getItemInHand(hand));
            }
            blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
            clickedState = level.getBlockState(blockhitresult.getBlockPos());
            if (!clickedState.isAir()) {
                var targetPos = pos.relative(blockhitresult.getDirection());
                if (!fluid.isEmpty()) targetPos = pos;
                var result = FluidUtil.tryPlaceFluid(player,
                                                     level,
                                                     hand,
                                                     targetPos,
                                                     player.getItemInHand(hand),
                                                     FluidUtil.getFluidContained(player.getItemInHand(hand)).orElse(
                                                             FluidStack.EMPTY));
                if (result.isSuccess()) {
                    level
                            .playLocalSound(pos.getX(),
                                            pos.getY(),
                                            pos.getZ(),
                                            SoundEvents.BOTTLE_EMPTY,
                                            SoundSource.BLOCKS,
                                            1,
                                            1,
                                            false);
                    if (!player.getAbilities().instabuild) {
                        if (stack.getCount() != 1) {
                            if (!player.addItem(result.result))
                                ItemHandlerHelper.giveItemToPlayer(player, result.result);
                            player.setItemInHand(hand, ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1));
                        }
                        else {
                            player.setItemInHand(hand, result.result);
                        }
                    }
                    return InteractionResultHolder.success(player.getItemInHand(hand));
                }
            }
        }


        if (!clickedState.isAir()) {
            InteractionResult interactionresult = super.useOn(new UseOnContext(player, hand, blockhitresult));
            return new InteractionResultHolder<>(interactionresult, player.getItemInHand(hand));
        } else {
            if (!DrinkManager.tryDrinking(player, level, stack, hand)) return super.use(level, player, hand);
            return InteractionResultHolder.consume(stack);
        }
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.HONEY_DRINK;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (livingEntity instanceof Player player && DrinkManager.wasDrinking(player)) {
            DrinkManager.finishDrinking(player, player.getUsedItemHand());
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int ticksUsed) {
        if (livingEntity instanceof Player player && DrinkManager.wasDrinking(player)) {
            DrinkManager.resetDrinking(player);
        }
        super.releaseUsing(stack, level, livingEntity, ticksUsed);
    }

    // TODO: Maybe make this configurable depending on how much are going to be drunk
    @Override
    public int getUseDuration(ItemStack stack) {
        return PotionItem.EAT_DURATION;
    }
}
