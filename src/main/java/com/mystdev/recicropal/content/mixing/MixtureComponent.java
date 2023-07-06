package com.mystdev.recicropal.content.mixing;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.common.util.INBTSerializable;

public class MixtureComponent implements INBTSerializable<CompoundTag> {
    private float molarity;
    private final String id;

    public MixtureComponent(String id, float molarity) {
        this.molarity = molarity;
        this.id = id;
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
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.molarity = nbt.getFloat("molarity");
    }
}
