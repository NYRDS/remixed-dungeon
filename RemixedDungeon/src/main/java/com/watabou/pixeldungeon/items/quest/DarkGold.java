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
package com.watabou.pixeldungeon.items.quest;

import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Splash;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

import org.jetbrains.annotations.NotNull;

public class DarkGold extends Item {
	
	{
		image = ItemSpriteSheet.ORE;
		stackable = true;
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public int price() {
		return quantity();
	}

	@Override
	public void doDrop(@NotNull Char hero) {
		if(Dungeon.depth > 0) {
			super.doDrop(hero);
		}   else {
			detachAll(hero.getBelongings().backpack);
			new ItemSprite(this).drop(hero.getPos());
			melt(hero.getPos());
		}
	}

	@Override
	protected void onThrow(int cell, @NotNull Char thrower, Char enemy) {
		if(Dungeon.depth > 0) {
			super.onThrow(cell, thrower, enemy);
		}   else {
			melt(cell);
		}
	}

	private void melt(int cell) {
		Sample.INSTANCE.play( Assets.SND_PUFF);
		Splash.at( cell, 0xa38d1c, 8 );
	}
}
