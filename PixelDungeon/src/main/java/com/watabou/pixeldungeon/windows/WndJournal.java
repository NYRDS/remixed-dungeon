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
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.WndHelper;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.elements.LabeledTab;
import com.watabou.pixeldungeon.windows.elements.Tab;

import java.util.Collections;

public class WndJournal extends WndTabbed {

	private static final int LEVEL_ITEM_HEIGHT	= 18;	// Height of a level entry
	private static final int LOGBOOK_ITEM_HEIGHT = 12;	// Height of a log book entry

	private static final String TXT_TITLE	= Game.getVar(R.string.WndJournal_Title);
	private static final String TXT_LEVELS	= Game.getVar(R.string.WndJournal_Levels);
	private static final String TXT_LOGBOOK	= Game.getVar(R.string.WndJournal_Logbook);

	private Text       txtTitle;
	private ScrollPane list;

	private static boolean showLevels = true;	// Indicates which tab is visible

	public WndJournal() {
		
		super();

		resize(WndHelper.getLimitedWidth(120), WndHelper.getFullscreenHeight() - 4*MARGIN);


		txtTitle = PixelScene.createText(TXT_TITLE, GuiProperties.titleFontSize());
		txtTitle.hardlight(Window.TITLE_COLOR);
		txtTitle.measure();
		txtTitle.x = PixelScene.align( PixelScene.uiCamera, (width - txtTitle.width()) / 2 );
		add(txtTitle);

		list = new ScrollPane(new Component());

		add(list);
		list.setRect(0, txtTitle.height(), width, height - txtTitle.height());

		boolean showLevels = WndJournal.showLevels;
		Tab[] tabs = {	// Create two tabs that will be in the bottom of the window
				new LabeledTab(this, TXT_LEVELS) {
					public void select(boolean value) {
						super.select(value);
						WndJournal.showLevels = value;
						updateList();
					}
				},
				new LabeledTab(this, TXT_LOGBOOK) {
					public void select(boolean value) {
						super.select(value);
						WndJournal.showLevels = !value;
						updateList();
					}
				}
		};
		for (Tab tab : tabs) {	// Add the tab buttons to the window
			tab.setSize(width / tabs.length, tabHeight());
			add(tab);
		}

		select(showLevels ? 0 : 1);	// Select the first tab and update the list
	}
	
	private static class ListLevelItem extends Component {
		
		private Text feature;
		private Text depth;
		
		private Image icon;
		
		public ListLevelItem(String text, int d ) {	// This constructor is for level item - should include depth
			super();
			
			feature.text( text );
			feature.measure();
			
			depth.text( Integer.toString( d ) );
			depth.measure();
			
			if (d == Dungeon.depth) {
				feature.hardlight( TITLE_COLOR );
				depth.hardlight( TITLE_COLOR );
			}
		}

		public ListLevelItem(String text ) {	// This constructor is for log book messages, no depth
			super();

			feature.text( text );	// Add the text of log book entry
			feature.measure();

			this.remove(depth);	// Remove depth and icon - log book entry has none of these
			this.remove(icon);
		}
		
		@Override
		protected void createChildren() {
			feature = PixelScene.createText(GuiProperties.titleFontSize());
			add( feature );
			
			depth = Text.createBasicText( PixelScene.font1x );
			add( depth );
			
			icon = Icons.get( Icons.DEPTH );
			add( icon );
		}
		
		@Override
		protected void layout() {

			if(showLevels) {	// Only layout depth and icon if showing level entries
				icon.x = width - icon.width;

				depth.x = icon.x - 1 - depth.width();
				depth.y = PixelScene.align(y + (height - depth.height()) / 2);

				icon.y = depth.y - 1;

				feature.y = PixelScene.align( depth.y + depth.baseLine() - feature.baseLine() );
			} else {	// Layout needed only for feature text which is the log book entry
				feature.y = PixelScene.align( y );
			}
		}
	}

	private void updateList(){	// Update the list according to the selected tab
		Component content = list.content();
		content.clear();
		list.scrollTo( 0, 0);	// Scroll to beginning in both cases

		if(showLevels) {	// Showing levels
			Collections.sort(Journal.records);

			float pos = 0;
			for (Journal.Record rec : Journal.records) {
				ListLevelItem item = new ListLevelItem(rec.getFeature(), rec.depth);
				item.setRect(0, pos, width, LEVEL_ITEM_HEIGHT);
				content.add(item);

				pos += item.height();
			}

			content.setSize(width, pos);
		} else {	// Showing log book
			float pos = 0;
			for (String rec : GLog.logbookEntries) {
				ListLevelItem item = new ListLevelItem( rec );
				item.setRect(0, pos, width, LOGBOOK_ITEM_HEIGHT);
				content.add(item);

				pos += item.height();
			}

			content.setSize(width, pos);
			if( pos >= list.height() ) {	// If scrollable, scroll to bottom to see the latest message
				list.scrollTo( 0, pos - list.height() );
			}
		}
	}
}
