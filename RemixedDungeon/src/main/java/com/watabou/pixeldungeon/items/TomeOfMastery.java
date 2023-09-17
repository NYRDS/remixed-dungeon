
package com.watabou.pixeldungeon.items;

import com.nyrds.pixeldungeon.items.common.MasteryItem;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndChooseWay;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TomeOfMastery extends MasteryItem {

	public static final String AC_READ = "TomeOfMastery_ACRead";

	{
		stackable = false;
		image = ItemSpriteSheet.MASTERY;
	}
	
	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );		
		actions.add( AC_READ );
		
		return actions;
	}

	@Override
	public boolean givesMasteryTo(Char hero) {
		switch (hero.getHeroClass()) {
			case NECROMANCER:
			case GNOLL:
				return false;
			default:
				return true;
		}
	}

	@Override
	public boolean doPickUp(@NotNull Char hero ) {
		if(givesMasteryTo(hero)) {
			Badges.validateMastery(hero.getHeroClass());
		}
		return super.doPickUp( hero );
	}

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		if (action.equals( AC_READ )) {
			
			if (chr.hasBuff( Blindness.class )) {
                GLog.w(StringsManager.getVar(R.string.TomeOfMastery_Blinded));
				return;
			}

			if(chr.getSubClass() != HeroSubClass.NONE) {
                GLog.w(StringsManager.getVar(R.string.TomeOfMastery_WayAlreadyChosen));
				return;
			}

			HeroSubClass way1;
			HeroSubClass way2;
			switch (chr.getHeroClass()) {
			case WARRIOR:
				way1 = HeroSubClass.GLADIATOR;
				way2 = HeroSubClass.BERSERKER;
				break;
			case MAGE:
				way1 = HeroSubClass.BATTLEMAGE;
				way2 = HeroSubClass.WARLOCK;
				break;
			case ROGUE:
				way1 = HeroSubClass.FREERUNNER;
				way2 = HeroSubClass.ASSASSIN;
				break;
			case HUNTRESS:
				way1 = HeroSubClass.SNIPER;
				way2 = HeroSubClass.WARDEN;
				break;
			case ELF:
				way1 = HeroSubClass.SCOUT;
				way2 = HeroSubClass.SHAMAN;
				break;
			default:
                GLog.w(StringsManager.getVar(R.string.TomeOfMastery_WayAlreadyChosen));
				return;
			}
			GameScene.show( new WndChooseWay(chr,  this, way1, way2 ) );
			
		} else {
			super._execute(chr, action );
		}
	}
}
