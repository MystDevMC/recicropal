package com.mystdev.recicropal.content.drinking;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mystdev.recicropal.ModRecipes;
import com.mystdev.recicropal.Recicropal;
import com.mystdev.recicropal.common.fluid.FluidIngredient;
import com.mystdev.recicropal.common.fluid.ModFluidUtils;
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
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A somewhat complicated recipe.
 * <p>
 * Here, fluids are handled a little bit different.
 * <ul>
 *     <li>
 *         {@link DrinkingRecipe#getDrinkable} will return a contextual stack depending on what type of fluid was consumed
 *         and not what was used for recipe matching.
 *     </li>
 *     <li>
 *         If there's a {@code tag} property (FluidTag), the recipe will match using the appropriate tag.
 *     </li>
 *     <li>
 *        If there's a {@code amount} property, the recipe will match using the amount, if not specified anything could be drunk.
 *     </li>
 * </ul>
 * <p>
 * DO NOT USE THIS OUTSIDE THE CONTEXT OF DRINKING such as calling {@link DrinkingRecipe#getResultItem()} and other methods
 * marked as {@link Deprecated}.
 */
public class DrinkingRecipe implements Recipe<FluidHandlerItemContainer> {
    public static final int DEFAULT_AMOUNT = 250;  // TODO: Make this configurable
    public static final Serializer SERIALIZER = new Serializer();
    private final ResourceLocation id;
    private final List<IDrinkResult> results;
    private final FluidIngredient ingredient;

    public DrinkingRecipe(ResourceLocation id, List<IDrinkResult> results, FluidIngredient ingredient) {
        this.id = id;
        this.results = results;
        this.ingredient = ingredient;
    }

    public FluidStack getDrinkable(DrinkContext context) {
        var fluid = FluidUtil.getFluidContained(context.stack()).orElse(FluidStack.EMPTY);
        Recicropal.LOGGER.debug(String.valueOf(Math.min(fluid.getAmount(), DEFAULT_AMOUNT)));
        return new FluidStack(fluid.getFluid(), Math.min(fluid.getAmount(), DEFAULT_AMOUNT), fluid.getTag());
    }

    public void assemble(DrinkContext ctx) {
        var player = ctx.player();

        // Assuming that it has already matched
        // Drink the liquid
        var wrappedInventory = new PlayerInvWrapper(player.getInventory());

        var drinkable = this.getDrinkable(ctx);

        var fluidRes = FluidUtil
                .tryEmptyContainerAndStow(ctx.stack(),
                                          ModFluidUtils.voidTank(),
                                          wrappedInventory,
                                          drinkable.getAmount(),
                                          player,
                                          true);

        // Return the new stack to player
        player.setItemInHand(player.getUsedItemHand(), fluidRes.result);

        // Apply post-drinking effects
        results.forEach(res -> res.apply(player, ctx.level(), drinkable));
    }

    @Nullable
    public Integer getAmount() {
        return ingredient.getAmount();
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

            var fluidJsonObject = jsonObject.getAsJsonObject("fluid");
            var ingredient = FluidIngredient.fromJson(fluidJsonObject);

            if (jsonObject.has("amount")) {
                var amount = GsonHelper.getAsInt(jsonObject, "amount", DEFAULT_AMOUNT);
                ingredient.withAmount(amount);
            }

            if (fluidJsonObject.has("nbt")) {
                var nbt = CompoundTag.CODEC
                        .parse(JsonOps.INSTANCE, fluidJsonObject.getAsJsonObject("nbt"))
                        .result()
                        .orElseThrow();
                ingredient.withNbt(nbt);
            }

            return new DrinkingRecipe(rl, drinkResults, ingredient);
        }

        @Override
        public @Nullable DrinkingRecipe fromNetwork(ResourceLocation rl, FriendlyByteBuf buf) {
            var ingredient = FluidIngredient.read(buf);

            buf.readNullable((byteBuf) -> ingredient.withAmount(byteBuf.readInt()));
            buf.readNullable((byteBuf) -> ingredient.withNbt(byteBuf.readNbt()));

            var drinkResults = buf.readCollection(ArrayList::new, (bufIn) -> {
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

            buf.writeNullable(recipe.ingredient.getAmount(), FriendlyByteBuf::writeInt);
            buf.writeNullable(recipe.ingredient.getTag(), FriendlyByteBuf::writeNbt);

            buf.writeCollection(recipe.results, (bufIn, drinkResult) -> {
                bufIn.writeUtf(DrinkResults.getKey(drinkResult).orElseThrow());
                if (drinkResult instanceof ISerializableDrinkResult<?> serializableDrinkResult) {
                    serializableDrinkResult.writeToNetwork(bufIn);
                }
            });
        }
    }
}
