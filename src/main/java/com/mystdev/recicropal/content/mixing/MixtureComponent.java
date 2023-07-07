package com.mystdev.recicropal.content.mixing;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MixtureComponent implements INBTSerializable<CompoundTag> {
    private float molarity;
    private final String id;
    private int color;

    public MixtureComponent(String id, float molarity, @Nullable Integer color) {
        this.id = id;
        this.molarity = molarity;
        this.color = Optional.ofNullable(color).orElse(PotionUtils.getColor(getPotion()));
    }

    public MixtureComponent(MixtureComponent component) {
        this.id = component.getId();
        this.color = component.color;
        this.molarity = component.molarity;
    }

    public MixtureComponent(String id) {
        this.id = id;
    }

    public float getMolarity() {
        return molarity;
    }

    public Potion getPotion() {
        return Potion.byName(this.id);
    }

    public int getColor() {
        return color;
    }

    public String getId() {
        return id;
    }

    public void setMolarity(float molarity) {
        this.molarity = molarity;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putFloat("molarity", this.molarity);
        tag.putInt("color", this.color);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.molarity = nbt.getFloat("molarity");
        this.color = nbt.getInt("color");
    }
}
