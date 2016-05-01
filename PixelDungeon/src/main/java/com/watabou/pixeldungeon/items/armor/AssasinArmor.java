package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;

public class AssasinArmor extends ClassArmor {
	
	private static final String TXT_FOV       = Game.getVar(R.string.RogueArmor_Fov);
	private static final String TXT_NOT_ROGUE = Game.getVar(R.string.RogueArmor_NotRogue);
	
	private static final String AC_SPECIAL = Game.getVar(R.string.RogueArmor_ACSpecial); 
	
	{
		image = 10;
	}
	
	@Override
	public String special() {
		return AC_SPECIAL;
	}
	
	@Override
	public void doSpecial() {			
		GameScene.selectCell( teleporter );
	}
	
	@Override
	public boolean doEquip( Hero hero ) {
		if (hero.heroClass == HeroClass.ROGUE) {
			return super.doEquip( hero );
		} else {
			GLog.w( TXT_NOT_ROGUE );
			return false;
		}
	}
	
	@Override
	public String desc() {
		return Game.getVar(R.string.RogueArmor_Desc);
	}
	
	protected static CellSelector.Listener teleporter = new  CellSelector.Listener() {
		
		@Override
		public void onSelect( Integer target ) {
			if (target != null) {

				if (!Dungeon.level.fieldOfView[target] || 
					!(Dungeon.level.passable[target] || Dungeon.level.avoid[target]) || 
					Actor.findChar( target ) != null) {
					
					GLog.w( TXT_FOV );
					return;
				}
				
				getCurUser().hp(getCurUser().hp() - (getCurUser().hp() / 3));
				
				for (Mob mob : Dungeon.level.mobs) {
					if (Dungeon.level.fieldOfView[mob.getPos()]) {
						Buff.prolong( mob, Blindness.class, 2 );
						mob.state = mob.WANDERING;
						mob.getSprite().emitter().burst( Speck.factory( Speck.LIGHT ), 4 );
					}
				}
				
				WandOfBlink.appear( getCurUser(), target );
				CellEmitter.get( target ).burst( Speck.factory( Speck.WOOL ), 10 );
				Sample.INSTANCE.play( Assets.SND_PUFF );
				Dungeon.level.press( target, getCurUser() );
				Dungeon.observe();
				
				getCurUser().spendAndNext( Actor.TICK );
			}
		}
		
		@Override
		public String prompt() {
			return Game.getVar(R.string.RogueArmor_Prompt);
		}
	};
}