package com.watabou.pixeldungeon.items.armor;

import java.util.HashMap;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.missiles.Shuriken;
import com.watabou.pixeldungeon.sprites.MissileSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Callback;

public class SniperArmor extends ClassArmor {
	
	private static final String TXT_NO_ENEMIES   = Game.getVar(R.string.HuntressArmor_NoEnemies);
	private static final String TXT_NOT_HUNTRESS = Game.getVar(R.string.HuntressArmor_NotHuntress);
	
	private static final String AC_SPECIAL = Game.getVar(R.string.HuntressArmor_ACSpecial); 
	
	{
		image = 9;
	}
	
	private HashMap<Callback, Mob> targets = new HashMap<Callback, Mob>();
	
	@Override
	public String special() {
		return AC_SPECIAL;
	}
	
	@Override
	public void doSpecial() {
		
		Item proto = new Shuriken();
		
		for (Mob mob : Dungeon.level.mobs) {
			if (Dungeon.level.fieldOfView[mob.pos]) {
				
				Callback callback = new Callback() {	
					@Override
					public void call() {
						curUser.attack( targets.get( this ) );
						targets.remove( this );
						if (targets.isEmpty()) {
							curUser.spendAndNext( curUser.attackDelay() );
						}
					}
				};
				
				((MissileSprite)curUser.getSprite().getParent().recycle( MissileSprite.class )).
					reset( curUser.pos, mob.pos, proto, callback );
				
				targets.put( callback, mob );
			}
		}
		
		if (targets.size() == 0) {
			GLog.w( TXT_NO_ENEMIES );
			return;
		}
		
		curUser.hp(curUser.hp() - (curUser.hp() / 3));
		
		curUser.getSprite().zap( curUser.pos );
		curUser.busy();
	}
	
	@Override
	public boolean doEquip( Hero hero ) {
		if (hero.heroClass == HeroClass.HUNTRESS && hero.subClass == HeroSubClass.SNIPER) {
			return super.doEquip( hero );
		} else {
			GLog.w( TXT_NOT_HUNTRESS );
			return false;
		}
	}
	
	@Override
	public String desc() {
		return Game.getVar(R.string.HuntressArmor_Desc);
	}
}