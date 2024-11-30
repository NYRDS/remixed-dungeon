package com.nyrds.pixeldungeon.items.accessories;


import com.watabou.pixeldungeon.Badges;

public class PlagueDoctorMaskHood extends Accessory{

    {
        coverFacialHair = true;
        coverHair = true;
        image = 24;
    }

    @Override
    public boolean nonIap() {
        return true;
    }

    public boolean haveIt() {
        return Badges.isUnlocked(Badges.Badge.DOCTOR_QUEST_COMPLETED);
    }
}
