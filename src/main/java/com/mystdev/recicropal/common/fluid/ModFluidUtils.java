package com.mystdev.recicropal.common.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;

public class ModFluidUtils {
    public static TagKey<Fluid> tag(String key) {
        return FluidTags.create(new ResourceLocation(key));
    }

    public static String key(Fluid fluid) {
        return Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluid)).toString();
    }

    public static List<Fluid> members(TagKey<Fluid> tag) {
        return Objects.requireNonNull(ForgeRegistries.FLUIDS.tags()).getTag(tag).stream().toList();
    }

    public static Fluid forcedMember(TagKey<Fluid> tag) {
        return Objects
                .requireNonNull(ForgeRegistries.FLUIDS.tags())
                .getTag(tag)
                .stream()
                .findFirst()
                .orElse(Fluids.EMPTY);
    }

    public static Fluid fluidOrAir(String key) {
        var fluid = fluid(key);
        return fluid == null ? Fluids.EMPTY : fluid;
    }

    public static Fluid fluid(String key) {
        return ForgeRegistries.FLUIDS.getValue(new ResourceLocation(key));
    }

    public static FluidTank voidTank() {
        return new FluidTank(Integer.MAX_VALUE);
    }
}
