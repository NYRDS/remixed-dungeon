package com.watabou.pixeldungeon.items.wands;

import com.nyrds.LuaInterface;
import com.watabou.utils.Random;
import lombok.SneakyThrows;

public abstract class SimpleWand extends Wand {
	
	@SuppressWarnings("rawtypes")
	private static final Class[] variants = {	WandOfAmok.class,
		WandOfAvalanche.class, 
		WandOfDisintegration.class, 
		WandOfFirebolt.class, 
		WandOfLightning.class,
		WandOfPoison.class, 
		WandOfRegrowth.class, 
		WandOfSlowness.class};

	@SneakyThrows
	@LuaInterface
	static public SimpleWand createRandomSimpleWand() {
		return (SimpleWand) Random.element(variants).newInstance();
	}
	
	
}
