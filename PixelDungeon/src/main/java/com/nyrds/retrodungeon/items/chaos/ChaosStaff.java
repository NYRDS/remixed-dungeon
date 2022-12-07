package com.nyrds.retrodungeon.items.chaos;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.wands.WandOfTeleportation;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class ChaosStaff extends Wand implements IChaosItem {

    private int charge = 0;

    public ChaosStaff() {
        imageFile = "items/chaosStaff.png";
        image = 0;
    }

    @Override
    protected void updateLevel() {
        super.updateLevel();
        selectImage();
    }

    @Override
    public void ownerTakesDamage(int damage) {
        charge++;
    }

    @Override
    public void ownerDoesDamage(Char ch, int damage) {
    }

    private void selectImage() {
        image = Math.max(0, Math.min(level() / 3, 4));
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);

        bundle.put(ChaosCommon.CHARGE_KEY, charge);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);

        charge = bundle.getInt(ChaosCommon.CHARGE_KEY);
    }

    @Override
    public int getCharge() {
        return charge;
    }

    @Override
    protected void onZap(int cell) {

        ChaosCommon.doChaosMark(cell, 10 + level() * 10 + charge);
        charge = 0;

        if (Math.random() < 0.1f) {
            Char ch = Actor.findChar(cell);
            if (ch instanceof Mob) {
                Mob mob = (Mob) ch;

                if ((mob instanceof Boss) || (mob instanceof NPC)) {
                    return;
                }

                switch (Random.Int(0, 4)) {
                    case 0:
                        mob.die(getCurUser());
                        break;
                    case 1:
                        Mob.makePet(mob, getCurUser());
                        break;

                    case 2:
                        int nextCell = Dungeon.level.getEmptyCellNextTo(cell);

                        if (Dungeon.level.cellValid(nextCell)) {
                            try {
                                Mob newMob = mob.getClass().newInstance();
                                Dungeon.level.spawnMob(newMob);
                            } catch (Exception e) {
                                throw new TrackedRuntimeException(e);
                            }
                        }
                        break;
                    case 3:
                        WandOfTeleportation.teleport(mob);
                        break;
                    case 4:
                        PotionOfHealing.heal(ch, 1);
                        break;
                }
            }
        }
    }

    @Override
    public String name() {
        return Game.getVar(R.string.ChaosStaff_Name);
    }

    @Override
    public String info() {
        return Game.getVar(R.string.ChaosStaff_Info);
    }
}
