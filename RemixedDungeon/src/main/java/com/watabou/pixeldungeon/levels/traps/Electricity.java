package com.watabou.pixeldungeon.levels.traps;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;

/**
 * Death-cause marker for electric damage (lightning traps, wands).
 * Extracted from LightningTrap so the trap trigger can live in lua
 * while the cause name stays stable for reports and immunities.
 */
@LuaInterface
public class Electricity implements NamedEntityKind {

	public static final NamedEntityKind INSTANCE = new Electricity();

	@Override
	public String getEntityKind() {
		return getClass().getSimpleName();
	}

	@Override
	public String name() {
		return getEntityKind();
	}
}
