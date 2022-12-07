package com.nyrds.retrodungeon.mobs.npc;

import com.nyrds.retrodungeon.items.food.Candy;
import com.nyrds.retrodungeon.items.food.PumpkinPie;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndQuest;
import com.watabou.pixeldungeon.windows.WndTradeItem;
import com.watabou.utils.Bundle;

public class ScarecrowNPC extends ImmortalNPC {

	private static final String TXT_QUEST_START_M = Game.getVar(R.string.ScarecrowNPC_Quest_Start_Male);
	private static final String TXT_QUEST_START_F = Game.getVar(R.string.ScarecrowNPC_Quest_Start_Female);
	private static final String TXT_QUEST_END     = Game.getVar(R.string.ScarecrowNPC_Quest_End);
	private static final String TXT_QUEST         = Game.getVar(R.string.ScarecrowNPC_Quest_Reminder);

	public ScarecrowNPC() {
	}

	@Override
	protected boolean act() {
		throwItem();

		getSprite().turnTo(getPos(), Dungeon.hero.getPos());
		spend(TICK);
		return true;
	}

	private static WndBag sell() {
		return GameScene.selectItem(itemSelector, WndBag.Mode.FOR_SALE, Game.getVar(R.string.Shopkeeper_Sell));
	}

	private static WndBag.Listener itemSelector = new WndBag.Listener() {
		@Override
		public void onSelect(Item item) {
			if (item != null) {
				WndBag parentWnd = sell();
				GameScene.show(new WndTradeItem(item, parentWnd));
			}
		}
	};

	public static void spawn(RegularLevel level) {
		ScarecrowNPC npc = new ScarecrowNPC();
		npc.setPos(level.randomRespawnCell());
		level.spawnMob(npc);
	}

	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo(getPos(), hero.getPos());
		if (Quest.completed) {
			this.die(null);
			return true;
		}

		if (Quest.given) {

			Item item = hero.belongings.getItem(Candy.class);
			if (item != null && item.quantity() == 5) {

				item.removeItemFrom(Dungeon.hero);

				Item reward = new PumpkinPie();
				reward.quantity(5);

				if (reward.doPickUp(Dungeon.hero)) {
					GLog.i(Hero.TXT_YOU_NOW_HAVE, reward.name());
				} else {
					Dungeon.level.drop(reward, hero.getPos()).sprite.drop();
				}
				Quest.complete();
				GameScene.show(new WndQuest(this, TXT_QUEST_END));
			} else {
				GameScene.show(new WndQuest(this, TXT_QUEST));
			}

		} else {
			String txtQuestStart = TXT_QUEST_START_M;
			if (Dungeon.hero.getGender() == Utils.FEMININE) {
				txtQuestStart = TXT_QUEST_START_F;
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
					Dungeon.level.drop(new Candy(), pos).sprite.drop();
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


