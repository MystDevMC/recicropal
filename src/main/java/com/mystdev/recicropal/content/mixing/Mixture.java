package com.mystdev.recicropal.content.mixing;

import com.mystdev.recicropal.ModFluids;
import com.mystdev.recicropal.common.fluid.ModFluidUtils;
import com.mystdev.recicropal.content.drinking.DrinkingRecipe;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Mixture implements INBTSerializable<CompoundTag> {
    public static final String TAG_POTIONS = "Potions";
    public static final String TAG_COLOR = "Color";
    public static final String TAG_CATEGORY = "Category";
    @Nullable
    private Category category;
    private final Map<String, MixtureComponent> components = new Object2ObjectOpenHashMap<>();
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
        return ModFluids.MIXTURE.get().getSource();
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
            if (potionTag.contains(MixtureComponent.TAG_MODIFIER)) {
                modifier = Modifier.from(potionTag.getString(MixtureComponent.TAG_MODIFIER));
            }

            var comp = new MixtureComponent(tag.getAsString(), 1, color, modifier);
            mixture.addMixtureComponent(comp, amount, 0);
        }
        return mixture;
    }

    public static Mixture getMixtureFromMixable(FluidStack mixable) {
        if (mixable.getFluid().is(ModFluidUtils.tag("forge:potion"))) {
            return Mixture.mixtureFromPotion(mixable);
        }
        else if (isMixture(mixable)) {
            return Mixture.fromFluid(mixable);
        }
        throw new IllegalArgumentException("Inserted fluid is not mixable!");
    }

    public static FluidStack saveMixtureData(FluidStack mixtureFluid, Mixture data) {
        mixtureFluid.setTag(data.serializeNBT());
        return mixtureFluid;
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
            for (MixtureComponent comp : this.components.values()) {
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

    public static FluidStack mix(Mixture mixture1, int mixture1Amount, Mixture mixture2, int mixture2Amount) {
        var newMixture = new Mixture();
        var resultingVolume = mixture1Amount + mixture2Amount;
        mixture1.components
                .values()
                .forEach(component -> {
                    var newComponent = new MixtureComponent(component);
                    var moles = component.getMolarity() * mixture1Amount;
                    newComponent.setMolarity(moles / resultingVolume);
                    newMixture.components.put(component.getId(), newComponent);
                });
        newMixture.updateCategory(mixture1.category);
        newMixture.updateColor(mixture1.getColor(), (float) mixture1Amount / resultingVolume);
        mixture2.components
                .values()
                .forEach(component -> {
                    var newComponent = new MixtureComponent(component);
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
        return saveMixtureData(new FluidStack(getMixtureFluid(), mixture1Amount + mixture2Amount), newMixture);
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
            var mixture = new MixtureComponent(key);
            mixture.deserializeNBT(potionsTag.getCompound(key));
            components.put(key, mixture);
        });
        if (nbt.contains(TAG_CATEGORY)) this.category = Category.from(nbt.getString(TAG_CATEGORY));
        if (nbt.contains(TAG_COLOR)) this.color = nbt.getInt(TAG_COLOR);
    }

    // Add singular mixture component
    public void addMixtureComponent(MixtureComponent component, int addedVolume, int previousVolume) {
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
            this.updateCategory(inferCategory(component.getPotion()));
            this.updateColor(component.getColor(), component.getMolarity());
        }
    }

    private Collection<MixtureComponent> collateSimilarEffects() {
        return components.values()
                         .stream()
                         .collect(Collectors.groupingBy(
                                 MixtureComponent::getPotion,
                                 Collectors.reducing(
                                         null,
                                         (e1, e2) -> {
                                             if (e1 == null) return e2;
                                             var mixtureComp = new MixtureComponent(e1);
                                             mixtureComp.setMolarity(mixtureComp.getMolarity() + e2.getMolarity());
                                             return mixtureComp;
                                         }
                                 )))
                         .values();
    }

    public List<MobEffectInstance> getRationedEffects(int drunkAmount) {
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
            var list = extractEffectsFromPotion(value.getPotion());
            for (var e : list) {
                totalDuration.value += e.getDuration();
                numberOfEffects.value++;
            }
            if (value.getModifier() == Modifier.SPLASH) splash.value += value.getMolarity();
            if (value.getModifier() == Modifier.LINGERING) lingering.value += value.getMolarity();
        });
        return this.collateSimilarEffects()
                   .stream()
                   .map(component -> {
//                       Recicropal.LOGGER.debug(component.getPotionName());
                       var list = extractEffectsFromPotion(component.getPotion());

                       // Balance the length by shortening it based on its molarity
                       var ratio = (component
                               .getMolarity() * (drunkAmount / DrinkingRecipe.DEFAULT_AMOUNT));

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

                       return list.stream().map(effectInstance -> {
                           var oldDuration = effectInstance.getDuration();

//                           Recicropal.LOGGER.debug("Splash ratio " + splashRatio);
//                           Recicropal.LOGGER.debug("Average duration " + averageDuration);

                           var splashedDuration = (oldDuration * (1 - splashRatio)) + (averageDuration * splashRatio);

//                           Recicropal.LOGGER.debug("Old duration " + oldDuration);
//                           Recicropal.LOGGER.debug("Splashed duration " + splashedDuration);

                           var lingeredDuration = splashedDuration * (1 + lingeringRatio);

//                           Recicropal.LOGGER.debug("Lingering ratio " + lingeringRatio);
//                           Recicropal.LOGGER.debug("Lingered duration " + lingeredDuration);

                           return copyEffectWithDuration(effectInstance, Math.round(ratio * lingeredDuration));
                       }).toList();
                   }).flatMap(List::stream)
                   .toList();
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

    private void updateCategory(Category incomingCategory) {
        if (this.category == null) {
            this.category = incomingCategory;
        }
        else if (incomingCategory != this.category) {
            this.category = Category.NEUTRAL;
        }
    }

    private static Category inferCategory(Potion potion) {
        Category start = null;
        for (var effectInstance : potion.getEffects()) {
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

    private static List<MobEffectInstance> extractEffectsFromPotion(Potion potion) {
        var list = new ObjectArrayList<MobEffectInstance>();
        if (potion != null) {
            list.addAll(potion.getEffects());
        }
        return list;
    }

    public enum Category implements StringRepresentable {
        BENEFICIAL("beneficial"),
        NEUTRAL("neutral"),
        HARMFUL("harmful");
        private final String name;

        Category(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
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

        @Override
        public String getSerializedName() {
            return name;
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
    }
}
