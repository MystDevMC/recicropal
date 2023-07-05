package com.mystdev.recicropal.content.mixing;

import com.mystdev.recicropal.content.crop.bottle_gourd.BottleGourdTank;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class MixingContainer extends RecipeWrapper {
    private final BottleGourdTank bottle;
    private final FluidStack incomingFluid;

    public MixingContainer(BottleGourdTank bottle, FluidStack incomingFluid) {
        super(new ItemStackHandler());
        this.bottle = bottle;
        this.incomingFluid = incomingFluid;
    }

    public BottleGourdTank getBottle() {
        return bottle;
    }

    public FluidStack getIncomingFluid() {
        return incomingFluid;
    }
}
