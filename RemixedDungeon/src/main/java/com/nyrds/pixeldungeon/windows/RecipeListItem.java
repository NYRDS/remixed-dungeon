package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.alchemy.AlchemyRecipes;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.IClickable;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Component for displaying an alchemy recipe in the WndAlchemy window
 */
public class RecipeListItem extends Component implements IClickable {

    @Getter
    private final List<String> inputs;

    private final HBox rowBox;
    private final List<Item> inputItems; // Store actual item instances to check against hero inventory
    private final HBox ingredientsBox;
    private final HBox outputBox;
    private Runnable clickListener;
    protected boolean clickable = false;
    @Getter
    private boolean isSelected = false;


    public RecipeListItem(List<String> inputs, List<String> outputs) {
        super();

        this.inputs = inputs;

        // Initialize the input items and ingredient slots lists
        inputItems = new ArrayList<>();

        rowBox = new HBox(150); 
        // Create HBox for ingredients
        ingredientsBox = new HBox(100);
        ingredientsBox.setGap(-10);

        // Add input ingredients to the ingredients box as ItemSlots
        for (String input : inputs) {
            Item ingredient = ItemFactory.itemByName(input);
            inputItems.add(ingredient);

            ItemSprite inputSprite = new ItemSprite(ingredient);

            boolean hasIngredient = checkHeroHasItem(ingredient);

            inputSprite.brightness(hasIngredient ? 1f : 0.5f);

            ingredientsBox.add(inputSprite);
        }

        // Create HBox for output
        outputBox = new HBox(100);
        outputBox.setGap(-5);

        // Add all output components to the output box
        for (String singleOutput : outputs) {
            AlchemyRecipes.OutputType singleOutputType = determineOutputType(singleOutput);

            if (singleOutputType == AlchemyRecipes.OutputType.ITEM) {
                Item outputItem  = ItemFactory.itemByName(singleOutput);
                ItemSprite outputSprite = new ItemSprite(outputItem);
                outputSprite.brightness(0.5f);

                outputBox.add(outputSprite);
            } else if (singleOutputType == AlchemyRecipes.OutputType.MOB) {

                Mob mob = MobFactory.mobByName(singleOutput);
                Image sprite = mob.newSprite().avatar();
                sprite.brightness(0.5f);

                outputBox.add(sprite);
            }
        }
        outputBox.add( new Component() {
            @Override
            public float width() {
                return 16;
            }

            @Override
            public float height() {
                return 16;
            }
        });

        rowBox.add(ingredientsBox);
//        rowBox.add(arrow);
        rowBox.add(outputBox);
        rowBox.setSize(width, height);
        rowBox.setAlign(HBox.Align.Width);
        add(rowBox);
        rowBox.layout();
        rowBox.setPos(x, y);

        setSize(rowBox.width(), rowBox.height());

    }

    /**
     * Determine the output type (item or mob) based on the entity name
     */
    private AlchemyRecipes.OutputType determineOutputType(String entityName) {
        // Check if it's a valid item class first
        if (ItemFactory.isValidItemClass(entityName)) {
            return AlchemyRecipes.OutputType.ITEM;
        }

        // If it's not an item, check if it's a mob
        if (MobFactory.hasMob(entityName)) {
            return AlchemyRecipes.OutputType.MOB;
        }

        // Default to item for backward compatibility
        return AlchemyRecipes.OutputType.ITEM;
    }

    private boolean checkHeroHasItem(Item item) {
        // Check if the hero exists and has the item in their inventory
        // Use the public checkItem method to see if the hero has this item
        Item heroItem = Dungeon.hero.checkItem(item.getEntityKind());

        // If the returned item is not a dummy, it means the hero has it
        return heroItem.valid();
    }

    @Override
    public void layout() {
        super.layout();
        rowBox.setMaxWidth(width);

        rowBox.setPos(PixelScene.align(x), PixelScene.align(y));
    }

    public void setOnClickListener(Runnable listener) {
        this.clickListener = listener;
        this.clickable = true;
    }


    public void setSelected(boolean selected) {
        isSelected = selected;
        for (int i = 0; i < ingredientsBox.getLength(); i++) {
            if(ingredientsBox.getMember(i) instanceof Image) {
                ((Image) ingredientsBox.getMember(i)).brightness(selected ? 1.0f : 0.5f);
            }
        }

        for(int i = 0; i < outputBox.getLength(); i++) {
            if(outputBox.getMember(i) instanceof Image) {
                ((Image) outputBox.getMember(i)).brightness(selected ? 1.0f : 0.5f);
            }
        }

    }

    /**
     * Gets the items that are used as ingredients in this recipe
     *
     * @return List of ingredient items
     */
    public List<Item> getIngredientItems() {
        return inputItems;
    }

    @Override
    public boolean onClick(float x, float y) {
        if (clickable && inside(x, y)) {
            if (clickListener != null) {
                clickListener.run();
            }
            return true;
        }
        return false;
    }
}