
package com.watabou.pixeldungeon.actors.mobs;

public class Shielded extends Brute {

	{
		baseDefenseSkill = 20;
	}
	
	@Override
	public int dr() {
		return 10;
	}
}
