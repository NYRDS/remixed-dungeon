package com.nyrds.pixeldungeon.items.accessories;


import com.nyrds.LuaInterface;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndHatInfo;

public class PlagueDoctorMask extends Accessory{

    {
        coverFacialHair = true;
        coverHair = false;
        image = 23;
    }

    @Override
    public boolean nonIap() {
        return true;
    }

    public boolean haveIt() {
        return Badges.isUnlocked(Badges.Badge.DOCTOR_QUEST_COMPLETED);
    }

    // special-reward hook of the lua doctor quest (scripts/npc/PlagueDoctor.lua);
    // was PlagueDoctorNPC.questCompleted, the java NPC class is gone
    @LuaInterface
    public static void questCompleted() {
        var hood = new PlagueDoctorMaskHood();
        hood.ownIt(true);
        var mask = new PlagueDoctorMask();
        mask.ownIt(true);

        if (!Badges.isUnlocked(Badges.Badge.DOCTOR_QUEST_COMPLETED)) {
            Badges.displayBadge(Badges.Badge.DOCTOR_QUEST_COMPLETED);
            hood.equip(false);
            GameScene.show(new WndHatInfo(hood.getClass().getSimpleName(), ""));
        }
    }
}
