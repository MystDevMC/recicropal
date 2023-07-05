package com.mystdev.recicropal.content.mixing;

import com.mystdev.recicropal.ModFluids;
import com.mystdev.recicropal.common.fluid.ModFluidUtils;
import com.mystdev.recicropal.content.drinking.DrinkingRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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

        var fluid = new FluidStack(ModFluidUtils.forcedMember(POTION_TAG),
                                   DrinkingRecipe.DEFAULT_AMOUNT,
                                   voidItem.getTag());

        if (potion == Potions.WATER) {
            fluid = new FluidStack(Fluids.WATER, DrinkingRecipe.DEFAULT_AMOUNT);
        }
        return fluid;
    }

    @Override
    public boolean matchForMixing(MixingContainer container, Level level) {
        var fluidIn = container.getIncomingFluid();
        var fluidInside = container.getBottle().getFluid();
        if (!fluidIn.getFluid().is(POTION_TAG) && fluidIn.getFluid() != ModFluids.MIXTURE.get().getSource())
            return false;
        var isPotion = fluidInside.getFluid().is(POTION_TAG);
        var isMixture = fluidInside.getFluid() == ModFluids.MIXTURE.get().getSource();
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
        FluidStack newMixture;
        if (fluidIn.getFluid().is(POTION_TAG)) {
            newMixture = mixtureFromPotion(fluidIn);
        }
        else {
            newMixture = fluidIn.copy();
        }
        FluidStack insideMixture;
        if (fluidInside.getFluid().is(POTION_TAG)) {
            insideMixture = mixtureFromPotion(fluidInside);
        }
        else {
            insideMixture = fluidInside.copy();
        }
        return mix(newMixture, insideMixture);
    }

    private static FluidStack mixtureFromPotion(FluidStack potionFluid) {
        var base = new FluidStack(ModFluids.MIXTURE.get().getSource(), potionFluid.getAmount());
        var potionTag = potionFluid.getOrCreateTag();
        var baseTag = new CompoundTag();
        var potionsTag = new ListTag();
        var tag = potionTag.get(PotionUtils.TAG_POTION);
        if (tag != null) {
            potionsTag.add(tag);
        }
        baseTag.put("Potions", potionsTag);
        base.setTag(baseTag);
        return base;
    }

    private static FluidStack mix(FluidStack fluid1, FluidStack fluid2) {
        var list1 = (ListTag) fluid1.getOrCreateTag().get("Potions");
        var list2 = (ListTag) fluid2.getOrCreateTag().get("Potions");
        if (list1 == null) list1 = new ListTag();
        if (list2 == null) list2 = new ListTag();
        list1.addAll(list2);
        var mergedTag = new CompoundTag();
        mergedTag.put("Potions", list1);
        return new FluidStack(ModFluids.MIXTURE.get().getSource(), fluid1.getAmount() + fluid2.getAmount(), mergedTag);
    }

    @Override
    public boolean matchForPouring(BottleInteractionContainer container, Level level) {
        var item = container.getItem(0).getItem();

        // Gonna hardcode this one for the moment
        if (item != Items.GLASS_BOTTLE) return false;

        var tank = container.getBottle().tank;
        return tank
                .getFluid()
                .getFluid() == ModFluids.POTION.get() && tank.getFluidAmount() >= DrinkingRecipe.DEFAULT_AMOUNT;
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
