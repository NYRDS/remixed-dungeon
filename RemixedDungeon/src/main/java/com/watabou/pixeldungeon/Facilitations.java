package com.watabou.pixeldungeon;

import java.io.OptionalDataException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.nyrds.pixeldungeon.items.Treasury;
import com.watabou.pixeldungeon.items.DewVial;
import com.watabou.pixeldungeon.items.Dewdrop;
import com.watabou.pixeldungeon.items.Stylus;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;


public class Facilitations {
    public static final int NO_HUNGER = (int)Math.pow(2,16);
    public static final int FAST_REGENERATION = (int)Math.pow(2,16+1);
    public static final int FAST_MANA_REGENERATION = (int)Math.pow(2,16+2);
    public static final int SUPER_STRENGTH = (int)Math.pow(2,16+3);

    public static final int[] MASKS = {NO_HUNGER, FAST_REGENERATION, FAST_MANA_REGENERATION, SUPER_STRENGTH};

    public static final Map<Integer, ArrayList<Integer>> conflictingChallenges = new HashMap<>();


    static {
        for(Integer mask:MASKS) {
            conflictingChallenges.put(mask, new ArrayList<>());
        }

        Objects.requireNonNull(conflictingChallenges.get(NO_HUNGER)).add(Challenges.NO_FOOD);
    }

}
