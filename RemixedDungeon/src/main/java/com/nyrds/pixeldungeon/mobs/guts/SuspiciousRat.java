package com.nyrds.pixeldungeon.mobs.guts;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

/**
 * Created by DeadDie on 12.02.2016
 */
public class SuspiciousRat extends Mob {

	private static final float TIME_TO_HATCH = 4f;

	{
		hp(ht(140));
		baseDefenseSkill = 25;
		baseAttackSkill  = 25;

		exp = 1;
		maxLvl = 30;

		pacified = true;

		addImmunity(ToxicGas.class);
	}

	@Packable
	private boolean transforming = false;

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(10, 15);
	}

	@Override
	public int dr() {
		return 2;
	}

	@Override
	public boolean act() {
		if (enemySeen) {
			if (!transforming) {
				spend(TIME_TO_HATCH);
				transforming = true;
				if (CharUtils.isVisible(this)) {
					getSprite().showStatus(CharSprite.NEGATIVE, Game.getVar(R.string.Goo_StaInfo1));
					GLog.n(Game.getVar(R.string.SuspiciousRat_Info1));
				}
				getSprite().zap(getEnemy().getPos());
				return true;
			} else {
				int wereratPos = this.getPos();
				if (Dungeon.level.cellValid(wereratPos)) {
					PseudoRat mob = new PseudoRat();
					mob.setPos(wereratPos);
					Dungeon.level.spawnMob(mob);
					Sample.INSTANCE.play(Assets.SND_CURSED);
				}
				die(this);
				return true;
			}
		}
		return super.act();
	}

	@Override
	public boolean zap(@NotNull Char enemy){
		return false;
	}
}
