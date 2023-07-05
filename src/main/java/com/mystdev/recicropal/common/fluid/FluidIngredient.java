package com.mystdev.recicropal.common.fluid;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

// Inspired by the Creators of Create
// i use some kind of lazy strategy pattern here XD
// This decomposes the FluidStack, so maybe it isn't the best
public class FluidIngredient implements Predicate<FluidStack> {

    public static final FluidIngredient EMPTY = new FluidIngredient(new EmptyValue());
    private final Value value;
    private final List<Fluid> dissolved = new ObjectArrayList<>();
    private CompoundTag tag;
    private boolean isTagSet;
    private Integer amount;

    private FluidIngredient(Value value) {
        this.value = value;
    }

    private void dissolve() {
        dissolved.addAll(value.getFluids());
    }

    private boolean dissolved() {
        return dissolved.size() != 0;
    }

    @Override
    public boolean test(FluidStack stack) {
        if (!dissolved()) dissolve();
        for (var f : dissolved) {
            if (f != stack.getFluid()) continue;
            if (isTagSet && !hasTheSameTagWith(stack)) continue;
            if (amount != null && stack.getAmount() < amount) continue;
            return true;
        }
        return false;
    }

    private boolean hasTheSameTagWith(FluidStack stack) {
        return tag == null ? stack.getTag() == null : stack.getTag() != null && tag.equals(stack.getTag());
    }

    public int getAmount() {
        return amount == null ? 0 : amount;
    }

    public boolean hasTag() {
        return isTagSet && tag != null;
    }

    public CompoundTag getTag() {
        return tag;
    }

    public FluidIngredient withAmount(Integer amt) {
        this.amount = amt;
        return this;
    }

    // Only set this when needed.
    public FluidIngredient withNbt(CompoundTag tag) {
        this.tag = tag;
        this.isTagSet = true;
        return this;
    }

    public JsonObject toJson() {
        var ret = new JsonObject();
        this.value.writeJson(ret);
        return ret;
    }

    public static FluidIngredient fromJson(JsonObject jsonObject) {
        var either = Either.<JsonObject, FriendlyByteBuf>left(jsonObject);
        var val = Value.getAppropriateFactory(either).apply(either);
        return new FluidIngredient(val);
    }

    public void write(FriendlyByteBuf buf) {
        this.value.write(buf);
    }

    public static FluidIngredient read(FriendlyByteBuf buf) {
        var either = Either.<JsonObject, FriendlyByteBuf>right(buf);
        var val = Value.getAppropriateFactory(either).apply(either);
        return new FluidIngredient(val);
    }

    private static class EmptyValue implements Value {
        @Override
        public Collection<Fluid> getFluids() {
            return Collections.emptyList();
        }

        @Override
        public void readJson(JsonObject object) {

        }

        @Override
        public void writeJson(JsonObject object) {

        }

        @Override
        public void write(FriendlyByteBuf buf) {

        }

        @Override
        public void read(FriendlyByteBuf buf) {

        }
    }

    public static class FluidValue implements Value {
        private Fluid fluid;

        @Override
        public Collection<Fluid> getFluids() {
            return Collections.singleton(fluid);
        }

        @Override
        public void readJson(JsonObject object) {
            this.fluid = ModFluidUtils.fluidOrAir(GsonHelper.getAsString(object, "fluid"));
        }

        @Override
        public void writeJson(JsonObject object) {
            object.addProperty("fluid", ModFluidUtils.key(fluid));
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeBoolean(false);
            buf.writeUtf(ModFluidUtils.key(fluid));
        }

        @Override
        public void read(FriendlyByteBuf buf) {
            this.fluid = ModFluidUtils.fluid(buf.readUtf());
        }
    }

    public static class FluidTagValue implements Value {

        private TagKey<Fluid> fluidTag;

        @Override
        public Collection<Fluid> getFluids() {
            return ModFluidUtils.members(fluidTag);
        }

        @Override
        public void readJson(JsonObject object) {
            this.fluidTag = ModFluidUtils.tag(GsonHelper.getAsString(object, "tag"));
        }

        @Override
        public void writeJson(JsonObject object) {
            object.addProperty("tag", fluidTag.location().toString());
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeBoolean(true);
            buf.writeUtf(this.fluidTag.location().toString());
        }

        @Override
        public void read(FriendlyByteBuf buf) {
            this.fluidTag = ModFluidUtils.tag(buf.readUtf());
        }
    }

    interface Value {
        Collection<Fluid> getFluids();

        void readJson(JsonObject object);

        void writeJson(JsonObject object);

        void write(FriendlyByteBuf buf);

        void read(FriendlyByteBuf buf);

        static <V extends Value> V fromJson(V toModify, JsonObject object) {
            toModify.readJson(object);
            return toModify;
        }

        static <V extends Value> V fromNetwork(V toModify, FriendlyByteBuf buf) {
            toModify.read(buf);
            return toModify;
        }

        static Function<Either<JsonObject, FriendlyByteBuf>, Value> getAppropriateFactory(Either<JsonObject, FriendlyByteBuf> data) {
            var jsonOpt = data.left();
            var bufOpt = data.right();
            if (jsonOpt.isPresent()) {
                var json = jsonOpt.get();
                var hasTag = json.has("tag");
                var hasFluid = json.has("fluid");
                if (hasTag && hasFluid)
                    throw new IllegalArgumentException("Fluid ingredient cannot have both tag and fluid property!");
                else if (hasTag) return either -> fromJson(new FluidTagValue(), either.orThrow());
                else if (hasFluid) return either -> fromJson(new FluidValue(), either.orThrow());
                else throw new IllegalArgumentException("Failed to read JSON");
            }
            else if (bufOpt.isPresent()) {
                var buf = bufOpt.get();
                var isTag = buf.readBoolean();
                if (isTag) return either -> fromNetwork(new FluidTagValue(), either.right().orElseThrow());
                else return either -> fromNetwork(new FluidValue(), either.right().orElseThrow());
            }
            else {
                throw new IllegalArgumentException("Failed to read JSON");
            }
        }
    }
}
