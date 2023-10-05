
package com.watabou.pixeldungeon.levels.traps;

import com.nyrds.pixeldungeon.levels.objects.ITrigger;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.Nullable;

public class AlarmTrap implements ITrigger {

	// 0xDD3333
	
	public static void trigger( int pos, @Nullable Char ch ) {
		
		for (Mob mob : Dungeon.level.mobs) {
			if (mob != ch) {
				mob.beckon( pos );
			}
		}
		
		if (Dungeon.isCellVisible(pos)) {
            GLog.w(StringsManager.getVar(R.string.AlarmTrap_Desc));
			CellEmitter.center( pos ).start( Speck.factory( Speck.SCREAM ), 0.3f, 3 );
		}
		
		Sample.INSTANCE.play( Assets.SND_ALERT );
	}

	@Override
	public void doTrigger(int cell, Char ch) {
		trigger(cell,ch);
	}
}
