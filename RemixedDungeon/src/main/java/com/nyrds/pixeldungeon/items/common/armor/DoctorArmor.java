package com.nyrds.pixeldungeon.items.common.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.MiasmaGas;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.armor.ClassArmor;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

public class DoctorArmor extends ClassArmor {

    private static final int CLOUD_VOLUME = 100;

    {
        image = 31;
        hasHelmet = true;
        hasCollar = true;
        coverHair = true;
        coverFacialHair = true;
    }

    @Override
    public String desc() {
        return info;
    }

    @Override
    public String getVisualName() {
        return "PlagueDoctorArmor";
    }

    @Override
    public String special() {
        return "DoctorSpecial";
    }

    @Override
    public void doSpecial(@NotNull Char user) {
        // Create miasma cloud centered on the doctor
        int pos = user.getPos();

        // Seed the miasma gas at the doctor's position
        GameScene.add(Blob.seed(pos, CLOUD_VOLUME, MiasmaGas.class));

        // Visual effect at the center
        CellEmitter.center(pos).burst(Speck.factory(Speck.MIASMA), 10);

        // Spend a turn
        user.spend(Actor.TICK);
    }

    @Override
    public boolean doEquip(@NotNull Char hero) {
        if (hero.getHeroClass() == HeroClass.DOCTOR) {
            return super.doEquip(hero);
        } else {
            GLog.w(StringsManager.getVar(R.string.DoctorArmor_NotDoctor));
            return false;
        }
    }
}
