package com.mystdev.recicropal.content.mixing;

import com.mystdev.recicropal.ModFluids;
import com.mystdev.recicropal.common.fluid.ModFluidUtils;
import com.mystdev.recicropal.common.fluid.VirtualFluid;
import com.mystdev.recicropal.content.drinking.DrinkingRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

public class PotionFluid extends VirtualFluid {
    public static final String NAME = "potion";
    public static final TagKey<Fluid> POTION_TAG = ModFluidUtils.tag("forge:potion");

    public PotionFluid() {
        super(new PotionFluidType(), ModFluids.POTION);
    }

    public static ItemStack extractPotionFrom(FluidStack potionFluid) {
        var stack = new ItemStack(Items.POTION);

        var fluidTag = potionFluid.getTag().copy();

        if (fluidTag.contains(MixturePart.TAG_MODIFIER)) {
            var modifierTag = Mixture.Modifier.from(fluidTag.getString(MixturePart.TAG_MODIFIER));
            if (modifierTag == Mixture.Modifier.LINGERING) {
                stack = new ItemStack(Items.LINGERING_POTION);
            }
            else if (modifierTag == Mixture.Modifier.SPLASH) {
                stack = new ItemStack(Items.SPLASH_POTION);
            }
            fluidTag.remove(MixturePart.TAG_MODIFIER);
        }

        stack.setTag(fluidTag);
        return stack;
    }

    public static boolean isPotion(FluidStack fluidStack) {
        return isPotion(fluidStack.getFluid());
    }

    public static boolean isPotion(Fluid fluid) {
        return fluid.is(POTION_TAG);
    }

    public static FluidStack fluidFrom(ItemStack stack) {
        var potion = PotionUtils.getPotion(stack);
        var color = PotionUtils.getColor(stack);
        var customEffects = PotionUtils.getCustomEffects(stack);

        var voidItem = new ItemStack(Items.POTION);
        PotionUtils.setPotion(voidItem, potion);
        PotionUtils.setCustomEffects(voidItem, customEffects);

        var tag = voidItem.getOrCreateTag();
        var modifier = Mixture.Modifier.from(stack);
        tag.putString(MixturePart.TAG_MODIFIER, modifier.getSerializedName());

        if (stack.getOrCreateTag().contains(PotionUtils.TAG_CUSTOM_POTION_COLOR)) {
            tag.putInt(PotionUtils.TAG_CUSTOM_POTION_COLOR, color);
        }

        var fluid = new FluidStack(ModFluidUtils.forcedMember(POTION_TAG),
                                   DrinkingRecipe.configuredMaxAmount(),
                                   voidItem.getTag());

        if (potion == Potions.WATER && modifier == Mixture.Modifier.NORMAL) {
            fluid = new FluidStack(Fluids.WATER, DrinkingRecipe.configuredMaxAmount());
        }
        return fluid;
    }

    public static class PotionFluidType extends VirtualFluidType {

        public PotionFluidType() {
            super(NAME);
        }

        @Override
        public Component getDescription(FluidStack stack) {
            var item = PotionFluid.extractPotionFrom(stack);
            var potion = PotionUtils.getPotion(item);
            var color = PotionUtils.getColor(potion);
            return Component
                    .translatable(item.getDescriptionId())
                    .withStyle(s -> s.withColor(color));
        }

        @Override
        protected IClientFluidTypeExtensions getExtensions() {
            return new Extensions() {
                @Override
                public VirtualFluidType getType() {
                    return PotionFluidType.this;
                }

                @Override
                public int getTintColor(FluidStack stack) {
                    var potionItem = new ItemStack(Items.POTION);
                    potionItem.setTag(stack.getOrCreateTag());
                    return PotionUtils.getColor(potionItem) | 0xff000000; // ORed with alpha. Thanks Create!
                }
            };
        }
    }
}
