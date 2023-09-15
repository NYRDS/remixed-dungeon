package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Group;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

class StatsTab extends Group {

    private static final int GAP = 2;

    private final WndHero wndHero;
    private float pos;

    public StatsTab(WndHero wndHero) {
        this.wndHero = wndHero;

        final Hero hero = Dungeon.hero;

        Text title = PixelScene.createText(
                Utils.format(R.string.WndHero_StaTitle, hero.lvl(), hero.className()).toUpperCase(), GuiProperties.titleFontSize());
        title.hardlight(Window.TITLE_COLOR);
        add(title);

        RedButton btnCatalogus = new RedButton(R.string.WndHero_StaCatalogus) {
            @Override
            protected void onClick() {
                wndHero.hide();
                GameScene.show(new WndCatalogus());
            }
        };
        btnCatalogus.setRect(0, title.getY() + title.height(), btnCatalogus.reqWidth() + 2, btnCatalogus.reqHeight() + 2);
        add(btnCatalogus);

        RedButton btnJournal = new RedButton(R.string.WndHero_StaJournal) {
            @Override
            protected void onClick() {
                wndHero.hide();
                GameScene.show(new WndJournal());
            }
        };
        btnJournal.setRect(
                btnCatalogus.right() + 1, btnCatalogus.top(),
                btnJournal.reqWidth() + 2, btnJournal.reqHeight() + 2);
        add(btnJournal);

        pos = btnCatalogus.bottom() + GAP;

        statSlot(StringsManager.getVar(R.string.WndHero_Health), hero.hp() + "/" + hero.ht());
        statSlot(StringsManager.getVar(R.string.Mana_Title), hero.getSkillPoints() + "/" + hero.getSkillPointsMax());

        Hunger hunger = hero.hunger();

        statSlot(StringsManager.getVar(R.string.WndHero_Satiety),
                Utils.EMPTY_STRING + ((int) ((Hunger.STARVING - hunger.getHungerLevel()) / Hunger.STARVING * 100)) + "%");

        statSlot(StringsManager.getVar(R.string.WndHero_Stealth), hero.stealth());

        statSlot(StringsManager.getVar(R.string.WndHero_Awareness), Utils.EMPTY_STRING + (int) (hero.getAwareness() * 100) + "%");

        statSlot(StringsManager.getVar(R.string.WndHero_AttackSkill), hero.attackSkill(CharsList.DUMMY));
        statSlot(StringsManager.getVar(R.string.WndHero_DefenceSkill), hero.defenseSkill(CharsList.DUMMY));


        statSlot(StringsManager.getVar(R.string.WndHero_Exp), hero.getExp() + "/" + hero.maxExp());

        pos += GAP;
        statSlot(StringsManager.getVar(R.string.WndHero_Str), hero.effectiveSTR());
        statSlot(StringsManager.getVar(R.string.WndHero_SkillLevel), hero.skillLevel());

        statSlot(StringsManager.getVar(R.string.WndHero_Gold), Statistics.goldCollected);
        statSlot(StringsManager.getVar(R.string.WndHero_Depth), Statistics.deepestFloor);


        pos += GAP;
    }

    private void statSlot(String label, String value) {

        Text txt = PixelScene.createText(label, GuiProperties.regularFontSize());
        txt.setY(pos);
        add(txt);

        txt = PixelScene.createText(value, GuiProperties.regularFontSize());
        txt.setX(PixelScene.align(WndHero.WIDTH * 0.65f));
        txt.setY(pos);
        add(txt);

        pos += GAP + txt.baseLine();
    }

    private void statSlot(String label, int value) {
        statSlot(label, Integer.toString(value));
    }

    public float height() {
        return pos;
    }
}
