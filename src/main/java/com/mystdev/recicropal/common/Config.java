package com.mystdev.recicropal.common;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Config {
    public static final ForgeConfigSpec COMMON;
    public static final String CATEGORY_MISC = "miscellaneous";
    public static final ForgeConfigSpec.BooleanValue SPAWN_VILLAGER_STRUCTURES;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DEBUG;
    public static final String CATEGORY_BOTTLE_GOURD = "bottle_gourd";
    public static final ForgeConfigSpec.BooleanValue ADD_TRADES;
    public static final ForgeConfigSpec.IntValue BOTTLE_CAPACITY;
    public static final ForgeConfigSpec.IntValue BOTTLE_TRANSFER_AMOUNT;
    public static final ForgeConfigSpec.BooleanValue BOTTLE_GOURDS_ROT;
    public static final ForgeConfigSpec.BooleanValue ENABLE_POTION_MIXING;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DRINKING;
    public static final ForgeConfigSpec.IntValue DRINKING_MAX_AMOUNT;
    public static final ForgeConfigSpec CLIENT;
    public static final String CATEGORY_CLIENT = "client";
    public static final ForgeConfigSpec.BooleanValue RENDER_BOTTLE_TOOLTIP;

    static {
        var COMMON_BUILDER = new ForgeConfigSpec.Builder();

        COMMON_BUILDER.comment("Miscellaneous").push(CATEGORY_MISC);
        SPAWN_VILLAGER_STRUCTURES =
                COMMON_BUILDER.comment("Enable Recicropal's structures to spawn in villages?")
                              .define("spawnVillagerStructures", true);
        ENABLE_DEBUG =
                COMMON_BUILDER.comment("Enable debug mode?")
                              .define("debug", false);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Bottle Gourds and Related Contents").push(CATEGORY_BOTTLE_GOURD);
        ADD_TRADES =
                COMMON_BUILDER.comment("Add bottle gourds as trading item for Savanna and Desert villagers?")
                              .define("addTrades", true);
        BOTTLE_CAPACITY =
                COMMON_BUILDER.comment("Set gourd bottles' capacity as...")
                              .defineInRange("bottleCapacity", 2000, 0, Integer.MAX_VALUE);
        BOTTLE_TRANSFER_AMOUNT =
                COMMON_BUILDER.comment("Set gourd bottle blocks' transfer amount as...")
                              .defineInRange("bottleTransferAmount", 1000, 0, Integer.MAX_VALUE);
        BOTTLE_GOURDS_ROT =
                COMMON_BUILDER.comment("Should bottle gourd fruits rot after a random amount of time?")
                              .define("bottleGourdsRot", true);
        ENABLE_POTION_MIXING =
                COMMON_BUILDER.comment("Allow potion to be mixed in gourd bottles?")
                              .define("potionMixing", true);
        ENABLE_DRINKING =
                COMMON_BUILDER.comment("Allow drinking from gourd bottles?")
                              .define("drinking", true);
        DRINKING_MAX_AMOUNT =
                COMMON_BUILDER.comment("Set maximum amount of fluid drunk in one drink as...")
                              .defineInRange("drinkingMaxAmount", 250, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON = COMMON_BUILDER.build();

        var CLIENT_BUILDER = new ForgeConfigSpec.Builder();

        CLIENT_BUILDER.comment("Client").push(CATEGORY_CLIENT);
        RENDER_BOTTLE_TOOLTIP =
                CLIENT_BUILDER.comment("Render special tooltip when sneaking and selecting gourd bottles?")
                              .define("renderBottleTooltip", true);
        CLIENT_BUILDER.pop();

        CLIENT = CLIENT_BUILDER.build();
    }
}
