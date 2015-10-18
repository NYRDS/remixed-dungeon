package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Regrowth;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;

public class ElfArmor extends ClassArmor {
	
	private static final String TXT_NOT_ELF = Game.getVar(R.string.ElfArmor_NotElf);
	private static final String AC_SPECIAL = Game.getVar(R.string.ElfArmor_ACSpecial); 
	
	public ElfArmor() {
		image = 13;
	}	
	
	@Override
	public String special() {
		return AC_SPECIAL;
	}
	
	@Override
	public void doSpecial() {
		
		for (Mob mob : Dungeon.level.mobs) {
			if (Dungeon.level.fieldOfView[mob.pos]) {
				GameScene.add( Blob.seed( mob.pos, 100, Regrowth.class ) );
			}
		}
		
		curUser.hp(curUser.hp() - (curUser.hp() / 3));
		
		curUser.spend( Actor.TICK );
		curUser.getSprite().operate( curUser.pos );
		curUser.busy();
		
		Sample.INSTANCE.play( Assets.SND_READ );
		
		GameScene.add( Blob.seed( curUser.pos, 100, Regrowth.class ) );
	}
	
	@Override
	public boolean doEquip( Hero hero ) {
		if (hero.heroClass == HeroClass.ELF) {
			return super.doEquip( hero );
		} else {
			GLog.w( TXT_NOT_ELF );
			return false;
		}
	}
	
	@Override
	public String desc() {
		return Game.getVar(R.string.ElfArmor_Desc);
	}
}