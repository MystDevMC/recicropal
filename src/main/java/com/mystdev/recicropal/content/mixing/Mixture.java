package com.mystdev.recicropal.content.mixing;

import com.mojang.datafixers.util.Either;
import com.mystdev.recicropal.ModFluids;
import com.mystdev.recicropal.content.drinking.DrinkingRecipe;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Mixture implements INBTSerializable<CompoundTag> {
    public static final String TAG_POTIONS = "Potions";
    public static final String TAG_COLOR = "Color";
    public static final String TAG_CATEGORY = "Category";
    private final Map<String, MixturePart> components = new Object2ObjectOpenHashMap<>();
    @Nullable
    private Category category;
    @Nullable
    private Integer color;

    public static Mixture fromFluid(FluidStack mixtureStack) {
        if (!isMixture(mixtureStack)) return new Mixture();
        var nbt = mixtureStack.getOrCreateTag();
        var mixture = new Mixture();
        mixture.deserializeNBT(nbt);
        return mixture;
    }

    public static boolean isMixture(FluidStack stack) {
        return stack.getFluid() == getMixtureFluid();
    }

    public static Fluid getMixtureFluid() {
        return ModFluids.MIXTURE.get();
    }

    public static Mixture mixtureFromPotion(FluidStack potionFluid) {
        var amount = potionFluid.getAmount();
        var mixture = new Mixture();

        var potionTag = potionFluid.getOrCreateTag();
        var tag = potionTag.get(PotionUtils.TAG_POTION);
        if (tag != null) {
            var hasColor = potionTag.contains(PotionUtils.TAG_CUSTOM_POTION_COLOR);

            Integer color = null;
            if (hasColor) {
                color = potionTag.getInt(PotionUtils.TAG_CUSTOM_POTION_COLOR);
            }

            var modifier = Modifier.NORMAL;
            if (potionTag.contains(MixturePart.TAG_MODIFIER)) {
                modifier = Modifier.from(potionTag.getString(MixturePart.TAG_MODIFIER));
            }

            var comp = new MixturePart(tag.getAsString(), 1, PotionUtils.getAllEffects(potionTag), color, modifier);
            mixture.addMixturePart(comp, amount, 0);
        }
        return mixture;
    }

    public static Mixture getMixtureFromMixable(FluidStack mixable) {
        if (PotionFluid.isPotion(mixable)) {
            return Mixture.mixtureFromPotion(mixable);
        }
        else if (isMixture(mixable)) {
            return Mixture.fromFluid(mixable);
        }
        throw new IllegalArgumentException("Inserted fluid is not mixable!");
    }

    public static FluidStack asFluid(Mixture mixture, int amount) {
        var mixtureFluid = new FluidStack(getMixtureFluid(), amount);
        mixtureFluid.setTag(mixture.serializeNBT());
        return mixtureFluid;
    }

    public static Mixture mix(Mixture mixture1, int mixture1Amount, Mixture mixture2, int mixture2Amount) {
        var newMixture = new Mixture();
        var resultingVolume = mixture1Amount + mixture2Amount;
        mixture1.components
                .values()
                .forEach(component -> {
                    var newComponent = new MixturePart(component);
                    var moles = component.getMolarity() * mixture1Amount;
                    newComponent.setMolarity(moles / resultingVolume);
                    newMixture.components.put(component.getId(), newComponent);
                });
        newMixture.updateCategory(mixture1.category);
        newMixture.updateColor(mixture1.getColor(), (float) mixture1Amount / resultingVolume);
        mixture2.components
                .values()
                .forEach(component -> {
                    var newComponent = new MixturePart(component);
                    var m1 = component.getMolarity() * mixture2Amount;
                    var moles = m1;
                    newComponent.setMolarity(m1 / resultingVolume);

                    var oldEntry = newMixture.components.get(component.getId());
                    if (oldEntry != null) {
                        var m2 = oldEntry.getMolarity() * resultingVolume;
                        moles = m1 + m2;
                        newComponent.setMolarity(moles / resultingVolume);
                    }

                    newMixture.components.put(component.getId(), newComponent);
                });
        newMixture.updateCategory(mixture2.category);
        newMixture.updateColor(mixture2.getColor(), (float) mixture2Amount / resultingVolume);
        return newMixture;
    }

    public static Pair<Integer, Category> getColorAndCategory(FluidStack mixtureFluid) {
        var tag = mixtureFluid.getOrCreateTag();
        return Pair.of(tag.getInt(TAG_COLOR), Category.from(tag.getString(TAG_CATEGORY)));
    }

    private static MobEffectInstance copyEffectWithDuration(MobEffectInstance instance,
                                                            int duration) {
        return new MobEffectInstance(instance.getEffect(),
                                     duration,
                                     instance.getAmplifier(),
                                     instance.isAmbient(),
                                     instance.isVisible(),
                                     instance.showIcon(),
                                     instance.hiddenEffect,
                                     instance.getFactorData());
    }

    private static Category inferCategory(List<MobEffectInstance> effects) {
        Category start = null;
        for (var effectInstance : effects) {
            var category = effectInstance.getEffect().getCategory();
            if (start == null) {
                start = Category.from(category);
            }

            else if (start != Category.from(category)) {
                return Category.NEUTRAL;
            }

            if (start == Category.NEUTRAL) {
                return Category.NEUTRAL;
            }
        }
        return start;
    }

    public Category getCategory() {
        return category == null ? Category.NEUTRAL : category;
    }

    public Integer getColor() {
        if (this.color == null) {
            if (this.components.isEmpty()) return PotionUtils.getColor(Potions.EMPTY);

            var r = 0;
            var g = 0;
            var b = 0;
            for (MixturePart comp : this.components.values()) {
                var color = comp.getColor();

                r += ((color >> 16) & 0xFF) * comp.getMolarity();
                g += ((color >> 8) & 0xFF) * comp.getMolarity();
                b += (color & 0xFF) * comp.getMolarity();
            }
            this.color = (r << 16) | (g << 8) | b;
        }
        return this.color;
    }

    private void updateColor(int incomingColor, float incomingMolarity) {
        if (this.color == null) this.color = incomingColor;
        else {
            var r1 = (color >> 16) & 0xFF;
            var g1 = (color >> 8) & 0xFF;
            var b1 = color & 0xFF;

            var r2 = (incomingColor >> 16) & 0xFF;
            var g2 = (incomingColor >> 8) & 0xFF;
            var b2 = incomingColor & 0xFF;

            var selfWeight = 1 - incomingMolarity;

            int r = (int) ((r1 * selfWeight) + (r2 * incomingMolarity));
            int g = (int) ((g1 * selfWeight) + (g2 * incomingMolarity));
            int b = (int) ((b1 * selfWeight) + (b2 * incomingMolarity));

            this.color = (r << 16) | (g << 8) | b;
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        var potionsTag = new CompoundTag();
        this.components.forEach((key, value) -> potionsTag.put(key, value.serializeNBT()));
        tag.put(TAG_POTIONS, potionsTag);
        if (this.category != null) tag.putString(TAG_CATEGORY, this.category.name);
        if (this.color != null) tag.putInt(TAG_COLOR, this.color);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        var potionsTag = nbt.getCompound(TAG_POTIONS);
        potionsTag.getAllKeys().forEach(key -> {
            var mixture = new MixturePart(key);
            mixture.deserializeNBT(potionsTag.getCompound(key));
            components.put(key, mixture);
        });
        if (nbt.contains(TAG_CATEGORY)) this.category = Category.from(nbt.getString(TAG_CATEGORY));
        if (nbt.contains(TAG_COLOR)) this.color = nbt.getInt(TAG_COLOR);
    }

    // Add singular mixture component
    public void addMixturePart(MixturePart component, int addedVolume, int previousVolume) {
        var currentVolume = previousVolume + addedVolume;
        if (previousVolume != 0 && addedVolume != 0) {
            this.components
                    .values()
                    .forEach(comp -> comp.setMolarity((comp.getMolarity() * previousVolume) / (currentVolume)));
        }

        var oldEntry = this.components.get(component.getId());
        if (oldEntry != null) {
            component.setMolarity((oldEntry.getMolarity() * currentVolume + component.getMolarity()) / currentVolume);
            this.components.put(component.getId(), component);
        }
        else {
            component.setMolarity(component.getMolarity() * addedVolume / currentVolume);
            this.components.put(component.getId(), component);
            this.updateCategory(inferCategory(component.getEffects()));
            this.updateColor(component.getColor(), component.getMolarity());
        }
    }

    private Stream<MixturePart.EffectEntry> collateSimilarEffects() {
        var map = new Object2ObjectOpenHashMap<String, List<MixturePart.EffectEntry>>();
        components
                .values()
                .stream()
                .map(MixturePart::toEffectEntries)
                .flatMap(Collection::stream)
                .forEach(effectEntry -> {
                    if (!map.containsKey(effectEntry.id)) {
                        var set = new ObjectArrayList<MixturePart.EffectEntry>();
                        set.add(effectEntry);
                        map.put(effectEntry.id, set);
                    }
                    else {
                        var set = map.get(effectEntry.id);
                        for (MixturePart.EffectEntry e : set) {
                            e.setMolarity(e.getMolarity() + effectEntry.getMolarity());
                        }
                        effectEntry.setMolarity(set.get(0).getMolarity());
                        set.add(effectEntry);
                    }
                });
        return map.values().stream().flatMap(Collection::stream);
    }

    public List<Either<MixturePart.EffectEntry, MobEffectInstance>> getRationedEffects(int drunkAmount) {
        // This should not happen unless the mixture is empty
        if (components.size() == 0) {
            return List.of();
        }
        // Hardcoded potion types handling
        var lingering = new Object() {
            float value = 0;
        };
        var splash = new Object() {
            float value = 0;
        };
        var totalDuration = new Object() {
            int value = 0;
        };
        var numberOfEffects = new Object() {
            int value = 0;
        };

        components.forEach((key, value) -> {
            var list = value.getEffects();
            for (var e : list) {
                totalDuration.value += e.getDuration();
                numberOfEffects.value++;
            }
            if (value.getModifier() == Modifier.SPLASH) splash.value += value.getMolarity();
            if (value.getModifier() == Modifier.LINGERING) lingering.value += value.getMolarity();
        });
        return this.collateSimilarEffects()
                   .map(component -> {
                       var effectInstance = component.getEffectInstance();

                       if (effectInstance.getEffect().isInstantenous()) {
                           return Either.<MixturePart.EffectEntry, MobEffectInstance>left(component);
                       }

                       // Balance the length by shortening it based on its molarity
                       var ratio = (component.getMolarity() * ((float) drunkAmount / DrinkingRecipe.configuredMaxAmount()));

                       // Splash potion averages the durations between sips. Before lingering potion's effects
                       float splashRatio;
                       float averageDuration;
                       if (numberOfEffects.value != 0) {
                           splashRatio = splash.value;
                           averageDuration = (float) totalDuration.value / numberOfEffects.value;
                       }
                       else {
                           splashRatio = 0;
                           averageDuration = 0;
                       }

                       // Lingering potion extends the duration of sips by a percentage
                       var lingeringRatio = lingering.value;

                       var oldDuration = effectInstance.getDuration();

                       var splashedDuration = (oldDuration * (1 - splashRatio)) + (averageDuration * splashRatio);

                       var lingeredDuration = splashedDuration * (1 + lingeringRatio);

                       var effect = copyEffectWithDuration(effectInstance, Math.round(ratio * lingeredDuration));
                       return Either.<MixturePart.EffectEntry, MobEffectInstance>right(effect);
                   })
                   .toList();
    }

    private void updateCategory(Category incomingCategory) {
        if (this.category == null) {
            this.category = incomingCategory;
        }
        else if (incomingCategory != this.category) {
            this.category = Category.NEUTRAL;
        }
    }

    public enum Category implements StringRepresentable {
        BENEFICIAL("beneficial"),
        NEUTRAL("neutral"),
        HARMFUL("harmful");
        private final String name;

        Category(String name) {
            this.name = name;
        }

        public static Category from(MobEffectCategory category) {
            switch (category) {
                case BENEFICIAL -> {
                    return BENEFICIAL;
                }
                case HARMFUL -> {
                    return HARMFUL;
                }
                default -> {
                    return NEUTRAL;
                }
            }
        }

        public static Category from(String name) {
            switch (name) {
                case "beneficial" -> {
                    return BENEFICIAL;
                }
                case "harmful" -> {
                    return HARMFUL;
                }
                default -> {
                    return NEUTRAL;
                }
            }
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    // Hardcoded for now
    public enum Modifier implements StringRepresentable {
        NORMAL("normal"),
        LINGERING("lingering"),
        SPLASH("splash");
        private final String name;

        Modifier(String name) {
            this.name = name;
        }

        public static Modifier from(ItemStack potionItem) {
            var item = potionItem.getItem();
            if (item == Items.LINGERING_POTION) return LINGERING;
            if (item == Items.SPLASH_POTION) return SPLASH;
            return NORMAL;
        }

        public static Modifier from(String name) {
            if (name.equals("lingering")) return LINGERING;
            if (name.equals("splash")) return SPLASH;
            return NORMAL;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
