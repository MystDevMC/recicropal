package com.mystdev.recicropal.content.drinking.result;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public class FinishItemTransferNbtDrinkResult implements ISerializableDrinkResult<FinishItemTransferNbtDrinkResult>{
    private Item item;
    @Override
    public void apply(Player player, Level level, FluidStack drunkStack) {
        var stack = new ItemStack(item);
        stack.setTag(drunkStack.getTag());
        item.finishUsingItem(stack, level, player);
    }

    @Override
    public Supplier<DrinkResultType<? extends IDrinkResult>> getType() {
        return DrinkResults.FINISH_ITEM_TRANSFER_NBT::get;
    }

    @Override
    public FinishItemTransferNbtDrinkResult readJson(JsonObject jsonObject) {
        this.item = GsonHelper.getAsItem(jsonObject, "item");
        return this;
    }

    @Override
    public @Nullable FinishItemTransferNbtDrinkResult readNetwork(FriendlyByteBuf buf) {
        this.item = Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(buf.readUtf())));
        return this;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.item)).toString());
    }
}
