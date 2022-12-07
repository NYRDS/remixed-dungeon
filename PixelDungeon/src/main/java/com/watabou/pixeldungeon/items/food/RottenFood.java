package com.watabou.pixeldungeon.items.food;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.ConfusionGas;
import com.watabou.pixeldungeon.actors.blobs.ParalyticGas;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

abstract public class RottenFood extends Food{
	{
		energy  = (Hunger.STARVING - Hunger.HUNGRY)/2;
		message = Game.getVar(R.string.RottenFood_Message);
	}
	
	private boolean molder(int cell){
		
		Sample.INSTANCE.play( Assets.SND_ROTTEN_DROP );
		
		switch (Random.Int( 4 )) {
		case 0:
			GameScene.add( Blob.seed( cell, 150 + 10 * Dungeon.depth, ConfusionGas.class ) );
			CellEmitter.get( cell ).burst( Speck.factory( Speck.CONFUSION ), 10 );
			break;
		case 1:
			GameScene.add( Blob.seed( cell, 500, ParalyticGas.class ) );
			CellEmitter.get( cell ).burst( Speck.factory( Speck.PARALYSIS ), 10 );
			break;
		case 2:
			GameScene.add( Blob.seed( cell, 500, ToxicGas.class ));
			CellEmitter.get( cell ).burst( Speck.factory( Speck.TOXIC ), 10 );
			break;
		case 3:
			return false;
		}
		
		return true;
	}
	
	public Food purify() {
		return this;
	}
	
	@Override
	public void execute( Hero hero, String action ) {
		
		super.execute( hero, action );
		
		if (action.equals( AC_EAT )) {
			GLog.w(message);
			molder(hero.getPos());
		}
	}
	
	@Override
	protected void onThrow( int cell ) {
	   if (Dungeon.level.map[cell] == Terrain.WELL || Dungeon.level.pit[cell]) {
			super.onThrow( cell );
		} else  {
			if(! molder( cell )){
				super.onThrow(cell);
			}
		}
	}
	
	public int price() {
		return 1 * quantity();
	}

}
