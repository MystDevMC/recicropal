package com.mystdev.recicropal.content.mixing;

import com.google.gson.JsonObject;
import com.mystdev.recicropal.ModRecipes;
import com.mystdev.recicropal.common.fluid.FluidIngredient;
import com.mystdev.recicropal.common.fluid.ModFluidUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class FluidConversionRecipe implements Recipe<FluidConversionContainer> {

    private final ResourceLocation rl;
    private final FluidIngredient ingredient;
    private final Fluid fluid;
    private final boolean isIngress;
    private final FluidTransformer transformer;

    public FluidConversionRecipe(ResourceLocation rl,
                                 FluidIngredient ingredient,
                                 Fluid fluid,
                                 boolean isIngress,
                                 FluidTransformer transformer) {
        this.rl = rl;
        this.ingredient = ingredient;
        this.fluid = fluid;
        this.isIngress = isIngress;
        this.transformer = transformer;
    }

    @Override
    public boolean matches(FluidConversionContainer container, Level level) {
        if (this.isIngress != container.isIngress) return false;
        return this.ingredient.test(container.fluidStack);
    }
    public FluidStack convert(FluidStack stack) {
        return this.transformer.convert(fluid, stack);
    }

    @Override
    public ResourceLocation getId() {
        return rl;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FLUID_CONVERSION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.FLUID_CONVERSION_RECIPE.get();
    }

    public static final Serializer SERIALIZER = new Serializer();

    public static class Serializer implements RecipeSerializer<FluidConversionRecipe> {

        @Override
        public FluidConversionRecipe fromJson(ResourceLocation rl, JsonObject jsonObject) {
            var ingredient = FluidIngredient.fromJson(jsonObject.getAsJsonObject("from"));
            var fluid = ModFluidUtils.fluidOrAir(GsonHelper.getAsString(jsonObject, "to"));
            if (fluid == Fluids.EMPTY) return null;
            var flow = GsonHelper.getAsBoolean(jsonObject, "ingress", true);
            var transformer = FluidTransformer.tryParse(GsonHelper.getAsString(jsonObject, "function", ""));
            return new FluidConversionRecipe(rl, ingredient, fluid, flow, transformer);
        }

        @Override
        public @Nullable FluidConversionRecipe fromNetwork(ResourceLocation rl, FriendlyByteBuf buf) {
            var ingredient = FluidIngredient.read(buf);
            var fluid = ModFluidUtils.fluidOrAir(buf.readUtf());
            if (fluid == Fluids.EMPTY) return null;
            var flow = buf.readBoolean();
            var transformer = buf.readEnum(FluidTransformer.class);
            return new FluidConversionRecipe(rl, ingredient, fluid, flow, transformer);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, FluidConversionRecipe recipe) {
            recipe.ingredient.write(buf);
            buf.writeUtf(ModFluidUtils.key(recipe.fluid));
            buf.writeBoolean(recipe.isIngress);
            buf.writeEnum(recipe.transformer);
        }
    }

    @Deprecated
    @Override
    public ItemStack assemble(FluidConversionContainer container) {
        return ItemStack.EMPTY;
    }

    @Deprecated
    @Override
    public boolean canCraftInDimensions(int x, int y) {
        return false;
    }

    @Deprecated
    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

}
