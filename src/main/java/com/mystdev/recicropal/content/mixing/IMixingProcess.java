package com.mystdev.recicropal.content.mixing;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

public interface IMixingProcess {
    static IMixingProcess get(String key) {
        if (key.equals(PotionProcess.INSTANCE.getId())) return PotionProcess.INSTANCE;
        else throw new IllegalArgumentException("There's no such thing as " + key);
    }

    boolean matchForFilling(BottleInteractionContainer container, Level level);

    ItemStack assembleForFilling(BottleInteractionContainer container);

    boolean matchForMixing(MixingContainer container, Level level);

    // Make sure to NOT modify anything from the inputs
    FluidStack getMixingResult(MixingContainer container);

    boolean matchForPouring(BottleInteractionContainer container, Level level);

    ItemStack assembleForPouring(BottleInteractionContainer container);

    String getId();
}
