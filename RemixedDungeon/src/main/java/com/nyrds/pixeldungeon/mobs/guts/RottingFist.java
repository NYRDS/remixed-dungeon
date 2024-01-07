package com.nyrds.pixeldungeon.mobs.guts;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Ooze;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.sprites.RottingFistSprite;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class RottingFist extends Mob {

    private static final int REGENERATION = 10;

    {
        spriteClass = RottingFistSprite.class;

        hp(ht(500));
        baseDefenseSkill = 25;
        baseAttackSkill = 36;

        expForKill = 0;

        dmgMin = 34;
        dmgMax = 46;
        dr = 15;

        setState(MobAi.getStateByClass(Wandering.class));

        addResistance(ToxicGas.class);

        addImmunity(Amok.class);
        addImmunity(Sleep.class);
        addImmunity(Terror.class);
        addImmunity(Poison.class);
        addImmunity(Burning.class);
    }

    public RottingFist() {
    }

    @Override
    public int attackProc(@NotNull Char enemy, int damage) {
        if (Random.Int(3) == 0) {
            Buff.affect(enemy, Ooze.class);
            enemy.getSprite().burst(0xFF000000, 5);
        }

        return damage;
    }

    @Override
    public void damage(int dmg, @NotNull NamedEntityKind src) {
        for (Mob mob : level().mobs) {
            mob.beckon(getPos());
        }

        super.damage(dmg, src);
    }

    @Override
    public boolean act() {

        if (Dungeon.level.water[getPos()] && hp() < ht()) {
            getSprite().emitter().burst(ShadowParticle.UP, 2);
            heal(REGENERATION, this, true);
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
