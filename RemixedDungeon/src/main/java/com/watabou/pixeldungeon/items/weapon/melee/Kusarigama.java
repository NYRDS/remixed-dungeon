package com.watabou.pixeldungeon.items.weapon.melee;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.effects.KusarigamaChain;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Kusarigama extends MeleeWeapon {
    {
        animation_class = SWORD_ATTACK;
    }

    private static final String AC_PULL = "Kusarigama_Pull";
    private static final float TIME_TO_IMPALE = 1.5f;

    public Kusarigama() {
        super(3, 2f, 1f);

        image = 0;
        imageFile = "items/kusarigama.png";
    }

    private static final CellSelector.Listener impaler = new KusarigamaCellListener();

    private static void drawChain(int tgt, Char caster) {
        CharSprite casterSprite = caster.getSprite();

        casterSprite.zap(tgt);
        casterSprite
                .getParent()
                .add(new KusarigamaChain(casterSprite.center(),
                        DungeonTilemap.tileCenterToWorld(tgt)));
    }

    @Override
    public void _execute(@NotNull Char chr, @NotNull String action) {

        if (action.equals(AC_PULL)) {
            chr.selectCell(impaler);
        } else {
            super._execute(chr, action);
        }
    }

    @Override
    public ArrayList<String> actions(Char hero) {
        ArrayList<String> actions = super.actions(hero);
        if (isEquipped(hero)) {
            actions.add(AC_PULL);
        }
        return actions;
    }

    @Override
    public void preAttack(Char tgt) {
        Char user = getOwner();
        if (user.level().distance(user.getPos(), tgt.getPos()) > 1) {
            drawChain(tgt.getPos(), user);
        }
    }

    @Override
    public void postAttack(Char tgt) {
        if (Random.Float(1) < 0.1f) {
            Buff.prolong(tgt, Vertigo.class, 3);
        }
    }

    @Override
    public int range() {
        return 2;
    }

    private static class KusarigamaCellListener implements CellSelector.Listener {
        @Override
        public void onSelect(Integer target, @NotNull Char selector) {

            if (target != null) {
                selector.spend(TIME_TO_IMPALE);
                int hitCell = Ballistica.cast(selector.getPos(), target, false, true);

                if (hitCell == selector.getPos()) {
                    return;
                }

                if (selector.level().distance(selector.getPos(), hitCell) < 4) {
                    Char chr = Actor.findChar(hitCell);

                    if (chr != null && chr.isMovable()) {
                        chr.placeTo(Ballistica.trace[1]);
                        chr.getSprite().move(chr.getPos(), Ballistica.trace[1]);

                        selector.observe();
                    }

                    drawChain(hitCell, selector);
                } else {
                    drawChain(Ballistica.trace[4], selector);
                }
            }
        }

        @Override
        public String prompt() {
            return StringsManager.getVar(R.string.Item_DirThrow);
        }

        @Override
        public Image icon() {
            return null;
        }
    }
}
