package com.watabou.pixeldungeon.items.weapon.melee;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.KusarigamaChain;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Kusarigama extends SpecialWeapon {
    {
        animation_class = SWORD_ATTACK;
    }

    private static final String AC_PULL = "Kusarigama_Pull";
    private static final float TIME_TO_IMPALE = 1.5f;

    public Kusarigama() {
        super(3, 2f, 1f);

        image = 0;
        imageFile = "items/kusarigama.png";

        range = 2;
    }

    private static CellSelector.Listener impaler = new CellSelector.Listener() {
        @Override
        public void onSelect(Integer target) {

            if (target != null) {
                getUser().spendAndNext(TIME_TO_IMPALE);
                int hitCell = Ballistica.cast(getUser().getPos(), target, false, true);

                if (hitCell == getUser().getPos()) {
                    return;
                }

                if (Dungeon.level.distance(getUser().getPos(), hitCell) < 4) {
                    Char chr = Actor.findChar(hitCell);

                    if (chr != null && chr.isMovable()) {
                        chr.placeTo(Ballistica.trace[1]);
                        chr.getSprite().move(chr.getPos(), Ballistica.trace[1]);

                        Dungeon.observe();
                    }

                    drawChain(hitCell);
                } else {
                    drawChain(Ballistica.trace[4]);
                }
            }
        }

        @Override
        public String prompt() {
            return Game.getVar(R.string.Item_DirThrow);
        }
    };

    private static void drawChain(int tgt) {
        getUser().getSprite().zap(tgt);
        getUser().getSprite()
                .getParent()
                .add(new KusarigamaChain(getUser().getSprite().center(),
                        DungeonTilemap.tileCenterToWorld(tgt)));
    }

    @Override
    public void execute(Hero hero, String action) {
        setUser(hero);
        if (action.equals(AC_PULL)) {
            GameScene.selectCell(impaler);
        } else {
            super.execute(hero, action);
        }
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if (isEquipped(hero)) {
            actions.add(AC_PULL);
        }
        return actions;
    }

    @Override
    public void preAttack(Hero user, Char tgt) {
        setUser(user);
        if (user.level().distance(user.getPos(), tgt.getPos()) > 1) {
            drawChain(tgt.getPos());
        }
    }

    @Override
    public void postAttack(Hero user, Char tgt) {
        if (Random.Float(1) < 0.1f) {
            Buff.prolong(tgt, Vertigo.class, 3);
        }
    }
}
