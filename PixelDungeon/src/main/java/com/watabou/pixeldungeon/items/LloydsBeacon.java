/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.items;

import com.nyrds.retrodungeon.ml.R;
import com.nyrds.retrodungeon.utils.DungeonGenerator;
import com.nyrds.retrodungeon.utils.Position;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class LloydsBeacon extends Item {

	private static final String TXT_PREVENTING = Game.getVar(R.string.LloidsBeacon_Preventing);
	private static final String TXT_CREATURES  = Game.getVar(R.string.LloidsBeacon_Creatures);
	private static final String TXT_RETURN     = Game.getVar(R.string.LloidsBeacon_Return);
	private static final String TXT_INFO       = Game.getVar(R.string.LloidsBeacon_Info);
	private static final String TXT_SET        = Game.getVar(R.string.LloidsBeacon_Set);

	private static final float TIME_TO_USE = 1;

	private static final String AC_SET    = Game.getVar(R.string.LloidsBeacon_ACSet);
	private static final String AC_RETURN = Game.getVar(R.string.LloidsBeacon_ACReturn);
	
	private Position returnTo;
	
	public LloydsBeacon() {
		image = ItemSpriteSheet.BEACON;
		returnTo = new Position();
		returnTo.cellId = -1;
		
		name = Game.getVar(R.string.LloidsBeacon_Name);
	}
	
	private static final String POSITION = "position";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		
		bundle.put(POSITION, returnTo);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		returnTo = (Position) bundle.get(POSITION);
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_SET );
		if (returnTo.cellId != -1) {
			actions.add( AC_RETURN );
		}
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {
		
		if (action.equals(AC_SET) || action.equals(AC_RETURN)) {
			
			if (Dungeon.bossLevel()) {
				hero.spend( LloydsBeacon.TIME_TO_USE );
				GLog.w( TXT_PREVENTING );
				return;
			}
			
			for (int i=0; i < Level.NEIGHBOURS8.length; i++) {
				if (Actor.findChar( hero.getPos() + Level.NEIGHBOURS8[i] ) != null) {
					GLog.w( TXT_CREATURES );
					return;
				}
			}
		}
		
		if (action.equals(AC_SET)) {
			
			returnTo = Dungeon.currentPosition();
			
			hero.spend( LloydsBeacon.TIME_TO_USE );
			hero.busy();
			
			hero.getSprite().operate( hero.getPos() );
			Sample.INSTANCE.play( Assets.SND_BEACON );
			
			GLog.i( TXT_RETURN );
			
		} else if (action.equals(AC_RETURN)) {
			if (returnTo.levelId.equals(Dungeon.level.levelId)) {
				WandOfBlink.appear( hero, returnTo.cellId );
				Dungeon.level.press( returnTo.cellId, hero );
				reset();
				Dungeon.observe();
			} else {
				InterlevelScene.mode = InterlevelScene.Mode.RETURN;
				InterlevelScene.returnTo = new Position(returnTo);
				reset();
				Game.switchScene( InterlevelScene.class );
			}
		} else {
			
			super.execute( hero, action );
			
		}
	}
	
	public void reset() {
		returnTo.cellId = -1;
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	private static final Glowing WHITE = new Glowing( 0xFFFFFF );
	
	@Override
	public Glowing glowing() {
		return returnTo.cellId != -1 ? WHITE : null;
	}
	
	@Override
	public String info() {
		return TXT_INFO + (returnTo.cellId == -1 ? "" : Utils.format( TXT_SET, DungeonGenerator.getLevelDepth(returnTo.levelId) ) );
	}
}
