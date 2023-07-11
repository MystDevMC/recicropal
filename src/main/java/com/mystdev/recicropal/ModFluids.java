package com.mystdev.recicropal;

import com.mystdev.recicropal.common.fluid.VirtualFluid;
import com.mystdev.recicropal.content.mixing.MixtureFluid;
import com.mystdev.recicropal.content.mixing.PotionFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModFluids {

    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS,
                                                                                 Recicropal.MOD_ID);
    public static final RegistryObject<VirtualFluid> MILK = virtualFluid("milk");
    public static final RegistryObject<VirtualFluid> HONEY = virtualFluid("honey");

    public static void init(IEventBus modBus) {
        FLUIDS.register(modBus);
    }

    private static RegistryObject<VirtualFluid> virtualFluid(String name) {
        Supplier<? extends VirtualFluid> lazyFluid = () -> (VirtualFluid) ForgeRegistries.FLUIDS.getValue(Recicropal.rl(
                name));
        return FLUIDS.register(name, () -> new VirtualFluid(new VirtualFluid.VirtualFluidType(name), lazyFluid));
    }

    public static final RegistryObject<VirtualFluid> MIXTURE = FLUIDS.register(MixtureFluid.NAME, MixtureFluid::new);


    public static final RegistryObject<VirtualFluid> POTION = FLUIDS.register(PotionFluid.NAME, PotionFluid::new);


}
