package com.nyrds.pixeldungeon.items.necropolis;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.Collection;

public class BlackSkull extends Artifact {

    private static final int ACTIVATED_IMAGE = 20;
    private static final int BASIC_IMAGE = 19;

    private static final int RESURRECTION_COST = 10;
    private static final int MAXIMUM_CHARGE = 10;

    @Packable
    private boolean activated = false;

    @Packable
    private int charge = 0;

    public BlackSkull() {
        imageFile = "items/artifacts.png";
        identify();
        image = BASIC_IMAGE;
    }

    @Override
    public void charDied(Char mob, NamedEntityKind cause) {
        Char hero = getOwner();

        Collection<Integer> pets = hero.getPets();

        if (pets.contains(mob.getId())) {
            return;
        }

        if (mob.canBePet()) {
            if (activated) {
                mob.resurrect(hero);
                GLog.w(StringsManager.getVar(R.string.BlackSkull_Ressurrect));
                charge = charge - RESURRECTION_COST;
                if (charge <= 0) {
                    GLog.w(StringsManager.getVar(R.string.BlackSkull_Deactivated));
                    activated = false;
                }
            } else {
                charge++;
                if (charge >= MAXIMUM_CHARGE) {
                    GLog.w(StringsManager.getVar(R.string.BlackSkull_Activated));
                    activated = true;
                }
            }
        }
    }

    @Override
    public int image() {
        if (activated) {
            return ACTIVATED_IMAGE;
        } else {
            return BASIC_IMAGE;
        }
    }

    @Override
    public String info() {
        if (activated) {
            return StringsManager.getVar(R.string.BlackSkull_Info_Awakened);
        } else {
            return StringsManager.getVar(R.string.BlackSkull_Info);
        }
    }

    @Override
    public String name() {
        if (activated) {
            return StringsManager.getVar(R.string.BlackSkull_Name_Awakened);
        } else {
            return StringsManager.getVar(R.string.BlackSkull_Name);
        }
    }
}
