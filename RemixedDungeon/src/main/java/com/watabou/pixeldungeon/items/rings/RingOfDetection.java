
package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;

import org.jetbrains.annotations.NotNull;

public class RingOfDetection extends Ring {
	
	@Override
	public boolean doEquip(@NotNull Char hero ) {
		if (super.doEquip( hero )) {
			Dungeon.hero.search( false );
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public  ArtifactBuff buff( ) {
		return new Detection();
	}
	
	@Override
	public String desc() {
        return isKnown() ? StringsManager.getVar(R.string.RingOfDetection_Info) : super.desc();
	}
	
	public class Detection extends RingBuff {
	}
}
