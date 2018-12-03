package com.nyrds.pixeldungeon.items.accessories;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.Iap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.Preferences;
import com.watabou.pixeldungeon.actors.hero.Hero;
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
    protected boolean coverItems;

    protected int image = 0;

    protected static final String imageFile = "items/accessories.png";

    static final private Map<String, Class<? extends Accessory>> allAccessoriesList = new HashMap<>();

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
        registerAccessory(DogeMask.class);
    }

    public static List<String> getAccessoriesList() {
        return new ArrayList<>(allAccessoriesList.keySet());
    }

    public String getLayerFile() {
        return "hero_modern/accessories/" + getClass().getSimpleName() + ".png";
    }

    Accessory() {
        coverHair  = false;
        coverItems = false;
    }

    public boolean isCoveringHair() {
        return coverHair;
    }

    public boolean isCoveringItems() {
        return coverItems;
    }


    public boolean usableBy(Hero hero) {
        return true;
    }

    public static Accessory getByName(String name) {
        try {
            return allAccessoriesList.get(name).newInstance();
        } catch (Exception e) {
            throw new TrackedRuntimeException(e);
        }
    }

    private String getClassParam(String paramName, String defaultValue) {
        return Utils.getClassParam(this.getClass().getSimpleName(), paramName, defaultValue, false);
    }

    public Image getImage() {
        return new Image(imageFile, image * 28, 0, 28, 28);
    }

    public static Image getSlotImage() {
        return new Image(imageFile, 0, 0, 28, 28);
    }

    public String desc() {
        return getClassParam("Info", Game.getVar(R.string.Item_Info));
    }

    public String name() {
        return getClassParam("Name", Game.getVar(R.string.Item_Name));
    }

    private String prefProperty() {
        return "Accessory" + getClass().getSimpleName();
    }

    static public void check() {
        Iap iap = PixelDungeon.instance().iap;

        if(iap == null) {
            EventCollector.logException("iap is null!!!");
            return;
        }

        for (String item : allAccessoriesList.keySet()) {
            if ( iap.checkPurchase(item)) {
                getByName(item).ownIt(true);
            } else {
                getByName(item).ownIt(false);
            }
        }


    }

    public boolean haveIt() {
        return PixelDungeon.donated() == 4 || Preferences.INSTANCE.getString(prefProperty(), "").equals(getClass().getSimpleName());
    }

    public void ownIt(boolean reallyOwn) {
        if (reallyOwn) {
            Preferences.INSTANCE.put(prefProperty(), getClass().getSimpleName());
        } else {
            Preferences.INSTANCE.put(prefProperty(), "");
        }
    }

    public void equip() {
        if (!haveIt()) {
            return;
        }

        Preferences.INSTANCE.put(Accessory.class.getSimpleName(), getClass().getSimpleName());
    }

    public static void unequip() {
        Preferences.INSTANCE.put(Accessory.class.getSimpleName(), "");
        Dungeon.hero.updateLook();
    }

    static public Accessory equipped() {
        String itemName = Preferences.INSTANCE.getString(Accessory.class.getSimpleName(), "");
        if (!itemName.equals("")) {
            return getByName(itemName);
        }

        return null;
    }
}
