package com.mystdev.recicropal.content.mixing;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mystdev.recicropal.ModRecipes;
import com.mystdev.recicropal.Recicropal;
import com.mystdev.recicropal.common.fluid.ModFluidUtils;
import com.mystdev.recicropal.common.fluid.provider.FluidStackProvider;
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

public class FillingRecipe implements Recipe<BottleInteractionContainer> {
    private final ResourceLocation rl;
    private final Ingredient ingredient;
    private final FluidStackProvider resultProvider;
    private final IMixingProcess process;
    private final ItemStack result;

    public FillingRecipe(ResourceLocation rl,
                         Ingredient ingredient,
                         FluidStackProvider resultProvider,
                         IMixingProcess process,
                         ItemStack result) {
        this.rl = rl;
        this.ingredient = ingredient;
        this.resultProvider = resultProvider;
        this.process = process;
        this.result = result;
    }

    @Override
    public boolean matches(BottleInteractionContainer container, Level level) {
        if (process == null) {
            // Idk why sometimes when you use a FluidHandlerItem, it'll detect air instead of the item
            if (!ingredient.test(container.getItem(0))) return false;
            var stackToFill = resultProvider.get();
            var test = container.getBottle().tank.fill(stackToFill, IFluidHandler.FluidAction.SIMULATE);
            return test == stackToFill.getAmount();
        }
        else {
            return process.matchForFilling(container, level);
        }
    }

    @Override
    public ItemStack assemble(BottleInteractionContainer container) {
        if (process == null) {
            var stackToFill = resultProvider.get();
            container.getBottle().tank.fill(stackToFill, IFluidHandler.FluidAction.EXECUTE);
            return result.copy();
        }
        else {
            return process.assembleForFilling(container);
        }
    }

    @Override
    public ResourceLocation getId() {
        return rl;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.FILLING_RECIPE.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FILLING_SERIALIZER.get();
    }

    public static final FillingRecipe.Serializer SERIALIZER = new FillingRecipe.Serializer();

    public static class Serializer implements RecipeSerializer<FillingRecipe> {

        @Override
        public FillingRecipe fromJson(ResourceLocation rl, JsonObject jsonObject) {
            if (jsonObject.has("process"))
                return new FillingRecipe(rl, Ingredient.EMPTY, null,
                                         IMixingProcess.get(GsonHelper.getAsString(jsonObject, "process")),
                                         ItemStack.EMPTY);

            var ingredient = Ingredient.fromJson(jsonObject.get("ingredient"));
            var provider = FluidStackProvider.fromJson(jsonObject.getAsJsonObject("fluid"));
            var result = ItemStack.CODEC
                    .decode(JsonOps.INSTANCE, jsonObject.get("result"))
                    .result().orElseThrow().getFirst();
            return new FillingRecipe(rl, ingredient, provider, null, result);
        }

        @Override
        public @Nullable FillingRecipe fromNetwork(ResourceLocation rl, FriendlyByteBuf buf) {
            if (buf.readBoolean())
                return new FillingRecipe(rl,
                                         Ingredient.EMPTY,
                                         null,
                                         IMixingProcess.get(buf.readUtf()),
                                         ItemStack.EMPTY);

            var ingredient = Ingredient.fromNetwork(buf);
            var provider = FluidStackProvider.fromNetwork(buf);
            var result = buf.readItem();
            return new FillingRecipe(rl, ingredient, provider, null, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, FillingRecipe recipe) {
            buf.writeBoolean(recipe.process != null);
            if (recipe.process != null) {
                buf.writeUtf(recipe.process.getId());
                return;
            }
            recipe.ingredient.toNetwork(buf);
            recipe.resultProvider.toNetwork(buf);
            buf.writeItem(recipe.result);
        }

    }

    @Override
    @Deprecated
    public boolean canCraftInDimensions(int x, int y) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return result.copy();
    }

}
