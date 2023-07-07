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
    private Mixture.Modifier modifier;

    public MixtureComponent(String id, float molarity, @Nullable Integer color, Mixture.Modifier modifier) {
        this.id = id;
        this.molarity = molarity;
        this.color = Optional.ofNullable(color).orElse(PotionUtils.getColor(getPotion()));
        this.modifier = modifier;
    }

    public MixtureComponent(MixtureComponent component) {
        this.id = component.getId();
        this.color = component.color;
        this.molarity = component.molarity;
        this.modifier = component.modifier;
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

    public Mixture.Modifier getModifier() {
        return modifier;
    }

    public static final String TAG_MOLARITY = "Molarity";
    public static final String TAG_COLOR = "Color";
    public static final String TAG_MODIFIER = "Modifier";

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putFloat(TAG_MOLARITY, this.molarity);
        tag.putInt(TAG_COLOR, this.color);
        tag.putString(TAG_MODIFIER, this.modifier.getSerializedName());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.molarity = nbt.getFloat(TAG_MOLARITY);
        this.color = nbt.getInt(TAG_COLOR);
        this.modifier = Mixture.Modifier.from(nbt.getString(TAG_MODIFIER));
    }
}
