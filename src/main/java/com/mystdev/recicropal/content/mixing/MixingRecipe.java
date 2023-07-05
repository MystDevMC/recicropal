package com.mystdev.recicropal.content.mixing;

import com.google.gson.JsonObject;
import com.mystdev.recicropal.ModRecipes;
import com.mystdev.recicropal.common.fluid.FluidIngredient;
import com.mystdev.recicropal.common.fluid.provider.FluidStackProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/**
 * For now, this only checks the fluid type and nbt if they are able to mix
 */
public class MixingRecipe implements Recipe<MixingContainer> {
    private final ResourceLocation rl;
    private final FluidIngredient ingressFluid;
    private final FluidIngredient insideFluid;
    private final IMixingProcess process;
    private final FluidStackProvider resultFluid;

    public MixingRecipe(ResourceLocation rl,
                        FluidIngredient ingressFluid, FluidIngredient insideFluid,
                        IMixingProcess process,
                        FluidStackProvider resultFluid) {
        this.rl = rl;
        this.ingressFluid = ingressFluid;
        this.insideFluid = insideFluid;
        this.process = process;
        this.resultFluid = resultFluid;
    }

    // Match reciprocally
    @Override
    public boolean matches(MixingContainer container, Level level) {
        if (process == null) {
            var in = container.getIncomingFluid();
            if (!ingressFluid.test(in) && !insideFluid.test(in)) return false;
            var inBottle = container.getBottle().getFluid();
            return insideFluid.test(inBottle) || insideFluid.test(inBottle);
        }
        else {
            return process.matchForMixing(container, level);
        }
    }

    public FluidStack getResult(MixingContainer container) {
        if (process != null) return process.getMixingResult(container);
        return resultFluid
                .ifNoAmountSpecified(
                        () -> container.getBottle().getFluidAmount() + container
                        .getIncomingFluid()
                        .getAmount())
                .get();
    }

    @Override
    public ResourceLocation getId() {
        return rl;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.MIXING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.MIXING_RECIPE.get();
    }

    public static final Serializer SERIALIZER = new Serializer();

    public static class Serializer implements RecipeSerializer<MixingRecipe> {

        @Override
        public MixingRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
            if (jsonObject.has("process"))
                return new MixingRecipe(resourceLocation,
                                        FluidIngredient.EMPTY,
                                        FluidIngredient.EMPTY,
                                        IMixingProcess.get(
                                                GsonHelper.getAsString(jsonObject, "process")),
                                        null);
            var ingressFluid = FluidIngredient.fromJson(jsonObject.getAsJsonObject("fluidIn"));
            var insideFluid = FluidIngredient.fromJson(jsonObject.getAsJsonObject("fluidInside"));
            var resultFluid = FluidStackProvider.fromJson(jsonObject.getAsJsonObject("result"));
            return new MixingRecipe(resourceLocation, ingressFluid, insideFluid, null, resultFluid);
        }

        @Override
        public @Nullable MixingRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf buf) {
            if (buf.readBoolean())
                return new MixingRecipe(resourceLocation,
                                        FluidIngredient.EMPTY,
                                        FluidIngredient.EMPTY,
                                        IMixingProcess.get(buf.readUtf()),
                                        null);
            var ingressFluid = FluidIngredient.read(buf);
            var insideFluid = FluidIngredient.read(buf);
            var resultFluid = FluidStackProvider.fromNetwork(buf);
            return new MixingRecipe(resourceLocation, ingressFluid, insideFluid, null, resultFluid);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, MixingRecipe recipe) {
            buf.writeBoolean(recipe.process != null);
            if (recipe.process != null) {
                buf.writeUtf(recipe.process.getId());
                return;
            }
            recipe.ingressFluid.write(buf);
            recipe.insideFluid.write(buf);
            recipe.resultFluid.toNetwork(buf);
        }
    }

    @Override
    @Deprecated
    public boolean canCraftInDimensions(int x, int y) {
        return false;
    }

    @Override
    @Deprecated
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    @Deprecated
    public ItemStack assemble(MixingContainer container) {
        return ItemStack.EMPTY;
    }

}
