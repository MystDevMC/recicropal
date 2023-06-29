package com.mystdev.recicropal;

import com.mojang.logging.LogUtils;
import com.mystdev.recicropal.crop.bottle_gourd.BottleGourdBlockEntity;
import com.mystdev.recicropal.crop.bottle_gourd.BottleGourdItem;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.common.Mod;
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

        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, Recicropal::attachItemCaps);
    }

    @SubscribeEvent
    public static void attachItemCaps(AttachCapabilitiesEvent<ItemStack> event) {
        if (!(event.getObject().getItem() instanceof BottleGourdItem)) return;
        event.addCapability(new ResourceLocation(Recicropal.MOD_ID, "bottle_gourd"),
                            new FluidHandlerItemStack(event.getObject(), BottleGourdBlockEntity.MAX_CAPACITY));
    }

}
