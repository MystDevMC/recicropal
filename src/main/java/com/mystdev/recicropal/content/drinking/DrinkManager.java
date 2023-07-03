package com.mystdev.recicropal.content.drinking;

import com.mystdev.recicropal.ModRecipes;
import com.mystdev.recicropal.common.fluid.ModFluidUtils;
import com.mystdev.recicropal.content.drinking.capability.DrinkContext;
import com.mystdev.recicropal.content.drinking.capability.IDrinkHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

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
        if (!canDrink(player)) return false;

        var drinkRecipe = hasDrink(stack, level);
        if (drinkRecipe.isEmpty()) return false;

        player.startUsingItem(usedHand);

        if (!level.isClientSide)
            getDrinkHandler(player).ifPresent(handler -> handler
                    .setContext(new DrinkContext(player, level, stack, drinkRecipe.get())));
        return true;
    }

    public static void finishDrinking(Player player, InteractionHand hand) {
        getDrinkHandler(player).ifPresent(handler -> {
            var ctx = handler.getContext();
            var recipe = ctx.recipe();

            // Assuming that it has already matched
            // This replaces the `assemble` call
            // Drink the liquid
            var wrappedInventory = new PlayerInvWrapper(player.getInventory());

            var fluidRes =
                    FluidUtil.tryEmptyContainerAndStow(
                            ctx.stack(), ModFluidUtils.voidTank(),
                            wrappedInventory, recipe.ingredient.getAmount(), player, true
                    );

            // Return the new stack to player
            player.setItemInHand(hand, fluidRes.result);

            // Apply post-drinking effects
            recipe.results.forEach(res -> res.apply(player, ctx.level(), recipe.getDrunk(ctx)));

            // Reset
            handler.setContext(null);
        });
    }

    public static void resetDrinking(Player player) {
        getDrinkHandler(player).ifPresent(handler -> {
            var ctx = handler.getContext();
            handler.setContext(null);
        });
    }

    public static LazyOptional<IDrinkHandler> getDrinkHandler(Player player) {
        return player.getCapability(IDrinkHandler.CAPABILITY);
    }

}