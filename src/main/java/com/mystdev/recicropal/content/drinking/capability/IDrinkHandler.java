package com.mystdev.recicropal.content.drinking.capability;

import com.mystdev.recicropal.Recicropal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

@AutoRegisterCapability
public interface IDrinkHandler extends INBTSerializable<CompoundTag> {
    Capability<IDrinkHandler> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    ResourceLocation ID = new ResourceLocation(Recicropal.MOD_ID, "drink_handler");

    DrinkContext getContext();

    void setContext(@Nullable DrinkContext ctx);

    EffectWaiter getEffectWaiter();

}
