package com.nyrds.retrodungeon.items.accessories;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.retrodungeon.ml.R;
import com.nyrds.retrodungeon.support.Iap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.Preferences;
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
	protected static String imageFile = "items/accessories.png";

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
		registerAccessory(Bowknot.class);
		registerAccessory(Nightcap.class);
		registerAccessory(RabbitEars.class);
		registerAccessory(WizardHat.class);
		registerAccessory(Shades.class);
		registerAccessory(NekoEars.class);
		registerAccessory(PirateSet.class);
		registerAccessory(ZombieMask.class);
		registerAccessory(VampireSkull.class);
		registerAccessory(Ushanka.class);
		registerAccessory(SantaHat.class);
		registerAccessory(Rudolph.class);
		registerAccessory(GnollCostume.class);
		registerAccessory(ChaosHelmet.class);
	}

	public static List<String> getAccessoriesList() {
		return new ArrayList<>(allAccessoriesList.keySet());
	}

	public String getLayerFile() {
		return "hero/accessories/"+ getClass().getSimpleName() + ".png";
	}

    Accessory (){
		//imageFile = "items/accessories.png";
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

	public static Image getSlotImage() {
		return new Image(imageFile, 0, 0, 28,28);
	}

	public String desc() {
		return info;
	}
	public String name() {
		return name;
	}

	private String prefProperty() {
		return "Accessory"+getClass().getSimpleName();
	}

	static public void check() {
		for(String item:allAccessoriesList.keySet()) {
			if(Iap.checkPurchase(item)) {
				getByName(item).ownIt(true);
			}   else {
				getByName(item).ownIt(false);
			}
		}
	}

	public boolean haveIt() {
		if (PixelDungeon.donated() == 4){
			return true;
		}
		return Preferences.INSTANCE.getString(prefProperty(),"").equals(getClass().getSimpleName());
	}

	public void ownIt(boolean reallyOwn) {
		if(reallyOwn) {
			Preferences.INSTANCE.put(prefProperty(), getClass().getSimpleName());
		} else {
			Preferences.INSTANCE.put(prefProperty(), "");
		}
	}

	public void equip (){
		if(!haveIt()) {
			return;
		}

		Preferences.INSTANCE.put(Accessory.class.getSimpleName(), getClass().getSimpleName());
	}

	public static void unequip (){
		Preferences.INSTANCE.put(Accessory.class.getSimpleName(), "");
		Dungeon.hero.updateLook();
	}

	static public Accessory equipped() {
		String itemName = Preferences.INSTANCE.getString(Accessory.class.getSimpleName(),"");
		if(!itemName.equals("")) {
			return getByName(itemName);
		}

		return null;
	}
}
