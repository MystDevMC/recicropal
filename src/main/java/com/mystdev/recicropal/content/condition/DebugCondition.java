package com.mystdev.recicropal.content.condition;

import com.google.gson.JsonObject;
import com.mystdev.recicropal.Recicropal;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class DebugCondition implements ICondition {
    public static final ResourceLocation DEBUG_RL = new ResourceLocation(Recicropal.MOD_ID, "debug");
    @Override
    public ResourceLocation getID() {
        return DEBUG_RL;
    }

    @Override
    public boolean test(IContext context) {
        return Recicropal.debug;
    }

    public static final IConditionSerializer<DebugCondition> SERIALIZER = new IConditionSerializer<>() {
        @Override
        public void write(JsonObject json, DebugCondition value){}

        @Override
        public DebugCondition read(JsonObject json) {
            return new DebugCondition();
        }

        @Override
        public ResourceLocation getID() {
            return DEBUG_RL;
        }
    };
}
