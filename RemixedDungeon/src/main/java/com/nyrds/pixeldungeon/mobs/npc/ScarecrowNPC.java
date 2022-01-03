package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.items.food.Candy;
import com.nyrds.pixeldungeon.items.food.PumpkinPie;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndQuest;
import com.watabou.utils.Bundle;

public class ScarecrowNPC extends ImmortalNPC {

	public ScarecrowNPC() {
	}

	@Override
    public boolean act() {
		ItemUtils.throwItemAway(getPos());

		getSprite().turnTo(getPos(), Dungeon.hero.getPos());
		spend(TICK);
		return true;
	}

	public static void spawn(RegularLevel level) {
		ScarecrowNPC npc = new ScarecrowNPC();
		npc.setPos(level.randomRespawnCell());
		level.spawnMob(npc);
	}

	@Override
	public boolean interact(final Char hero) {
		getSprite().turnTo(getPos(), hero.getPos());
		if (Quest.completed) {
			this.die(CharsList.DUMMY);
			return true;
		}

		if (Quest.given) {

			Item item = hero.getBelongings().getItem(Candy.class);
			if (item != null && item.quantity() == 5) {

				item.removeItemFrom(Dungeon.hero);

				Item reward = new PumpkinPie();
				reward.quantity(5);

				if (reward.doPickUp(Dungeon.hero)) {
					GLog.i(Hero.getHeroYouNowHave(), reward.name());
				} else {
					level().animatedDrop(reward, hero.getPos());
				}
				Quest.complete();
                GameScene.show(new WndQuest(this, StringsManager.getVar(R.string.ScarecrowNPC_Quest_End)));
			} else {
                GameScene.show(new WndQuest(this, StringsManager.getVar(R.string.ScarecrowNPC_Quest_Reminder)));
			}

		} else {
            String txtQuestStart = StringsManager.getVar(R.string.ScarecrowNPC_Quest_Start_Male);
			if (Dungeon.hero.getGender() == Utils.FEMININE) {
                txtQuestStart = StringsManager.getVar(R.string.ScarecrowNPC_Quest_Start_Female);
			}
			GameScene.show(new WndQuest(this, txtQuestStart));
			Quest.given = true;
			Quest.process(hero.getPos());
			Journal.add(Journal.Feature.SCARECROW.desc());
		}
		return true;
	}

	public static class Quest {

		private static boolean completed;
		private static boolean given;
		private static boolean processed;

		private static int   depth;
		private static float killed;

		public static void reset() {
			completed = false;
			processed = false;
			given = false;
		}

		private static final String COMPLETED = "completed";
		private static final String NODE      = "scarecrow";
		private static final String GIVEN     = "given";
		private static final String PROCESSED = "processed";
		private static final String DEPTH     = "depth";
		private static final String KILLED    = "killed";

		public static void storeInBundle(Bundle bundle) {
			Bundle node = new Bundle();

			node.put(GIVEN, given);
			node.put(DEPTH, depth);
			node.put(PROCESSED, processed);
			node.put(COMPLETED, completed);
			node.put(KILLED, killed);

			bundle.put(NODE, node);
		}

		public static void restoreFromBundle(Bundle bundle) {

			Bundle node = bundle.getBundle(NODE);

			if (!node.isNull()) {
				given = node.getBoolean(GIVEN);
				depth = node.getInt(DEPTH);
				processed = node.getBoolean(PROCESSED);
				completed = node.getBoolean(COMPLETED);
				killed = node.getFloat(KILLED);
			}
		}

		public static void process(int pos) {
			if (given && !processed) {
				killed++;
				if (killed != 0 && ((killed % 5) == 0)) {
					Dungeon.level.animatedDrop(new Candy(), pos);
				}
				if (killed >= 25) {
					processed = true;
				}
			}
		}

		static void complete() {
			completed = true;
			Journal.remove(Journal.Feature.SCARECROW.desc());
		}
	}
}


