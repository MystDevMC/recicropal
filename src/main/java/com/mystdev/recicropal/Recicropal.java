package com.mystdev.recicropal;

import com.mojang.logging.LogUtils;
import com.mystdev.recicropal.content.condition.DebugCondition;
import com.mystdev.recicropal.content.condition.FluidTagEmptyCondition;
import com.mystdev.recicropal.content.drinking.DrinkHandler;
import com.mystdev.recicropal.content.drinking.result.DrinkResults;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Recicropal.MOD_ID)
public class Recicropal {

    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "recicropal";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static boolean debug = true;
    public static final NonNullSupplier<Registrate> REGISTRATE = NonNullSupplier.lazy(() -> Registrate
            .create(MOD_ID)
            .creativeModeTab(() -> new CreativeModeTab("recicropal") {
                @Override
                public ItemStack makeIcon() {
                    return ModItems.BOTTLE_GOURD.asStack();
                }
            }));

    public Recicropal() {
        ModBlocks.init();
        ModItems.init();
        ModBlockEntities.init();
        ModPotions.init();
        ModFluids.init();

        var forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addGenericListener(Entity.class, DrinkHandler::attachPlayerCaps);
        forgeBus.addListener(ModItems::registerTrades);
        forgeBus.addListener(ModWorldGen::addVillageBuildings);

        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLootAPI.init(modBus);
        ModWorldGen.init(modBus);
        ModRecipes.init(modBus);

        modBus.addListener(Recicropal::registerConditions);

        DrinkResults.DRINK_RESULTS.makeRegistry(RegistryBuilder::new);
        DrinkResults.init(modBus);

        modBus.addListener(Recicropal::onCommonSetup);
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModPotions.addRecipes();
            ModItems.registerCompostables();
            ModWorldGen.register();
        });
    }

    public static void registerConditions(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.RECIPE_SERIALIZERS,
                       helper -> {
                           CraftingHelper.register(FluidTagEmptyCondition.SERIALIZER);
                           CraftingHelper.register(DebugCondition.SERIALIZER);
                       }
        );
    }
}
