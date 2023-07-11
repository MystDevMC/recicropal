package com.mystdev.recicropal.content.crop.bottle_gourd;

import com.google.common.base.Suppliers;
import com.mystdev.recicropal.ModRecipes;
import com.mystdev.recicropal.common.Config;
import com.mystdev.recicropal.content.mixing.FluidConversionContainer;
import com.mystdev.recicropal.content.mixing.FluidConversionRecipe;
import com.mystdev.recicropal.content.mixing.MixingContainer;
import com.mystdev.recicropal.content.mixing.MixingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;

// This creates a rather high coupling.
// But where else I would use this thing anyway
public class BottleGourdTank extends FluidTank {
    private final Supplier<Level> lazyLevel;
    private final Runnable bottleUpdater;

    public BottleGourdTank(BottleGourdBlockEntity bottle) {
        super(configuredCapacity());
        this.lazyLevel = Suppliers.memoize(bottle::getLevel);
        this.bottleUpdater = () -> {
            bottle.setChanged();
            lazyLevel.get().sendBlockUpdated(bottle.getBlockPos(),
                                             bottle.getBlockState(),
                                             bottle.getBlockState(),
                                             Block.UPDATE_CLIENTS);
        };
    }

    public static int configuredCapacity() {
        return Config.BOTTLE_CAPACITY.get();
    }

    public static int configuredTransferAmount() {
        return Config.BOTTLE_TRANSFER_AMOUNT.get();
    }

    private Optional<MixingRecipe> getMixingRecipe(MixingContainer container) {
        return lazyLevel
                .get()
                .getRecipeManager()
                .getRecipeFor(ModRecipes.MIXING_RECIPE.get(), container, lazyLevel.get());
    }

    private Optional<FluidConversionRecipe> getConversionRecipe(FluidStack stack, boolean isIngress) {
        return lazyLevel
                .get()
                .getRecipeManager()
                .getRecipeFor(ModRecipes.FLUID_CONVERSION_RECIPE.get(),
                              new FluidConversionContainer(stack, isIngress),
                              lazyLevel.get());
    }

//    @Override
//    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
//        // Conversion on drain
//        var recipeOpt = getConversionRecipe(fluid, false);
//        if (recipeOpt.isEmpty()) {
//            return super.drain(resource, action);
//        } else {
//            var recipe = recipeOpt.get();
//            if (resource.isEmpty() || !resource.isFluidEqual(recipe.convert(fluid))) {
//                return FluidStack.EMPTY;
//            }
//            return recipe.convert(drain(resource.getAmount(), action));
//        }
//    }


//    @Override
//    public @NotNull FluidStack getFluid() {
//        // Conversion on drain
//        var recipeOpt = getConversionRecipe(fluid, false);
//        var hiddenFluid = super.getFluid();
//        if (recipeOpt.isEmpty()) return hiddenFluid;
//        return recipeOpt.get().convert(hiddenFluid);
//    }
//
//    @Override
//    public @NotNull FluidStack getFluidInTank(int tank) {
//        return this.getFluid();
//    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        // Conversion on fill
        var conversionRecipe = getConversionRecipe(resource, true);
        if (conversionRecipe.isPresent()) {
            resource = conversionRecipe.get().convert(resource);
        }

        if (resource.isEmpty() || !isFluidValid(resource)) {
            return 0;
        }
        if (action.simulate()) {
            var container = new MixingContainer(this, resource);
            var recipe = getMixingRecipe(container);
            if (recipe.isPresent()) {
                var resAmount = recipe.get().getResult(container).getAmount();
                return Math.min(capacity, resAmount) - fluid.getAmount();
            }

            if (fluid.isEmpty()) {
                return Math.min(capacity, resource.getAmount());
            }
            if (!fluid.isFluidEqual(resource)) {
                return 0;
            }
            return Math.min(capacity - fluid.getAmount(), resource.getAmount());
        }
        var container = new MixingContainer(this, resource);
        var recipe = getMixingRecipe(container);

        if (recipe.isPresent()) {
            int filled = capacity - fluid.getAmount();
            var result = recipe.get().getResult(container);
            var resAmount = result.getAmount();
            if ((resAmount - fluid.getAmount()) < filled) {
                filled = resAmount - fluid.getAmount();
                fluid = result;
            }
            else {
                fluid.grow(filled);
            }
            onContentsChanged();
            return filled;
        }
        if (fluid.isEmpty()) {
            fluid = new FluidStack(resource, Math.min(capacity, resource.getAmount()));
            onContentsChanged();
            return fluid.getAmount();
        }
        if (!fluid.isFluidEqual(resource)) {
            return 0;
        }
        int filled = capacity - fluid.getAmount();

        if (resource.getAmount() < filled) {
            fluid.grow(resource.getAmount());
            filled = resource.getAmount();
        }
        else {
            fluid.setAmount(capacity);
        }
        if (filled > 0)
            onContentsChanged();
        return filled;
    }

    @Override
    protected void onContentsChanged() {
        bottleUpdater.run();
    }
}
