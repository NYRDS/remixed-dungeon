
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.WndHelper;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.Logbook;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.windows.elements.LabeledTab;
import com.watabou.pixeldungeon.windows.elements.Tab;

import java.util.Collections;

public class WndJournal extends WndTabbed {

    private static final int LEVEL_ITEM_HEIGHT = 18;    // Height of a level entry

    private ScrollPane list;

    public WndJournal() {

        super();

        resize(WndHelper.getLimitedWidth(120), WndHelper.getFullscreenHeight() - tabHeight() - MARGIN);

        Text txtTitle = PixelScene.createText(StringsManager.getVar(R.string.WndJournal_Title), GuiProperties.titleFontSize());
        txtTitle.hardlight(Window.TITLE_COLOR);
        txtTitle.setX(PixelScene.align(PixelScene.uiCamera, (width - txtTitle.width()) / 2));
        add(txtTitle);

        list = new ScrollPane(new Component());

        add(list);
        list.setRect(0, txtTitle.height(), width, height - txtTitle.height());

        Tab[] tabs = {    // Create two tabs that will be in the bottom of the window
                new JournalTab(),
                new LogbookTab()
        };

        for (Tab tab : tabs) {    // Add the tab buttons to the window
            tab.setSize(width / tabs.length, tabHeight());
            add(tab);
        }

        select(0);    // Select the first tab and update the list
    }

    private static class ListLevelItem extends Component {

        private Text feature;
        private Text depth;

        private Image icon;

        ListLevelItem(String text, int d) {
            super();

            feature.text(text);

            depth.text(Integer.toString(d));

            if (d == Dungeon.depth) {
                feature.hardlight(TITLE_COLOR);
                depth.hardlight(TITLE_COLOR);
            }
        }

        @Override
        protected void createChildren() {
            feature = PixelScene.createText(GuiProperties.titleFontSize());
            add(feature);

            depth = Text.createBasicText(PixelScene.font1x);
            add(depth);

            icon = Icons.get(Icons.DEPTH);
            add(icon);
        }

        @Override
        protected void layout() {
            icon.setX(width - icon.width);

            depth.setX(icon.getX() - 1 - depth.width());
            depth.setY(PixelScene.align(y + (height - depth.height()) / 2));

            icon.setY(depth.getY() - 1);

            feature.setY(PixelScene.align(depth.getY() + depth.baseLine() - feature.baseLine()));
        }
    }

    private static class ListLogItem extends Component {    // Class for a row in the log book

        private Text logEntry;

        ListLogItem(Logbook.logBookEntry entry, int maxWidth) {
            super();

            logEntry.text(entry.text);    // Add the text of log book entry
            logEntry.hardlight(entry.color);

            logEntry.maxWidth(maxWidth);
        }

        @Override
        public float height() {
            return logEntry.height();
        }

        @Override
        protected void createChildren() {
            logEntry = PixelScene.createMultiline(GuiProperties.titleFontSize());
            add(logEntry);
        }

        @Override
        protected void layout() {
            logEntry.setY(PixelScene.align(y));
        }
    }

    private abstract class ContentTab extends LabeledTab {
        protected float y;

        ContentTab(WndTabbed tabbed, String label) {
            super(tabbed, label);
        }

        @Override
        public void select(boolean value) {
            super.select(value);
            if (!value) {
                return;
            }
            y = 0;
            list.content().clear();
            createContent();
            list.content().setSize(WndJournal.this.width, y);
            setScrollPosition();
        }

        protected void setScrollPosition() {
        }

        abstract protected void createContent();
    }

    private class JournalTab extends ContentTab {
        JournalTab() {
            super(WndJournal.this, StringsManager.getVar(R.string.WndJournal_Levels));
        }

        @Override
        protected void createContent() {
            Collections.sort(Journal.records);

            for (Journal.Record rec : Journal.records) {
                ListLevelItem item = new ListLevelItem(rec.getFeature(), rec.depth);
                item.setRect(0, y, WndJournal.this.width, LEVEL_ITEM_HEIGHT);
                list.content().add(item);

                y += item.height();
            }
        }

        @Override
        protected void setScrollPosition() {
            list.scrollTo(0, 0);    // Scroll to the beginning
        }
    }

    private class LogbookTab extends ContentTab {
        LogbookTab() {
            super(WndJournal.this, StringsManager.getVar(R.string.WndJournal_Logbook));
        }

        @Override
        protected void setScrollPosition() {
            if (y >= list.height()) {    // If scrollable, scroll to bottom to see the latest message
                list.scrollTo(0, y - list.height());
            } else {
                list.scrollTo(0, 0);    // Scroll to beginning
            }
        }

        @Override
        protected void createContent() {
            for (Logbook.logBookEntry rec : Logbook.logbookEntries) {
                ListLogItem item = new ListLogItem(rec, WndJournal.this.width);
                item.setRect(0, y, WndJournal.this.width, item.height());
                list.content().add(item);

                y += item.height();
            }
        }
    }
}
