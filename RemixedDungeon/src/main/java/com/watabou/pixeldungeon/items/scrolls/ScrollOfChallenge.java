
package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

public class ScrollOfChallenge extends Scroll {

	@Override
	protected void doRead(@NotNull Char reader) {
		
		CharUtils.challengeAllMobs(reader,Assets.SND_CHALLENGE);

        GLog.w(StringsManager.getVar(R.string.ScrollOfChallenge_Info1));
		setKnown();

		reader.spend( TIME_TO_READ );
	}
}
