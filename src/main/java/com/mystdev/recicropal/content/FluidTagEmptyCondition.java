package com.mystdev.recicropal.content;

import com.google.gson.JsonObject;
import com.mystdev.recicropal.Recicropal;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class FluidTagEmptyCondition implements ICondition {
    public static final ResourceLocation FLUID_TAG_EMPTY_RL = new ResourceLocation(Recicropal.MOD_ID, "fluid_tag_empty");
    ResourceLocation fluidTag;

    @Override
    public ResourceLocation getID() {
        return FLUID_TAG_EMPTY_RL;
    }

    @Override
    public boolean test(IContext context) {
        return context.getTag(FluidTags.create(fluidTag)).isEmpty();
    }

    public static final IConditionSerializer<FluidTagEmptyCondition> SERIALIZER = new IConditionSerializer<>() {
        @Override
        public void write(JsonObject json, FluidTagEmptyCondition value) {
            json.addProperty("tag", value.fluidTag.toString());
        }

        @Override
        public FluidTagEmptyCondition read(JsonObject json) {
            var ret = new FluidTagEmptyCondition();
            ret.fluidTag = new ResourceLocation(GsonHelper.getAsString(json, "tag"));
            return ret;
        }

        @Override
        public ResourceLocation getID() {
            return FLUID_TAG_EMPTY_RL;
        }
    };
};