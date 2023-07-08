package com.mystdev.recicropal.content.drinking.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DrinkHandler implements IDrinkHandler, ICapabilityProvider {
    private final LazyOptional<IDrinkHandler> lazyBackend = LazyOptional.of(() -> this);

    private DrinkContext ctx;

    public static void attachPlayerCaps(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player)) return;
        event.addCapability(IDrinkHandler.ID, new DrinkHandler());
    }

    @Override
    public DrinkContext getContext() {
        return ctx;
    }

    @Override
    public void setContext(DrinkContext ctx) {
        this.ctx = ctx;
    }

    private final EffectWaiter effectWaiter = new EffectWaiter();
    @Override
    public EffectWaiter getEffectWaiter() {
        return effectWaiter;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == IDrinkHandler.CAPABILITY) {
            return this.lazyBackend.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put("Effects", this.effectWaiter.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.effectWaiter.deserializeNBT(nbt.getCompound("Effects"));
    }
}
