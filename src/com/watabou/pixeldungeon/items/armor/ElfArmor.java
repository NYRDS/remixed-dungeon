package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;

public class ElfArmor extends ClassArmor {
	
	private static final String TXT_NOT_ELF = Game.getVar(R.string.ElfArmor_NotElf);
	private static final String AC_SPECIAL = Game.getVar(R.string.ElfArmor_ACSpecial); 
	
	public ElfArmor() {
		name = Game.getVar(R.string.ElfArmor_Name);
		image = ItemSpriteSheet.ARMOR_ELF;
	}	
	
	@Override
	public String special() {
		return AC_SPECIAL;
	}
	
	@Override
	public void doSpecial() {
			
		curUser.HP -= (curUser.HP / 3);
		
		curUser.getSprite().zap( curUser.pos );
		curUser.busy();
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