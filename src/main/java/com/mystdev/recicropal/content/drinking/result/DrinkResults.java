package com.mystdev.recicropal.content.drinking.result;

import com.mystdev.recicropal.Recicropal;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class DrinkResults {
    public static final ResourceKey<Registry<DrinkResultType<?>>> DRINK_RESULT_TYPE_KEY = ResourceKey
            .createRegistryKey(new ResourceLocation("recicropal:drink_result_type"));

    public static final DeferredRegister<DrinkResultType<?>> DRINK_RESULTS = DeferredRegister.createOptional(
            DRINK_RESULT_TYPE_KEY,
            Recicropal.MOD_ID);

    public static final RegistryObject<DrinkResultType<?>> FINISH_ITEM =
            DRINK_RESULTS.register("finish_item", type(FinishItemDrinkResult::new));

    public static final RegistryObject<DrinkResultType<?>> FINISH_ITEM_TRANSFER_NBT =
            DRINK_RESULTS.register("finish_item_transfer_nbt", type(FinishItemTransferNbtDrinkResult::new));

    public static final RegistryObject<DrinkResultType<?>> MIXTURE =
            DRINK_RESULTS.register("mixture", type(MixtureDrinkResult::new));

    // TODO: These are debugs that may be got cleaned up one day
    public static final RegistryObject<DrinkResultType<?>> HEAL =
            DRINK_RESULTS.register("heal", type(HealDrinkResult::new));

    public static Optional<IDrinkResult> get(String s) {
        var type =
                RegistryManager.ACTIVE.getRegistry(DRINK_RESULT_TYPE_KEY).getValue(new ResourceLocation(s));
        if (type == null) return Optional.empty();
        return Optional.of(type.drinkResultFactory().get());
    }

    public static Optional<String> getKey(IDrinkResult drinkResult) {
        var resourceLocation =
                RegistryManager.ACTIVE.getRegistry(DRINK_RESULT_TYPE_KEY).getKey(drinkResult.getType());
        if (resourceLocation == null) return Optional.empty();
        return Optional.of(resourceLocation.toString());
    }

    public static void init(IEventBus modBus) {
        DRINK_RESULTS.register(modBus);
    }

    private static <T extends IDrinkResult> Supplier<DrinkResultType<T>> type(Supplier<T> factory) {
        return () -> new DrinkResultType<>(factory);
    }

    public static final RegistryObject<DrinkResultType<IDrinkResult>> SET_FIRE =
            DRINK_RESULTS.register("set_fire", type(() -> new IDrinkResult() {
                @Override
                public void apply(Player player, Level level, FluidStack drunkStack) {
                    player.setRemainingFireTicks(100);
                }

                @Override
                public DrinkResultType<? extends IDrinkResult> getType() {
                    return SET_FIRE.get();
                }
            }));


    public static final RegistryObject<DrinkResultType<IDrinkResult>> ZAP =
            DRINK_RESULTS.register("zap", type(() -> new IDrinkResult() {
                @Override
                public void apply(Player player, Level level, FluidStack drunkStack) {
                    var bolt = Objects.requireNonNull(EntityType.LIGHTNING_BOLT.create(level));
                    bolt.setDamage(0);
                    bolt.setPos(player.position());
                    level.addFreshEntity(bolt);
                }

                @Override
                public DrinkResultType<? extends IDrinkResult> getType() {
                    return ZAP.get();
                }
            }));


}
