package com.mystdev.recicropal.common.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class InjectTableModifier extends LootModifier {

    // Inspired by Farmer's Delight
    public static final Supplier<Codec<InjectTableModifier>> CODEC = Suppliers.memoize(
            () -> RecordCodecBuilder.create(
                    inst -> codecStart(
                            inst)
                            .and(ResourceLocation.CODEC
                                         .fieldOf("lootTable")
                                         .forGetter((m) -> m.lootTable))
                            .apply(inst, InjectTableModifier::new)));
    private final ResourceLocation lootTable;

    /**
     * Constructs a LootModifier.
     *
     * @param conditionsIn the ILootConditions that need to be matched before the loot is modified.
     */
    protected InjectTableModifier(LootItemCondition[] conditionsIn, ResourceLocation lootTable) {
        super(conditionsIn);
        this.lootTable = lootTable;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
                                                          LootContext context) {
        var additionalLoot = context.getLootTable(this.lootTable);
        additionalLoot.getRandomItems(context, generatedLoot::add);
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
