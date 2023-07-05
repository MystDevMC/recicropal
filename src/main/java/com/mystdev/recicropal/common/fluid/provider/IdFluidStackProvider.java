package com.mystdev.recicropal.common.fluid.provider;

import com.google.gson.JsonObject;
import com.mystdev.recicropal.common.fluid.ModFluidUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

class IdFluidStackProvider extends FluidStackProvider {
    public static final String KEY = "fluid";
    private Fluid fluid;

    @Override
    protected void readJson(JsonObject obj) {
        this.fluid = ModFluidUtils.fluidOrAir(GsonHelper.getAsString(obj, KEY));
    }

    @Override
    protected String getKey() {
        return KEY;
    }

    @Override
    protected void read(FriendlyByteBuf buf) {
        this.fluid = ModFluidUtils.fluid(buf.readUtf());
    }

    @Override
    protected void write(FriendlyByteBuf buf) {
        buf.writeUtf(ModFluidUtils.key(this.fluid));
    }

    @Override
    public FluidStack get() {
        return new FluidStack(this.fluid, this.getAmount(), this.nbt);
    }
}
