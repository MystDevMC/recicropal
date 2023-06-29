package com.mystdev.recicropal;

import com.mystdev.recicropal.crop.bottle_gourd.BottleGourdItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Rarity;

import static com.mystdev.recicropal.Recicropal.REGISTRATE;

public class ModItems {
    public static void init() {
    }

    public static final ItemEntry<ItemNameBlockItem> BOTTLE_GOURD_SEEDS =
            REGISTRATE.get()
                      .item("bottle_gourd_seeds", p -> new ItemNameBlockItem(ModBlocks.BOTTLE_GOURD_CROP.get(), p))
                      .lang("Bottle Gourd Seeds")
                      .tag(TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge:seeds/bottle_gourd")))
                      .defaultModel()
                      .register();

    public static final ItemEntry<ItemNameBlockItem> CLIMBING_MELON_SEEDS =
            REGISTRATE.get()
                      .item("climbing_melon_seeds", p -> new ItemNameBlockItem(ModBlocks.MELON_CROP.get(), p))
                      .tag(TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge:seeds/melon")))
                      .register();

    public static final ItemEntry<ItemNameBlockItem> CLIMBING_PUMPKIN_SEEDS =
            REGISTRATE.get()
                      .item("climbing_pumpkin_seeds", p -> new ItemNameBlockItem(ModBlocks.PUMPKIN_CROP.get(), p))
                      .tag(TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge:seeds/pumpkin")))
                      .register();

    public static final ItemEntry<BottleGourdItem> BOTTLE_GOURD =
            REGISTRATE.get()
                      .item("bottle_gourd", BottleGourdItem::new)
                      .properties((p) -> p.stacksTo(16))
                      .lang("Gourd Bottle")
                      .defaultModel()
                      .register();

    public static final ItemEntry<Item> GLAMOROUS_GOURD =
            REGISTRATE.get()
                      .item("glamorous_gourd", Item::new)
                      .properties((p) -> p.rarity(Rarity.RARE))
                      .register();


}
