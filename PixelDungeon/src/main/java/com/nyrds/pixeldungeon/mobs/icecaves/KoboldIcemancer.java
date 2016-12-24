package com.nyrds.pixeldungeon.mobs.icecaves;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.actors.buffs.Weakness;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.WarlockSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class KoboldIcemancer extends Mob implements Callback {

	private static final float TIME_TO_ZAP = 1f;

	private static final String TXT_ICEBOLT_KILLED = Game
			.getVar(R.string.KoboldIcemancer_Killed);

	public KoboldIcemancer() {
		hp(ht(70));
		defenseSkill = 18;

		EXP = 11;
		maxLvl = 21;

		loot = Generator.Category.POTION;
		lootChance = 0.83f;

		RESISTANCES.add(Death.class);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(15, 17);
	}

	@Override
	public int attackSkill(Char target) {
		return 25;
	}

	@Override
	public int dr() {
		return 11;
	}

	protected void fx( int cell, Callback callback ) {
		if(getSprite().getParent()==null) {
			EventCollector.logException(new Exception("null parent"));
			return;
		}
		MagicMissile.blueLight( getSprite().getParent(), getPos(), cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
		getSprite().setVisible(false);
	}

	@Override
	protected boolean canAttack(Char enemy) {
		return Ballistica.cast(getPos(), enemy.getPos(), false, true) == enemy.getPos();
	}

	protected boolean doAttack(Char enemy) {

		if (Dungeon.level.adjacent(getPos(), enemy.getPos())) {
			return super.doAttack(enemy);

		} else {
			boolean visible = Dungeon.level.fieldOfView[getPos()]
					|| Dungeon.level.fieldOfView[enemy.getPos()];
			if (visible) {
				getSprite().zap(enemy.getPos());
			}
			zap();

			return !visible;
		}
	}

	private void zap() {
		spend(TIME_TO_ZAP);

		if (hit(this, getEnemy(), true)) {
			if (getEnemy() == Dungeon.hero && Random.Int(2) == 0) {
				Buff.prolong( getEnemy(), Slow.class, 1 );
			}

			int dmg = Random.Int(12, 18);
			getEnemy().damage(dmg, this);

			if (!getEnemy().isAlive() && getEnemy() == Dungeon.hero) {
				Dungeon.fail(Utils.format(ResultDescriptions.MOB,
						Utils.indefinite(getName()), Dungeon.depth));
				GLog.n(TXT_ICEBOLT_KILLED, getName());
			}
		} else {
			getEnemy().getSprite().showStatus(CharSprite.NEUTRAL,
					getEnemy().defenseVerb());
		}
	}

	public void onZapComplete() {
		next();
	}

	@Override
	public void call() {
		next();
	}
}
