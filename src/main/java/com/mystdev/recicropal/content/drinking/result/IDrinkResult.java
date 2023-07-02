package com.mystdev.recicropal.content.drinking.result;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Supplier;

public interface IDrinkResult {
    void apply(Player player, Level level, FluidStack drunkStack);

    Supplier<DrinkResultType<? extends IDrinkResult>> getType();
}
