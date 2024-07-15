package com.nyrds.pixeldungeon.items.accessories;


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
}
