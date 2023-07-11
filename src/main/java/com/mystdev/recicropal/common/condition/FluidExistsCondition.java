package com.mystdev.recicropal.common.condition;

import com.google.gson.JsonObject;
import com.mystdev.recicropal.Recicropal;
import com.mystdev.recicropal.common.fluid.ModFluidUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.common.crafting.conditions.ItemExistsCondition;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

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