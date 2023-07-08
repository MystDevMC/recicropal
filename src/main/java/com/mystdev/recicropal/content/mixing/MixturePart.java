package com.mystdev.recicropal.content.mixing;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class MixturePart implements INBTSerializable<CompoundTag> {
    public static final String TAG_EFFECTS = "Effects";
    public static final String TAG_MOLARITY = "Molarity";
    public static final String TAG_COLOR = "Color";
    public static final String TAG_MODIFIER = "Modifier";
    private final String id;
    private float molarity;
    private List<MobEffectInstance> effects;
    private int color;
    private Mixture.Modifier modifier;

    public MixturePart(String id,
                       float molarity,
                       List<MobEffectInstance> effects,
                       @Nullable Integer color,
                       Mixture.Modifier modifier) {
        this.id = id + "." + modifier;
        this.molarity = molarity;
        this.effects = effects;
        this.color = Optional.ofNullable(color).orElse(PotionUtils.getColor(effects));
        this.modifier = modifier;
    }

    public MixturePart(MixturePart component) {
        this.id = component.getId();
        this.effects = component.effects;
        this.color = component.color;
        this.molarity = component.molarity;
        this.modifier = component.modifier;
    }

    public MixturePart(String id) {
        this.id = id;
    }

    public static String effectKey(MobEffectInstance effectInstance) {
        return Objects.requireNonNull(ForgeRegistries.MOB_EFFECTS.getKey(effectInstance.getEffect())).toString();
    }

    public float getMolarity() {
        return molarity;
    }

    public void setMolarity(float molarity) {
        this.molarity = molarity;
    }

    public List<MobEffectInstance> getEffects() {
        return this.effects;
    }

    public int getColor() {
        return color;
    }

    public String getId() {
        return id;
    }

    public Mixture.Modifier getModifier() {
        return modifier;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        var effectsTag = new CompoundTag();
        var counter = new AtomicInteger(0);
        effects.forEach(e -> effectsTag.put(String.valueOf(counter.getAndIncrement()), e.save(new CompoundTag())));
        tag.put(TAG_EFFECTS, effectsTag);
        tag.putFloat(TAG_MOLARITY, this.molarity);
        tag.putInt(TAG_COLOR, this.color);
        tag.putString(TAG_MODIFIER, this.modifier.getSerializedName());
        return tag;
    }

    // Got 9 From PotionUtils
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.effects = new ObjectArrayList<>();
        var effectTag = nbt.getCompound(TAG_EFFECTS);
        effectTag.getAllKeys().forEach(key -> effects.add(MobEffectInstance.load(effectTag.getCompound(key))));
        this.molarity = nbt.getFloat(TAG_MOLARITY);
        this.color = nbt.getInt(TAG_COLOR);
        this.modifier = Mixture.Modifier.from(nbt.getString(TAG_MODIFIER));
    }

    public Collection<EffectEntry> toEffectEntries() {
        var collection = new ObjectArrayList<EffectEntry>();
        effects.forEach(effectInstance -> collection.add(new EffectEntry(effectKey(effectInstance),
                                                                         molarity,
                                                                         effectInstance)));
        return collection;
    }

    public static class EffectEntry {
        public final String id;
        private final MobEffectInstance effectInstance;
        private float molarity;

        public EffectEntry(String id, float molarity, MobEffectInstance effectInstance) {
            this.id = id;
            this.molarity = molarity;
            this.effectInstance = effectInstance;
        }

        public MobEffectInstance getEffectInstance() {
            return effectInstance;
        }

        public float getMolarity() {
            return molarity;
        }

        public void setMolarity(float molarity) {
            this.molarity = molarity;
        }
    }
}
