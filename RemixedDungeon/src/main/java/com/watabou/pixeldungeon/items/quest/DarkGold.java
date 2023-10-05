
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
