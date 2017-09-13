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

public class WardenArmor extends ClassArmor {
	
	private static final String TXT_NO_ENEMIES   = Game.getVar(R.string.HuntressArmor_NoEnemies);
	private static final String TXT_NOT_HUNTRESS = Game.getVar(R.string.HuntressArmor_NotHuntress);
	
	private static final String AC_SPECIAL = Game.getVar(R.string.HuntressArmor_ACSpecial); 
	
	{
		name = Game.getVar(R.string.HuntressArmor_Name);
		image = 16;
		hasHelmet = true;
		coverHair = true;
	}
	
	private HashMap<Callback, Mob> targets = new HashMap<>();
	
	@Override
	public String special() {
		return AC_SPECIAL;
	}
	
	@Override
	public void doSpecial() {
		
		Item proto = new Shuriken();
		
		for (Mob mob : Dungeon.level.mobs) {
			if (Dungeon.level.fieldOfView[mob.getPos()]) {
				
				Callback callback = new Callback() {	
					@Override
					public void call() {
						getCurUser().attack( targets.get( this ) );
						targets.remove( this );
						if (targets.isEmpty()) {
							getCurUser().spendAndNext( getCurUser().attackDelay() );
						}
					}
				};
				
				((MissileSprite)getCurUser().getSprite().getParent().recycle( MissileSprite.class )).
					reset( getCurUser().getPos(), mob.getPos(), proto, callback );
				
				targets.put( callback, mob );
			}
		}
		
		if (targets.size() == 0) {
			GLog.w( TXT_NO_ENEMIES );
			return;
		}
		
		getCurUser().getSprite().zap( getCurUser().getPos() );
		getCurUser().busy();
	}
	
	@Override
	public boolean doEquip( Hero hero ) {
		if (hero.heroClass == HeroClass.HUNTRESS && hero.subClass == HeroSubClass.WARDEN) {
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