package com.mystdev.recicropal;

import com.mystdev.recicropal.content.drinking.DrinkingRecipe;
import com.mystdev.recicropal.content.mixing.FillingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {

    public static final DeferredRegister<RecipeType<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Recicropal.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Recicropal.MOD_ID);

    public static final RegistryObject<RecipeType<DrinkingRecipe>> DRINKING_RECIPE =
            RECIPES.register("drinking", () -> new RecipeType<>(){});

    public static final RegistryObject<RecipeSerializer<DrinkingRecipe>> DRINKING_SERIALIZER =
            SERIALIZERS.register("drinking", () -> DrinkingRecipe.SERIALIZER);

    public static final RegistryObject<RecipeType<FillingRecipe>> FILLING_RECIPE =
            RECIPES.register("filling", () -> new RecipeType<>(){});

    public static final RegistryObject<RecipeSerializer<FillingRecipe>> FILLING_SERIALIZER =
            SERIALIZERS.register("filling", () -> FillingRecipe.SERIALIZER);

    public static void init(IEventBus modBus) {
        RECIPES.register(modBus);
        SERIALIZERS.register(modBus);
    }
}
