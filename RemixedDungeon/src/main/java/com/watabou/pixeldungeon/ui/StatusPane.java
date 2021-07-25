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
package com.watabou.pixeldungeon.ui;

import com.nyrds.market.MarketOptions;
import com.nyrds.platform.input.Touchscreen.Touch;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.Text;
import com.watabou.noosa.TouchArea;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.particles.BloodParticle;
import com.watabou.pixeldungeon.items.Amulet;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndGame;
import com.watabou.pixeldungeon.windows.WndHats;
import com.watabou.pixeldungeon.windows.WndHero;

public class StatusPane extends Component {

    private NinePatch shield;
    private Image     avatar;
    private Emitter   blood;

    private Image hp;
    private Image sp;
    private Image exp;

    private int lastLvl  = -1;
    private int lastKeys = -1;

    private Text level;
    private Text depth;
    private Text keys;
    private Text hpText;
    private Text manaText;


    private DangerIndicator danger;
    private LootIndicator   loot;
    private BuffIndicator   buffs;
    private Compass         compass;

    private MenuButton btnMenu;
    private MenuButton btnHats;


    private final Level currentLevel;
    private final Hero  hero;

    public StatusPane(Hero hero, Level level) {
        super(true);
        this.hero = hero;
        this.currentLevel = level;
        createChildren();
    }

    @Override
    protected void createChildren() {

        shield = new NinePatch(Assets.getStatus(), 80, 0, 30 + 18, 0);
        add(shield);

        add(new TouchArea(0, 1, 30, 30) {
            @Override
            protected void onClick(Touch touch) {
                Image sprite = hero.getSprite();
                if (!sprite.isVisible()) {
                    Camera.main.focusOn(sprite);
                }
                GameScene.show(new WndHero());
            }
        });

        avatar = hero.getSprite().avatar();
        add(avatar);

        blood = new Emitter();
        blood.pos(avatar);
        blood.pour(BloodParticle.FACTORY, 0.3f);
        blood.autoKill = false;
        blood.on = false;
        add(blood);

        int compassTarget = currentLevel.entrance;

        if (currentLevel.hasCompassTarget()) {
            compassTarget = currentLevel.getCompassTarget();    // Set to compass target if exists
        } else if ( currentLevel.hasExit(0)
                    && hero.getBelongings().getItem(Amulet.class) == null) {
            compassTarget = currentLevel.getExit(0);    // Set to first exit if exists
        }

        compass = new Compass(compassTarget, currentLevel);
        add(compass);


        hp = new Image(Assets.HP_BAR);
        add(hp);

        sp = new Image(Assets.SP_BAR);
        add(sp);

        exp = new Image(Assets.XP_BAR);
        add(exp);

        hpText = new BitmapText(PixelScene.font1x);
        hpText.hardlight(0x777777);
        hpText.setScale(0.5f,0.5f);
        add(hpText);

        manaText = new BitmapText(PixelScene.font1x);
        manaText.hardlight(0xaaaaaa);
        manaText.setScale(0.5f,0.5f);
        add(manaText);

        level = new BitmapText(PixelScene.font1x);
        level.hardlight(0xFFEBA4);
        add(level);

        depth = new BitmapText(Integer.toString(Dungeon.depth), PixelScene.font1x);
        depth.hardlight(0xCACFC2);
        add(depth);

        IronKey.countIronKeys();
        keys = new BitmapText(PixelScene.font1x);
        keys.hardlight(0xCACFC2);
        add(keys);

        danger = new DangerIndicator();
        add(danger);

        loot = new LootIndicator();
        add(loot);

        buffs = new BuffIndicator(hero);
        add(buffs);

        btnMenu = new MenuButton(new Image(Assets.getStatus(), 114, 3, 12, 11), WndGame.class);
        add(btnMenu);

        btnHats = new MenuButton(new Image(Assets.getStatus(), 114, 18, 12, 11), WndHats.class);

        if (!MarketOptions.haveHats()) {
            btnHats.enable(false);
        }

        add(btnHats);
    }

    @Override
    protected void layout() {
        shield.size(width, shield.height);

        avatar.x = PixelScene.align(camera(), shield.x + 15 - avatar.width / 2);
        avatar.y = PixelScene.align(camera(), shield.y + 16 - avatar.height / 2);

        compass.x = avatar.x + avatar.width / 2 - compass.origin.x;
        compass.y = avatar.y + avatar.height / 2 - compass.origin.y;

        hp.x = 30;
        hp.y = 3;

        sp.x = 30;
        sp.y = 9;

        hpText.x = 30;
        hpText.y = 3.5f;

        manaText.x = 30;
        manaText.y = 9.5f;

        level.x = PixelScene.align(27.0f - level.width() / 2);
        level.y = PixelScene.align(27.5f - level.baseLine() / 2);

        depth.x = width - 24 - depth.width() - 18;
        depth.y = 6;

        keys.x = width - 8 - keys.width() - 18;
        keys.y = 6;

        danger.setPos(width - danger.width(), 40);

        loot.setPos(width - loot.width(), danger.bottom() + 2);

        buffs.setPos(35, 16);

        btnMenu.setPos(width - btnMenu.width(), 1);
        btnHats.setPos(width - btnHats.width(), btnMenu.bottom());
    }

    @Override
    public float bottom() {
        return btnHats.bottom();
    }

    @Override
    public void update() {
        super.update();

        Char chr = hero.getControlTarget();
        if(chr.invalid()) {
            return;
        }

        int hp = chr.hp();
        int ht = chr.ht();

        float health =  (float) hp / ht;

        hpText.text(Utils.format("%d/%d",hp, ht));

        int sp = chr.getSkillPoints();
        int st = chr.getSkillPointsMax();

        float sPoints = (float) sp / st;
        manaText.text(Utils.format("%d/%d",sp, st));

        if(avatar!=chr.getSprite().avatar()) {
            remove(avatar);
            avatar = chr.getSprite().avatar();
            add(avatar);
            layout();
        }

        if (health == 0) {
            avatar.tint(0x000000, 0.6f);
            blood.on = false;
        } else if (health < 0.25f) {
            avatar.tint(0xcc0000, 0.4f);
            blood.on = true;
        } else {
            avatar.resetColor();
            blood.on = false;
        }

        this.hp.Scale().x = health;
        this.sp.Scale().x = sPoints;
        exp.Scale().x = (width / exp.width) * hero.getExp() / hero.maxExp();

        if (chr.lvl() != lastLvl) {

            if (lastLvl != -1) {
                Emitter emitter = (Emitter) recycle(Emitter.class);
                emitter.revive();
                emitter.pos(27, 27);
                emitter.burst(Speck.factory(Speck.STAR), 12);
            }

            lastLvl = chr.lvl();
            level.text(Integer.toString(lastLvl));
            level.x = PixelScene.align(27.0f - level.width() / 2);
            level.y = PixelScene.align(27.5f - level.baseLine() / 2);
        }

        int k = IronKey.curDepthQuantity;
        if (k != lastKeys) {
            lastKeys = k;
            keys.text(Integer.toString(lastKeys));
            keys.x = width - 8 - keys.width() - 18;
        }
    }

}
