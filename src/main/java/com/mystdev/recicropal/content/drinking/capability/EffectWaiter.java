package com.mystdev.recicropal.content.drinking.capability;

import com.mystdev.recicropal.Recicropal;
import com.mystdev.recicropal.content.drinking.DrinkingRecipe;
import com.mystdev.recicropal.content.mixing.MixturePart;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Map;

public class EffectWaiter implements INBTSerializable<CompoundTag> {
    private final Map<String, Integer> map = new Object2ObjectOpenHashMap<>();


    /**
     * Creates a key for hashing purposes.
     *
     * @param instance the effect instance.
     * @return a key for said instance. Null if effect could not be found in the registry.
     * @throws IllegalArgumentException when tried to accept non-instantaneous effects.
     */
    @Nullable
    public static String hash(MobEffectInstance instance) {
        if (!instance.getEffect().isInstantenous())
            throw new IllegalArgumentException("No non-instantaneous effects should be put here!");
        var rl = ForgeRegistries.MOB_EFFECTS.getKey(instance.getEffect());
        if (rl == null) return null;
        return MessageFormat.format("{0}.{1}", rl.toString(), instance.getAmplifier());
    }


    public void put(MixturePart.EffectEntry entry, int drunkAmount, Player player) {
        var hash = hash(entry.getEffectInstance());
        if (hash == null) return;
        var moles = Math.round(drunkAmount * entry.getMolarity());

        // Before we count the insides...
        while (moles >= DrinkingRecipe.DEFAULT_AMOUNT) {
            entry.getEffectInstance().applyEffect(player);
            if (moles == DrinkingRecipe.DEFAULT_AMOUNT) {
                break;
            }
            else {
                moles -= DrinkingRecipe.DEFAULT_AMOUNT;
            }
        }

        // Count the insides
        if (map.get(hash) != null) {
            var totalMoles = map.get(hash) + moles;
            while (totalMoles >= DrinkingRecipe.DEFAULT_AMOUNT) {
                entry.getEffectInstance().applyEffect(player);
                if (totalMoles == DrinkingRecipe.DEFAULT_AMOUNT) {
                    map.remove(hash);
                    break;
                }
                else {
                    totalMoles -= DrinkingRecipe.DEFAULT_AMOUNT;
                }
            }
            map.put(hash, totalMoles);
        }
        else {
            map.put(hash, moles);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        map.forEach(tag::putInt);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        nbt.getAllKeys().forEach(key -> map.put(key, nbt.getInt(key)));
    }
}
