package com.mystdev.recicropal.content.drinking.result;

import com.mystdev.recicropal.content.mixing.Mixture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

public class MixtureDrinkResult implements IDrinkResult {
    @Override
    public void apply(Player player, Level level, FluidStack drunkStack) {
        if (Mixture.isMixture(drunkStack)) {
            var mixture = Mixture.fromFluid(drunkStack);
            var allEffects = mixture.getRationedEffects(drunkStack.getAmount());
            var potionItem = new ItemStack(Items.POTION);
            PotionUtils.setCustomEffects(potionItem, allEffects);
            potionItem.finishUsingItem(level, player);
        }
    }

    @Override
    public DrinkResultType<? extends IDrinkResult> getType() {
        return DrinkResults.MIXTURE.get();
    }

}
