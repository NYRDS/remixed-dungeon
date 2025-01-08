package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.platform.util.PUtil;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.elements.TabContent;

class StatsTab extends TabContent {

    private static final int GAP = 2;

    private float pos;

    public StatsTab(final Char chr, int width) {
        Text title = PixelScene.createText(
                Utils.format(R.string.WndHero_StaTitle, chr.lvl(), chr.className()).toUpperCase(), GuiProperties.titleFontSize());
        title.hardlight(Window.TITLE_COLOR);
        add(title);

        if (chr instanceof Hero) {
            RedButton btnCatalogus = new RedButton(R.string.WndHero_StaCatalogus) {
                @Override
                protected void onClick() {
                    GameScene.show(new WndCatalogus());
                }
            };
            btnCatalogus.setRect(0, title.getY() + title.height(), btnCatalogus.reqWidth() + 2, btnCatalogus.reqHeight() + 2);
            add(btnCatalogus);

            RedButton btnJournal = new RedButton(R.string.WndHero_StaJournal) {
                @Override
                protected void onClick() {
                    GameScene.show(new WndJournal());
                }
            };
            btnJournal.setRect(
                    btnCatalogus.right() + 1, btnCatalogus.top(),
                    btnJournal.reqWidth() + 2, btnJournal.reqHeight() + 2);
            add(btnJournal);

            pos = btnCatalogus.bottom() + GAP;
        }   else {
            pos = title.bottom() + GAP;
        }

        statSlot(R.string.WndHero_Health, chr.hp() + "/" + chr.ht());
        statSlot(R.string.Mana_Title, chr.getSkillPoints() + "/" + chr.getSkillPointsMax());

        int dmgMin = Integer.MAX_VALUE, dmgMax = 0;
        int drMin = Integer.MAX_VALUE, drMax = 0;

        for (int i =0;i<100;i++) {
            int dmg = chr.damageRoll();
            if (dmg < dmgMin) {
                dmgMin = dmg;
            }
            if (dmg > dmgMax) {
                dmgMax = dmg;
            }

            int dr = chr.defenceRoll(CharsList.DUMMY);
            if (dr < drMin) {
                drMin = dr;
            }
            if (dr > drMax) {
                drMax = dr;
            }
        }

        float timeScale = chr.timeScale();


        String chrSpeed = "???";
        String chrAttackDelay = "???";

        try {
            chrSpeed = Utils.format("%3.2f%%", chr.speed()*100 / timeScale);
            chrAttackDelay = Utils.format("%3.2f", chr.attackDelay()*timeScale);
        } catch (Exception e) {
        }


        statSlot(R.string.Typical_Damage, Utils.format("%d - %d", dmgMin, dmgMax));
        statSlot(R.string.Damage_Reduction, Utils.format("%d - %d", drMin, drMax));
        statSlot(R.string.Movement_Speed, chrSpeed);
        statSlot(R.string.Attack_Cooldown, chrAttackDelay);
        statSlot(R.string.Actions_Speed, Utils.format("%3.2f%%" ,100/timeScale));
        statSlot(R.string.Attack_Range, Utils.EMPTY_STRING + chr.getAttackRange());
        statSlot(R.string.View_Distance, Utils.EMPTY_STRING + chr.getViewDistance());


        if(chr instanceof Hero) {
            Hunger hunger = chr.hunger();

            statSlot(R.string.WndHero_Satiety,
                    Utils.EMPTY_STRING + ((int) ((Hunger.STARVING - hunger.getHungerLevel()) / Hunger.STARVING * 100)) + "%");

            statSlot(R.string.WndHero_Stealth, chr.stealth());

            Hero hero = ((Hero) chr);
            statSlot(R.string.WndHero_Awareness, Utils.format("%3.2f%%",(hero.getAwareness() * 100)));
        }

        statSlot(R.string.WndHero_AttackSkill, chr.attackSkill(CharsList.DUMMY));
        statSlot(R.string.WndHero_DefenceSkill, chr.defenseSkill(CharsList.DUMMY));


        statSlot(R.string.WndHero_Exp, chr.getExpForLevelUp() + "/" + chr.expToLevel());

        pos += GAP;
        statSlot(R.string.WndHero_Str, chr.effectiveSTR());
        statSlot(R.string.WndHero_SkillLevel, chr.skillLevel());

        pos += GAP;
        setMaxWidth(width);
    }

    private void statSlot(int label, String value) {

        Text txt = PixelScene.createText(StringsManager.getVar(label), GuiProperties.regularFontSize());
        txt.setY(pos);
        add(txt);

        txt = PixelScene.createText(value, GuiProperties.regularFontSize());

        txt.setX(PixelScene.align(maxWidth - txt.width() - GAP));
        PUtil.slog("text","txt:"+txt.width());
        txt.setY(pos);
        add(txt);

        pos += txt.baseLine();
    }

    private void statSlot(int label, int value) {
        statSlot(label, Integer.toString(value));
    }

    public float height() {
        return pos;
    }
}
