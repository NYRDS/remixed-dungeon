package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.pixeldungeon.levels.com.nyrds.pixeldungeon.levels.Tools;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Darkness;
import com.watabou.pixeldungeon.actors.blobs.Foliage;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Shadow;
import com.watabou.pixeldungeon.actors.mobs.Wraith;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 13.02.2016
 */
public class ShadowLord extends Boss {

	private int cooldown = -1;

	public ShadowLord() {
		hp(ht(260));
		defenseSkill = 34;

		EXP = 56;

		lootChance = 0.5f;
	}

	@Override
	public boolean isAbsoluteWalker() {
		return true;
	}

	public void spawnShadow() {
		int cell = Dungeon.level.getSolidCellNextTo(getPos());

		if (cell != -1) {
			Mob mob = new Shadow();

			mob.state = mob.WANDERING;
			Dungeon.level.spawnMob(mob, 1);

			WandOfBlink.appear(mob, cell);
		}
	}

	public void spawnWraith() {
		for (int i = 0; i < 4; i++) {
			int cell = Dungeon.level.getEmptyCellNextTo(getPos());

			if (cell != -1) {
				Mob mob = new Wraith();

				mob.state = mob.WANDERING;
				Dungeon.level.spawnMob(mob, 1);
				WandOfBlink.appear(mob, cell);
			}
		}
	}

	public void twistLevel() {
		Tools.buildShadowLordMaze(Dungeon.level, 6);

		int cell = Dungeon.level.getRandomTerrainCell(Terrain.PEDESTAL);
		if (Dungeon.level.cellValid(cell)) {
			if (Actor.findChar(cell) == null) {
				Mob mob = Crystal.makeShadowLordCrystal();
				Dungeon.level.spawnMob(mob);
				WandOfBlink.appear(mob, cell);

				int x, y;
				x = Dungeon.level.cellX(cell);
				y = Dungeon.level.cellY(cell);

				Dungeon.level.fillAreaWith(Darkness.class, x - 2, y - 2, 5, 5, 1);
			} else {
				damage(ht() / 9, this);
			}
		}
	}

	@Override
	protected boolean canAttack(Char enemy) {
		return Dungeon.level.distance(getPos(), enemy.getPos()) < 4 && Ballistica.cast(getPos(), enemy.getPos(), false, true) == enemy.getPos();
	}

	@Override
	protected boolean doAttack(Char enemy) {

		if (Dungeon.level.distance(getPos(), enemy.getPos()) <= 1) {
			return super.doAttack(enemy);
		} else {

			getSprite().zap(enemy.getPos());

			spend(1);

			if (hit(this, enemy, true)) {
				enemy.damage(damageRoll(), this);
			}
			return true;
		}
	}

	private void blink(int epos) {

		if (Dungeon.level.distance(getPos(), epos) == 1) {
			int y = Dungeon.level.cellX(getPos());
			int x = Dungeon.level.cellY(getPos());

			int ey = Dungeon.level.cellX(epos);
			int ex = Dungeon.level.cellY(epos);

			int dx = x - ex;
			int dy = y - ey;

			x += 2 * dx;
			y += 2 * dy;

			final int tgt = Dungeon.level.cell(x, y);
			if (Dungeon.level.cellValid(tgt)) {
				final Char ch = this;
				fx(getPos(), new Callback() {
					@Override
					public void call() {
						WandOfBlink.appear(ch, tgt);
					}
				});
			}
		}
	}

	protected void fx(int cell, Callback callback) {
		MagicMissile.purpleLight(getSprite().getParent(), getPos(), cell, callback);
		Sample.INSTANCE.play(Assets.SND_ZAP);
		getSprite().setVisible(false);
	}

	@Override
	public void damage(int dmg, Object src) {
		super.damage(dmg, src);
		if (src != this) {
			if (dmg > 0 && cooldown < 0) {
				state = FLEEING;
				if (src instanceof Char) {
					blink(((Char) src).getPos());
				}
				twistLevel();
				cooldown = 10;
			}
		}
	}

	@Override
	protected boolean act() {
		if (state == FLEEING) {
			cooldown--;
			if (cooldown < 0) {
				state = WANDERING;
				if (Math.random() < 0.7) {
					//spawnWraith();
				} else {
					//spawnShadow();
				}

				yell("Prepare yourself!");
			}
		}

		if (Dungeon.level.blobAmoutAt(Darkness.class, getPos()) > 0 && hp() < ht()) {
			getSprite().emitter().burst(Speck.factory(Speck.HEALING), 1);
			hp(Math.min(hp() + (ht() - hp()) / 4, ht()));
		}

		if (Dungeon.level.blobAmoutAt(Foliage.class, getPos()) > 0) {
			getSprite().emitter().burst(Speck.factory(Speck.BONE), 1);
			damage(1, this);
		}

		return super.act();
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(1, 2);
	}

	@Override
	public int attackSkill(Char target) {
		return 1;
	}

	@Override
	public int dr() {
		return 2;
	}

	@Override
	public void die(Object cause) {
		super.die(cause);
		Tools.makeEmptyLevel(Dungeon.level);
	}
}
