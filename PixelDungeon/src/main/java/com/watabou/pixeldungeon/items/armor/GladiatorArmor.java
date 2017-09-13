package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.HeroSpriteDef;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Callback;

public class GladiatorArmor extends ClassArmor {
	
	private static int LEAP_TIME	= 1;
	private static int SHOCK_TIME	= 3;
	
	private static final String AC_SPECIAL = Game.getVar(R.string.WarriorArmor_ACSpecial); 
	
	private static final String TXT_NOT_WARRIOR	= Game.getVar(R.string.WarriorArmor_NotWarrior);
	
	{
		name = Game.getVar(R.string.WarriorArmor_Name);
		image = 7;
		hasHelmet = true;
		coverHair = true;
	}
	
	@Override
	public String special() {
		return AC_SPECIAL;
	}
	
	@Override
	public void doSpecial() {
		GameScene.selectCell( leaper );
	}
	
	@Override
	public boolean doEquip( Hero hero ) {
		if (hero.heroClass == HeroClass.WARRIOR && hero.subClass == HeroSubClass.GLADIATOR) {
			return super.doEquip( hero );
		} else {
			GLog.w( TXT_NOT_WARRIOR );
			return false;
		}
	}
	
	@Override
	public String desc() {
		return Game.getVar(R.string.WarriorArmor_Desc);
	}

	protected static CellSelector.Listener leaper = new  CellSelector.Listener() {
		
		@Override
		public void onSelect( Integer target ) {
			if (target != null && target != getCurUser().getPos()) {
				
				int cell = Ballistica.cast( getCurUser().getPos(), target, false, true );
				if (Actor.findChar( cell ) != null && cell != getCurUser().getPos()) {
					cell = Ballistica.trace[Ballistica.distance - 2];
				}
				
				getCurUser().checkIfFurious();
				
				Invisibility.dispel(getCurUser());
				
				final int dest = cell;
				getCurUser().busy();
				((HeroSpriteDef)getCurUser().getSprite()).jump( getCurUser().getPos(), cell, new Callback() {
					@Override
					public void call() {
						getCurUser().move( dest );
						Dungeon.level.press( dest, getCurUser() );
						Dungeon.observe();
						
						for (int i=0; i < Level.NEIGHBOURS8.length; i++) {
							Char mob = Actor.findChar( getCurUser().getPos() + Level.NEIGHBOURS8[i] );
							if (mob != null && mob != getCurUser()) {
								Buff.prolong( mob, Paralysis.class, SHOCK_TIME );
							}
						}
						
						CellEmitter.center( dest ).burst( Speck.factory( Speck.DUST ), 10 );
						Camera.main.shake( 2, 0.5f );
						
						getCurUser().spendAndNext( LEAP_TIME );
					}
				} );
			}
		}
		
		@Override
		public String prompt() {
			return Game.getVar(R.string.WarriorArmor_Prompt);
		}
	};
}