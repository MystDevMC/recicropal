package com.mystdev.recicropal.content.drinking;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record DrinkContext(Player player, Level level, ItemStack stack, DrinkRecipe recipe) {}
