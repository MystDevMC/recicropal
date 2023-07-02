package com.mystdev.recicropal.content.drinking.result;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Make sure that the readers modify {@code this}
 * @param <T>
 */
public interface ISerializableDrinkResult<T extends ISerializableDrinkResult<T>> extends IDrinkResult {

    T readJson(JsonObject jsonObject);

    @org.jetbrains.annotations.Nullable
    T readNetwork(FriendlyByteBuf buf);

    void writeToNetwork(FriendlyByteBuf buf);
}
