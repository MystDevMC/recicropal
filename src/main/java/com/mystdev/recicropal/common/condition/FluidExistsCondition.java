package com.mystdev.recicropal.common.condition;

import com.google.gson.JsonObject;
import com.mystdev.recicropal.Recicropal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidExistsCondition implements ICondition {
    public static final ResourceLocation FLUID_EXISTS_RL = new ResourceLocation(Recicropal.MOD_ID, "fluid_exists");
    public static final IConditionSerializer<FluidExistsCondition> SERIALIZER = new IConditionSerializer<>() {
        @Override
        public void write(JsonObject json, FluidExistsCondition condition) {
            json.addProperty("fluid", condition.fluid.toString());
        }

        @Override
        public FluidExistsCondition read(JsonObject json) {
            var ret = new FluidExistsCondition();
            ret.fluid = new ResourceLocation(GsonHelper.getAsString(json, "fluid"));
            return ret;
        }

        @Override
        public ResourceLocation getID() {
            return FLUID_EXISTS_RL;
        }
    };
    ResourceLocation fluid;

    @Override
    public String toString() {
        return "fluid_exists(\"" + fluid + "\")";
    }

    @Override
    public ResourceLocation getID() {
        return FLUID_EXISTS_RL;
    }

    @Override
    public boolean test(IContext context) {
        return ForgeRegistries.FLUIDS.containsKey(fluid);
    }
}