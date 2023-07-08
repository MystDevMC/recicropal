package com.mystdev.recicropal.content.mixing;

import com.mystdev.recicropal.ModFluids;
import com.mystdev.recicropal.common.fluid.VirtualFluid;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

public class MixtureFluid extends VirtualFluid {
    public static final String NAME = "mixture";

    public MixtureFluid() {
        super(new MixtureFluidType(), ModFluids.MIXTURE);
    }

    public static class MixtureFluidType extends VirtualFluidType {

        public MixtureFluidType() {
            super(NAME);
        }

        @Override
        public Component getDescription(FluidStack stack) {
            var info = Mixture.getColorAndCategory(stack);
            return Component
                    .translatable(this.getDescriptionId() + "." + info.right().getSerializedName())
                    .withStyle(s -> s.withColor(info.left()));
        }

        @Override
        protected IClientFluidTypeExtensions getExtensions() {
            return new Extensions() {
                @Override
                public VirtualFluidType getType() {
                    return MixtureFluidType.this;
                }

                @Override
                public int getTintColor(FluidStack stack) {
                    return Mixture.getColorAndCategory(stack).left() | 0xff000000; // ORed with alpha. Thanks Create!
                }
            };
        }
    }
}
