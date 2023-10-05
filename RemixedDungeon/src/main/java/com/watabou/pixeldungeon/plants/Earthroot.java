
package com.watabou.pixeldungeon.plants;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Camera;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Barkskin;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.EarthParticle;
import com.watabou.pixeldungeon.items.potions.PotionOfParalyticGas;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class Earthroot extends Plant {

    public Earthroot() {
        imageIndex = 5;
    }

    public void effect(int pos, Presser ch) {
        if (ch instanceof Char) {
            Char chr = (Char) ch;

            Armor buff = Buff.affect(chr, Armor.class);
            buff.level = chr.ht();
        }

        if (Dungeon.isCellVisible(pos)) {
            CellEmitter.bottom(pos).start(EarthParticle.FACTORY, 0.05f, 8);
            Camera.main.shake(1, 0.4f);
        }
    }

    public static class Seed extends com.watabou.pixeldungeon.plants.Seed {
        {
            plantName = StringsManager.getVar(R.string.Earthroot_Name);

            name = Utils.format(R.string.Plant_Seed, plantName);
            image = 5;

            plantClass = Earthroot.class;
            alchemyClass = PotionOfParalyticGas.class;
        }

        @Override
        public String desc() {
            return StringsManager.getVar(R.string.Earthroot_Desc);
        }

        @Override
        public void _execute(@NotNull Char chr, @NotNull String action) {

            super._execute(chr, action);

            if (action.equals(CommonActions.AC_EAT)) {
                Buff.affect(chr, Roots.class, 25);
                Buff.affect(chr, Barkskin.class).level(chr.effectiveSTR() / 4);
            }
        }
    }

    public static class Armor extends Buff {

        private static final float STEP = 1f;

        @Packable
        private int pos;
        @Packable
        private int level;

        @Override
        public boolean attachTo(@NotNull Char target) {
            pos = target.getPos();
            return super.attachTo(target);
        }

        @Override
        public boolean act() {
            if (target.getPos() != pos) {
                detach();
            }
            spend(STEP);
            return true;
        }

        public int absorb(int damage) {
            if (damage >= level) {
                detach();
                return damage - level;
            } else {
                level -= damage;
                return 0;
            }
        }

        @Override
        public int defenceProc(Char defender, Char enemy, int damage) {
            return absorb(damage);
        }

        public void level(int value) {
            if (level < value) {
                level = value;
            }
        }

        @Override
        public int icon() {
            return BuffIndicator.ARMOR;
        }

        @Override
        public String name() {
            return StringsManager.getVar(R.string.EarthrootBuff_Name);
        }

        @Override
        public String desc() {
            return StringsManager.getVar(R.string.EarthrootBuff_Info);
        }
    }
}
