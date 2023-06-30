package com.mystdev.recicropal.crop.bottle_gourd;

import com.mystdev.recicropal.ModBlocks;
import com.mystdev.recicropal.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

public class BottleGourdListing implements VillagerTrades.ItemListing {
    @Nullable
    public MerchantOffer getOffer(Entity entity, RandomSource random) {
        if (!(entity instanceof VillagerDataHolder)) return null;
        var type = ((VillagerDataHolder) entity).getVillagerData().getType();
        var shouldSellFruits = random.nextInt(5) == 0;
        // TODO: Maybe implement something to patch Vanilla's offer rolls so null != no trade
        if ((type != VillagerType.DESERT && type != VillagerType.SAVANNA) || shouldSellFruits) {
            var itemstack = new ItemStack(ModBlocks.BOTTLE_GOURD_FRUIT.get(), random.nextInt(1, 4));
            return new MerchantOffer(new ItemStack(Items.EMERALD), itemstack, 10, 3, 0.05F);
        }
        var itemstack = new ItemStack(ModItems.BOTTLE_GOURD.get());
        FluidUtil.getFluidHandler(itemstack).ifPresent(bottle -> bottle.fill(new FluidStack(Fluids.WATER, 2000), IFluidHandler.FluidAction.EXECUTE));
        return new MerchantOffer(ItemHandlerHelper.copyStackWithSize(itemstack, random.nextInt(3, 6)), new ItemStack(Items.EMERALD), 10, 3, 0.05F);
    }
}
