package com.mystdev.recicropal.content.mixing;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IFillingProcess {
    boolean matches(BottleInteractionContainer container, Level level);
    ItemStack assemble(BottleInteractionContainer container);
    String getId();
}
