package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.mechanics.spells.SpellFactory;
import com.nyrds.pixeldungeon.mechanics.spells.SpellHelper;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.windows.IconTitle;



public class WndHeroSpells extends Window {

    private final Listener listener;
    private final Hero     hero;

    public WndHeroSpells(Listener listener) {
        super();

        resize(WndHelper.getLimitedWidth(120), WndHelper.getFullscreenHeight() - WINDOW_MARGIN);
        this.listener = listener;
        this.hero = Dungeon.hero;

        String affinity = hero.getHeroClass().getMagicAffinity();

        Text title = PixelScene.createText(StringsManager.getVar(R.string.WndHero_SkillLevel) + ": " + hero.skillLevel(), GuiProperties.titleFontSize());
        title.hardlight(Window.TITLE_COLOR);
        title.setPos(width - title.width(), 0);
        add(title);

        IconTitle masteryTitle = new IconTitle(new Image(
                Assets.UI_ICONS_12,
                12,
                SpellHelper.iconIdByAffinity(affinity)),
                SpellHelper.getMasteryTitleByAffinity(affinity));

        masteryTitle.setRect(0, title.bottom() + 2, width, WndHelper.getFullscreenHeight());

        add(masteryTitle);

        var spellsSet = new VHBox(WndHelper.getLimitedWidth(120) - chrome.marginHor());
        spellsSet.setAlign(HBox.Align.Width);
        spellsSet.setGap(4);

        var spells = SpellFactory.getSpellsByAffinity(affinity);
        // var spells = SpellFactory.getAllSpells();
        for (String spellName : spells) {
            Spell spell = SpellFactory.getSpellByName(spellName);
            if (spell.level() > hero.skillLevel()) {
                continue;
            }
            spellsSet.add(new SpellButton(this, hero, spell));
        }

        spellsSet.setPos(chrome.marginLeft(), Math.max(title.bottom(), masteryTitle.bottom()) + 2);
        add(spellsSet);

        resize(WndHelper.getLimitedWidth(120), (int) (spellsSet.bottom() + chrome.marginBottom()));
    }


    public void onSpellClick(Spell spell){
        if (listener != null) {
            listener.onSelect(spell.itemForSlot(), hero);
        } else {
            GameScene.show(new WndSpellInfo(this, hero, spell));
        }
    }

    public interface Listener {
        void onSelect(Spell.SpellItem spell, Char selector);
    }

}