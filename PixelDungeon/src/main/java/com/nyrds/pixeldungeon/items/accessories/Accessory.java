package com.nyrds.pixeldungeon.items.accessories;

/**
 * Created by DeadDie on 26.05.2016
 */
public class Accessory {

	protected boolean coverHair;

	public String getLayerFile() {
		return "hero/accessories/"+ getClass().getSimpleName() + ".png";
	}

    Accessory (){
        coverHair = false;
    }

	public boolean isCoveringHair() {
		return coverHair;
	}
}
