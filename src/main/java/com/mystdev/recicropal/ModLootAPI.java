package com.mystdev.recicropal;

import com.mojang.serialization.Codec;
import com.mystdev.recicropal.content.loot.InjectTableModifier;
import com.mystdev.recicropal.content.loot.SetFluidFunction;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootAPI {
    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTIONS = DeferredRegister
            .create(Registry.LOOT_FUNCTION_TYPE.key(),
                    Recicropal.MOD_ID);

    public static final RegistryObject<LootItemFunctionType> SET_FLUID =
            LOOT_FUNCTIONS.register("set_fluid", () -> SetFluidFunction.TYPE);

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS = DeferredRegister.create(
            ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
            Recicropal.MOD_ID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> INJECT_TABLE = LOOT_MODIFIER_SERIALIZERS.register(
            "inject_table",
            InjectTableModifier.CODEC);

    public static void init(IEventBus modBus) {
        LOOT_FUNCTIONS.register(modBus);
        LOOT_MODIFIER_SERIALIZERS.register(modBus);
    }
}