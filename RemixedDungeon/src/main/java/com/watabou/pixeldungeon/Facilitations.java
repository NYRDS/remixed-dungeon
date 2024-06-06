package com.watabou.pixeldungeon;

import java.util.ArrayList;
import org.apache.commons.collections4.map.HashedMap;
import java.util.Map;
import java.util.Objects;


public class Facilitations {
    public static final int NO_HUNGER = (int)Math.pow(2,16);
    public static final int FAST_REGENERATION = (int)Math.pow(2,16+1);
    public static final int FAST_MANA_REGENERATION = (int)Math.pow(2,16+2);
    public static final int SUPER_STRENGTH = (int)Math.pow(2,16+3);

    public static final int[] MASKS = {NO_HUNGER, FAST_REGENERATION, FAST_MANA_REGENERATION, SUPER_STRENGTH};

    public static final Map<Integer, ArrayList<Integer>> conflictingChallenges = new HashedMap<>();


    static {
        for(Integer mask:MASKS) {
            conflictingChallenges.put(mask, new ArrayList<>());
        }

        Objects.requireNonNull(conflictingChallenges.get(NO_HUNGER)).add(Challenges.NO_FOOD);
    }

}
