package com.mystdev.recicropal.content.drinking.result;

import com.mystdev.recicropal.content.drinking.DrinkManager;
import com.mystdev.recicropal.content.mixing.Mixture;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public class MixtureDrinkResult implements IDrinkResult {
    @Override
    public void apply(Player player, Level level, FluidStack drunkStack) {
        if (Mixture.isMixture(drunkStack)) {
            var mixture = Mixture.fromFluid(drunkStack);
            var eithers = mixture.getRationedEffects(drunkStack.getAmount());
            var allEffects = new ArrayList<MobEffectInstance>();
            eithers.forEach(either -> {
                var longEffect = either.right();
                var instantEffect = either.left();
                if (longEffect.isPresent()) allEffects.add(longEffect.get());
                else {
                    instantEffect.ifPresent(effectEntry -> DrinkManager
                            .getDrinkHandler(player)
                            .ifPresent(handler -> handler
                                    .getEffectWaiter()
                                    .put(effectEntry, drunkStack.getAmount(), player)));
                }
            });
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
