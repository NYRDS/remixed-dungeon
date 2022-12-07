package com.nyrds.retrodungeon.items.necropolis;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import java.util.Collection;

public class BlackSkull extends Artifact {

	private static final int    ACTIVATED_IMAGE = 20;
	private static final int    BASIC_IMAGE     = 19;

	private static final int    RESSURRECTION_COST  = 10;
	private static final int    MAXIMUM_CHARGE  = 10;

	private static final String CHARGE_KEY      = "charge";
	private static final String ACTIVATED_KEY   = "activated";
	private static final String TXT_SKULL_ACTIVATED = Game.getVar(R.string.BlackSkull_Activated);
	private static final String TXT_SKULL_DEACTIVATED = Game.getVar(R.string.BlackSkull_Deactivated);
	private static final String TXT_SKULL_RESSURRECT = Game.getVar(R.string.BlackSkull_Ressurrect);

	private boolean activated = false;

	private int charge = 0;

	public BlackSkull() {
		imageFile = "items/artifacts.png";
		identify();
		image = BASIC_IMAGE;
	}

	public void mobDied(Mob mob, Hero hero) {
		Collection<Mob> pets = hero.getPets();

		if (pets.contains(mob)){
			return;
		}

		if (mob.canBePet()) {
			if (activated) {
				mob.ressurrect(hero);
				GLog.w( TXT_SKULL_RESSURRECT );
				charge = charge - RESSURRECTION_COST;
				if (charge <= 0) {
					GLog.w( TXT_SKULL_DEACTIVATED );
					activated = false;
				}
			} else {
				charge++;
				if (charge >= MAXIMUM_CHARGE) {
					GLog.w( TXT_SKULL_ACTIVATED );
					activated = true;
				}
			}
		}
	}

	@Override
	public int image() {
		if (activated) {
			return ACTIVATED_IMAGE;
		} else {
			return BASIC_IMAGE;
		}
	}

	@Override
	public String info() {
		if (activated) {
			return Game.getVar(R.string.BlackSkull_Info_Awakened);
		} else {
			return Game.getVar(R.string.BlackSkull_Info);
		}
	}

	@Override
	public String name() {
		if (activated) {
			return Game.getVar(R.string.BlackSkull_Name_Awakened);
		} else {
			return Game.getVar(R.string.BlackSkull_Name);
		}
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);

		bundle.put(CHARGE_KEY, charge);
		bundle.put(ACTIVATED_KEY, activated);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

		charge = bundle.getInt(CHARGE_KEY);
		activated = bundle.getBoolean(ACTIVATED_KEY);
	}
}
