package com.nyrds.pixeldungeon.items.chaos;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.wands.WandOfTeleportation;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class ChaosStaff extends Wand {

    @Packable
    private int charge = 0;

    public ChaosStaff() {
        imageFile = "items/chaosStaff.png";
        image = 0;
    }


    @Override
    public Item upgrade() {
        super.upgrade();
        selectImage();
        return this;
    }

    @Override
    public Item degrade() {
        super.degrade();
        selectImage();
        return this;
    }

    @Override
    public void ownerTakesDamage(int damage) {
        charge++;
    }

    @Override
    public void ownerDoesDamage(int damage) {
    }

    private void selectImage() {
        image = Math.max(0, Math.min(level() / 3, 4));
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);

        selectImage();
    }

    @Override
    protected void onZap(int cell) {

        ChaosCommon.doChaosMark(cell, 10 + level() * 10 + charge);
        charge = 0;

        if (Math.random() < 0.1f) {
            Char ch = Actor.findChar(cell);
            if (ch instanceof Mob) {
                Mob mob = (Mob) ch;

                if ((mob.isBoss()) || (mob instanceof NPC)) {
                    return;
                }

                switch (Random.Int(0, 4)) {
                    case 0:
                        mob.die(getOwner());
                        break;
                    case 1:
                        Mob.makePet(mob, getOwner().getId());
                        break;

                    case 2:
                        Level level = Dungeon.level;
                        int nextCell = level.getEmptyCellNextTo(cell);

                        if (level.cellValid(nextCell)) {
                            Mob newMob = MobFactory.mobByName(mob.getEntityKind());
                            newMob.setPos(nextCell);
                            level.spawnMob(newMob);
                        }
                        break;
                    case 3:
                        WandOfTeleportation.teleport(mob,getOwner());
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
        return StringsManager.getVar(R.string.ChaosStaff_Name);
    }

    @Override
    public String info() {
        return StringsManager.getVar(R.string.ChaosStaff_Info);
    }
}
