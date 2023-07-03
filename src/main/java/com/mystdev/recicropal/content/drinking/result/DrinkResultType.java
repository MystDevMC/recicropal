package com.mystdev.recicropal.content.drinking.result;

import java.util.function.Supplier;

public record DrinkResultType<T extends IDrinkResult>(Supplier<T> drinkResultFactory) {
}
