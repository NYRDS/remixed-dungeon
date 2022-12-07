package com.nyrds.retrodungeon.mobs.common;

import com.nyrds.retrodungeon.levels.Tools;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Darkness;
import com.watabou.pixeldungeon.actors.blobs.Foliage;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Shadow;
import com.watabou.pixeldungeon.actors.mobs.WalkingType;
import com.watabou.pixeldungeon.actors.mobs.Wraith;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfWeaponUpgrade;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 13.02.2016
 */
public class ShadowLord extends Boss implements IZapper {

	private boolean levelCreated         = false;
	private int cooldown                 = -1;

	private static final String LEVELCREATED   = "levelCreated";

	private static final String TXT_INTRO = Game.getVar(R.string.ShadowLord_Intro);
	private static final String TXT_DENY = Game.getVar(R.string.ShadowLord_Death);

	public ShadowLord() {
		hp(ht(260));
		defenseSkill = 40;

		exp = 60;

		lootChance = 0.5f;
		loot = new ScrollOfWeaponUpgrade();

		walkingType = WalkingType.ABSOLUTE;
	}

	private void spawnShadow() {
		int cell = Dungeon.level.getSolidCellNextTo(getPos());

		if (cell != -1) {
			Mob mob = new Shadow();

			mob.setState(mob.WANDERING);
			Dungeon.level.spawnMob(mob, 1);

			WandOfBlink.appear(mob, cell);
		}
	}

	private void spawnWraith() {
		for (int i = 0; i < 4; i++) {
			int cell = Dungeon.level.getEmptyCellNextTo(getPos());

			if (cell != -1) {
				Wraith.spawnAt(cell);
			}
		}
	}

	private void twistLevel() {

		if(!isAlive()) {
			return;
		}

		Level level = Dungeon.level;

		if(!levelCreated)
		{
			Tools.makeEmptyLevel(level);
			Tools.buildShadowLordMaze(level, 6);
			levelCreated = true;
		}

		int cell = level.getRandomTerrainCell(Terrain.PEDESTAL);
		if (level.cellValid(cell)) {
			if (Actor.findChar(cell) == null) {
				Mob mob = Crystal.makeShadowLordCrystal();
				mob.setPos(cell);
				level.spawnMob(mob);

				mob.getSprite().alpha( 0 );
				mob.getSprite().getParent().add( new AlphaTweener( mob.getSprite(), 1, 0.4f ) );

				mob.getSprite().emitter().start( Speck.factory( Speck.LIGHT ), 0.2f, 3 );
				Sample.INSTANCE.play( Assets.SND_TELEPORT );

				int x, y;
				x = level.cellX(cell);
				y = level.cellY(cell);

				level.fillAreaWith(Darkness.class, x - 2, y - 2, 5, 5, 1);
			} else {
				damage(ht() / 9, this);
			}
		}
	}

	@Override
	protected boolean canAttack(Char enemy) {
		return Dungeon.level.distance(getPos(), enemy.getPos()) < 4 && Ballistica.cast(getPos(), enemy.getPos(), false, true) == enemy.getPos();
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
				setState(FLEEING);
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
		if (getState() == FLEEING) {
			cooldown--;
			if (cooldown < 0) {
				setState(WANDERING);
				if (Math.random() < 0.7) {
					spawnWraith();
				} else {
					spawnShadow();
				}

				yell(TXT_INTRO);
			}
		}

		if (Dungeon.level.blobAmountAt(Darkness.class, getPos()) > 0 && hp() < ht()) {
			getSprite().emitter().burst(Speck.factory(Speck.HEALING), 1);
			hp(Math.min(hp() + (ht() - hp()) / 4, ht()));
		}

		if (Dungeon.level.blobAmountAt(Foliage.class, getPos()) > 0) {
			getSprite().emitter().burst(Speck.factory(Speck.BONE), 1);
			damage(1, this);
		}

		return super.act();
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(30, 40);
	}

	@Override
	public int attackSkill(Char target) {
		return 30;
	}

	@Override
	public int dr() {
		return 40;
	}

	@Override
	public void die(Object cause) {
		super.die(cause);
		yell(TXT_DENY);
		Tools.makeEmptyLevel(Dungeon.level);
		Badges.validateBossSlain(Badges.Badge.SHADOW_LORD_SLAIN);
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LEVELCREATED, levelCreated);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		levelCreated   = bundle.optBoolean(LEVELCREATED, false);
	}

}
