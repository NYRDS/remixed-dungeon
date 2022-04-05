package com.watabou.pixeldungeon;

import java.io.OptionalDataException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


public class Facilitations {
    public static final int NO_HUNGER = 1;
    public static final int FAST_REGENERATION = 2;
    public static final int FAST_MANA_REGENERATION = 4;
    public static final int SUPER_STRENGTH = 8;

    public static final int[] MASKS = {NO_HUNGER, FAST_REGENERATION, FAST_MANA_REGENERATION, SUPER_STRENGTH};

    public static final Multimap<Integer, Integer> nonCompatibleChallenges = ArrayListMultimap.create();

    static
    {
        nonCompatibleChallenges.put(NO_HUNGER, Challenges.NO_FOOD);
    }

}
