package com.mystdev.recicropal.crop.bottle_gourd;

import com.mystdev.recicropal.ModBlocks;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.ItemHandlerHelper;

public class BottleGourdItem extends BlockItem {
    public BottleGourdItem(Properties props) {
        super(ModBlocks.BOTTLE_GOURD.get(), props);
    }

    @Override
    public InteractionResult useOn(UseOnContext p_40581_) {
        return InteractionResult.PASS;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        var pos = blockhitresult.getBlockPos();
        var fluid = level.getFluidState(pos);
        var isSneaking = player.isCrouching();

        var stack = player.getItemInHand(hand);
        var usedStack = ItemHandlerHelper.copyStackWithSize(stack, 1);

        if (!fluid.isEmpty() && fluid.isSource() && !isSneaking) {
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
        else if (!isSneaking) {
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

        var clickedState = level.getBlockState(blockhitresult
                                                       .getBlockPos());

        if (!clickedState.isAir()) {
            InteractionResult interactionresult = super.useOn(new UseOnContext(player, hand, blockhitresult));
            return new InteractionResultHolder<>(interactionresult, player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }

}
