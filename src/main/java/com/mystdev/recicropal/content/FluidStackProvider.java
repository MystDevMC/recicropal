package com.mystdev.recicropal.content;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.function.Function;

public abstract class FluidStackProvider {

    // TODO: Refactor this
    private static final Object2ObjectArrayMap<String, Function<String, FluidStackProvider>> fauxRegister = new Object2ObjectArrayMap<>();


    static {
        fauxRegister.put("chooseFromTag", (id) -> new FluidStackProvider(id) {
            private TagKey<Fluid> tag;

            @Override
            protected void readJson(JsonObject obj) {
                this.tag = FluidTags.create(new ResourceLocation(GsonHelper.getAsString(obj, this.id)));
            }

            @Override
            protected void read(FriendlyByteBuf buf) {
                this.tag = FluidTags.create(new ResourceLocation(buf.readUtf()));
            }

            @Override
            protected void write(FriendlyByteBuf buf) {
                buf.writeUtf(this.tag.location().toString());
            }

            @Override
            public FluidStack get() {
                var fluid = Objects
                        .requireNonNull(ForgeRegistries.FLUIDS.tags())
                        .getTag(this.tag)
                        .stream()
                        .findFirst()
                        .orElse(Fluids.EMPTY);
                return new FluidStack(fluid, this.amount, this.nbt);
            }
        });

        fauxRegister.put("fluid", (id) -> new FluidStackProvider(id) {
            private Fluid fluid;

            @Override
            protected void readJson(JsonObject obj) {
                this.fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(obj, this.id)));
                if (this.fluid == null) this.fluid = Fluids.EMPTY;
            }

            @Override
            protected void read(FriendlyByteBuf buf) {
                this.fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(buf.readUtf()));
            }

            @Override
            protected void write(FriendlyByteBuf buf) {
                buf.writeUtf(Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(this.fluid)).toString());
            }

            @Override
            public FluidStack get() {
                return new FluidStack(this.fluid, this.amount, this.nbt);
            }
        });
    }

    final String id;
    int amount;
    CompoundTag nbt;

    public FluidStackProvider(String id) {
        this.id = id;
    }

    public static FluidStackProvider fromJson(JsonObject obj) {
        if (obj.has("chooseFromTag")) {
            var ret = fauxRegister.get("chooseFromTag").apply("chooseFromTag");
            ret.readJson(obj);
            parseAmountAndTag(ret, obj);
            return ret;
        }
        else if (obj.has("fluid")) {
            var ret = fauxRegister.get("fluid").apply("fluid");
            ret.readJson(obj);
            parseAmountAndTag(ret, obj);
            return ret;
        }
        else throw new IllegalArgumentException("Failed to parse FluidStackProvider");
    }

    private static void parseAmountAndTag(FluidStackProvider provider, JsonObject object) {
        provider.amount = GsonHelper.getAsInt(object, "amount");
        if (object.has("nbt")) {
            provider.nbt = CompoundTag.CODEC.decode(JsonOps.INSTANCE, object.get("nbt")).get().orThrow().getFirst();
        }
    }

    protected abstract void readJson(JsonObject obj);

    public static FluidStackProvider fromNetwork(FriendlyByteBuf buf) {
        var id = buf.readUtf();
        var ret = fauxRegister.get(id).apply(id);
        ret.read(buf);
        return ret;
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(this.id);
        this.write(buf);
    }

    protected abstract void read(FriendlyByteBuf buf);

    protected abstract void write(FriendlyByteBuf buf);

    public abstract FluidStack get();
}
