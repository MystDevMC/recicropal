package com.mystdev.recicropal.common.fluid.provider;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Predicate;

public abstract class FluidStackProvider {
    int amount;
    CompoundTag nbt;

    public static FluidStackProvider fromJson(JsonObject obj) {
        var prov = createFromKey(obj::has);
        prov.readJson(obj);
        parseAmountAndTag(prov, obj);
        return prov;
    }

    private static void parseAmountAndTag(FluidStackProvider provider, JsonObject object) {
        provider.amount = GsonHelper.getAsInt(object, "amount");
        if (object.has("nbt")) {
            provider.nbt = CompoundTag.CODEC.decode(JsonOps.INSTANCE, object.get("nbt")).get().orThrow().getFirst();
        }
    }

    private static FluidStackProvider createFromKey(Predicate<String> predicate) {
        if (predicate.test(TagFluidStackProvider.KEY)) return new TagFluidStackProvider();
        else if (predicate.test(IdFluidStackProvider.KEY)) return new IdFluidStackProvider();
        else throw new IllegalArgumentException("Failed to parse FluidStackProvider");
    }

    protected abstract void readJson(JsonObject obj);

    public static FluidStackProvider fromNetwork(FriendlyByteBuf buf) {
        var key = buf.readUtf();
        var prov = createFromKey(key::equals);
        prov.read(buf);
        return prov;
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(this.getKey());
        this.write(buf);
    }

    protected abstract String getKey();

    protected abstract void read(FriendlyByteBuf buf);

    protected abstract void write(FriendlyByteBuf buf);

    public abstract FluidStack get();
}
