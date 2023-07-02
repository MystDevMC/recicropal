package com.mystdev.recicropal.content.drinking;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.Objects;

public class DrinkRecipe {
    public final List<IDrinkResult> results;

    public final int amount;

    public DrinkRecipe(List<IDrinkResult> results, int amount) {
        this.results = results;
        this.amount = amount;
    }

    public FluidStack getDrunk() {
        return FluidStack.EMPTY;
    }

    public static final IDrinkResult HEAL = (player, level, drunkStack) ->
            player.addEffect(
                    new MobEffectInstance(MobEffects.HEAL, 1));

    public static final IDrinkResult SET_FIRE = (player, level, drunkStack) ->
            player.setRemainingFireTicks(100);

    public static final IDrinkResult ZAP = (player, level, drunkStack) -> {
        var bolt = Objects.requireNonNull(EntityType.LIGHTNING_BOLT.create(level));
        bolt.setDamage(0);
        bolt.setPos(player.position());
        level.addFreshEntity(bolt);
    };
}
