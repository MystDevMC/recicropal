package com.mystdev.recicropal.content.mixing;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class EffectProvider {
    private final List<MobEffectInstance> effects = new ObjectArrayList<>();
    private final List<Supplier<List<MobEffectInstance>>> lazyEffects = new ObjectArrayList<>();

    public EffectProvider of(Supplier<Potion> potion) {
        this.lazyEffects.add(() -> potion.get().getEffects());
        return this;
    }

    public EffectProvider of(Potion potion) {
        this.effects.addAll(potion.getEffects());
        return this;
    }

    public EffectProvider of(Collection<MobEffectInstance> effects) {
        this.effects.addAll(effects);
        return this;
    }

    public List<MobEffectInstance> getEffects() {
        var list = new ObjectArrayList<MobEffectInstance>();
        list.addAll(effects);
        list.addAll(lazyEffects.stream().flatMap(s -> s.get().stream()).toList());
        return list;
    }
}
