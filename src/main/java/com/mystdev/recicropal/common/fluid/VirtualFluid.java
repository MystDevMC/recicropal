package com.mystdev.recicropal.common.fluid;

import com.mystdev.recicropal.Recicropal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import java.text.MessageFormat;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

// Kinda inspired by the Creators of Create
// Trying to do this without Registrate but still not sure if
// this is how it supposed to work...
public class VirtualFluid extends ForgeFlowingFluid {
    public VirtualFluid(VirtualFluidType type, Supplier<? extends VirtualFluid> still) {
        super(new VirtualFluidProperties(type, still));
    }

    @Override
    public boolean isSource(FluidState state) {
        return true;
    }

    @Override
    public Fluid getFlowing() {
        return super.getFlowing();
    }

    @Override
    public Fluid getSource() {
        return this;
    }

    @Override
    public int getAmount(FluidState state) {
        return 0;
    }

    @Override
    public Item getBucket() {
        return Items.AIR;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return Blocks.AIR.defaultBlockState();
    }

    public static class VirtualFluidProperties extends Properties {
        public VirtualFluidProperties(VirtualFluidType type, Supplier<? extends VirtualFluid> still) {
            super(() -> type, still, () -> Fluids.EMPTY); // Can this even be EMPTY? Better than null I guess.
        }
    }

    public static class VirtualFluidType extends FluidType {
        private static final Function<VirtualFluidType, ResourceLocation> TEXTURE_FUNCTION =
                (fluid) -> Recicropal.rl("fluid/" + fluid.name);
        private final String name;

        public VirtualFluidType(String name) {
            super(Properties.create().descriptionId(MessageFormat.format("fluid.{0}.{1}", Recicropal.MOD_ID, name)));
            this.name = name;
        }

        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(this.getExtensions());
        }

        protected IClientFluidTypeExtensions getExtensions() {
            return (Extensions) () -> VirtualFluidType.this;
        }

        protected interface Extensions extends IClientFluidTypeExtensions {
            VirtualFluidType getType();

            @Override
            default ResourceLocation getFlowingTexture() {
                return TEXTURE_FUNCTION.apply(getType());
            }

            @Override
            default ResourceLocation getStillTexture() {
                return TEXTURE_FUNCTION.apply(getType());
            }
        }
    }
}
