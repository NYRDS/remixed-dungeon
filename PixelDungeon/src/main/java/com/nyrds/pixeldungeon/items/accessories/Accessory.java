package com.nyrds.pixeldungeon.items.accessories;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.utils.Utils;

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

	protected String name = getClassParam("Name", Game.getVar(R.string.Item_Name), false);
	protected String info = getClassParam("Info", Game.getVar(R.string.Item_Info), false);

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

	public static Accessory getByName(String name) {
		try {
			return allAccessoriesList.get(name).newInstance();
		} catch (Exception e) {
			throw new TrackedRuntimeException(e);
		}
	}

	protected String getClassParam(String paramName, String defaultValue, boolean warnIfAbsent) {
		return Utils.getClassParam(this.getClass().getSimpleName(), paramName, defaultValue, warnIfAbsent);
	}

	public Image getImage() {
		return new Image(imageFile, image*28, 0, 28,28);
	}

	public String desc() {
		return info;
	}
	public String name() {
		return name;
	}
}
