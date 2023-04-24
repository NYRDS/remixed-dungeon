package com.watabou.pixeldungeon.items.food;

import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.ConfusionGas;
import com.watabou.pixeldungeon.actors.blobs.ParalyticGas;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

abstract public class RottenFood extends Food{
	{
		energy  = (Hunger.STARVING - Hunger.HUNGRY)/2;
        message = StringsManager.getVar(R.string.RottenFood_Message);
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
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		
		super._execute(chr, action );
		
		if (action.equals( CommonActions.AC_EAT )) {
			GLog.w(message);
			molder(chr.getPos());
		}
	}
	
	@Override
	protected void onThrow(int cell, @NotNull Char thrower, Char enemy) {
		final Level level = Dungeon.level;
		LevelObject lo = level.getTopLevelObject(cell);
		if ((lo != null && lo.affectItems()) || level.pit[cell]) {
			super.onThrow( cell, thrower, enemy);
		} else  {
			if(! molder( cell )){
				super.onThrow(cell, thrower, enemy);
			}
		}
	}
	
	public int price() {
		return 1 * quantity();
	}

}
