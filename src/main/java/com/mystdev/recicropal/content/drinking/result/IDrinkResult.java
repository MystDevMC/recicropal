package com.mystdev.recicropal.content.drinking.result;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

public interface IDrinkResult {
    void apply(Player player, Level level, FluidStack drunkStack);

    DrinkResultType<? extends IDrinkResult> getType();
}
