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

public class SpiderCharm extends Artifact {
	
	public static final float TIME_TO_USE = 1;
	public static final String AC_USE = Game.getVar(R.string.SpiderCharm_Use);
	
	public SpiderCharm() {
		image = ItemSpriteSheet.SPIDER_CHARM;
		unique = true;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
	
	private static final Glowing WHITE = new Glowing( 0xFFFFFF );
	
	@Override
	public Glowing glowing() {
		return WHITE;
	}
	
	@Override
	public void execute( final Hero ch, String action ) {
		setCurUser(ch);
		
		if (action.equals( AC_USE )) {
			Wound.hit(ch);
			ch.damage(ch.ht()/4, this);
			
			ArrayList<Integer> spawnPoints = new ArrayList<Integer>();
			
			for (int i=0; i < Level.NEIGHBOURS8.length; i++) {
				int p = ch.getPos() + Level.NEIGHBOURS8[i];
				if (Dungeon.level.cellValid(p) && Actor.findChar( p ) == null && (Dungeon.level.passable[p] || Dungeon.level.avoid[p])) {
					spawnPoints.add( p );
				}
			}
			
			if (spawnPoints.size() > 0) {
				Mob pet = Mob.makePet(new SpiderServant(), getCurUser());
				pet.setPos(Random.element( spawnPoints ));
				
				GameScene.add(Dungeon.level, pet );
				Actor.addDelayed( new Pushing( pet, ch.getPos(), pet.getPos() ), -1 );
			}

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
