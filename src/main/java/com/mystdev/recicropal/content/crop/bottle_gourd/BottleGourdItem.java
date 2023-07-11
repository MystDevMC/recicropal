package com.mystdev.recicropal.content.crop.bottle_gourd;

import com.mystdev.recicropal.ModBlocks;
import com.mystdev.recicropal.content.drinking.DrinkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BottleGourdItem extends BlockItem {
    public BottleGourdItem(Properties props) {
        super(ModBlocks.BOTTLE_GOURD.get(), props);
    }

    private static void playSound(Level level, BlockPos pos, SoundEvent soundEvent) {
        level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                             soundEvent, SoundSource.BLOCKS, 1, 1, false);
    }

    @Override
    public InteractionResult useOn(UseOnContext p_40581_) {
        return InteractionResult.PASS;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        var usedStack = ItemHandlerHelper.copyStackWithSize(stack, 1);

        var isSneaking = player.isCrouching() || player.isShiftKeyDown();

        var sourceRay = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        var blockRay = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);

        var sourcePos = sourceRay.getBlockPos();
        var sourceFluid = level.getFluidState(sourcePos);

        var blockPos = blockRay.getBlockPos();
        var blockState = level.getBlockState(blockPos);

        if (DrinkManager.wasDrinking(player)) {
            DrinkManager.resetDrinking(player);
        }

        if (!isSneaking) {
            if (!sourceFluid.isEmpty()) {
                var result =
                        FluidUtil.tryPickUpFluid(usedStack, player, level,
                                                 sourcePos, sourceRay.getDirection());
                if (result.isSuccess()) {
                    playSound(level, sourcePos, SoundEvents.BOTTLE_FILL);
                    return updatePlayerAndSucceed(player, hand, stack, result);
                }
            }
            if (!blockState.isAir()) {
                var targetPos = sourcePos.relative(sourceRay.getDirection());
                if (!sourceFluid.isEmpty()) targetPos = sourcePos;
                var result =
                        FluidUtil.tryPlaceFluid(player, level, hand,
                                                targetPos, usedStack,
                                                FluidUtil.getFluidContained(usedStack)
                                                         .orElse(FluidStack.EMPTY));
                if (result.isSuccess()) {
                    playSound(level, sourcePos, SoundEvents.BOTTLE_EMPTY);
                    return updatePlayerAndSucceed(player, hand, stack, result);
                }
            }
            if (DrinkManager.tryDrinking(player, level, stack, hand))
                return InteractionResultHolder.consume(stack);
        }

        var clickedAir = blockState.isAir();
        if (!clickedAir) {
            InteractionResult interactionresult = super.useOn(new UseOnContext(player, hand, blockRay));
            return new InteractionResultHolder<>(interactionresult, player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }

    @NotNull
    private InteractionResultHolder<ItemStack> updatePlayerAndSucceed(
            Player player,
            InteractionHand hand,
            ItemStack stack,
            FluidActionResult result
    ) {
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
            DrinkManager.finishDrinking(player);
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

    @Override
    public int getUseDuration(ItemStack stack) {
        return PotionItem.EAT_DURATION;
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new FluidHandlerItemStack(stack, BottleGourdTank.configuredCapacity());
    }
}
