package com.mystdev.recicropal.common.fluid.provider;

import com.google.gson.JsonObject;
import com.mystdev.recicropal.common.fluid.ModFluidUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

class TagFluidStackProvider extends FluidStackProvider {
    public static final String KEY = "tag";
    private TagKey<Fluid> tag;

    @Override
    protected void readJson(JsonObject obj) {
        this.tag = ModFluidUtils.tag(GsonHelper.getAsString(obj, KEY));
    }

    @Override
    protected String getKey() {
        return KEY;
    }

    @Override
    protected void read(FriendlyByteBuf buf) {
        this.tag = ModFluidUtils.tag(buf.readUtf());
    }

    @Override
    protected void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.tag.location().toString());
    }

    @Override
    public FluidStack get() {
        var fluid = ModFluidUtils.forcedMember(this.tag);
        return new FluidStack(fluid, this.getAmount(), this.nbt);
    }
}
