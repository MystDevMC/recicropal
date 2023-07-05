package com.mystdev.recicropal.content.drinking.result;

import com.mystdev.recicropal.ModFluids;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public class MixtureDrinkResult implements IDrinkResult {
    @Override
    public void apply(Player player, Level level, FluidStack drunkStack) {
        if (drunkStack.getFluid() == ModFluids.MIXTURE.get().getSource()) {
            var potions = (ListTag) drunkStack.getOrCreateTag().get("Potions");
            if (potions == null) return;
            var potionItem = new ItemStack(Items.POTION);
            ArrayList<MobEffectInstance> allEffects = new ArrayList<>();
            potions.forEach(tag -> {
                var potion = Potion.byName(tag.getAsString());
                allEffects.addAll(potion.getEffects());
            });
            PotionUtils.setCustomEffects(potionItem, allEffects);
            potionItem.finishUsingItem(level, player);
        }
    }

    @Override
    public DrinkResultType<? extends IDrinkResult> getType() {
        return DrinkResults.MIXTURE.get();
    }

}
