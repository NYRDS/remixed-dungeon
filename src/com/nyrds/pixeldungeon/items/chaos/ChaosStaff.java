package com.nyrds.pixeldungeon.items.chaos;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.wands.WandOfTeleportation;
import com.watabou.utils.Bundle;

public class ChaosStaff extends Wand implements IChaosItem {

	private int charge = 0;
	
	public ChaosStaff() {
		
		imageFile = "items/chaosStaff.png";
		image = 0;
		upgrade();
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	private int chargeForLevel() {
		return (int) (10 * Math.pow(level(), 1.4));
	}
	
	@Override
	public void ownerTakesDamage(int damage) {
		charge--;
		if(charge < 0) {
			charge = 0;
		}
		
		if(level() > 0) {
			if(charge == 0) {
				degrade();
				charge = chargeForLevel();
				selectImage();
			}
		}
	}
	
	@Override
	public void ownerDoesDamage(int damage) {
		
		if(cursed) {
			return;
		}
		
		if(damage > 0) {
			charge++;
			if(charge > chargeForLevel()) {
				upgrade();
				selectImage();
				charge = 0;
			}
		}
	}
	
	private void selectImage() {
		image = Math.max(0, Math.min(level()/3, 4));
	}
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		
		bundle.put(ChaosCommon.CHARGE_KEY, charge);
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		
		charge = bundle.getInt(ChaosCommon.CHARGE_KEY);
	}

	@Override
	public int getCharge() {
		return charge;
	}

	@Override
	protected void onZap(int cell) {
		Char ch = Actor.findChar(cell);
		
		if(ch instanceof Mob) {
			Mob mob = (Mob) ch;
			//===
			if(!(mob instanceof Boss)){
				mob.die(getCurUser());
			}
			//===
			Mob.makePet(mob, getCurUser());
			//===
			int nextCell = Dungeon.level.getEmptyCellNextTo(cell);
			
			if(nextCell!=-1) {
				try {
					Mob newMob = mob.getClass().newInstance();
					Dungeon.level.spawnMob(newMob);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			//===
			WandOfTeleportation.teleport(mob);
			//===
			
			//===
		}
	}
}
