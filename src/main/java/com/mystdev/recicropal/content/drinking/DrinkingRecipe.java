package com.mystdev.recicropal.content.drinking;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mystdev.recicropal.ModRecipes;
import com.mystdev.recicropal.common.fluid.FluidIngredient;
import com.mystdev.recicropal.content.drinking.capability.DrinkContext;
import com.mystdev.recicropal.content.drinking.result.DrinkResults;
import com.mystdev.recicropal.content.drinking.result.IDrinkResult;
import com.mystdev.recicropal.content.drinking.result.ISerializableDrinkResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A somewhat complicated recipe.
 * <p>
 * Here, fluids are handled a little bit different.
 * <ul>
 *     <li>
 *         {@link DrinkingRecipe#getDrunk} will return a contextual stack depending on what type of fluid was consumed
 *         and not what was used for recipe matching.
 *     </li>
 *     <li>
 *         If there's a {@code tag} property (FluidTag), the recipe will match using the appropriate tag. The returned stack
 *         from {@link DrinkingRecipe#getDrunk} should still follow its context.
 *     </li>
 * </ul>
 * <p>
 * DO NOT USE THIS OUTSIDE THE CONTEXT OF DRINKING such as calling {@link DrinkingRecipe#getResultItem()} and other methods
 * marked as {@link Deprecated}.
 */
public class DrinkingRecipe implements Recipe<FluidHandlerItemContainer> {
    public static final int DEFAULT_AMOUNT = 250;  // TODO: Make this configurable
    public final ResourceLocation id;
    public final List<IDrinkResult> results;
    public final FluidIngredient ingredient;

    public DrinkingRecipe(ResourceLocation id, List<IDrinkResult> results, FluidIngredient ingredient) {
        this.id = id;
        this.results = results;
        this.ingredient = ingredient;
    }

    public FluidStack getDrunk(DrinkContext context) {
        var fluidOpt = FluidUtil.getFluidContained(context.stack());
        if (fluidOpt.isEmpty()) return FluidStack.EMPTY;
        var fluid = fluidOpt.get();
        return new FluidStack(fluid.getFluid(), ingredient.getAmount(), fluid.getTag());
    }

    @Override
    public boolean matches(FluidHandlerItemContainer container, Level level) {
        return this.ingredient.test(container.getFluid());
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.DRINKING_RECIPE.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.DRINKING_SERIALIZER.get();
    }

    public static final Serializer SERIALIZER = new Serializer();

    static class Serializer implements RecipeSerializer<DrinkingRecipe> {

        @Override
        public DrinkingRecipe fromJson(ResourceLocation rl, JsonObject jsonObject) {
            var drinkResults = new ArrayList<IDrinkResult>();
            if (jsonObject.has("results")) {
                var srList = jsonObject.getAsJsonArray("results");
                srList.forEach(el -> {
                    var obj = el.getAsJsonObject();
                    var dr = DrinkResults.get(GsonHelper.getAsString(obj, "type")).orElseThrow();
                    if (dr instanceof ISerializableDrinkResult<?> sDr) {
                        dr = sDr.readJson(obj);
                    }
                    drinkResults.add(dr);
                });
            }

            var amount = GsonHelper.getAsInt(jsonObject, "amount", DEFAULT_AMOUNT);
            var fluidJsonObject = jsonObject.getAsJsonObject("fluid");
            var ingredient = FluidIngredient.fromJson(fluidJsonObject).withAmount(amount);

            if (fluidJsonObject.has("nbt")) {
                var nbt = CompoundTag.CODEC.decode(JsonOps.INSTANCE, fluidJsonObject.getAsJsonObject("nbt"))
                                           .get().orThrow().getFirst();
                ingredient = ingredient.withNbt(nbt);
            }

            return new DrinkingRecipe(rl, drinkResults, ingredient);
        }

        @Override
        public @Nullable DrinkingRecipe fromNetwork(ResourceLocation rl, FriendlyByteBuf buf) {
            var ingredient = FluidIngredient.read(buf);
            ingredient = ingredient.withAmount(buf.readInt());
            if (buf.readBoolean()) ingredient = ingredient.withNbt(buf.readNbt());

            var drinkResults = buf
                    .readCollection(ArrayList::new,
                                    (bufIn) -> {
                                        var key = bufIn.readUtf();
                                        var drinkResult = DrinkResults.get(key).orElseThrow();
                                        if (drinkResult instanceof ISerializableDrinkResult<?> serializableDrinkResult) {
                                            drinkResult = serializableDrinkResult.readNetwork(bufIn);
                                        }
                                        return drinkResult;
                                    });

            return new DrinkingRecipe(rl, drinkResults, ingredient);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, DrinkingRecipe recipe) {
            recipe.ingredient.write(buf);
            buf.writeInt(recipe.ingredient.getAmount());
            var hasTag = recipe.ingredient.hasTag();
            buf.writeBoolean(hasTag);
            if (hasTag) {
                buf.writeNbt(recipe.ingredient.getTag());
            }
            buf.writeCollection(recipe.results, (bufIn, drinkResult) -> {
                bufIn.writeUtf(DrinkResults.getKey(drinkResult).orElseThrow());
                if (drinkResult instanceof ISerializableDrinkResult<?> serializableDrinkResult) {
                    serializableDrinkResult.writeToNetwork(bufIn);
                }
            });
        }
    }

    @Override
    @Deprecated
    public ItemStack assemble(FluidHandlerItemContainer container) {
        return container.getFluidHandlerItem();
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
}
