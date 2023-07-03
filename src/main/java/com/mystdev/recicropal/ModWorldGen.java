package com.mystdev.recicropal;

import com.mojang.datafixers.util.Pair;
import com.mystdev.recicropal.content.vine_patch.VinePatchConfiguration;
import com.mystdev.recicropal.content.vine_patch.VinePatchFeature;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class ModWorldGen {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, Recicropal.MOD_ID);

    public static final RegistryObject<VinePatchFeature> VINE_PATCH_BIOME_FEATURE =
            FEATURES.register("vine_patch", () -> new VinePatchFeature(VinePatchConfiguration.CODEC));

    public static Holder<ConfiguredFeature<?, ?>> VINE_PATCH_CONFIGURED_FEATURE;
    public static Holder<PlacedFeature> VINE_PATCH_PLACEMENT;

    public static void register() {
        VINE_PATCH_CONFIGURED_FEATURE = BuiltinRegistries
                .register(
                        BuiltinRegistries.CONFIGURED_FEATURE,
                        Recicropal.rl("vine_patch"),
                        new ConfiguredFeature<>(
                                VINE_PATCH_BIOME_FEATURE.get(),
                                new VinePatchConfiguration("recicropal:bottle_gourd_crop", 8, 3)
                        )
                );
        VINE_PATCH_PLACEMENT = BuiltinRegistries
                .register(
                        BuiltinRegistries.PLACED_FEATURE,
                        Recicropal.rl("vine_patch"),
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

    public static void addVillageBuildings(ServerAboutToStartEvent event) {
        var tpReg = event.getServer().registryAccess().registry(Registry.TEMPLATE_POOL_REGISTRY).get();

        var pool = tpReg.get(new ResourceLocation("minecraft:village/desert/houses"));
        if (pool != null) {
            var piece = SinglePoolElement.single("recicropal:village/houses/drinking_well").apply(
                    StructureTemplatePool.Projection.RIGID);
            pool.templates.add(piece);
            pool.templates.add(piece);

            var entries = new ArrayList<>(pool.rawTemplates);
            entries.add(new Pair<>(piece, 2));
            pool.rawTemplates = entries;
        }
        pool = tpReg.get(new ResourceLocation("minecraft:village/taiga/houses"));
        if (pool != null) {
            var piece = SinglePoolElement.single("recicropal:village/houses/pumpkin_garden").apply(
                    StructureTemplatePool.Projection.RIGID);
            pool.templates.add(piece);
            pool.templates.add(piece);
            pool.templates.add(piece);
            pool.templates.add(piece);

            var entries = new ArrayList<>(pool.rawTemplates);
            entries.add(new Pair<>(piece, 4));
            pool.rawTemplates = entries;
        }
    }

}
