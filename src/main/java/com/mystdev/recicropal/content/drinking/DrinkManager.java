package com.mystdev.recicropal.content.drinking;

import com.mystdev.recicropal.ModRecipes;
import com.mystdev.recicropal.common.Config;
import com.mystdev.recicropal.content.drinking.capability.DrinkContext;
import com.mystdev.recicropal.content.drinking.capability.IDrinkHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;

public class DrinkManager {

    public static Optional<DrinkingRecipe> hasDrink(ItemStack stack, Level level) {
        var rm = level.getRecipeManager();
        return rm.getRecipeFor(ModRecipes.DRINKING_RECIPE.get(), new FluidHandlerItemContainer(stack), level);
    }

    public static boolean canDrink(Player player) {
        var opt = getDrinkHandler(player);
        return opt.isPresent() && opt.orElseThrow(IllegalArgumentException::new).getContext() == null;
    }

    public static boolean wasDrinking(Player player) {
        var opt = getDrinkHandler(player);
        return opt.isPresent() && opt.orElseThrow(IllegalArgumentException::new).getContext() != null;
    }

    public static boolean tryDrinking(Player player, Level level, ItemStack stack, InteractionHand usedHand) {
        if (!Config.ENABLE_DRINKING.get()) return false;
        if (!canDrink(player)) return false;

        var drinkRecipe = hasDrink(stack, level);
        if (drinkRecipe.isEmpty()) return false;

        player.startUsingItem(usedHand);

        if (!level.isClientSide)
            getDrinkHandler(player).ifPresent(handler -> handler
                    .setContext(new DrinkContext(player, level, stack, drinkRecipe.get())));
        return true;
    }

    public static void finishDrinking(Player player) {
        getDrinkHandler(player).ifPresent(handler -> {
            var ctx = handler.getContext();
            var recipe = ctx.recipe();
            recipe.assemble(ctx);
            // Reset
            handler.setContext(null);
        });
    }

    public static void resetDrinking(Player player) {
        getDrinkHandler(player).ifPresent(handler -> handler.setContext(null));
    }

    public static LazyOptional<IDrinkHandler> getDrinkHandler(Player player) {
        return player.getCapability(IDrinkHandler.CAPABILITY);
    }

}