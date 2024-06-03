
package com.watabou.pixeldungeon.windows;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.elements.LabeledTab;
import com.watabou.pixeldungeon.windows.elements.Tab;

import lombok.val;

public class WndChar extends WndTabbed {
    public static final int WIDTH = 100;
    private static final int TAB_WIDTH = 33;
    private final Char target;
    private final Char selector;

    public WndChar(final Char chr, final Char selector) {

        super();
        target = chr;
        this.selector  = selector;

        var desc = new CharDescTab(chr, selector, WIDTH);
        add(desc);

        var stats = new StatsTab(chr);
        add(stats);

        var buffs = new BuffsTab(chr);
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
            tab.setSize(TAB_WIDTH, tabHeight());
        }


        resize(WIDTH, (int) Utils.max(desc.height() + GAP,stats.height() + GAP, buffs.height() + GAP));

        select(0);
    }

    @LuaInterface
    public Char getTarget() {
        return target;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        selector.readyAndIdle();
    }
}
