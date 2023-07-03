package com.mystdev.recicropal.content.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mystdev.recicropal.content.crop.bottle_gourd.BottleGourdBlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class EmptyTankCondition implements LootItemCondition {
    public static final LootItemConditionType TYPE = new LootItemConditionType(new ConditionSerializer());

    @Override
    public LootItemConditionType getType() {
        return TYPE;
    }

    // This only works on my Gourd Bottle for now
    @Override
    public boolean test(LootContext lootContext) {
        var be = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (!(be instanceof BottleGourdBlockEntity bottle)) return true;
        var tank = bottle.tank;
        for (int i = 0; i < tank.getTanks(); i++) {
            if (!tank.getFluidInTank(i).isEmpty()) return false;
        }
        return true;
    }

    public static class ConditionSerializer implements Serializer<EmptyTankCondition> {

        @Override
        public void serialize(JsonObject jsonObject,
                              EmptyTankCondition condition,
                              JsonSerializationContext serializationContext) {

        }

        @Override
        public EmptyTankCondition deserialize(JsonObject object, JsonDeserializationContext deserializationContext) {
            return new EmptyTankCondition();
        }
    }
}
