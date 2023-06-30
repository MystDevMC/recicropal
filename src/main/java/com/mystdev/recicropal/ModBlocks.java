package com.mystdev.recicropal;

import com.mystdev.recicropal.content.trellis.TrellisBlock;
import com.mystdev.recicropal.content.trellis.TrellisCropBlock;
import com.mystdev.recicropal.content.trellis.TrellisVineBlock;
import com.mystdev.recicropal.crop.bottle_gourd.BottleGourdBlock;
import com.mystdev.recicropal.crop.bottle_gourd.BottleGourdCropBlock;
import com.mystdev.recicropal.crop.bottle_gourd.BottleGourdFruitBlock;
import com.mystdev.recicropal.crop.bottle_gourd.BottleGourdVineBlock;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

import static com.mystdev.recicropal.Recicropal.MOD_ID;
import static com.mystdev.recicropal.Recicropal.REGISTRATE;

public class ModBlocks {

    public static void init() {
    }

    public static NonNullLazy<? extends Block> lazyBlock(String resName) {
        return NonNullLazy.of(() -> Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(resName))));
    }

    public static final BlockEntry<TrellisBlock> TRELLIS =
            REGISTRATE.get()
                      .block("trellis", TrellisBlock::new)
                      .properties((p) -> BlockBehaviour.Properties.copy(Blocks.CHAIN))
                      .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                      .lang("Trellis")
                      .item()
                      .model((ctx, provider) -> provider.generated(ctx,
                                                                   new ResourceLocation(MOD_ID + ":block/trellis")))
                      .build()
                      .register();

    public static final BlockEntry<BottleGourdBlock> BOTTLE_GOURD =
            REGISTRATE.get().block("bottle_gourd", BottleGourdBlock::new)
                      .properties((p) -> BlockBehaviour.Properties.copy(Blocks.MELON).instabreak())
                      .lang("Gourd Bottle")
                      .register();

    public static final BlockEntry<BottleGourdFruitBlock> BOTTLE_GOURD_FRUIT =
            REGISTRATE.get().block("bottle_gourd_fruit", BottleGourdFruitBlock::new)
                      .properties((p) -> BlockBehaviour.Properties.copy(Blocks.MELON))
                      .item((block, properties) -> {
                          properties.food(new FoodProperties.Builder().nutrition(5).saturationMod(0.1F).build());
                          return new BlockItem(block, properties);
                      })
                      .build()
                      .register();

    public static final BlockEntry<BottleGourdVineBlock> BOTTLE_GOURD_VINE =
            REGISTRATE.get()
                      .block("bottle_gourd_vine", BottleGourdVineBlock::new)
                      .properties((p) -> BlockBehaviour.Properties.copy(Blocks.VINE))
                      .register();

    public static final BlockEntry<BottleGourdCropBlock> BOTTLE_GOURD_CROP =
            REGISTRATE.get()
                      .block("bottle_gourd_crop", BottleGourdCropBlock::new)
                      .properties((p) -> BlockBehaviour.Properties.copy(Blocks.VINE))
                      .lang("Bottle Gourd Crop")
                      .register();

    public static final BlockEntry<? extends TrellisVineBlock> MELON_VINE =
            REGISTRATE.get()
                      .block("melon_vine",
                             (p) -> new TrellisVineBlock(p, Blocks.MELON,
                                                         (NonNullLazy<TrellisCropBlock>) lazyBlock(MOD_ID + ":melon_crop")) {
                                 @Override
                                 protected ItemLike getBaseSeedId() {
                                     return ModItems.CLIMBING_MELON_SEEDS.get();
                                 }
                             })
                      .properties((p) -> BlockBehaviour.Properties.copy(Blocks.VINE))
                      .register();

    public static final BlockEntry<? extends TrellisCropBlock> MELON_CROP =
            REGISTRATE.get()
                      .block("melon_crop", (p) -> new TrellisCropBlock(p, MELON_VINE.get()) {
                          @Override
                          protected ItemLike getBaseSeedId() {
                              return ModItems.CLIMBING_MELON_SEEDS.get();
                          }
                      })
                      .properties((p) -> BlockBehaviour.Properties.copy(Blocks.VINE))
                      .register();

    public static final BlockEntry<? extends TrellisVineBlock> PUMPKIN_VINE =
            REGISTRATE.get()
                      .block("pumpkin_vine",
                             (p) -> new TrellisVineBlock(p,
                                                         Blocks.PUMPKIN,
                                                         (NonNullLazy<TrellisCropBlock>) lazyBlock(MOD_ID + ":pumpkin_crop")) {
                                 @Override
                                 protected ItemLike getBaseSeedId() {
                                     return ModItems.CLIMBING_PUMPKIN_SEEDS.get();
                                 }
                             })
                      .properties((p) -> BlockBehaviour.Properties.copy(Blocks.VINE))
                      .register();

    public static final BlockEntry<? extends TrellisCropBlock> PUMPKIN_CROP =
            REGISTRATE.get()
                      .block("pumpkin_crop", (p) -> new TrellisCropBlock(p, PUMPKIN_VINE.get()) {
                          @Override
                          protected ItemLike getBaseSeedId() {
                              return ModItems.CLIMBING_PUMPKIN_SEEDS.get();
                          }
                      })
                      .properties((p) -> BlockBehaviour.Properties.copy(Blocks.VINE))
                      .register();

}
