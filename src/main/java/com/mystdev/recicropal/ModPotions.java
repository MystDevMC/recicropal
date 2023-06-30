package com.mystdev.recicropal;

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.registries.ForgeRegistries;

import static com.mystdev.recicropal.Recicropal.REGISTRATE;

public class ModPotions {

    public static void init() {
    }

    public static final RegistryEntry<Potion> REVITALIZATION = REGISTRATE.get().simple(
            "revitalization",
            ForgeRegistries.POTIONS.getRegistryKey(), () -> new Potion(
                    new MobEffectInstance(MobEffects.REGENERATION, 3600),
                    new MobEffectInstance(MobEffects.HEALTH_BOOST, 3600),
                    new MobEffectInstance(MobEffects.HEAL)
            ));

    public static final RegistryEntry<Potion> LONG_REVITALIZATION = REGISTRATE.get().simple(
            "long_revitalization",
            ForgeRegistries.POTIONS.getRegistryKey(), () -> new Potion(
                    "revitalization",
                    new MobEffectInstance(MobEffects.REGENERATION, 7200),
                    new MobEffectInstance(MobEffects.HEALTH_BOOST, 7200),
                    new MobEffectInstance(MobEffects.HEAL)
            ));

    public static final RegistryEntry<Potion> STRONG_REVITALIZATION = REGISTRATE.get().simple(
            "strong_revitalization",
            ForgeRegistries.POTIONS.getRegistryKey(), () -> new Potion(
                    "revitalization",
                    new MobEffectInstance(MobEffects.REGENERATION, 1800, 1),
                    new MobEffectInstance(MobEffects.HEALTH_BOOST, 1800, 2),
                    new MobEffectInstance(MobEffects.HEAL, 1)
            ));


    public static void addRecipes() {
        BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD)),
                Ingredient.of(ModItems.GLAMOROUS_GOURD.get()),
                PotionUtils.setPotion(new ItemStack(Items.POTION), REVITALIZATION.get())
        );
        BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), REVITALIZATION.get())),
                Ingredient.of(ItemTags.create(new ResourceLocation("forge:dusts/redstone"))),
                PotionUtils.setPotion(new ItemStack(Items.POTION), LONG_REVITALIZATION.get())
        );
        BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), REVITALIZATION.get())),
                Ingredient.of(ItemTags.create(new ResourceLocation("forge:dusts/glowstone"))),
                PotionUtils.setPotion(new ItemStack(Items.POTION), STRONG_REVITALIZATION.get())
        );
    }

}
