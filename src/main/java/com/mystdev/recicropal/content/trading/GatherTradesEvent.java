package com.mystdev.recicropal.content.trading;

import com.mystdev.recicropal.content.mixin.GatherTradesHook;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraftforge.eventbus.api.Event;

/**
 * An event I made to intercept the listing list gathered by villagers when they update them.
 * By default, this should return the regular listings based on the villager's profession.
 * This event adds the functionality to further modify the trade pool a villager could choose from.
 * <p>
 * Borrowing Forge's bus to post this.
 * {@link GatherTradesHook}
 * for hook implementation
 * <p>
 * In the meantime, I don't know what to implement if this event is cancelled so gonna set this as non-cancellable.
 */
public class GatherTradesEvent extends Event {

    private Int2ObjectMap<VillagerTrades.ItemListing[]> trades;
    private final Villager villager;

    @Override
    public boolean isCancelable() {
        return false;
    }

    public GatherTradesEvent(Villager villager, Int2ObjectMap<VillagerTrades.ItemListing[]> trades) {
        this.trades = trades;
        this.villager = villager;
    }

    public Villager getVillager() {
        return villager;
    }

    public Int2ObjectMap<VillagerTrades.ItemListing[]> getTrades() {
        return trades;
    }

    public void setTrades(Int2ObjectMap<VillagerTrades.ItemListing[]> trades) {
        this.trades = trades;
    }
}
