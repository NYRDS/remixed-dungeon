
package com.watabou.pixeldungeon.items.wands;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.DeathRay;
import com.watabou.pixeldungeon.effects.particles.PurpleParticle;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class WandOfDisintegration extends SimpleWand  {
	{
		hitChars = false;
	}
	
	@Override
	protected void onZap( int cell, Char victim ) {
		
		boolean terrainAffected = false;
		
		int level = effectiveLevel();
		
		int maxDistance = distance();
		Ballistica.distance = Math.min( Ballistica.distance, maxDistance );
		
		ArrayList<Char> chars = new ArrayList<>();
		
		for (int i=1; i < Ballistica.distance; i++) {
			
			int c = Ballistica.trace[i];
			
			Char ch;
			if ((ch = Actor.findChar( c )) != null) {
				chars.add( ch );
			}

			final Level level1 = Dungeon.level;
			int terr = level1.map[c];
			if (terr == Terrain.DOOR || terr == Terrain.BARRICADE) {
				
				level1.set( c, Terrain.EMBERS );
				GameScene.updateMapPair( c );
				terrainAffected = true;
				
			} else if (terr == Terrain.HIGH_GRASS) {
				
				level1.set( c, Terrain.GRASS );
				GameScene.updateMap( c );
				terrainAffected = true;
				
			}
			
			CellEmitter.center( c ).burst( PurpleParticle.BURST, Random.IntRange( 1, 2 ) );
		}
		
		if (terrainAffected) {
			Dungeon.observe();
		}
		
		int lvl = level + chars.size();
		int dmgMin = lvl;
		int dmgMax = 8 + lvl * lvl / 3;
		for (Char ch : chars) {
			ch.damage( Random.NormalIntRange( dmgMin, dmgMax ), this );
			ch.getSprite().centerEmitter().burst( PurpleParticle.BURST, Random.IntRange( 1, 2 ) );
			ch.getSprite().flash();
		}
	}
	
	private int distance() {
		return effectiveLevel() + 4;
	}
	
	@Override
	protected void fx( int cell, Callback callback ) {
		cell = Ballistica.trace[Math.min( Ballistica.distance, distance() ) - 1];
		getOwner().getSprite().getParent().add( new DeathRay( getOwner().getPos(),  cell  ) );
		callback.call();
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.WandOfDisintegration_Info);
    }
}
