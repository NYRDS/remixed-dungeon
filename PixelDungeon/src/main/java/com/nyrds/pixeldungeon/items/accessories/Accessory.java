package com.nyrds.pixeldungeon.items.accessories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by DeadDie on 26.05.2016
 */
public class Accessory {

	protected boolean coverHair;
	protected int image = 0;
	protected String imageFile;

	static final private Map<String,Class<? extends Accessory>> allAccessoriesList = new HashMap<>();

	private static void registerAccessory(Class<? extends Accessory> Clazz) {
		allAccessoriesList.put(Clazz.getSimpleName(), Clazz);
	}

	static {
		registerAccessory(Fez.class);
		registerAccessory(Pumpkin.class);
		registerAccessory(Capotain.class);
		registerAccessory(BowTie.class);
		registerAccessory(SleepyHat.class);
		registerAccessory(RabbitEars.class);
		registerAccessory(WizardHat.class);
		registerAccessory(Shades.class);
		registerAccessory(NekoEars.class);
	}

	public static List<String> getAccessoriesList() {
		return new ArrayList<>(allAccessoriesList.keySet());
	}

	public String getLayerFile() {
		return "hero/accessories/"+ getClass().getSimpleName() + ".png";
	}

    Accessory (){
		imageFile = "items/accessories.png";
        coverHair = false;
    }

	public boolean isCoveringHair() {
		return coverHair;
	}
}
