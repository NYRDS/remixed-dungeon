
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.nyrds.util.Util;
import com.watabou.noosa.Group;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Highlighter;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.elements.RankingTab;
import com.watabou.pixeldungeon.windows.elements.Tab;

public class WndClass extends WndTabbed {

    private static final int WIDTH_P = 112;
    private static final int WIDTH_L = 160;

    private static int WIDTH = 110;

    private static final int TAB_WIDTH = 50;

    private final HeroClass cl;

    public WndClass(HeroClass cl) {

        super();

        WIDTH = RemixedDungeon.landscape() ? WIDTH_L : WIDTH_P;

        this.cl = cl;

        PerksTab tabPerks = new PerksTab();
        add(tabPerks);

        Tab tab = new RankingTab(this, Utils.capitalize(cl.title()), tabPerks);
        tab.setSize(TAB_WIDTH, tabHeight());
        add(tab);

        if (Badges.isUnlocked(cl.masteryBadge()) || Util.isDebug()) {
            MasteryTab tabMastery = new MasteryTab();
            add(tabMastery);

            tab = new RankingTab(this, StringsManager.getVar(R.string.WndClass_Mastery), tabMastery);
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

        public  float width, height;

        public PerksTab() {
            super();

            Text desc = PixelScene.createMultiline(cl.getDescription(), GuiProperties.regularFontSize());
            desc.maxWidth((int) (WIDTH - MARGIN * 2));
            desc.setPos(MARGIN, MARGIN);

            width = 2*MARGIN + desc.width();
            height = 2*MARGIN + desc.height();

            add(desc);
        }
    }

    private class MasteryTab extends Group {

        private static final int MARGIN = 4;

        public float height;
        public float width;

        public MasteryTab() {
            super();

            String text = Utils.EMPTY_STRING;
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
                case GNOLL:
                    text = HeroSubClass.GUARDIAN.desc() + "\n\n" + HeroSubClass.WITCHDOCTOR.desc();
                    break;
            }

            Text normal = Highlighter.addHilightedText(MARGIN, MARGIN, WIDTH - MARGIN * 2, this, text);

            height = normal.getY() + normal.height() + MARGIN;
            width = normal.getX() + normal.width() + MARGIN;
        }
    }
}
