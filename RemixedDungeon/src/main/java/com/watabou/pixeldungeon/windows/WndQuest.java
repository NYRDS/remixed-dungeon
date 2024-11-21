
package com.watabou.pixeldungeon.windows;

import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.nyrds.util.Utils;
import com.nyrds.util.Random;

public class WndQuest extends WndTitledMessage {
	
	public WndQuest(Char questgiver, String text ) {
		super( questgiver.newSprite(), Utils.capitalize( questgiver.getName() ), text );
	}

	public WndQuest( Char questgiver, int ... phrases ) {
        this(questgiver, StringsManager.getVar(phrases[Random.Int(0, phrases.length)]));
	}
}
