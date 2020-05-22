package com.nyrds.pixeldungeon.items.accessories;

import com.watabou.pixeldungeon.Badges;

public class MedicineMask extends Accessory {
    {
        image = 19;
    }

    @Override
    public boolean haveIt() {
        return Badges.isUnlocked(Badges.Badge.DOCTOR_QUEST_COMPLETED);
    }
}
