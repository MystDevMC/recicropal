package com.mystdev.recicropal;

import com.mystdev.recicropal.content.crop.bottle_gourd.BottleGourdBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public class ModBlockEntities {

    public static final BlockEntityEntry<BottleGourdBlockEntity> BOTTLE_GOURD = Recicropal.REGISTRATE
            .get()
            .blockEntity("bottle_gourd", BottleGourdBlockEntity::new)
            .validBlock(ModBlocks.BOTTLE_GOURD)
            .register();

    public static void init() {
    }
}
