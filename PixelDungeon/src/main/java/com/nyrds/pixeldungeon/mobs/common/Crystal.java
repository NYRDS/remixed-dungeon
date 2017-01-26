package com.nyrds.pixeldungeon.mobs.common;

import android.support.annotation.NonNull;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Darkness;
import com.watabou.pixeldungeon.actors.blobs.Foliage;
import com.watabou.pixeldungeon.actors.blobs.ParalyticGas;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Weakness;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.items.wands.SimpleWand;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

public class Crystal extends MultiKindMob implements IDepthAdjustable{

	static private int ctr = 0;

	{
		movable = false;
	}

	public Crystal() {
		adjustStats(Dungeon.depth);

		loot = SimpleWand.createRandomSimpleWand();
		((Wand) loot).upgrade(Dungeon.depth);

		lootChance = 0.25f;
	}

	static public Crystal makeShadowLordCrystal() {
		Crystal crystal = new Crystal();
		crystal.kind = 2;
		crystal.lootChance = 0;

		return crystal;
	}

	public void adjustStats(int depth) {
		kind = (ctr++) % 2;

		hp(ht(Dungeon.depth * 4 + 1));
		defenseSkill = depth * 2 + 1;
		EXP = depth + 1;
		maxLvl = depth + 2;

		IMMUNITIES.add(ScrollOfPsionicBlast.class);
		IMMUNITIES.add(ToxicGas.class);
		IMMUNITIES.add(ParalyticGas.class);
	}

	@Override
	public int getKind() {
		return kind;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(hp() / 2, ht() / 2);
	}

	@Override
	protected boolean canAttack(Char enemy) {
		return Ballistica.cast(getPos(), enemy.getPos(), false, true) == enemy.getPos();
	}

	@Override
	public int attackSkill(Char target) {
		if (kind < 2) {
			return 1000;
		} else {
			return 35;
		}
	}

	@Override
	public int dr() {
		return EXP / 3;
	}

	@Override
	public int attackProc(@NonNull final Char enemy, int damage) {

		if (kind < 2) {
			final Wand wand = ((Wand) loot);

			wand.mobWandUse(this, enemy.getPos());

			return 0;
		} else {
			getSprite().zap(enemy.getPos());
			if (enemy == Dungeon.hero && Random.Int(2) == 0) {
				Buff.prolong(enemy, Weakness.class, Weakness.duration(enemy));
			}
			return damage;
		}
	}

	@Override
	protected boolean getCloser(int target) {
		return false;
	}

	@Override
	protected boolean getFurther(int target) {
		return false;
	}

	@Override
	public void die(Object cause) {
		int pos = getPos();

		if (Dungeon.level.map[pos] == Terrain.PEDESTAL) {
			Dungeon.level.set(pos, Terrain.EMBERS);
			int x, y;
			x = Dungeon.level.cellX(pos);
			y = Dungeon.level.cellY(pos);

			Dungeon.level.clearAreaFrom(Darkness.class, x - 2, y - 2, 5, 5);
			Dungeon.level.fillAreaWith(Foliage.class, x - 2, y - 2, 5, 5, 1);

			GameScene.updateMap();
		}
		super.die(cause);
	}

	@Override
	public boolean canBePet() {
		return false;
	}
}
