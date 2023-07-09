package com.mystdev.recicropal.content.mixing;

import com.mystdev.recicropal.common.Config;
import com.mystdev.recicropal.content.drinking.DrinkingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

class PotionProcess implements IMixingProcess {
    public static PotionProcess INSTANCE = new PotionProcess();

    @Override
    public boolean matchForFilling(BottleInteractionContainer container, Level level) {
        var item = container.getItem(0).getItem();

        // Gonna hardcode this one for the moment
        if (item != Items.POTION && item != Items.SPLASH_POTION && item != Items.LINGERING_POTION) return false;
        var fluid = PotionFluid.fluidFrom(container.getItem(0));
        return container.getBottle().tank.fill(fluid, IFluidHandler.FluidAction.SIMULATE) == fluid.getAmount();
    }

    @Override
    public ItemStack assembleForFilling(BottleInteractionContainer container) {
        var fluid = PotionFluid.fluidFrom(container.getItem(0));
        container.getBottle().tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
        return new ItemStack(Items.GLASS_BOTTLE);
    }

    @Override
    public boolean matchForMixing(MixingContainer container, Level level) {
        if (!Config.ENABLE_POTION_MIXING.get()) return false;
        var fluidIn = container.getIncomingFluid();
        var fluidInside = container.getBottle().getFluid();
        if (!PotionFluid.isPotion(fluidIn) && !Mixture.isMixture(fluidIn)) return false;
        var isPotion = PotionFluid.isPotion(fluidInside);
        if (PotionFluid.isPotion(fluidIn) && isPotion && FluidStack.areFluidStackTagsEqual(fluidInside, fluidIn))
            return false;
        var isMixture = Mixture.isMixture(fluidInside);
        var isEmpty = fluidInside.isEmpty();
        return isEmpty || isMixture || isPotion;
    }

    @Override
    public FluidStack getMixingResult(MixingContainer container) {
        var fluidIn = container.getIncomingFluid();
        var fluidInside = container.getBottle().getFluid();

        if (fluidInside.isEmpty()) return fluidIn.copy();

        Mixture newMixture = Mixture.getMixtureFromMixable(fluidIn.copy());
        Mixture insideMixture = Mixture.getMixtureFromMixable(fluidInside.copy());
        var mix = Mixture.mix(newMixture, fluidIn.getAmount(), insideMixture, fluidInside.getAmount());
        return Mixture.asFluid(mix, fluidIn.getAmount() + fluidInside.getAmount());
    }

    @Override
    public boolean matchForPouring(BottleInteractionContainer container, Level level) {
        var item = container.getItem(0).getItem();

        // Gonna hardcode this one for the moment
        if (item != Items.GLASS_BOTTLE) return false;

        var tank = container.getBottle().tank;
        return PotionFluid.isPotion(tank.getFluid()) && tank.getFluidAmount() >= DrinkingRecipe.configuredMaxAmount(); // TODO: Change this with drain test
    }

    @Override
    public ItemStack assembleForPouring(BottleInteractionContainer container) {
        var tank = container.getBottle().tank;
        var stack = PotionFluid.extractPotionFrom(tank.getFluid());
        tank.drain(DrinkingRecipe.configuredMaxAmount(), IFluidHandler.FluidAction.EXECUTE);
        return stack;
    }

    @Override
    public String getId() {
        return "recicropal:potion";
    }
}
