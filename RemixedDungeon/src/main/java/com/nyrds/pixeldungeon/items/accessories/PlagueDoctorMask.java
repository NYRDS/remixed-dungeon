package com.nyrds.pixeldungeon.items.accessories;


import com.watabou.pixeldungeon.Badges;

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
}
