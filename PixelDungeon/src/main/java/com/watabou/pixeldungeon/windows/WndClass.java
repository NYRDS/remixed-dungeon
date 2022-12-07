/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Badges;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.elements.RankingTab;
import com.watabou.pixeldungeon.windows.elements.Tab;

public class WndClass extends WndTabbed {

	private static final String TXT_MASTERY = Game.getVar(R.string.WndClass_Mastery);

	private static final int WIDTH_P = 112;
	private static final int WIDTH_L = 160;

	private static int WIDTH = 110;

	private static final int TAB_WIDTH = 50;

	private HeroClass cl;

	public WndClass(HeroClass cl) {

		super();

		WIDTH = PixelDungeon.landscape() ? WIDTH_L : WIDTH_P;

		this.cl = cl;

		PerksTab tabPerks = new PerksTab();
		add(tabPerks);

		Tab tab = new RankingTab(this, Utils.capitalize(cl.title()), tabPerks);
		tab.setSize(TAB_WIDTH, tabHeight());
		add(tab);

		if (Badges.isUnlocked(cl.masteryBadge())) {
			MasteryTab tabMastery = new MasteryTab();
			add(tabMastery);

			tab = new RankingTab(this, TXT_MASTERY, tabMastery);
			tab.setSize(TAB_WIDTH, tabHeight());
			add(tab);

			resize(
					(int) Math.max(tabPerks.width, tabMastery.width),
					(int) Math.max(tabPerks.height, tabMastery.height));
		} else {
			resize((int) tabPerks.width, (int) tabPerks.height);
		}

		select(0);
	}

	private class PerksTab extends Group {

		private static final int MARGIN = 4;
		private static final int GAP    = 4;

		private static final String DOT = "#";

		public float height;
		public float width;

		public PerksTab() {
			super();

			float dotWidth = 0;

			String[] items = cl.perks();
			float pos = MARGIN;

			for (int i = 0; i < items.length; i++) {

				if (i > 0) {
					pos += GAP;
				}

				Text dot = PixelScene.createText(DOT, GuiProperties.smallFontSize());
				dot.x = MARGIN;
				dot.y = pos;
				if (dotWidth == 0) {
					dot.measure();
					dotWidth = dot.width();
				}
				add(dot);

				Text item = PixelScene.createMultiline(items[i], GuiProperties.regularFontSize());
				item.x = dot.x + dotWidth;
				item.y = pos;
				item.maxWidth((int) (WIDTH - MARGIN * 2 - dotWidth));
				item.measure();
				add(item);

				pos += item.height();
				float w = item.width();
				if (w > width) {
					width = w;
				}
			}

			width += MARGIN + dotWidth;
			height = pos + MARGIN;
		}
	}

	private class MasteryTab extends Group {

		private static final int MARGIN = 4;

		private Text normal;
		private Text highlighted;

		public float height;
		public float width;

		public MasteryTab() {
			super();

			String text = "";
			switch (cl) {
				case WARRIOR:
					text = HeroSubClass.GLADIATOR.desc() + "\n\n" + HeroSubClass.BERSERKER.desc();
					break;
				case MAGE:
					text = HeroSubClass.BATTLEMAGE.desc() + "\n\n" + HeroSubClass.WARLOCK.desc();
					break;
				case ROGUE:
					text = HeroSubClass.FREERUNNER.desc() + "\n\n" + HeroSubClass.ASSASSIN.desc();
					break;
				case HUNTRESS:
					text = HeroSubClass.SNIPER.desc() + "\n\n" + HeroSubClass.WARDEN.desc();
					break;
				case ELF:
					text = HeroSubClass.SCOUT.desc() + "\n\n" + HeroSubClass.SHAMAN.desc();
					break;
				case NECROMANCER:
					text = HeroSubClass.LICH.desc();
					break;
			}

			Highlighter hl = new Highlighter(text);

			normal = PixelScene.createMultiline(hl.text, GuiProperties.regularFontSize());
			if (hl.isHighlighted()) {
				normal.mask = hl.inverted();
			}
			normal.maxWidth(WIDTH - MARGIN * 2);
			normal.measure();
			normal.x = MARGIN;
			normal.y = MARGIN;
			add(normal);

			if (hl.isHighlighted()) {
				highlighted = PixelScene.createMultiline(hl.text, GuiProperties.regularFontSize());
				highlighted.mask = hl.mask;

				highlighted.maxWidth(normal.getMaxWidth());
				highlighted.measure();
				highlighted.x = normal.x;
				highlighted.y = normal.y;
				add(highlighted);

				highlighted.hardlight(TITLE_COLOR);
			}

			height = normal.y + normal.height() + MARGIN;
			width = normal.x + normal.width() + MARGIN;
		}
	}
}
