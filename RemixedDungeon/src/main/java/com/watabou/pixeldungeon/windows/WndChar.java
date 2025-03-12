
package com.watabou.pixeldungeon.windows;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.elements.LabeledTab;
import com.watabou.pixeldungeon.windows.elements.Tab;

import lombok.val;

public class WndChar extends WndTabbed {
    public static final int WIDTH = 100;
    public static final int L_WIDTH = 140;

    private final Char target;
    private final Char selector;

    public WndChar(final Char chr, final Char selector) {

        super();
        target = chr;
        this.selector  = selector;

        if(chr.friendly(selector)) {
            CharUtils.mark(target);
            CharUtils.markTarget(target);
        }

        int width = RemixedDungeon.landscape() ? L_WIDTH : WIDTH;

        var desc = new CharDescTab(chr, selector, width);
        add(desc);

        var stats = new StatsTab(chr, width);
        add(stats);

        var buffs = new BuffsTab(chr, width);
        add(buffs);


        val infoTab = new LabeledTab(this, StringsManager.getVar(R.string.WndHero_Info)) {
            public void select(boolean value) {
                super.select(value);
                desc.setVisible(desc.setActive(selected));
            }
        };

        val statsTab = new LabeledTab(this, StringsManager.getVar(R.string.WndHero_Stats)) {
            public void select(boolean value) {
                super.select(value);
                stats.setVisible(stats.setActive(selected));
            }
        };

        val buffsTab = new LabeledTab(this, StringsManager.getVar(R.string.WndHero_Buffs)) {
            public void select(boolean value) {
                super.select(value);
                buffs.setVisible(buffs.setActive(selected));
            }
        };

        if (target instanceof Hero) {
            add(statsTab);
            add(buffsTab);
            add(infoTab);
        } else {
            add(infoTab);
            add(statsTab);
            add(buffsTab);
        }

        for (Tab tab : tabs) {
            tab.setSize((float) width /3, tabHeight());
        }

        resize(width, (int) Utils.max(desc.height() + GAP, stats.height() + GAP, buffs.height() + GAP));

        select(0);
    }

    @LuaInterface
    public Char getTarget() {
        return target;
    }

    @Override
    public void hide() {
        CharUtils.clearMarkers();
        super.hide();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        selector.readyAndIdle();
    }
}
