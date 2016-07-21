package com.nyrds.pixeldungeon.items.necropolis;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Skeleton;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.Bundle;

public class BlackSkull extends Artifact {

	//Supposed to accumulate charge when player kills mobs, and once charge is full goes "Awakened" state
	//Once Awakened Skull gets 25 charges and 10% chance to revive next slain enemy as an ally
	//Each try to revive consumes 1 charge of Awakend Skull. Once charges goes down to 0, it becomes regular "non-Awakened" Black Skull
	//If Black Skull was discovered in bones of previous hero, it will instantly get full charge and thus become an Awakened Skull
	//Must be placed as loot in Necropolis Treasury Room (Which is accessed after killing Lich)

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

	private void resurrectMobAsPet(Mob mob, Hero hero){
		int spawnPos = Dungeon.level.getEmptyCellNextTo(hero.getPos());

		if (Dungeon.level.cellValid(spawnPos)) {
			Mob clone;
			try {
				clone = mob.getClass().newInstance();
			} catch (Exception e) {
				throw new TrackedRuntimeException("blackskull issue");
			}
			clone.setPos(spawnPos);
			Mob.makePet(clone, getCurUser());

			Dungeon.level.spawnMob(clone );
			Actor.addDelayed( new Pushing( clone, hero.getPos(), clone.getPos() ), -1 );
		}
	}

	public void mobDied(Mob mob, Hero hero){
		if (activated){

			//resurrectMobAsPet(mob, hero);
			Mob staticMob = new Skeleton();
			staticMob.ressurrect(hero, mob);

			charge = charge - 5;
			if(charge <= 0){
				activated = false;
				setImage();
			}
		} else{
			charge++;
			if (charge >= MAXIMUM_CHARGE){
				activated = true;
				setImage();
			}
		}
	}

	private void setImage() {
		if(activated){
			image = ACTIVATED_IMAGE;
		}else{
			image = BASIC_IMAGE;
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
