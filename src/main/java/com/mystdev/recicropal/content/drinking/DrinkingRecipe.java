package com.mystdev.recicropal.content.drinking;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mystdev.recicropal.ModRecipes;
import com.mystdev.recicropal.Recicropal;
import com.mystdev.recicropal.content.drinking.result.DrinkResults;
import com.mystdev.recicropal.content.drinking.result.IDrinkResult;
import com.mystdev.recicropal.content.drinking.result.ISerializableDrinkResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A somewhat complicated recipe.
 * <p>
 * Here, fluids are handled a little bit different.
 * <ul>
 *     <li>
 *         If there's a {@code nbt} property, the recipe will match using {@link FluidStack#areFluidStackTagsEqual}.
 *         If not, the recipe will not match any tags. This allows fluids like potions to match only by fluid ID.
 *     </li>
 *     <li>
 *         {@link DrinkingRecipe#getDrunk} will return a contextual stack depending on what type of fluid was consumed
 *         and not what was used for recipe matching.
 *     </li>
 *     <li>
 *         If there's a {@code tag} property (FluidTag), the recipe will match using the appropriate tag. The returned stack
 *         from {@link DrinkingRecipe#getDrunk} should still follow its context. This will be ANDed if there's also the
 *         {@code nbt} prop.
 *     </li>
 * </ul>
 * <p>
 * DO NOT USE THIS OUTSIDE THE CONTEXT OF DRINKING such as calling {@link DrinkingRecipe#assemble} and other methods
 * marked as {@link Deprecated}.
 */
public class DrinkingRecipe implements Recipe<FluidHandlerItemContainer> {
    public final ResourceLocation id;
    public final List<IDrinkResult> results;
    public final FluidStack stack; // TODO: Should've used fluid ingredient or something
    public final boolean hasNbt;
    public final @Nullable TagKey<Fluid> fluidTag;

    public DrinkingRecipe(ResourceLocation id, List<IDrinkResult> results, FluidStack stack,
                          boolean hasNbt,
                          @Nullable TagKey<Fluid> fluidTag) {
        this.id = id;
        this.results = results;
        this.stack = stack;
        this.hasNbt = hasNbt;
        this.fluidTag = fluidTag;
    }

    // TODO: Haven't checked whether it'll return a modified stack or not
    public FluidStack getDrunk(DrinkContext context) {
        var fluidOpt = FluidUtil.getFluidContained(context.stack());
        if (fluidOpt.isEmpty()) return FluidStack.EMPTY;
        var fluid = fluidOpt.get();
        return new FluidStack(fluid.getFluid(), stack.getAmount(), fluid.getTag());
    }

    @Override
    public boolean matches(FluidHandlerItemContainer container, Level level) {
        var ret = false;
        var fluidStackIn = container.getFluid();
        var fluidIn = fluidStackIn.getFluid();
        if (fluidTag != null) ret = ForgeRegistries.FLUIDS.tags().getTag(fluidTag).contains(fluidIn);
        else ret = stack.getFluid() == fluidIn;
        if (hasNbt) ret = ret && FluidStack.areFluidStackTagsEqual(fluidStackIn, stack);
        return ret;
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

            var amount = GsonHelper.getAsInt(jsonObject, "amount", 250); // TODO: Make this configurable
            var fluidJsonObject = jsonObject.getAsJsonObject("fluid");
            CompoundTag nbt = null;
            if (fluidJsonObject.has("nbt"))
                nbt = CompoundTag.CODEC.decode(JsonOps.INSTANCE, fluidJsonObject.getAsJsonObject("nbt"))
                                       .get().orThrow().getFirst();

            var fluid = Fluids.EMPTY;

            TagKey<Fluid> tag = null;
            if (fluidJsonObject.has("tag") && fluidJsonObject.has("id")) throw new IllegalArgumentException(
                    "Drinking recipe with id " + rl + " has both id and tag!"
            );
            else if (fluidJsonObject.has("tag")) {
                tag = FluidTags.create(new ResourceLocation(GsonHelper.getAsString(fluidJsonObject, "tag")));
                final TagKey<Fluid> finalTag = tag;
                fluid = ForgeRegistries.FLUIDS
                        .getValues().stream()
                        .filter(f -> ForgeRegistries.FLUIDS.tags().getTag(finalTag).contains(f))
                        .findFirst().orElse(Fluids.EMPTY);
            }
            else if (fluidJsonObject.has("id")) {
                fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(fluidJsonObject,
                                                                                                    "id")));
                if (fluid == null) fluid = Fluids.EMPTY;
            }

            var fluidStack = new FluidStack(fluid, amount, nbt);
            if (fluidStack.isEmpty()) return null;

            return new DrinkingRecipe(rl,
                                      drinkResults,
                                      fluidStack,
                                      fluidJsonObject.has("nbt"),
                                      tag);
        }

        @Override
        public @Nullable DrinkingRecipe fromNetwork(ResourceLocation rl, FriendlyByteBuf buf) {
            var resultTag = buf.readNbt();
            if (resultTag == null) throw new IllegalArgumentException();
            var hasNbt = false;
            var nbt = new CompoundTag();
            if (resultTag.contains("nbt")) nbt = resultTag.getCompound("nbt");
            hasNbt = true;
            var fluid = Fluids.EMPTY;
            TagKey<Fluid> tag = null;
            if (!resultTag.getString("tag").isEmpty()) {
                tag = FluidTags.create(new ResourceLocation(resultTag.getString("tag")));
                final TagKey<Fluid> finalTag = tag;
                fluid = ForgeRegistries.FLUIDS
                        .getValues().stream()
                        .filter(f -> ForgeRegistries.FLUIDS.tags().getTag(finalTag).contains(f)).findFirst().orElse(Fluids.EMPTY);
            }
            else {
                fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(resultTag.getString("id")));
                if (fluid == null) fluid = Fluids.EMPTY;
            }

            var amount = resultTag.getInt("amount");
            var fluidStack = new FluidStack(fluid, amount, nbt);

            if (fluidStack.isEmpty()) return null;

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

            return new DrinkingRecipe(rl,
                                      drinkResults,
                                      fluidStack,
                                      hasNbt,
                                      tag);
        }

        // A bit hacky cuz I'm lazy
        @Override
        public void toNetwork(FriendlyByteBuf buf, DrinkingRecipe recipe) {
            if (recipe.stack.isEmpty()) return;

            var resultTag = new CompoundTag();
            if (recipe.hasNbt) {
                resultTag.put("nbt", recipe.stack.getTag());
            }

            resultTag.putString("tag", recipe.fluidTag == null ? "" : recipe.fluidTag.location().toString());
            resultTag.putString("id", recipe.fluidTag == null ?
                                      Objects
                                              .requireNonNull(ForgeRegistries.FLUIDS.getKey(recipe.stack.getFluid()))
                                              .toString()
                                                              : "");
            resultTag.putInt("amount", recipe.stack.getAmount());
            buf.writeNbt(resultTag);
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
