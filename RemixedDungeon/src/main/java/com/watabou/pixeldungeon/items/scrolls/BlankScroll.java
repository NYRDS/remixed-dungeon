package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

public class BlankScroll extends Scroll {
	{
		image = ItemSpriteSheet.SCROLL_BLANK;
		stackable = true;
	}
	
	@Override
	public Item burn(int cell) {
        return super.burn(cell);
    }
	
	@Override
	public boolean isUpgradable() {
        return super.isUpgradable();
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
	protected void doRead(@NotNull Char reader) {
		collect( reader.getBelongings().backpack );
		reader.spend( TIME_TO_READ );

        GLog.i(StringsManager.getVar(R.string.BlankScroll_ReallyBlank));
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
