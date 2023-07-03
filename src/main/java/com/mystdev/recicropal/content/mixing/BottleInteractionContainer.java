package com.mystdev.recicropal.content.mixing;

import com.mystdev.recicropal.content.crop.bottle_gourd.BottleGourdBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class BottleInteractionContainer extends RecipeWrapper {

    private final BottleGourdBlockEntity bottle;

    public BottleInteractionContainer(ItemStack stack, BottleGourdBlockEntity bottle) {
        super(new ItemStackHandler());
        this.setItem(0, stack);
        this.bottle = bottle;
    }

    public BottleGourdBlockEntity getBottle() {
        return bottle;
    }
}
