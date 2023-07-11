package com.mystdev.recicropal.content.mixing;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.function.BiFunction;

public enum FluidTransformer {
    NORMAL((fluid, stack) -> new FluidStack(fluid, stack.getAmount(), stack.getTag())),

    POTION_FROM_CREATE_POTION((fluid, stack) -> {
        var tag = stack.getOrCreateTag().copy();
        if (tag.contains("Bottle")) {
            var bottleType = tag.getString("Bottle");
            var modifier = Mixture.Modifier.from(bottleType);
            tag.remove("Bottle");
            tag.putString(MixturePart.TAG_MODIFIER, modifier.getSerializedName());
        }
        return new FluidStack(fluid, stack.getAmount(), tag);
    }),

    CREATE_POTION_FROM_POTION((fluid, stack) -> {
        var tag = stack.getOrCreateTag().copy();
        if (tag.contains(MixturePart.TAG_MODIFIER)) {
            var modifierType = Mixture.Modifier.from(tag.getString(MixturePart.TAG_MODIFIER));
            var bottleType = modifierType != Mixture.Modifier.NORMAL ? modifierType.getSerializedName() : "regular";
            tag.remove(MixturePart.TAG_MODIFIER);
            tag.putString("Bottle", bottleType);
        }
        return new FluidStack(fluid, stack.getAmount(), tag);
    }),

    POTION_FROM_COFH_CORE_POTION((fluid, stack) -> {
        var tag = stack.getOrCreateTag().copy();
        tag.putString(MixturePart.TAG_MODIFIER, Mixture.Modifier.NORMAL.getSerializedName());
        return new FluidStack(fluid, stack.getAmount(), tag);
    });

    private final BiFunction<Fluid, FluidStack, FluidStack> converter;

    FluidTransformer(BiFunction<Fluid, FluidStack, FluidStack> converter) {
        this.converter = converter;
    }

    public FluidStack convert(Fluid fluid, FluidStack stack) {
        return this.converter.apply(fluid, stack);
    }

    public static FluidTransformer tryParse(String name) {
        if (name.isEmpty()) return NORMAL;
        return Arrays
                .stream(FluidTransformer.values())
                .filter(transformer -> transformer.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(NORMAL);
    }
}
