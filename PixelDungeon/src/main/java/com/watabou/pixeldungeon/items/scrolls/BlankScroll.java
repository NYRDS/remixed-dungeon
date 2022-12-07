package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;

public class BlankScroll extends Scroll {
	{
		image = ItemSpriteSheet.SCROLL_BLANK;
		stackable = true;
	}
	
	@Override
	public Item burn(int cell){
		return null;
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
		return 10 * quantity();
	}

	@Override
	protected void doRead() {
		curItem.collect( getCurUser().belongings.backpack );
		getCurUser().spendAndNext( TIME_TO_READ );
		
		GLog.i(Game.getVar(R.string.BlankScroll_ReallyBlank));
	}
	
	@Override
	public String name() {
		return name; 
	}
	
	@Override
	public String info() {
		return desc();
	}
}
