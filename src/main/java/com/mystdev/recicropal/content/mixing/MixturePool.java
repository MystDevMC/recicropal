package com.mystdev.recicropal.content.mixing;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MixturePool {
    private final List<EffectProvider> pool = new ObjectArrayList<>();

    public MixturePool withEntry(EffectProvider provider, int weight) {
        var i = 0;
        while (i < weight) {
            pool.add(provider);
            i++;
        }
        return this;
    }

    public EffectProvider pull(RandomSource randomSource) {
        return pool.get(randomSource.nextInt(pool.size()));
    }

    public Mixture pullMixture(String name, @Nullable Mixture.Modifier modifier, int amount, RandomSource randomSource) {
        var prov = this.pull(randomSource);
        var part = new MixturePart(name, 1, prov.getEffects(), null, modifier == null ? Mixture.Modifier.NORMAL : modifier);
        var mixture = new Mixture();
        mixture.addMixturePart(part, amount, 0);
        return mixture;
    }
}
