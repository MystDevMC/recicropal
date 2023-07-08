package com.mystdev.recicropal.content.mixing;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mystdev.recicropal.ModRecipes;
import com.mystdev.recicropal.common.fluid.FluidIngredient;
import com.mystdev.recicropal.content.drinking.DrinkingRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class PouringRecipe implements Recipe<BottleInteractionContainer> {

    public static final Serializer SERIALIZER = new Serializer();
    public final ResourceLocation rl;
    public final Ingredient ingredient;
    public final FluidIngredient fluidIngredient;
    public final IMixingProcess process;
    public final ItemStack result;

    public PouringRecipe(ResourceLocation rl,
                         Ingredient ingredient,
                         FluidIngredient fluidIngredient,
                         IMixingProcess process,
                         ItemStack result) {
        this.rl = rl;
        this.ingredient = ingredient;
        this.fluidIngredient = fluidIngredient;
        this.process = process;
        this.result = result;
    }

    @Override
    public boolean matches(BottleInteractionContainer container, Level level) {
        if (process == null) {
            if (!ingredient.test(container.getItem(0))) return false;
            return fluidIngredient.test(container.getBottle().tank.getFluid());
        }
        else {
            return process.matchForPouring(container, level);
        }
    }

    @Override
    public ItemStack assemble(BottleInteractionContainer container) {
        if (process == null) {
            container.getBottle().tank.drain(fluidIngredient.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            return result.copy();
        }
        else {
            return process.assembleForPouring(container);
        }
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.POURING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.POURING_RECIPE.get();
    }

    @Override
    public ItemStack getResultItem() {
        return result.copy();
    }

    @Override
    @Deprecated
    public boolean canCraftInDimensions(int x, int y) {
        return false;
    }

    @Override
    @Deprecated
    public ResourceLocation getId() {
        return rl;
    }

    public static class Serializer implements RecipeSerializer<PouringRecipe> {

        @Override
        public PouringRecipe fromJson(ResourceLocation rl, JsonObject jsonObject) {
            if (jsonObject.has("process"))
                return new PouringRecipe(rl,
                                         Ingredient.EMPTY,
                                         null,
                                         IMixingProcess.get(GsonHelper.getAsString(jsonObject, "process")),
                                         ItemStack.EMPTY);

            var ingredient = Ingredient.fromJson(jsonObject.get("ingredient"));
            var fluidJson = jsonObject.getAsJsonObject("fluid");
            var amt = GsonHelper.getAsInt(fluidJson, "amount", DrinkingRecipe.DEFAULT_AMOUNT);
            var fluidIngredient = FluidIngredient.fromJson(fluidJson);
            if (fluidJson.has("nbt")) {
                var nbt = CompoundTag.CODEC.parse(JsonOps.INSTANCE, fluidJson.get("nbt")).result().orElseThrow();
                fluidIngredient.withNbt(nbt);
            }

            var result = ItemStack.CODEC
                    .parse(JsonOps.INSTANCE, jsonObject.get("result"))
                    .result()
                    .orElseThrow();
            return new PouringRecipe(rl, ingredient, fluidIngredient.withAmount(amt), null, result);
        }

        @Override
        public @Nullable PouringRecipe fromNetwork(ResourceLocation rl, FriendlyByteBuf buf) {
            if (buf.readBoolean())
                return new PouringRecipe(rl,
                                         Ingredient.EMPTY,
                                         null,
                                         IMixingProcess.get(buf.readUtf()),
                                         ItemStack.EMPTY);

            var ingredient = Ingredient.fromNetwork(buf);
            var fluidIngredient = FluidIngredient.read(buf);
            var amt = buf.readInt();
            if (buf.readBoolean()) fluidIngredient.withNbt(buf.readNbt());
            var result = buf.readItem();
            return new PouringRecipe(rl, ingredient, fluidIngredient.withAmount(amt), null, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, PouringRecipe recipe) {
            buf.writeBoolean(recipe.process != null);
            if (recipe.process != null) {
                buf.writeUtf(recipe.process.getId());
                return;
            }
            recipe.ingredient.toNetwork(buf);
            recipe.fluidIngredient.write(buf);
            buf.writeInt(recipe.fluidIngredient.getAmount());
            var hasTag = recipe.fluidIngredient.hasTag();
            buf.writeBoolean(hasTag);
            if (hasTag) {
                buf.writeNbt(recipe.fluidIngredient.getTag());
            }
            buf.writeItem(recipe.result);
        }

    }
}
