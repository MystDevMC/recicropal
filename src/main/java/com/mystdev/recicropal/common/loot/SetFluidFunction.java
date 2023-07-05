package com.mystdev.recicropal.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.JsonOps;
import com.mystdev.recicropal.common.fluid.ModFluidUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Optional;

public class SetFluidFunction extends LootItemConditionalFunction {

    public static final LootItemFunctionType TYPE = new LootItemFunctionType(new Serializer());
    private final Fluid fluid;
    private final NumberProvider amount;
    private final CompoundTag nbt;

    public SetFluidFunction(LootItemCondition[] conditions,
                            Fluid fluid,
                            NumberProvider amount,
                            @Nullable CompoundTag nbt) {
        super(conditions);
        this.fluid = fluid;
        this.amount = amount;
        this.nbt = nbt;
    }

    @Override
    public LootItemFunctionType getType() {
        return TYPE;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext ctx) {
        var modifiedStack = ItemHandlerHelper.copyStackWithSize(stack, 1);
        FluidUtil.getFluidHandler(modifiedStack)
                 .ifPresent(tank -> {
                     var amt = (this.amount.getInt(ctx) * 5); // TODO: Make this not strictly multiplied
                     var fluidStack = new FluidStack(this.fluid, amt);
                     if (nbt != null) fluidStack.setTag(this.nbt);
                     tank.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                 });
        modifiedStack.setCount(stack.getCount());
        return modifiedStack;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetFluidFunction> {

        @Override
        public void serialize(JsonObject jsonObject, SetFluidFunction function, JsonSerializationContext ctx) {
            super.serialize(jsonObject, function, ctx);
            jsonObject.addProperty("fluid", ModFluidUtils.key(function.fluid));
            jsonObject.add("amount", ctx.serialize(function.amount));
            if (function.nbt != null) {
                var nbtJson = CompoundTag.CODEC.encodeStart(JsonOps.INSTANCE, function.nbt).get().left();
                nbtJson.ifPresent(nbt -> jsonObject.add("nbt", nbt));
            }
        }

        @Override
        public SetFluidFunction deserialize(JsonObject object,
                                            JsonDeserializationContext ctx,
                                            LootItemCondition[] conditions) {
            var fluidRL = new ResourceLocation(GsonHelper.getAsString(object, "fluid"));
            var holder = new Object() {
                Fluid fluid = Fluids.EMPTY;
                NumberProvider provider = ConstantValue.exactly(0);
                CompoundTag nbt = null;
            };
            Optional.ofNullable(ForgeRegistries.FLUIDS.getValue(fluidRL)).ifPresent(fluid -> {
                holder.fluid = fluid;
                holder.provider = GsonHelper.getAsObject(object, "amount", ctx, NumberProvider.class);
                var nbt = CompoundTag.CODEC.parse(JsonOps.INSTANCE, object.get("nbt")).result();
                nbt.ifPresent(tag -> holder.nbt = tag);
            });
            return new SetFluidFunction(conditions, holder.fluid, holder.provider, holder.nbt);
        }
    }
}
