package com.mystdev.recicropal.content.crop.bottle_gourd;

import com.mystdev.recicropal.ModBlocks;
import com.mystdev.recicropal.ModItems;
import com.mystdev.recicropal.content.trellis.TrellisCropBlock;
import net.minecraft.world.level.ItemLike;

public class BottleGourdCropBlock extends TrellisCropBlock {
    public BottleGourdCropBlock(Properties props) {
        super(props, ModBlocks.BOTTLE_GOURD_VINE.get());
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.BOTTLE_GOURD_SEEDS.get();
    }
}
