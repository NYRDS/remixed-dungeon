package com.watabou.pixeldungeon.levels;

import com.nyrds.pixeldungeon.levels.objects.Trap;
import com.nyrds.platform.EventCollector;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.utils.Random;

import java.util.HashMap;
import java.util.Map;

abstract public class CommonLevel extends Level {

	abstract protected int nTraps();

	public static Map<String, Float> traps = new HashMap<>();
	static {
		traps.put("ToxicTrap",1f);
		traps.put("FireTrap",1f);
		traps.put("AlarmTrap",1f);
		traps.put("GrippingTrap",1f);
		traps.put("PoisonTrap",1f);
		traps.put("ParalyticTrap",1f);
		traps.put("SummoningTrap",1f);
		traps.put("LightningTrap",1f);
	}

	protected void placeTraps() {
		int nTraps = nTraps();

		for (int i=0; i < nTraps; i++) {
			int trapPos = Random.Int( getLength() );
			
			if (map[trapPos] == Terrain.EMPTY) {
				addLevelObject(Trap.makeSimpleTrap(trapPos, Random.chances(traps), true));
			}
		}
	}

	@Override
	protected void createMobs() {
		int nMobs = nMobs();
		for (int i = 0; i < nMobs; i++) {
			Mob mob = createMob();

			if(cellValid(mob.getPos())) {
				mobs.add(mob);
				Actor.occupyCell(mob);
				continue;
			}

			EventCollector.logException("trying to spawn " + mob.getEntityKind() + " on invalid cell " + mob.getPos());
		}
	}
}
