package com.mystdev.recicropal.content.mixing;

import com.mystdev.recicropal.ModFluids;
import com.mystdev.recicropal.content.drinking.DrinkingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

class PotionProcess implements IMixingProcess {
    public static PotionProcess INSTANCE = new PotionProcess();

    @Override
    public boolean matchForFilling(BottleInteractionContainer container, Level level) {
        var item = container.getItem(0).getItem();

        // Gonna hardcode this one for the moment
        if (item != Items.POTION && item != Items.SPLASH_POTION && item != Items.LINGERING_POTION) return false;

        var tank = container.getBottle().tank;
        return tank.getFluidAmount() + DrinkingRecipe.DEFAULT_AMOUNT <= tank.getCapacity();
    }

    @Override
    public ItemStack assembleForFilling(BottleInteractionContainer container) {
        var stack = container.getItem(0);
        var item = stack.getItem();

        var potion = PotionUtils.getPotion(stack);
        var color = PotionUtils.getColor(stack);
        var customEffects = PotionUtils.getCustomEffects(stack);

        var voidItem = ItemStack.EMPTY.copy();
        PotionUtils.setPotion(voidItem, potion);
        PotionUtils.setCustomEffects(voidItem, customEffects);

        if (stack.getOrCreateTag().contains(PotionUtils.TAG_CUSTOM_POTION_COLOR)) {
            var tag = voidItem.getTag();
            assert tag != null;
            tag.putInt(PotionUtils.TAG_CUSTOM_POTION_COLOR, color);
        }

        var fluid = new FluidStack(ModFluids.POTION.get(), DrinkingRecipe.DEFAULT_AMOUNT, voidItem.getTag());

        if (potion == Potions.WATER) {
            fluid = new FluidStack(Fluids.WATER, DrinkingRecipe.DEFAULT_AMOUNT);
        }
        container.getBottle().tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
        return new ItemStack(Items.GLASS_BOTTLE);
    }

    @Override
    public boolean matchForPouring(BottleInteractionContainer container, Level level) {
        var item = container.getItem(0).getItem();

        // Gonna hardcode this one for the moment
        if (item != Items.GLASS_BOTTLE) return false;

        var tank = container.getBottle().tank;
        return tank.getFluid().getFluid() == ModFluids.POTION.get() && tank.getFluidAmount() >= DrinkingRecipe.DEFAULT_AMOUNT;
    }

    @Override
    public ItemStack assembleForPouring(BottleInteractionContainer container) {
        var stack = new ItemStack(Items.POTION);

        var tank = container.getBottle().tank;
        var fluidTag = tank.getFluid().getTag();

        stack.setTag(fluidTag);

        tank.drain(DrinkingRecipe.DEFAULT_AMOUNT, IFluidHandler.FluidAction.EXECUTE);
        return stack;
    }

    @Override
    public String getId() {
        return "recicropal:potion";
    }
}
