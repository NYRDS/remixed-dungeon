package com.nyrds.pixeldungeon.items.accessories;


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
}
