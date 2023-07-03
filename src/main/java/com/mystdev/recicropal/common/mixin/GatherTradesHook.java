package com.mystdev.recicropal.common.mixin;

import com.mystdev.recicropal.content.trading.GatherTradesEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Villager.class)
public abstract class GatherTradesHook extends Entity {

    private final Villager recicropal_theVillager = (Villager) (Object) this;

    public GatherTradesHook(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @ModifyVariable(
            at = @At(
                    value = "LOAD",
                    target = "Lnet/minecraft/world/entity/npc/Villager;getVillagerData()Lnet/minecraft/world/entity/npc/VillagerData;"
            ),
            method = "updateTrades"
    )
    public Int2ObjectMap<VillagerTrades.ItemListing[]> recicropal_updateTrades(Int2ObjectMap<VillagerTrades.ItemListing[]> oldValue) {
        var event = new GatherTradesEvent(recicropal_theVillager, oldValue);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getTrades();
    }
}
