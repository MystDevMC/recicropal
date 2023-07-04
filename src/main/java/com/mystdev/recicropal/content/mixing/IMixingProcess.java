package com.mystdev.recicropal.content.mixing;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IMixingProcess {
    boolean matchForFilling(BottleInteractionContainer container, Level level);

    ItemStack assembleForFilling(BottleInteractionContainer container);

    boolean matchForPouring(BottleInteractionContainer container, Level level);

    ItemStack assembleForPouring(BottleInteractionContainer container);

    String getId();

    static IMixingProcess get(String key) {
        if (key.equals(PotionProcess.INSTANCE.getId())) return PotionProcess.INSTANCE;
        else throw new IllegalArgumentException("There's no such thing as " + key);
    }
}