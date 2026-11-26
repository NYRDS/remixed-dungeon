package com.nyrds.pixeldungeon.items.common;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

/**
 * Java side of the mastery-window contract: the completion driven by
 * WndChooseWay (the tomes themselves are script items - TomeOfMastery,
 * BlackSkullOfMastery).
 */
public class MasteryItem {

	public static final float TIME_TO_READ = 10;

	// the generic tome serves only the classes without their own mastery item
	// (necromancer: BlackSkullOfMastery, gnoll/doctor/priest: none)
	public static boolean tomeGivesMasteryTo(Char hero) {
		switch (hero.getHeroClass()) {
			case NECROMANCER:
			case GNOLL:
			case DOCTOR:
			case PRIEST:
				return false;
			default:
				return true;
		}
	}

	public static void choose(Char hero, Item masteryItem, HeroSubClass way) {
		masteryItem.detach( hero.getBelongings().backpack );

		hero.setSubClass(way);

		hero.doOperate(TIME_TO_READ);
		Sample.INSTANCE.play( Assets.SND_MASTERY );

		SpellSprite.show(hero, SpellSprite.MASTERY );
		hero.getSprite().emitter().burst( Speck.factory( Speck.MASTERY ), 12 );

		if (way == HeroSubClass.LICH) {
			int penalty = 2;
            GLog.w(Utils.format(R.string.Necromancy_BecameALich, penalty) );
			hero.STR(hero.STR() - penalty);
			hero.setMaxSkillPoints(hero.getSkillPointsMax() * 2);
		}

        GLog.w(StringsManager.getVar(R.string.TomeOfMastery_Choose), Utils.capitalize( way.title() ) );

		hero.updateSprite();
	}
}
