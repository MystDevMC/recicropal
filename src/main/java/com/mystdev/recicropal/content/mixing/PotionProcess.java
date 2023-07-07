package com.mystdev.recicropal.content.mixing;

import com.mystdev.recicropal.common.fluid.ModFluidUtils;
import com.mystdev.recicropal.content.drinking.DrinkingRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

class PotionProcess implements IMixingProcess {
    public static PotionProcess INSTANCE = new PotionProcess();

    public static final TagKey<Fluid> POTION_TAG = ModFluidUtils.tag("forge:potion");

    @Override
    public boolean matchForFilling(BottleInteractionContainer container, Level level) {
        var item = container.getItem(0).getItem();

        // Gonna hardcode this one for the moment
        if (item != Items.POTION && item != Items.SPLASH_POTION && item != Items.LINGERING_POTION) return false;
        var fluid = fluidFromPotion(container.getItem(0));
        return container.getBottle().tank.fill(fluid, IFluidHandler.FluidAction.SIMULATE) == fluid.getAmount();
    }

    @Override
    public ItemStack assembleForFilling(BottleInteractionContainer container) {
        var fluid = fluidFromPotion(container.getItem(0));
        container.getBottle().tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
        return new ItemStack(Items.GLASS_BOTTLE);
    }

    private FluidStack fluidFromPotion(ItemStack stack) {
        var potion = PotionUtils.getPotion(stack);
        var color = PotionUtils.getColor(stack);
        var customEffects = PotionUtils.getCustomEffects(stack);

        var voidItem = new ItemStack(Items.POTION);
        PotionUtils.setPotion(voidItem, potion);
        PotionUtils.setCustomEffects(voidItem, customEffects);

        var tag = voidItem.getOrCreateTag();
        var modifier = Mixture.Modifier.from(stack);
        tag.putString(MixturePart.TAG_MODIFIER, modifier.getSerializedName());

        if (stack.getOrCreateTag().contains(PotionUtils.TAG_CUSTOM_POTION_COLOR)) {
            tag.putInt(PotionUtils.TAG_CUSTOM_POTION_COLOR, color);
        }

        var fluid = new FluidStack(ModFluidUtils.forcedMember(POTION_TAG),
                                   DrinkingRecipe.DEFAULT_AMOUNT,
                                   voidItem.getTag());

        if (potion == Potions.WATER && modifier == Mixture.Modifier.NORMAL) {
            fluid = new FluidStack(Fluids.WATER, DrinkingRecipe.DEFAULT_AMOUNT);
        }
        return fluid;
    }

    @Override
    public boolean matchForMixing(MixingContainer container, Level level) {
        var fluidIn = container.getIncomingFluid();
        var fluidInside = container.getBottle().getFluid();
        if (!fluidIn.getFluid().is(POTION_TAG) && !Mixture.isMixture(fluidIn)) return false;
        var isPotion = fluidInside.getFluid().is(POTION_TAG);
        if (fluidIn.getFluid().is(POTION_TAG) && isPotion && FluidStack.areFluidStackTagsEqual(fluidInside, fluidIn)) return false;
        var isMixture = Mixture.isMixture(fluidInside);
        var isEmpty = fluidInside.isEmpty();
        return isEmpty || isMixture || isPotion;
    }

    @Override
    public FluidStack getMixingResult(MixingContainer container) {
        var fluidIn = container.getIncomingFluid();
        var fluidInside = container.getBottle().getFluid();
        if (fluidInside.isEmpty()) {
            return fluidIn.copy();
        }
        Mixture newMixture = Mixture.getMixtureFromMixable(fluidIn.copy());
        Mixture insideMixture = Mixture.getMixtureFromMixable(fluidInside.copy());
        var mix =  Mixture.mix(newMixture, fluidIn.getAmount(), insideMixture, fluidInside.getAmount());
        return Mixture.asFluid(mix, fluidIn.getAmount() + fluidInside.getAmount());
    }

    @Override
    public boolean matchForPouring(BottleInteractionContainer container, Level level) {
        var item = container.getItem(0).getItem();

        // Gonna hardcode this one for the moment
        if (item != Items.GLASS_BOTTLE) return false;

        var tank = container.getBottle().tank;
        return tank
                .getFluid()
                .getFluid().is(POTION_TAG) && tank.getFluidAmount() >= DrinkingRecipe.DEFAULT_AMOUNT;
    }

    @Override
    public ItemStack assembleForPouring(BottleInteractionContainer container) {
        var stack = new ItemStack(Items.POTION);

        var tank = container.getBottle().tank;
        var fluidTag = tank.getFluid().getTag();

        if (fluidTag.contains(MixturePart.TAG_MODIFIER)) {
            var modifierTag = Mixture.Modifier.from(fluidTag.getString(MixturePart.TAG_MODIFIER));
            if (modifierTag == Mixture.Modifier.LINGERING) {
                stack =  new ItemStack(Items.LINGERING_POTION);
            } else if (modifierTag == Mixture.Modifier.SPLASH) {
                stack = new ItemStack(Items.SPLASH_POTION);
            }
            fluidTag.remove(MixturePart.TAG_MODIFIER);
        }

        stack.setTag(fluidTag);

        tank.drain(DrinkingRecipe.DEFAULT_AMOUNT, IFluidHandler.FluidAction.EXECUTE);
        return stack;
    }

    @Override
    public String getId() {
        return "recicropal:potion";
    }
}
