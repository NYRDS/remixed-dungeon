package com.nyrds.pixeldungeon.items.drinks;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.ArrayList;

abstract public class Drink extends Item {

	public static final float TIME_TO_DRINK	= 1f;
	
	public static final String AC_DRINK = Game.getVar(R.string.Drink_ACDrink);
	
	public int mana			= 0;
	public String message 	= Game.getVar(R.string.Drink_Message);
	
	{
		stackable = true;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_DRINK );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		if (action.equals( AC_DRINK )) {
			detach( hero.belongings.backpack );
			GLog.i( message );
			
			switch (hero.heroClass) {
			case WARRIOR:
				break;
			case NECROMANCER:
			case MAGE:
				hero.setSoulPoints(hero.getSoulPoints() + mana);
				break;
			case ROGUE:
			case HUNTRESS:
			case ELF:
				break;
			}
			
			hero.getSprite().operate( hero.getPos() );
			hero.busy();
			SpellSprite.show( hero, SpellSprite.FOOD );
			Sample.INSTANCE.play( Assets.SND_DRINK );
			
			hero.spend( TIME_TO_DRINK );
		} else {
			super.execute( hero, action );
		}
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
}
