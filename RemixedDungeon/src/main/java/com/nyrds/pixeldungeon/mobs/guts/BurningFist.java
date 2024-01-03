package com.nyrds.pixeldungeon.mobs.guts;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.IZapper;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.wands.WandOfFirebolt;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;

import org.jetbrains.annotations.NotNull;

public class BurningFist extends Mob implements IZapper {

    {

        hp(ht(400));
        baseDefenseSkill = 25;
        baseAttackSkill = 26;

        expForKill = 0;

        dmgMin = 40;
        dmgMax = 62;
        dr = 15;

        setState(MobAi.getStateByClass(Wandering.class));

        addResistance(ToxicGas.class);

        addImmunity(Amok.class);
        addImmunity(Sleep.class);
        addImmunity(Terror.class);
        addImmunity(Fire.class);
        addImmunity(Burning.class);
        addImmunity(WandOfFirebolt.class);
    }

    public BurningFist() {
    }

    @Override
    public boolean canAttack(@NotNull Char enemy) {
        return Ballistica.cast(getPos(), enemy.getPos(), false, true) == enemy.getPos();
    }

    @Override
    public boolean attack(@NotNull Char enemy) {
        if (super.attack(enemy)) {
            if (!adjacent(enemy)) {
                enemy.getSprite().flash();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean act() {

        for (int i = 0; i < Level.NEIGHBOURS9.length; i++) {
            GameScene.add(Blob.seed(getPos() + Level.NEIGHBOURS9[i], 2, Fire.class));
        }

        return super.act();
    }

    @Override
    public String getDescription() {
        return StringsManager.getVar(R.string.Yog_Desc);
    }

    @Override
    public boolean canBePet() {
        return false;
    }
}
