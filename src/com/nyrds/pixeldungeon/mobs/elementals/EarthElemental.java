package com.nyrds.pixeldungeon.mobs.elementals;

import com.nyrds.pixeldungeon.mobs.elementals.sprites.EarthElementalSprite;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.blobs.Regrowth;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.plants.Earthroot;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

public class EarthElemental extends Mob {
	public EarthElemental() {
		spriteClass = EarthElementalSprite.class;

		adjustLevel(Dungeon.depth);

		loot = new Earthroot.Seed();
		lootChance = 0.1f;
	}

	private void adjustLevel(int depth) {
		hp(ht(depth * 10 + 1));
		defenseSkill = depth * 2 + 1;
		EXP = depth + 1;
		maxLvl = depth + 2;
		
		IMMUNITIES.add(Roots.class);
		IMMUNITIES.add(Paralysis.class);
		IMMUNITIES.add(ToxicGas.class);
		IMMUNITIES.add(Fire.class);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(hp() / 5, ht() / 5);
	}

	@Override
	public int attackSkill(Char target) {
		return defenseSkill / 2;
	}

	@Override
	public int dr() {
		return EXP;
	}

	@Override
	public int attackProc(Char enemy, int damage) {

		int cell = enemy.pos;

		if (Random.Int(2) == 0) {
			int c = Dungeon.level.map[cell];
			if (c == Terrain.EMPTY || c == Terrain.EMBERS
					|| c == Terrain.EMPTY_DECO || c == Terrain.GRASS
					|| c == Terrain.HIGH_GRASS) {
				
				GameScene.add(Blob.seed(cell, EXP * 15, Regrowth.class));
			}
		}
		return damage;
	}
}
