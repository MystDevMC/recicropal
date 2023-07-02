package com.mystdev.recicropal.content.trading;

import com.mystdev.recicropal.Recicropal;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = Recicropal.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public abstract class SensitiveItemListing implements VillagerTrades.ItemListing {

    protected final Predicate<VillagerData> onlyAddIf;

    public SensitiveItemListing(Predicate<VillagerData> onlyAddIf) {
        this.onlyAddIf = onlyAddIf;
    }

    @SubscribeEvent
    public static void filterTrades(GatherTradesEvent event) {
        var trades = new HashMap<Integer, VillagerTrades.ItemListing[]>();
        event.getTrades()
             .int2ObjectEntrySet()
             .forEach(entry -> {
                          var value = Arrays.stream(entry.getValue()).filter(itemListing -> {
                              if (itemListing instanceof SensitiveItemListing sensitiveItemListing) {
                                  return sensitiveItemListing.onlyAddIf.test(event.getVillager().getVillagerData());
                              }
                              return true;
                          }).toArray(VillagerTrades.ItemListing[]::new);
                          trades.put(entry.getIntKey(), value);
                      }
             );
        event.setTrades(new Int2ObjectOpenHashMap<>(trades));
    }
}
