package com.mystdev.recicropal.content.mixing;

import com.mystdev.recicropal.ModFluids;
import com.mystdev.recicropal.common.fluid.ModFluidUtils;
import com.mystdev.recicropal.content.drinking.DrinkingRecipe;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;

public class Mixture implements INBTSerializable<CompoundTag> {
    public static final String TAG_POTIONS = "Potions";
    public static final String TAG_MODIFIERS = "Modifiers";
    public static final String TAG_COLOR = "CustomColor";
    public static final String TAG_CATEGORY = "Category";
    private Category category;
    private final Map<String, MixtureComponent> components = new Object2ObjectOpenHashMap<>();

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
            mixture.addMixtureComponent(new MixtureComponent(tag.getAsString(), 1), amount, 0);
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

    public static FluidStack mix(Mixture mixture1, int mixture1Amount, Mixture mixture2, int mixture2Amount) {
        var newMixture = new Mixture();
        var resultingVolume = mixture1Amount + mixture2Amount;
        mixture1.components
                .values()
                .forEach(component -> {
                    var newComponent = new MixtureComponent(component.getId());
                    var moles = component.getMolarity() * mixture1Amount;
                    newComponent.setMolarity(moles / resultingVolume);
                    newMixture.components.put(component.getId(), newComponent);
                });
        newMixture.updateCategory(mixture1.category);
        mixture2.components
                .values()
                .forEach(component -> {
                    var newComponent = new MixtureComponent(component.getId());
                    var m1 = component.getMolarity() * mixture2Amount;
                    newComponent.setMolarity(m1 / resultingVolume);

                    var oldEntry = newMixture.components.get(component.getId());
                    if (oldEntry != null) {
                        var m2 = oldEntry.getMolarity() * resultingVolume;
                        newComponent.setMolarity((m1 + m2) / resultingVolume);
                    }
                    newMixture.components.put(component.getId(), newComponent);
                });
        newMixture.updateCategory(mixture2.category);
        return saveMixtureData(new FluidStack(getMixtureFluid(), mixture1Amount + mixture2Amount), newMixture);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        var potionsTag = new CompoundTag();
        this.components.forEach((key, value) -> potionsTag.put(key, value.serializeNBT()));
        tag.put(TAG_POTIONS, potionsTag);
        if (this.category != null) tag.putString(TAG_CATEGORY, this.category.name);
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
        }
    }

    public List<MobEffectInstance> getRationedEffects(int drunkAmount) {
        // This should not happen unless the mixture is empty
        if (components.size() == 0) {
            return List.of();
        }
        return components
                .entrySet()
                .stream()
                .map(entry -> {
                    var list = extractEffectsFromPotionName(entry.getKey());
                    var ratio = (entry
                            .getValue()
                            .getMolarity() * (drunkAmount / DrinkingRecipe.DEFAULT_AMOUNT));
                    return list.stream().map(effectInstance -> {
                        var oldDuration = effectInstance.getDuration();
                        return copyEffectWithDuration(effectInstance, Math.round(ratio * oldDuration));
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

    private static List<MobEffectInstance> extractEffectsFromPotionName(String name) {
        var potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(name));
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
}
