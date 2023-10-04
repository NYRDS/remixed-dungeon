
package com.watabou.pixeldungeon.windows;

import com.nyrds.platform.game.Game;
import com.watabou.pixeldungeon.Chrome;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.windows.elements.Tab;
import com.watabou.pixeldungeon.windows.elements.TabContent;

import java.util.ArrayList;

public class WndTabbed extends Window {

    protected ArrayList<Tab> tabs = new ArrayList<>();
    protected Tab selected;

    public WndTabbed() {
        super(0, 0, Chrome.get(Chrome.Type.TAB_SET));
    }

    protected Tab add(Tab tab) {

        tab.setPos(tabs.size() == 0 ?
                -chrome.marginLeft() + 1 :
                tabs.get(tabs.size() - 1).right(), height);
        tab.select(false);
        super.add(tab);

        tabs.add(tab);

        return tab;
    }

    public void select(int index) {
        select(tabs.get(index));
    }

    public void select(Tab tab) {
        if (tab != selected) {
            for (Tab t : tabs) {
                if (t == selected) {
                    t.select(false);
                } else if (t == tab) {
                    t.select(true);
                }
            }

            selected = tab;
        }
    }

    @Override
    public void resize(int w, int h) {
        // -> super.resize(...)
        this.width = w;
        this.height = h;

        chrome.size(
                width + chrome.marginHor(),
                height + chrome.marginVer());

        camera.resize((int) chrome.width, chrome.marginTop() + height + tabHeight());
        camera.x = (int) (Game.width() - camera.screenWidth()) / 2;
        camera.y = (int) (Game.height() - camera.screenHeight()) / 2;
        // <- super.resize(...)

        for (Tab tab : tabs) {
            remove(tab);
        }

        ArrayList<Tab> tabs = new ArrayList<>(this.tabs);
        this.tabs.clear();

        for (Tab tab : tabs) {
            add(tab);
        }

        for (var child: members) {
            if (child instanceof TabContent) {
                ((TabContent) child).setMaxWidth(width);
            }
        }
    }

    protected int tabHeight() {
        return 25;
    }

    public void onClick(Tab tab) {
        select(tab);
    }

}
