package com.nyrds.util;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class SeededRandom extends Random {

    @SafeVarargs
    public final <T> T oneOf(int seed, T @NotNull ... array) {
        setSeed(seed);
        return array[Math.abs(this.nextInt()) % array.length];
    }
}
