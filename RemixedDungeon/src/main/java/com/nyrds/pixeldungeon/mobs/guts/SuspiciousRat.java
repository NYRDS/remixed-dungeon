package com.nyrds.pixeldungeon.mobs.guts;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.GLog;

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
		dmgMin = 10;
		dmgMax = 15;
		dr = 2;

		expForKill = 1;
		maxLvl = 30;

		pacified = true;

		addImmunity(ToxicGas.class);
	}

	@Packable
	private boolean transforming = false;

	@Override
	public boolean act() {
		if (enemySeen) {
			if (!transforming) {
				spend(TIME_TO_HATCH);
				transforming = true;
				if (CharUtils.isVisible(this)) {
                    getSprite().showStatus(CharSprite.NEGATIVE, StringsManager.getVar(R.string.Goo_StaInfo1));
                    GLog.n(StringsManager.getVar(R.string.SuspiciousRat_Info1));
				}
				getSprite().zap(getEnemy().getPos());
			} else {
				int wereratPos = this.getPos();
				if (level().cellValid(wereratPos)) {
					PseudoRat mob = new PseudoRat();
					mob.setPos(wereratPos);
					level().spawnMob(mob);
					Sample.INSTANCE.play(Assets.SND_CURSED);
				}
				die(this);
			}
			return true;
		}
		return super.act();
	}

	@Override
	public boolean zap(@NotNull Char enemy){
		return false;
	}
}
