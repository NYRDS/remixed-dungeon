
package com.watabou.pixeldungeon.items.wands;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Camera;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Lightning;
import com.watabou.pixeldungeon.effects.particles.SparkParticle;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.traps.LightningTrap;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class WandOfLightning extends SimpleWand  {
	
	private final ArrayList<Char> affected = new ArrayList<>();
	
	private final int[] points = new int[20];
	private int nPoints;
	
	@Override
	protected void onZap( int cell, Char ch ) {

		if ((getOwner() == Dungeon.hero) && !getOwner().isAlive()) {
			Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.WAND), name, Dungeon.depth ) );
            GLog.n(StringsManager.getVar(R.string.WandOfLightning_Info1));
		}
	}
	
	private void hit( Char ch, int damage ) {
		
		if (damage < 1) {
			return;
		}
		
		if (ch == Dungeon.hero) {
			Camera.main.shake( 2, 0.3f );
		}
		
		affected.add( ch );
		ch.damage( Dungeon.level.water[ch.getPos()] && !ch.isFlying() ? damage * 2 : damage, LightningTrap.LIGHTNING  );
		
		ch.getSprite().centerEmitter().burst( SparkParticle.FACTORY, 3 );
		ch.getSprite().flash();
		
		points[nPoints++] = ch.getPos();
		
		HashSet<Char> ns = new HashSet<>();
		for (int i=0; i < Level.NEIGHBOURS8.length; i++) {
			Char n = Actor.findChar( ch.getPos() + Level.NEIGHBOURS8[i] );
			if (n != null && !affected.contains( n )) {
				ns.add( n );
			}
		}
		
		if (!ns.isEmpty()) {
			hit( Random.element( ns ), Random.Int( damage / 2, damage ) );
		}
	}
	
	@Override
	protected void fx( int cell, Callback callback ) {
		
		nPoints = 0;
		points[nPoints++] = getOwner().getPos();
		
		Char ch = Actor.findChar( cell );
		if (ch != null) {
			affected.clear();
			int lvl = effectiveLevel();
			hit( ch, Random.Int( 5 + lvl / 2, 10 + lvl ) );
		} else {
			points[nPoints++] = cell;
			CellEmitter.center( cell ).burst( SparkParticle.FACTORY, 3 );
		}
			GameScene.addToMobLayer(new Lightning(Arrays.copyOfRange(points, 0, nPoints), callback));
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.WandOfLightning_Info);
    }
}
