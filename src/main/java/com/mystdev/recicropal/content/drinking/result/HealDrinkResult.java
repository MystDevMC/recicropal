package com.mystdev.recicropal.content.drinking.result;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Supplier;

public class HealDrinkResult implements IDrinkResult{
    @Override
    public void apply(Player player, Level level, FluidStack drunkStack) {
        player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1));
    }

    @Override
    public Supplier<DrinkResultType<? extends IDrinkResult>> getType() {
        return DrinkResults.HEAL::get;
    }
}
