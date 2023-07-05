package com.mystdev.recicropal.content.crop.bottle_gourd;

import com.google.common.base.Suppliers;
import com.mystdev.recicropal.ModRecipes;
import com.mystdev.recicropal.content.mixing.MixingContainer;
import com.mystdev.recicropal.content.mixing.MixingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.Optional;
import java.util.function.Supplier;

// This creates a rather high coupling.
// But where else I would use this thing anyway
public class BottleGourdTank extends FluidTank {
    public static final int CAPACITY = 2000;
    private final Supplier<Level> lazyLevel;

    public BottleGourdTank(BottleGourdBlockEntity bottle) {
        super(CAPACITY);
        lazyLevel = Suppliers.memoize(bottle::getLevel);
    }

    private Optional<MixingRecipe> getMixingRecipe(MixingContainer container) {
        return lazyLevel
                .get()
                .getRecipeManager()
                .getRecipeFor(ModRecipes.MIXING_RECIPE.get(), container, lazyLevel.get());
    }
    @Override
    public int fill(FluidStack resource, FluidAction action) {
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
                fluid = result;
                filled = (resAmount - fluid.getAmount());
            }
            else {
                fluid = new FluidStack(result.getFluid(), capacity, result.getTag());
            }
            if (filled > 0)
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
}
