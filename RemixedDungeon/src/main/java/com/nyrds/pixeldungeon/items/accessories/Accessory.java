package com.nyrds.pixeldungeon.items.accessories;

import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.Iap;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.storage.Preferences;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;

/**
 * Created by DeadDie on 26.05.2016
 */
public class Accessory {

    protected boolean coverHair;
    protected boolean coverFacialHair;
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
        registerAccessory(MedicineMask.class);
        registerAccessory(FilteredMask.class);
        registerAccessory(FullFaceMask.class);
        registerAccessory(KrampusHead.class);
    }

    public static List<String> getAccessoriesList() {
        return new ArrayList<>(allAccessoriesList.keySet());
    }

    public String getLayerFile() {
        return "hero_modern/accessories/" + getClass().getSimpleName() + ".png";
    }

    Accessory() { }

    public boolean isCoveringHair() {
        return coverHair;
    }

    public boolean isCoverFacialHair() {
        return coverFacialHair;
    }

    public boolean isCoveringItems() {
        return coverItems;
    }


    public boolean usableBy(Hero hero) {
        return true;
    }

    @SneakyThrows
    public static Accessory getByName(String name) {
        return allAccessoriesList.get(name).newInstance();
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
        return getClassParam("Info", StringsManager.getVar(R.string.Item_Info));
    }

    public String name() {
        return getClassParam("Name", StringsManager.getVar(R.string.Item_Name));
    }

    private String prefProperty() {
        return "Accessory" + getClass().getSimpleName();
    }

    static public void check() {
        Iap iap = RemixedDungeon.instance().iap;

        if(iap == null) {
            EventCollector.logException("iap is null!!!");
            return;
        }

        for (String item : allAccessoriesList.keySet()) {
            getByName(item).ownIt(iap.checkPurchase(item));
        }
    }

    public boolean haveIt() {
        return GamePreferences.donated() == 4 || Preferences.INSTANCE.getString(prefProperty(), Utils.EMPTY_STRING).equals(getClass().getSimpleName());
    }

    public void ownIt(boolean reallyOwn) {
        if (reallyOwn) {
            Preferences.INSTANCE.put(prefProperty(), getClass().getSimpleName());
        } else {
            Preferences.INSTANCE.put(prefProperty(), Utils.EMPTY_STRING);
        }
    }

    public boolean nonIap() {
        return false;
    }

    public void equip() {
        if (!haveIt()) {
            return;
        }

        Preferences.INSTANCE.put(Accessory.class.getSimpleName(), getClass().getSimpleName());

        if(GameScene.isSceneReady()) {
            Dungeon.hero.updateSprite();
        }
    }

    public static void unequip() {
        Preferences.INSTANCE.put(Accessory.class.getSimpleName(), Utils.EMPTY_STRING);
        Dungeon.hero.updateSprite();
    }

    static public @Nullable Accessory equipped() {
        String itemName = Preferences.INSTANCE.getString(Accessory.class.getSimpleName(), Utils.EMPTY_STRING);
        if (!itemName.equals(Utils.EMPTY_STRING)) {
            return getByName(itemName);
        }

        return null;
    }
}
