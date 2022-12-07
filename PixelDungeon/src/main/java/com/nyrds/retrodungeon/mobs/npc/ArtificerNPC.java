package com.nyrds.retrodungeon.mobs.npc;

import com.nyrds.retrodungeon.mechanics.quest.Quest;
import com.watabou.pixeldungeon.actors.hero.Hero;

public class ArtificerNPC extends ImmortalNPC {

	public ArtificerNPC() {
		quest = new Quest();
	}

	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo( getPos(), hero.getPos() );

		if (quest.isTurnedIn()){
			// thanks message
		} else if(quest.isCompleted()) {
			// turn in message and reward
		}

		if (quest.isStarted()){
			// reminder of the quest message
		} else {
			// introduction and instruction to the quest
		}

		return true;
	}
}


