package com.mystdev.recicropal.content.crop.bottle_gourd;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BottleGourdBlockEntity extends BlockEntity {
    public BottleGourdTank tank = new BottleGourdTank(this);
    private final LazyOptional<IFluidHandler> lazyTank = LazyOptional.of(() -> this.tank);

    public BottleGourdBlockEntity(BlockEntityType type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        var res = new CompoundTag();
        tank.writeToNBT(res);
        tag.put("Fluid", res);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.tank = new BottleGourdTank(this);
        this.tank.readFromNBT(tag.getCompound("Fluid"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        var res = new CompoundTag();
        res.put("Fluid", this.tank.writeToNBT(new CompoundTag()));
        return res;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // Will get tag from #getUpdateTag
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER
                && (side == null || side == Direction.UP || side == Direction.DOWN)) {
            return this.lazyTank.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.lazyTank.invalidate();
    }

    public void updateTank(IFluidHandlerItem cap) {
        this.tank.setFluid(cap.getFluidInTank(0));
    }
}
