package com.mystdev.recicropal.content.mixing;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mystdev.recicropal.ModFluids;
import com.mystdev.recicropal.ModRecipes;
import com.mystdev.recicropal.content.FluidStackProvider;
import com.mystdev.recicropal.content.drinking.DrinkingRecipe;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class FillingRecipe implements Recipe<BottleInteractionContainer> {
    private final ResourceLocation rl;
    private final Ingredient ingredient;
    private final FluidStackProvider resultProvider;
    private final IFillingProcess process;
    private final ItemStack result;

    public FillingRecipe(ResourceLocation rl,
                         Ingredient ingredient,
                         FluidStackProvider resultProvider,
                         IFillingProcess process,
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
            return process.matches(container, level);
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
            return process.assemble(container);
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

        // TODO: Fix this
        private static final Object2ObjectArrayMap<String, IFillingProcess> fauxRegister = new Object2ObjectArrayMap<>();

        static {
            fauxRegister.put("recicropal:potion", new IFillingProcess() {
                @Override
                public boolean matches(BottleInteractionContainer container, Level level) {
                    var item = container.getItem(0).getItem();

                    // Gonna hardcode this one for the moment
                    if (item != Items.POTION && item != Items.SPLASH_POTION && item != Items.LINGERING_POTION) return false;

                    var tank = container.getBottle().tank;
                    return  tank.getFluidAmount() + DrinkingRecipe.DEFAULT_AMOUNT <= tank.getCapacity();
                }

                @Override
                public ItemStack assemble(BottleInteractionContainer container) {
                    var stack = container.getItem(0);
                    var item = stack.getItem();

                    var potion = PotionUtils.getPotion(stack);
                    var color = PotionUtils.getColor(stack);
                    var customEffects = PotionUtils.getCustomEffects(stack);

                    var voidItem = ItemStack.EMPTY.copy();
                    voidItem = PotionUtils.setPotion(voidItem, potion);
                    voidItem = PotionUtils.setCustomEffects(voidItem, customEffects);

                    if (stack.getOrCreateTag().contains(PotionUtils.TAG_CUSTOM_POTION_COLOR)) {
                        var tag = voidItem.getTag();
                        tag.putInt(PotionUtils.TAG_CUSTOM_POTION_COLOR, color);
                    }

                    var fluid = new FluidStack(ModFluids.POTION.get(), DrinkingRecipe.DEFAULT_AMOUNT, voidItem.getTag());
                    container.getBottle().tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
                    return new ItemStack(Items.GLASS_BOTTLE);
                }

                @Override
                public String getId() {
                    return "recicropal:potion";
                }
            });
        }

        @Override
        public FillingRecipe fromJson(ResourceLocation rl, JsonObject jsonObject) {
            if (jsonObject.has("process"))
                return new FillingRecipe(rl,
                                         Ingredient.EMPTY,
                                         null,
                                         fauxRegister.get(GsonHelper.getAsString(jsonObject, "process")),
                                         ItemStack.EMPTY);

            var ingredient = Ingredient.fromJson(jsonObject.get("ingredient"));
            var provider = FluidStackProvider.fromJson(jsonObject.getAsJsonObject("fluid"));
            var result = ItemStack.CODEC.decode(JsonOps.INSTANCE, jsonObject.get("result"))
                    .result().orElseThrow().getFirst();
            return new FillingRecipe(rl, ingredient, provider, null, result);
        }

        @Override
        public @Nullable FillingRecipe fromNetwork(ResourceLocation rl, FriendlyByteBuf buf) {
            if (buf.readBoolean())
                return new FillingRecipe(rl,
                                         Ingredient.EMPTY,
                                         null,
                                         fauxRegister.get(buf.readUtf()),
                                         ItemStack.EMPTY);

            var ingredient = Ingredient.fromNetwork(buf);
            var provider = FluidStackProvider.fromNetwork(buf);
            var result = buf.readItem();
            return new FillingRecipe(rl, ingredient, provider, null, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, FillingRecipe recipe) {
            if (recipe.process != null) {
                buf.writeBoolean(true);
                buf.writeUtf(recipe.process.getId());
                return;
            }
            recipe.ingredient.toNetwork(buf);
            buf.writeBoolean(false);
            recipe.resultProvider.toNetwork(buf);
            buf.writeItem(recipe.result);
        }
    }

    @Override @Deprecated
    public boolean canCraftInDimensions(int x, int y) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return result.copy();
    }
}
