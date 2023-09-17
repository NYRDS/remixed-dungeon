
package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;

import org.jetbrains.annotations.NotNull;

public class RingOfHaggler extends Ring {
	
	@Override
	public  ArtifactBuff buff( ) {
		return new Haggling();
	}
	
	@Override
	public Item random() {
		level(+1);
		return this;
	}
	
	@Override
	public boolean doPickUp(@NotNull Char hero ) {
		identify();
		Badges.validateRingOfHaggler();
		Badges.validateItemLevelAcquired( this );
		return super.doPickUp(hero);
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	
	@Override
	public String desc() {
        return isKnown() ? StringsManager.getVar(R.string.RingOfHaggler_Info) : super.desc();
	}
	
	public class Haggling extends RingBuff {	
	}
}
