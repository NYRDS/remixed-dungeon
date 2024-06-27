package com.nyrds.pixeldungeon.items.artifacts;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.MindVision;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.utils.Bundle;

import org.jetbrains.annotations.NotNull;

public class CandleOfMindVision extends Artifact implements IActingItem {

	private static final String CHARGES   = "charges";
	private static final String ACTIVATED = "activated";

	private static final int MAX_CHARGES = 50;

	private float   charges;
	private boolean activated;


	public CandleOfMindVision() {
		imageFile = "items/candle.png";
		charges   = MAX_CHARGES;
		activated = false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);

		bundle.put(CHARGES, charges);
		bundle.put(ACTIVATED, activated);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

		charges   = bundle.getFloat(CHARGES);
		activated = bundle.getBoolean(ACTIVATED);
	}

	@Override
	public boolean doEquip(@NotNull Char hero) {
		boolean ret = super.doEquip(hero);
		
		if(ret) {
			if (!activated) {
				activated = true;
			}

			if (charges > 0) {
				Buff.affect(hero, MindVision.class, charges);
			}
		}
		return ret;
	}

	@Override
	public boolean doUnequip(Char hero, boolean collect) {
		if(charges > 0) {
			Buff.detach(hero, MindVision.class);
		}
		return super.doUnequip(hero, collect);
	}

	@Override
	public int image() {
		if (!activated) {
			return 0;
		} else {
			if (charges > MAX_CHARGES / 4 * 3) {
				return 1;
			}
			if (charges > MAX_CHARGES / 4) {
				return 2;
			}
			if (charges > 0) {
				return 3;
			}
			return 4;
		}
	}

	@Override
	public String desc() {
		if(!activated) {
            return StringsManager.getVar(R.string.CandleOfMindVision_Info);
        } else {
			if(charges>0) {
                return StringsManager.getVar(R.string.CandleOfMindVision_Info_Lighted);
            } else {
                return StringsManager.getVar(R.string.CandleOfMindVision_Info_Exhausted);
            }
		}
	}

	@Override
	public void spend(Char hero, float time) {
		if (activated) {
			if (time > 0) {
				charges -= time;
			}
		}
	}

}
