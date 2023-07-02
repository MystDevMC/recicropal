package com.mystdev.recicropal.content.drinking;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

@FunctionalInterface
public interface IDrinkResult {
    void apply(Player player, Level level, FluidStack drunkStack);
}
