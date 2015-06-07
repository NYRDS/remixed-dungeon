package com.watabou.pixeldungeon.actors.mobs;

import java.util.HashSet;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.blobs.Freezing;
import com.watabou.pixeldungeon.actors.blobs.Regrowth;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.potions.PotionOfFrost;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.EarthElementalSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class EarthElemental extends Mob {

	public EarthElemental() {
		spriteClass = EarthElementalSprite.class;

		adjustLevel(Dungeon.depth);

		loot = new PotionOfFrost();
		lootChance = 0.1f;
	}

	private void adjustLevel(int depth) {
		hp(ht(depth * 10));
		defenseSkill = depth * 2;
		EXP = depth;
		maxLvl = depth + 2;

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
		return 10;
	}

	@Override
	public int attackProc(Char enemy, int damage) {

		int cell = enemy.pos;

		if (Random.Int(2) == 0) {
			int c = Dungeon.level.map[cell];
			if (c == Terrain.EMPTY || c == Terrain.EMBERS
					|| c == Terrain.EMPTY_DECO || c == Terrain.GRASS
					|| c == Terrain.HIGH_GRASS) {
				
				GameScene.add(Blob.seed(cell, (EXP + 2) * 20, Regrowth.class));
			}
		}
		return damage;
	}

	@Override
	public boolean act() {

		return super.act();
	}

	private static final HashSet<Class<?>> IMMUNITIES = new HashSet<Class<?>>();
	static {
		IMMUNITIES.add(Roots.class);
		IMMUNITIES.add(Paralysis.class);
		IMMUNITIES.add(ToxicGas.class);
		IMMUNITIES.add(Fire.class);
	}

	@Override
	public HashSet<Class<?>> immunities() {
		return IMMUNITIES;
	}
}
