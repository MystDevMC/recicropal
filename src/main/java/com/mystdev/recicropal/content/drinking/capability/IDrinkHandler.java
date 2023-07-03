package com.mystdev.recicropal.content.drinking.capability;

import com.mystdev.recicropal.Recicropal;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import javax.annotation.Nullable;

@AutoRegisterCapability
public interface IDrinkHandler {
    Capability<IDrinkHandler> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    ResourceLocation ID = new ResourceLocation(Recicropal.MOD_ID, "drink_handler");

    void setContext(@Nullable DrinkContext ctx);

    DrinkContext getContext();
}
