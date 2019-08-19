package com.nyrds.pixeldungeon.items;

import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.ModError;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.utils.Random;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Treasury {

    public static Item random(Class<? extends Item> cl) {
        try {
            return cl.newInstance().random();
        } catch (Exception e) {
            throw new TrackedRuntimeException(e);
        }
    }

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
        DRINK,
        UNIQUE
    }

    private class CategoryItems {
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Float>  probs = new ArrayList<>();
    }

    private ArrayList<String>        names = new ArrayList<>();
    private ArrayList<CategoryItems> items = new ArrayList<>();
    private ArrayList<Float>         probs = new ArrayList<>();

    private Set<String> forbiddenCategories = new HashSet<>();

    public static Treasury create(String file) {
        Treasury treasury = new Treasury();
        treasury.loadFromFile("levelsDesc/"+file);
        return treasury;
    }

    private void loadFromFile(String file) {
        try {
            names.clear();
            items.clear();

            JSONObject treasury = JsonHelper.readJsonFromAsset(file);

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

                    currentCategory.names.add(item);
                    currentCategory.probs.add((float) catData.getDouble(item));
                }
            }
        } catch (JSONException e) {
            throw new TrackedRuntimeException(e);
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

        if(forbiddenCategories.contains(names.get(categoryIndex))){
            return ItemFactory.itemByName("Gold");
        }

        CategoryItems category = items.get(categoryIndex);
        int itemIndex = Random.chances(category.probs);
        return ItemFactory.itemByName(category.names.get(itemIndex)).random();
    }

    public Item random(Category category){
        return random(category.name());
    }

    public Item random(String Category) {

        if(forbiddenCategories.contains(Category)) {
            return ItemFactory.itemByName("Gold");
        }

        for(int i = 0;i<names.size();++i) {
            if(names.get(i).equals(Category)) {
                CategoryItems category = items.get(i);
                int itemIndex = Random.chances(category.probs);
                return ItemFactory.itemByName(category.names.get(itemIndex)).random();
            }
        }
        throw new ModError("Unknown item category:"+Category);
    }
}
