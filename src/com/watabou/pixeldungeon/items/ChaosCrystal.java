package com.watabou.pixeldungeon.items;

import java.util.ArrayList;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.spiders.SpiderServant;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.effects.Wound;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class ChaosCrystal extends Artifact {
	
	public static final float TIME_TO_USE = 1;
	public static final String AC_USE = Game.getVar(R.string.SpiderCharm_Use);
	
	private static final int CHAOS_CRYSTALL_IMAGE = 9;
	
	public ChaosCrystal() {
		imageFile = "items/artifacts.png";
		image = CHAOS_CRYSTALL_IMAGE;
		unique = true;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public Glowing glowing() {
		return new Glowing( (int) (Math.random() * 0xffffff) );
	}
	
	@Override
	public void execute( final Hero ch, String action ) {
		setCurUser(ch);
		
		if (action.equals( AC_USE )) {

		} else {
			
			super.execute( ch, action );
			
		}
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_USE );
		return actions;
	}
}
