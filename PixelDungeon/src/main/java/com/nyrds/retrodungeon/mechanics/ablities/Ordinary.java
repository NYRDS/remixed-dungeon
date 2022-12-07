package com.nyrds.retrodungeon.mechanics.ablities;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mike on 12.02.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class Ordinary implements Abilities {

	private static final Set<Class<?>> no_immunities = new HashSet<>();

	@Override
	public Set<Class<?>> immunities() {
		return no_immunities;
	}

	public static Ordinary instance = new Ordinary();
}
