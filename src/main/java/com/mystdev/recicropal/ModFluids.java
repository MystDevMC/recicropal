package com.mystdev.recicropal;

import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class ModFluids {

    public static void init() {}

    public static final FluidEntry<? extends ForgeFlowingFluid> MIXTURE = fluid("mixture");
    public static final FluidEntry<? extends ForgeFlowingFluid> POTION = fluid("potion");
    public static final FluidEntry<? extends ForgeFlowingFluid> MILK = fluid("milk");
    public static final FluidEntry<? extends ForgeFlowingFluid> HONEY = fluid("honey");

    private static FluidEntry<? extends ForgeFlowingFluid> fluid(String name) {
        return Recicropal.REGISTRATE
                .get()
                .fluid(name,
                       new ResourceLocation("block/fluid/" + name + "_still"),
                       new ResourceLocation("block/fluid/" + name + "_flowing"))
                .register();
    }
}
