package com.nyrds.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.mechanics.spells.SpellFactory;
import com.nyrds.pixeldungeon.mechanics.spells.SpellHelper;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.ImageButton;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.windows.IconTitle;

import java.util.ArrayList;

public class WndHeroSpells extends Window {

    private static final String TXT_LVL = Game.getVar(R.string.WndHero_SkillLevel);

    private static final int WINDOW_MARGIN = 10;

    private Listener listener;
    private Hero     hero;

    public WndHeroSpells(Listener listener) {
        super();
        resize(WndHelper.getLimitedWidth(120), WndHelper.getFullscreenHeight() - WINDOW_MARGIN);
        this.listener = listener;
        this.hero = Dungeon.hero;

        String affinity = hero.heroClass.getMagicAffinity();

        Text title = PixelScene.createText(TXT_LVL + ": " + hero.magicLvl(), GuiProperties.titleFontSize());
        title.hardlight(Window.TITLE_COLOR);
        title.setPos(width - title.width(),0);
        add(title);

        IconTitle masteryTitle = new IconTitle(new Image(
                Assets.UI_ICONS_12,
                12,
                SpellHelper.iconIdByAffinity(affinity)),
                SpellHelper.getMasteryTitleByAffinity(affinity));

        masteryTitle.setRect(0, title.bottom() + 2, width, WndHelper.getFullscreenHeight());

        add(masteryTitle);

        HBox spellsSet = new HBox(WndHelper.getLimitedWidth(120) - chrome.marginHor());
        spellsSet.setAlign(HBox.Align.Width);

        ArrayList<String> spells = SpellFactory.getSpellsByAffinity(affinity);
        if (spells != null) {
            for (String spellName : spells) {
                if(SpellFactory.hasSpellForName(spellName)) {
                    Spell spell = SpellFactory.getSpellByName(spellName);
                    if (spell.level() > hero.magicLvl()) {
                        continue;
                    }
                    spellsSet.add(new SpellButton(spell));
                }
            }

            spellsSet.setPos(chrome.marginLeft(), Math.max(title.bottom(),masteryTitle.bottom()) + 2);
            add(spellsSet);
        }

        resize(WndHelper.getLimitedWidth(120), (int) (spellsSet.bottom() + chrome.marginBottom()));
    }

    public interface Listener {
        void onSelect(Spell.SpellItem spell);
    }

    private class SpellButton extends ImageButton {

        private final Spell      spell;
        protected     ColorBlock bg;

        public SpellButton(Spell spell) {
            super(spell.image());
            this.spell = spell;
        }

        @Override
        protected void createChildren() {
            super.createChildren();
            bg = new ColorBlock(width + 6, height + 6, 0xFF4A4D44);
            add(bg);
        }

        @Override
        protected void layout() {
            super.layout();
            bg.x = x - 3;
            bg.y = y - 3;
            bg.size(width + 6, height + 6);

            image.x = x;
            image.y = y;
        }

        @Override
        protected void onClick() {
            super.onClick();
            if (listener != null) {
                listener.onSelect(spell.itemForSlot());
            } else {
                GameScene.show(new WndSpellInfo(WndHeroSpells.this, hero, spell));
            }
        }

        @Override
        protected boolean onLongClick() {
            hide();
            QuickSlot.selectSlotFor(spell);
            return true;
        }

    }
}