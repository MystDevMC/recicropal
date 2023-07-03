package com.mystdev.recicropal.common.condition;

import com.google.gson.JsonObject;
import com.mystdev.recicropal.Recicropal;
import com.mystdev.recicropal.common.fluid.ModFluidUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class FluidTagEmptyCondition implements ICondition {
    public static final ResourceLocation FLUID_TAG_EMPTY_RL = new ResourceLocation(Recicropal.MOD_ID,
                                                                                   "fluid_tag_empty");
    TagKey<Fluid> fluidTag;

    @Override
    public ResourceLocation getID() {
        return FLUID_TAG_EMPTY_RL;
    }

    @Override
    public boolean test(IContext context) {
        return context.getTag(fluidTag).isEmpty();
    }

    public static final IConditionSerializer<FluidTagEmptyCondition> SERIALIZER = new IConditionSerializer<>() {
        @Override
        public void write(JsonObject json, FluidTagEmptyCondition condition) {
            json.addProperty("tag", condition.fluidTag.location().toString());
        }

        @Override
        public FluidTagEmptyCondition read(JsonObject json) {
            var ret = new FluidTagEmptyCondition();
            ret.fluidTag = ModFluidUtils.tag(GsonHelper.getAsString(json, "tag"));
            return ret;
        }

        @Override
        public ResourceLocation getID() {
            return FLUID_TAG_EMPTY_RL;
        }
    };
};