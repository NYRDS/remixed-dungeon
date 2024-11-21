
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.PurpleParticle;
import com.watabou.pixeldungeon.items.Dewdrop;
import com.watabou.pixeldungeon.items.wands.WandOfDisintegration;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.items.weapon.enchantments.Leech;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.nyrds.util.Random;

import org.jetbrains.annotations.NotNull;

public class Eye extends Mob {

    int [] trace;
    int distance = 0;

    public Eye() {
        hp(ht(100));
        baseDefenseSkill = 20;
        baseAttackSkill  = 30;
        expForKill = 13;
        maxLvl = 25;

        dmgMin = 14;
        dmgMax = 20;

        flying = true;

        loot(Dewdrop.class, 0.5f);

        addResistance(WandOfDisintegration.class);
        addResistance(Death.class);
        addResistance(Leech.class);

        addImmunity(Terror.class);
    }

    @Override
    public void onSpawn(Level level) {
        super.onSpawn(level);
        setViewDistance(level.getViewDistance() + 1);
    }

    @Override
    public int dr() {
        return 10;
    }

    @Override
    public boolean canAttack(@NotNull Char enemy) {

        Ballistica.cast(getPos(), enemy.getPos(), true, false);

        trace = Ballistica.trace.clone();
        distance = Ballistica.distance;

        for (int i = 1; i < distance; i++) {
            if (trace[i] == enemy.getPos()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public float _attackDelay() {
        return 1.6f;
    }

    @Override
    protected int zapProc(@NotNull Char enemy, int damage) {
        for (int i = 1; i < distance; i++) {
            int cell = trace[i];

            Char victim = Actor.findChar(cell);
            if (victim != null) {
                if (CharUtils.hit(this, victim, true)) {
                    victim.damage(Random.NormalIntRange(dmgMin, dmgMax), this);
                    int pos = victim.getPos();

                    if (Dungeon.isCellVisible(pos)) {
                        victim.getSprite().flash();
                        CellEmitter.center(pos).burst(PurpleParticle.BURST, Random.IntRange(1, 2));
                    }

                    CharUtils.checkDeathReport(this ,victim, StringsManager.getVar(R.string.Eye_Kill));
                } else {
                    victim.showStatus(CharSprite.NEUTRAL, victim.defenseVerb());
                }
            }
        }
        distance = 0;
        return damage;
    }
}