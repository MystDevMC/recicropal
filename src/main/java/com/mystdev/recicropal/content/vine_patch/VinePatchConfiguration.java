package com.mystdev.recicropal.content.vine_patch;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mystdev.recicropal.content.trellis.TrellisCropBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

public class VinePatchConfiguration implements FeatureConfiguration {
    public static final Codec<VinePatchConfiguration> CODEC = RecordCodecBuilder.create(cfg -> cfg.group(
            Codec.STRING.fieldOf("blockToPlaceRL").forGetter(VinePatchConfiguration::getBlockToPlaceRL),
            Codec.INT.fieldOf("spreadRadius").forGetter(VinePatchConfiguration::getSpreadRadius),
            Codec.INT.fieldOf("heightRange").forGetter(VinePatchConfiguration::getHeightRange)
    ).apply(cfg, VinePatchConfiguration::new));
    private String blockToPlaceRL;
    private final int spreadRadius;
    private final int heightRange;
    private final Supplier<Optional<TrellisCropBlock>> blockToPlace = Suppliers.memoize(
            () -> Optional.ofNullable((TrellisCropBlock) ForgeRegistries.BLOCKS.getValue(new ResourceLocation(this.blockToPlaceRL))));

    public VinePatchConfiguration(String blockToPlaceRL, int spreadRadius, int heightRange) {
        this.blockToPlaceRL = blockToPlaceRL;
        this.spreadRadius = spreadRadius;
        this.heightRange = heightRange;
    }

    public String getBlockToPlaceRL() {
        return blockToPlaceRL;
    }

    public Optional<TrellisCropBlock> getBlockToPlace() {
        return blockToPlace.get();
    }

    public int getSpreadRadius() {
        return spreadRadius;
    }

    public int getHeightRange() {
        return heightRange;
    }
}
