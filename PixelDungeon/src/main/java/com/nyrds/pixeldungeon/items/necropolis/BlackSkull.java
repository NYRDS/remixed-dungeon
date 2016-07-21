package com.nyrds.pixeldungeon.items.necropolis;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Skeleton;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.Bundle;

public class BlackSkull extends Artifact {

	private static final int BASIC_IMAGE = 19;
	private static final int ACTIVATED_IMAGE = 20;
	private static final int MAXIMUM_CHARGE = 10;
	private static final String CHARGE_KEY = "charge";
	private static final String ACTIVATED_KEY = "activated";

	private boolean activated = false;

	private int charge = 0;

	public BlackSkull() {
		imageFile = "items/artifacts.png";
		image = BASIC_IMAGE;
	}

	public void mobDied(Mob mob, Hero hero){
	if ( mob.isCanBePet() ){
			if (activated){
				Mob staticMob = new Skeleton();
				staticMob.ressurrect(hero, mob);

				charge = charge - 5;
				if(charge <= 0){
					activated = false;
				}
			} else{
				charge++;
				if (charge >= MAXIMUM_CHARGE){
					activated = true;
				}
			}
		}
	}

	@Override public int image() {
		if(activated){
			return ACTIVATED_IMAGE;
		}else{
			return BASIC_IMAGE;
		}
	}

	@Override public String info(){
		if (activated){
			return Game.getVar(R.string.BlackSkull_Info_Awakened);
		} else{
			return Game.getVar(R.string.BlackSkull_Info);
		}

	}

	@Override public String name(){
		if (activated){
			return Game.getVar(R.string.BlackSkull_Name_Awakened);
		} else{
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
