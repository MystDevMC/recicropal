package com.mystdev.recicropal;

import com.mojang.logging.LogUtils;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Recicropal.MOD_ID)
public class Recicropal {

    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "recicropal";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
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

        var forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addGenericListener(ItemStack.class, ModItems::attachItemCaps);
        forgeBus.addListener(ModItems::registerTrades);

        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLootAPI.init(modBus);
        ModWorldGen.init(modBus);

        modBus.addListener(Recicropal::onCommonSetup);
    }



    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModPotions.addRecipes();
            ModItems.registerCompostables();
            ModWorldGen.register();
        });
    }

}
