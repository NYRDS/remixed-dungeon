
package com.watabou.pixeldungeon.actors.blobs;

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.items.guts.weapon.melee.Claymore;
import com.nyrds.pixeldungeon.items.guts.weapon.melee.Halberd;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.Journal.Feature;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.items.potions.PotionOfMight;
import com.watabou.pixeldungeon.items.potions.PotionOfStrength;
import com.watabou.pixeldungeon.items.rings.Ring;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfWeaponUpgrade;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.weapon.Weapon.Enchantment;
import com.watabou.pixeldungeon.items.weapon.melee.BattleAxe;
import com.watabou.pixeldungeon.items.weapon.melee.Dagger;
import com.watabou.pixeldungeon.items.weapon.melee.Glaive;
import com.watabou.pixeldungeon.items.weapon.melee.Knuckles;
import com.watabou.pixeldungeon.items.weapon.melee.Longsword;
import com.watabou.pixeldungeon.items.weapon.melee.Mace;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.items.weapon.melee.Quarterstaff;
import com.watabou.pixeldungeon.items.weapon.melee.Spear;
import com.watabou.pixeldungeon.items.weapon.melee.Sword;
import com.watabou.pixeldungeon.items.weapon.melee.WarHammer;
import com.watabou.pixeldungeon.plants.Seed;

public class WaterOfTransmutation extends WellWater {
	
	@Override
	protected Item affectItem( Item item ) {
		
		if (item instanceof MeleeWeapon) {
			
			return changeWeapon( (MeleeWeapon)item );
			
		} else if (item instanceof Scroll) {
			
			Journal.remove( Feature.WELL_OF_TRANSMUTATION.desc() );
			return changeScroll(item);
			
		} else if (item instanceof Potion) {
			
			Journal.remove( Feature.WELL_OF_TRANSMUTATION.desc() );
			return changePotion(item);
			
		} else if (item instanceof Ring) {
			
			Journal.remove( Feature.WELL_OF_TRANSMUTATION.desc() );
			return changeRing(item);
			
		} else if (item instanceof Wand) {	
			
			Journal.remove( Feature.WELL_OF_TRANSMUTATION.desc() );
			return changeWand(item);
			
		} else if (item instanceof Seed) {
			
			Journal.remove( Feature.WELL_OF_TRANSMUTATION.desc() );
			return changeSeed(item);
			
		} else {
			return null;
		}
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );	
		emitter.start( Speck.factory( Speck.CHANGE ), 0.2f, 0 );
	}
	
	private MeleeWeapon changeWeapon( MeleeWeapon w ) {
		
		MeleeWeapon n = null;
		
		if (w instanceof Knuckles) {
			n = new Dagger();
		} else if (w instanceof Dagger) {
			n = new Knuckles();
		}
		
		else if (w instanceof Spear) {
			n = new Quarterstaff();
		} else if (w instanceof Quarterstaff) {
			n = new Spear();
		}
		
		else if (w instanceof Sword) {
			n = new Mace();
		} else if (w instanceof Mace) {
			n = new Sword();
		}
		
		else if (w instanceof Longsword) {
			n = new BattleAxe();
		} else if (w instanceof BattleAxe) {
			n = new Longsword();
		}
		
		else if (w instanceof Glaive) {
			n = new WarHammer();
		} else if (w instanceof WarHammer) {
			n = new Glaive();
		}

		else if (w instanceof Claymore) {
			n = new Halberd();
		} else if (w instanceof Halberd) {
			n = new Claymore();
		}

		if (n != null) {
			
			int level = w.level();
			if (level > 0) {
				n.upgrade( level );
			} else if (level < 0) {
				n.degrade( -level );
			}
			
			if (w.isEnchanted()) {
				n.enchant( Enchantment.random() );
			}
			
			n.setLevelKnown(w.isLevelKnown());
			n.setCursedKnown(w.isCursedKnown());
			n.setCursed(w.isCursed());
			
			Journal.remove( Feature.WELL_OF_TRANSMUTATION.desc() );
			
			return n;
		} else {
			return null;
		}
	}
	
	private Item changeRing( Item r ) {
		Item n;
		do {
			n = Treasury.getLevelTreasury().random(Treasury.Category.RING );
		} while (n.getClass() == r.getClass());
		
		n.level(0);
		
		int level = r.level();
		if (level > 0) {
			n.upgrade( level );
		} else if (level < 0) {
			n.degrade( -level );
		}
		
		n.setLevelKnown(r.isLevelKnown());
		n.setCursedKnown(r.isCursedKnown());
		n.setCursed(r.isCursed());
		
		return n;
	}
	
	private Item changeWand( Item w ) {
		
		Item n;
		do {
			n = Treasury.getLevelTreasury().random(Treasury.Category.WAND );
		} while (n.getEntityKind().equals(w.getEntityKind()));
		
		n.level(0);
		n.upgrade( w.level() );
		
		n.setLevelKnown(w.isLevelKnown());
		n.setCursedKnown(w.isCursedKnown());
		n.setCursed(w.isCursed());
		
		return n;
	}
	
	private Item changeSeed(Item s ) {
		
		Item n;

		do {
			n = Treasury.getLevelTreasury().random(Treasury.Category.SEED );
		} while (n.getEntityKind().equals(s.getEntityKind()));
		
		return n;
	}
	
	private Item changeScroll( Item s ) {
		if (s instanceof ScrollOfUpgrade) {
			
			return new ScrollOfWeaponUpgrade();
			
		} else if (s instanceof ScrollOfWeaponUpgrade) {
			
			return new ScrollOfUpgrade();
			
		} else {
			
			Item n;
			do {
				n =  Treasury.getLevelTreasury().random(Treasury.Category.SCROLL );
			} while (n.getEntityKind().equals(s.getEntityKind()));
			return n;
		}
	}
	
	private Item changePotion(Item p ) {
		if (p instanceof PotionOfStrength) {
			return new PotionOfMight();
		} else {
			Item n;
			do {
				n  = Treasury.getLevelTreasury().random(Treasury.Category.POTION );
			} while (n.getEntityKind().equals(p.getEntityKind()));
			return n;
		}
	}
	
	@Override
	public String tileDesc() {
        return StringsManager.getVar(R.string.WaterOfTransmutation_Info);
    }
}
