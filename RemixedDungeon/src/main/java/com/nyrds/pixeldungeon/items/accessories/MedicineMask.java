package com.nyrds.pixeldungeon.items.accessories;

import com.watabou.pixeldungeon.Badges;

public class MedicineMask extends Accessory {
    {
        image = 19;
        coverFacialHair = true;
    }

    @Override
    public boolean nonIap() {
        return true;
    }

    @Override
    public boolean haveIt() {
        return Badges.isUnlocked(Badges.Badge.DOCTOR_QUEST_COMPLETED);
    }
}
