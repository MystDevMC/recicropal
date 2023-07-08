package com.mystdev.recicropal.common.fluid.provider;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.IntSupplier;
import java.util.function.Predicate;

public abstract class FluidStackProvider {
    IntSupplier amountIfNotSpecified = () -> 0;
    CompoundTag nbt;
    private Integer amount;

    public static FluidStackProvider fromJson(JsonObject obj) {
        var prov = createFromKey(obj::has);
        prov.readJson(obj);
        parseAmountAndTag(prov, obj);
        return prov;
    }

    private static void parseAmountAndTag(FluidStackProvider provider, JsonObject object) {
        if (object.has("amount")) {
            provider.amount = GsonHelper.getAsInt(object, "amount");
        }
        if (object.has("nbt")) {
            provider.nbt = CompoundTag.CODEC.parse(JsonOps.INSTANCE, object.get("nbt")).result().orElseThrow();
        }
    }

    private static void parseAmountAndTag(FluidStackProvider provider, FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            provider.amount = buf.readInt();
        }
        if (buf.readBoolean()) {
            provider.nbt = buf.readNbt();
        }
    }

    private static void writeAmountAndTag(FluidStackProvider provider, FriendlyByteBuf buf) {
        buf.writeBoolean(provider.amount != null);
        if (provider.amount != null) {
            buf.writeInt(provider.amount);
        }
        buf.writeBoolean(provider.nbt != null);
        if (provider.nbt != null) {
            buf.writeNbt(provider.nbt);
        }
    }

    private static FluidStackProvider createFromKey(Predicate<String> predicate) {
        if (predicate.test(TagFluidStackProvider.KEY)) return new TagFluidStackProvider();
        else if (predicate.test(IdFluidStackProvider.KEY)) return new IdFluidStackProvider();
        else throw new IllegalArgumentException("Failed to parse FluidStackProvider");
    }

    public static FluidStackProvider fromNetwork(FriendlyByteBuf buf) {
        var key = buf.readUtf();
        var prov = createFromKey(key::equals);
        parseAmountAndTag(prov, buf);
        prov.read(buf);
        return prov;
    }

    public FluidStackProvider ifNoAmountSpecified(IntSupplier amountIfNotSpecified) {
        this.amountIfNotSpecified = amountIfNotSpecified;
        return this;
    }

    protected abstract void readJson(JsonObject obj);

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(this.getKey());
        writeAmountAndTag(this, buf);
        this.write(buf);
    }

    protected abstract String getKey();

    protected abstract void read(FriendlyByteBuf buf);

    protected abstract void write(FriendlyByteBuf buf);

    public abstract FluidStack get();

    public int getAmount() {
        return amount == null ? amountIfNotSpecified.getAsInt() : amount;
    }
}
