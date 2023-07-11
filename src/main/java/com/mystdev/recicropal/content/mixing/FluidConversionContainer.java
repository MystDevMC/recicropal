package com.mystdev.recicropal.content.mixing;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class FluidConversionContainer extends RecipeWrapper {
    public final FluidStack fluidStack;
    public final boolean isIngress;
    public FluidConversionContainer(FluidStack fluidStack, boolean isIngress) {
        super(new ItemStackHandler());
        this.fluidStack = fluidStack;
        this.isIngress = isIngress;
    }
}
