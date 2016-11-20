package com.nyrds.pixeldungeon.items.common;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

public class MasteryItem extends Item {

	public static final float TIME_TO_READ = 10;

	@Override
	public boolean doPickUp( Hero hero ) {
		Badges.validateMastery();
		return super.doPickUp( hero );
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	private void specialChooseMessage(int penalty){
		GLog.w(Utils.format(Game.getVar(R.string.Necromancy_BecameALich), penalty) );
	}

	public void choose( HeroSubClass way ) {
		detach( getCurUser().belongings.backpack );

		getCurUser().subClass = way;

		getCurUser().getSprite().operate( getCurUser().getPos() );
		Sample.INSTANCE.play( Assets.SND_MASTERY );

		SpellSprite.show( getCurUser(), SpellSprite.MASTERY );
		getCurUser().getSprite().emitter().burst( Speck.factory( Speck.MASTERY ), 12 );
		if (way == HeroSubClass.LICH){
			int penalty = 2;
			specialChooseMessage(penalty);
			getCurUser().STR(getCurUser().STR() - penalty);
		} else {
			GLog.w(Game.getVar(R.string.TomeOfMastery_Choose), Utils.capitalize( way.title() ) );
		}

		getCurUser().checkIfFurious();
		getCurUser().updateLook();

		getCurUser().spendAndNext( TIME_TO_READ );
		getCurUser().busy();
	}
}
