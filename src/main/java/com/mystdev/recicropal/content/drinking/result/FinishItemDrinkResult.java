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

public class FinishItemDrinkResult implements ISerializableDrinkResult<FinishItemDrinkResult> {
    private Item item;

    @Override
    public void apply(Player player, Level level, FluidStack drunkStack) {
        new ItemStack(item).finishUsingItem(level, player);
    }

    @Override
    public DrinkResultType<? extends IDrinkResult> getType() {
        return DrinkResults.FINISH_ITEM.get();
    }

    @Override
    public FinishItemDrinkResult readJson(JsonObject jsonObject) {
        this.item = GsonHelper.getAsItem(jsonObject, "item");
        return this;
    }

    @Override
    public @Nullable FinishItemDrinkResult readNetwork(FriendlyByteBuf buf) {
        this.item = Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(buf.readUtf())));
        return this;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.item)).toString());
    }
}
