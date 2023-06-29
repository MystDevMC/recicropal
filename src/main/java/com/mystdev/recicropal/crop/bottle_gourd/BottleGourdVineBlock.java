package com.mystdev.recicropal.crop.bottle_gourd;

import com.mystdev.recicropal.ModBlocks;
import com.mystdev.recicropal.ModItems;
import com.mystdev.recicropal.content.trellis.TrellisVineBlock;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.util.NonNullLazy;

public class BottleGourdVineBlock extends TrellisVineBlock {
    public BottleGourdVineBlock(Properties props) {
        super(props, ModBlocks.BOTTLE_GOURD_FRUIT.get(), NonNullLazy.of(ModBlocks.BOTTLE_GOURD_CROP::get));
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.BOTTLE_GOURD_SEEDS.get();
    }
}
