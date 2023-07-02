package com.mystdev.recicropal.content.drinking;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class FluidHandlerItemContainer extends RecipeWrapper {

    private final ItemStack fluidHandlerItem;

    public FluidHandlerItemContainer(ItemStack fluidHandlerItem) {
        super(new ItemStackHandler());
        this.fluidHandlerItem = fluidHandlerItem;
    }

    public FluidStack getFluid() {
        return FluidUtil.getFluidContained(fluidHandlerItem).orElse(FluidStack.EMPTY);
    }

    public ItemStack getFluidHandlerItem() {
        return fluidHandlerItem;
    }
}
