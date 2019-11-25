package com.nyrds.pixeldungeon.items;

import com.nyrds.LuaInterface;
import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.ModError;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.watabou.pixeldungeon.Challenges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Treasury {

    public enum Category {
        WEAPON,
        ARMOR,
        POTION,
        SCROLL,
        WAND,
        RING,
        SEED,
        FOOD,
        GOLD,
        RANGED,
        BULLETS,
        THROWABLE,
        UNIQUE
    }

    private class CategoryItems {
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Float>  probs = new ArrayList<>();
    }

    private ArrayList<String>        names = new ArrayList<>();
    private ArrayList<CategoryItems> items = new ArrayList<>();
    private ArrayList<Float>         probs = new ArrayList<>();

    private float decayFactor = 1;

    private Set<String> forbidden = new HashSet<>();

    private static Treasury defaultTreasury;
    private static Map<String, Treasury> tresurySet = new HashMap<>();

    public static void reset() {
        defaultTreasury = null;
    }

    public static Treasury get() {
        if(defaultTreasury == null) {
            defaultTreasury = create("Treasury.json");
        }
        return defaultTreasury;

    }

    public static Treasury getLevelTreasury() {
        String id = Dungeon.levelId;

        if(!tresurySet.containsKey(id)) {
            tresurySet.put(id, create(DungeonGenerator.getLevelProperty(id, "treasury","Treasury.json")));
        }

        return tresurySet.get(id);
    }

    public static Treasury create(String file) {
        Treasury treasury = new Treasury();
        treasury.loadFromFile("levelsDesc/"+file);
        Challenges.forbidCategories(Dungeon.challenges,treasury);
        return treasury;
    }

    private void loadFromFile(String file) {
        try {
            names.clear();
            items.clear();

            JSONObject treasury = JsonHelper.readJsonFromAsset(file);

            int version = treasury.optInt("version", 0);
            switch (version) {
                case 0:
                    loadCategories(treasury);
                break;

                case 1:
                    loadTreasury(treasury,version);
                break;
                default:
                    throw new ModError("Unknown version in "+ file);
            }
        } catch (JSONException e) {
            throw new TrackedRuntimeException(e);
        }
    }

    private void loadTreasury(@NotNull JSONObject treasury, int version) throws JSONException {
        JSONObject categories = treasury.getJSONObject("categories");
        loadCategories(categories);

        JSONObject settings = treasury.getJSONObject("settings");

        JSONObject probabilities = settings.getJSONObject("probabilities");

        for (String name:names) {
            probs.add((float) probabilities.optDouble(name,0));
        }

        decayFactor = (float) settings.optDouble("decayFactor");
    }

    private void loadCategories(@NotNull JSONObject treasury) throws JSONException {
        Iterator<String> cats = treasury.keys();

        while (cats.hasNext()) {
            String cat = cats.next();
            CategoryItems currentCategory = new CategoryItems();
            names.add(cat);
            items.add(currentCategory);

            JSONObject catData = treasury.getJSONObject(cat);
            Iterator<String> items = catData.keys();
            while (items.hasNext()) {
                String item = items.next();

                if(item.equals("categoryChance")) {
                    probs.add((float) catData.getDouble(item));
                    continue;
                }

                if(ItemFactory.isValidItemClass(item)) {
                    currentCategory.names.add(item);
                    currentCategory.probs.add((float) catData.getDouble(item));
                } else {
                    ModError.doReport(item,new Exception("Treasury: unknown item"));
                }
            }
        }
    }

    public Item worstOf(Category cat, int n) {
        Item ret = random(cat);

        for (int i=n+1; i < n; i++) {
            Item another = random(cat);

            if (another.level() < ret.level()) {
                ret = another;
            }
        }
        return ret;
    }

    public Item bestOf(Category cat, int n) {
        Item ret = random(cat);

        for (int i=n+1; i < n; i++) {
            Item another = random(cat);

            if (another.level() > ret.level()) {
                ret = another;
            }
        }
        return ret;
    }

    public Item random() {
        int categoryIndex = Random.chances(probs);

        String categoryName = names.get(categoryIndex);
        if(forbidden.contains(categoryName)) {
            GLog.debug("Forbidden category: %s",categoryName);
            return ItemFactory.itemByName("Gold");
        }

        return random(categoryName);
    }

    public boolean isForbidden(@NotNull String itemClass) {
        if(forbidden.contains(itemClass)) {
            return true;
        }
        return false;
    }

    public Item check(@NotNull Item item) {
        if(forbidden.contains(item.getClassName())) {
            GLog.debug("Forbidden item: %s",item.getClassName());
            return ItemFactory.itemByName("Gold").quantity(item.price());
        }
        return item;
    }

    public Item random(Category category){
        return random(category.name());
    }

    private Item randomItem(String itemName) {
        return check(ItemFactory.itemByName(itemName).random());
    }

    @LuaInterface
    public Item random(String categoryOrItem) {

        if(forbidden.contains(categoryOrItem)) {
            GLog.debug("Forbidden category or item: %s",categoryOrItem);
            return ItemFactory.itemByName("Gold");
        }

        for(int i = 0;i<names.size();++i) {
            if(names.get(i).equals(categoryOrItem)) {
                probs.set(i,probs.get(i)/decayFactor);
                CategoryItems category = items.get(i);
                int itemIndex = Random.chances(category.probs);
                String itemName = category.names.get(itemIndex);
                return randomItem(itemName);
            }
        }

        return randomItem(categoryOrItem);
    }

    public Item random(Class<? extends Item> cl) {
        try {
            return check(cl.newInstance().random());
        } catch (Exception e) {
            throw new TrackedRuntimeException(e);
        }
    }

    public void forbid(String itemOrCat) {
        forbidden.add(itemOrCat);

        for(int i = 0;i<names.size();++i) {
            if(names.get(i).equals(itemOrCat)) {
                CategoryItems category = items.get(i);
                forbidden.addAll(category.names);
                return;
            }
        }
    }
}
