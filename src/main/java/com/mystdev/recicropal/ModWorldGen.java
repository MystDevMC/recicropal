package com.mystdev.recicropal;

import com.mystdev.recicropal.content.vine_patch.VinePatchConfiguration;
import com.mystdev.recicropal.content.vine_patch.VinePatchFeature;
import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class ModWorldGen {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, Recicropal.MOD_ID);

    public static final RegistryObject<VinePatchFeature> VINE_PATCH_BIOME_FEATURE =
            FEATURES.register("vine_patch", () -> new VinePatchFeature(VinePatchConfiguration.CODEC));

    public static Holder<ConfiguredFeature<?, ?>> VINE_PATCH_CONFIGURED_FEATURE;
    public static Holder<PlacedFeature> VINE_PATCH_PLACEMENT;

    public static void register() {
        // TODO: Make Biome Modifiers instead so vine patches could accept other vines as well
        VINE_PATCH_CONFIGURED_FEATURE = BuiltinRegistries
                .register(
                        BuiltinRegistries.CONFIGURED_FEATURE,
                        new ResourceLocation(Recicropal.MOD_ID, "vine_patch"),
                        new ConfiguredFeature<>(
                                VINE_PATCH_BIOME_FEATURE.get(),
                                new VinePatchConfiguration("recicropal:bottle_gourd_crop", 8, 3)
                        )
                );
        VINE_PATCH_PLACEMENT = BuiltinRegistries
                .register(
                        BuiltinRegistries.PLACED_FEATURE,
                        new ResourceLocation(Recicropal.MOD_ID, "vine_patch"),
                        new PlacedFeature(
                                Holder.hackyErase(VINE_PATCH_CONFIGURED_FEATURE),
                                List.of(
                                        RarityFilter.onAverageOnceEvery(128),
                                        InSquarePlacement.spread(),
                                        PlacementUtils.HEIGHTMAP,
                                        BiomeFilter.biome()
                                )
                        )
                );
    }

    public static void init(IEventBus modBus) {
        FEATURES.register(modBus);
    }

}
