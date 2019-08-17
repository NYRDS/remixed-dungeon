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
import java.util.Iterator;

public class Treasury {

    class Category {
        ArrayList<String> names;
        ArrayList<Float>  probs;
    }

    ArrayList<String>   names;
    ArrayList<Category> items;
    ArrayList<Float>    probs;


    //Map<String, Map<String,Double>> categories = new HashMap<>();

    public void loadFromFile(String file) {
        try {
            names.clear();
            items.clear();

            JSONObject treasury = JsonHelper.readJsonFromAsset(file);

            Iterator<String> cats = treasury.keys();

            while (cats.hasNext()) {
                String cat = cats.next();
                Category currentCategory = new Category();
                names.add(cat);
                items.add(currentCategory);

                JSONObject catData = treasury.getJSONObject(cat);
                Iterator<String> items = catData.keys();
                while (items.hasNext()) {
                    String item = items.next();
                    currentCategory.names.add(item);
                    currentCategory.probs.add((float) catData.getDouble(item));
                }
            }
        } catch (JSONException e) {
            throw new TrackedRuntimeException(e);
        }
    }

    public Item random() {
        int categoryIndex = Random.chances(probs);
        Category category = items.get(categoryIndex);
        int itemIndex = Random.chances(category.probs);
        return ItemFactory.itemByName(category.names.get(itemIndex));
    }

    public Item random(String Category) {
        for(int i = 0;i<names.size();++i) {
            if(names.get(i).equals(Category)) {
                Category category = items.get(i);
                int itemIndex = Random.chances(category.probs);
                return ItemFactory.itemByName(category.names.get(itemIndex));
            }
        }
        throw new ModError("Unknown item category:"+Category);
    }
}
