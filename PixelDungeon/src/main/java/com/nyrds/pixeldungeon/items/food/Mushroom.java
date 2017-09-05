package com.nyrds.pixeldungeon.items.food;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfRecharging;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.ArrayList;

abstract public class Mushroom extends Food {
	{
		imageFile = "items/shrooms";
		stackable = true;
		message = Game.getVar(R.string.Mushroom_Eat_Message);
	}

	@Override
	public void execute( Hero hero, String action ) {
		if (action.equals( AC_EAT )) {
			applyEffect(hero);
			GLog.i( message );
		} else {
			super.execute( hero, action );
		}
	}

	protected void applyEffect(Hero hero){}
}
