
package com.watabou.pixeldungeon.items.potions;

import com.nyrds.pixeldungeon.items.common.UnknownItem;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.effects.Splash;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.ItemStatusHandler;
import com.watabou.pixeldungeon.items.bags.PotionBelt;
import com.watabou.pixeldungeon.items.food.RottenFood;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.weapon.missiles.Arrow;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndOptions;
import com.watabou.utils.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;

public class Potion extends Item implements UnknownItem {

	private static final String AC_MOISTEN = "Potion_ACMoisten";
	
	private static final float TIME_TO_DRINK = 1f;
	private static final float TIME_TO_MOISTEN = 1f;

	protected int labelIndex = -1;
	
	private static final Class<?>[] potions = {
		PotionOfHealing.class, 
		PotionOfExperience.class, 
		PotionOfToxicGas.class, 
		PotionOfLiquidFlame.class,
		PotionOfStrength.class,
		PotionOfParalyticGas.class,
		PotionOfLevitation.class,
		PotionOfMindVision.class, 
		PotionOfPurity.class,
		PotionOfInvisibility.class,
		PotionOfMight.class,
		PotionOfFrost.class,
		PotionOfMana.class
	};

	private static final Integer[] images;

	private static ItemStatusHandler<Potion> handler;
	
	private final String color;

	static {
		images = new Integer[13];
		for (int i = 0;i<13;i++) {
			images[i] = 32 + i;
		}
	}

	{
		stackable = true;
		setDefaultAction(CommonActions.AC_DRINK);
		imageFile = "items/potions.png";
	}
	
	
	private boolean shattered = false;

	@SuppressWarnings("unchecked")
	public static void initColors() {
		handler = new ItemStatusHandler<>((Class<? extends Potion>[]) potions, images);
	}
	
	public static void save( Bundle bundle ) {
		handler.save( bundle );
	}
	
	@SuppressWarnings("unchecked")
	public static void restore( Bundle bundle ) {
		handler = new ItemStatusHandler<>((Class<? extends Potion>[]) potions, images, bundle);
	}
	
	public Potion() {
		image = handler.index( this );
		color = StringsManager.getVars(R.array.Potion_Colors)[ItemStatusHandler.indexByImage(image, images)];
		if(knownHamful()) {
			setDefaultAction(AC_THROW);
		}
	}
	
	@Override
	public ArrayList<String> actions(Char hero ) {
		if(knownHamful()) {
			setDefaultAction(AC_THROW);
		}

		ArrayList<String> actions = super.actions( hero );
		actions.add(CommonActions.AC_DRINK);
		actions.add(AC_MOISTEN);
		return actions;
	}
	
	@Override
	public void _execute(@NotNull final Char chr, @NotNull String action ) {
		switch (action) {
			case CommonActions.AC_DRINK:
				if (knownHamful()) {

                    GameScene.show(
							new WndOptions(StringsManager.getVar(R.string.Potion_Harmfull),
                                    StringsManager.getVar(R.string.Potion_SureDrink),
                                    StringsManager.getVar(R.string.Potion_Yes),
                                    StringsManager.getVar(R.string.Potion_No)) {
								@Override
								public void onSelect(int index) {
									if (index == 0) {
										drink(chr);
									}
								}
							}
					);

				} else {
					drink(chr);
				}

				break;
			case AC_MOISTEN:
				moisten(chr);
				break;
			default:

				super._execute(chr, action);

				break;
		}
	}

	public boolean knownHamful() {
		return isKnown() && (
				this instanceof PotionOfLiquidFlame ||
						this instanceof PotionOfToxicGas ||
						this instanceof PotionOfParalyticGas ||
						this instanceof PotionOfFrost);
	}

	@Override
	public void doThrow(@NotNull final Char chr) {

		if (isKnown() && (
			this instanceof PotionOfExperience || 
			this instanceof PotionOfHealing || 
			this instanceof PotionOfLevitation ||
			this instanceof PotionOfMindVision ||
			this instanceof PotionOfStrength ||
			this instanceof PotionOfInvisibility || 
			this instanceof PotionOfMight ||
			this instanceof PotionOfMana)) {

            GameScene.show(
				new WndOptions(StringsManager.getVar(R.string.Potion_Beneficial),
                        StringsManager.getVar(R.string.Potion_SureThrow),
                        StringsManager.getVar(R.string.Potion_Yes),
                        StringsManager.getVar(R.string.Potion_No)) {
					@Override
					public void onSelect(int index) {
						if (index == 0) {
							Potion.super.doThrow(chr);
						}
					}
				}
			);
			
		} else {
			super.doThrow(chr);
		}
	}
	
	protected void drink(Char hero ) {
		
		detach( hero.getBelongings().backpack );

		onThrow( hero.getPos(), hero, hero);
		
		Sample.INSTANCE.play( Assets.SND_DRINK );
		
		hero.doOperate(TIME_TO_DRINK);
		shattered = false;
	}
	
	private void moisten(Char hero) {
        GameScene.selectItem(hero, itemSelector, WndBag.Mode.MOISTABLE, StringsManager.getVar(R.string.Potion_SelectForMoisten));
		hero.doOperate(TIME_TO_MOISTEN);
	}
	
	@Override
	protected void onThrow(int cell, @NotNull Char thrower, Char enemy) {
		if (thrower.getPos() == cell) {
			apply( thrower );
		} else {
			Level level = thrower.level();
			LevelObject lo = level.getTopLevelObject(cell);
			if ((lo != null && lo.affectItems()) || level.pit[cell]) {
				super.onThrow( cell, thrower, enemy);
			} else  {
				shatter( cell );
			}
		}
	}
	
	protected void apply(Char hero ) {
		shatter( hero.getPos() );
	}

	protected boolean canShatter() {
		if(!shattered) {
			shattered = true;
			return true;
		}
		return false;
	}

	public void shatter( int cell ) {
        GLog.i(Utils.format(R.string.Potion_Shatter, color()));
		Sample.INSTANCE.play( Assets.SND_SHATTER );
		splash( cell );
	}
	
	public boolean isKnown() {
		return handler.isKnown( this );
	}
	
	public void setKnown() {
		if (!isKnown()) {
			handler.know( this );
		}
		
		Badges.validateAllPotionsIdentified();
	}
	
	@Override
	public Item identify() {
		setKnown();
		return this;
	}
	
	protected String color() {
		return color;
	}
	
	@Override
	public String name() {
        return isKnown() ? name : Utils.format(R.string.Potion_Name, color);
	}
	
	@Override
	public String info() {
        return isKnown() ? desc() : Utils.format(R.string.Potion_Info, color);
	}
	
	@Override
	public boolean isIdentified() {
		return isKnown();
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	public static HashSet<Class<? extends Potion>> getKnown() {
		return handler.known();
	}
	
	public static HashSet<Class<? extends Potion>> getUnknown() {
		return handler.unknown();
	}
	
	public static boolean allKnown() {
		return handler.known().size() == potions.length;
	}
	
	protected void splash( int cell ) {

		if(!GameScene.isSceneReady()) {
			return;
		}

		final int color = ItemSprite.pick( image, 8, 10 );
		Splash.at( cell, color, 5 );
	}

	public int basePrice() {
		return 20;
	}

	@Override
	public int price() {
		return (int) ((isKnown() ? basePrice() : 20) * quantity() * qualityFactor());
	}
	
	@Override
	public Item freeze(int cell){
		shatter(cell);
		return null;
	}
	
	private final WndBag.Listener itemSelector = (item, selector) -> {
		if (item != null) {

			if(item instanceof Arrow) {
				moistenArrow ((Arrow) item, item.getOwner());
			}

			if(item instanceof Scroll) {
				moistenScroll ((Scroll) item, item.getOwner());
			}

			if(item instanceof RottenFood) {
				moistenRottenFood((RottenFood) item, item.getOwner());
			}
		}
	};

	protected int detachMoistenItems(Item item, int maxQuantity) {
		setKnown();
		int quantity = item.quantity();
		
		if(quantity <= maxQuantity){
			Char owner = getOwner();

			if(item.equals(owner.getItemFromSlot(Belongings.Slot.WEAPON))) {
				owner.getBelongings().setItemForSlot(ItemsList.DUMMY, Belongings.Slot.WEAPON);
			} else {
				item.detachAll( owner.getBelongings().backpack );
			}
		} else {
			item.quantity(item.quantity() - maxQuantity);
			quantity = maxQuantity;
		}
		return quantity;
	}

	double qualityFactor() {
		return 1;
	}

	protected int reallyMoistArrows(Arrow arrow,Char owner) {
		int quantity = detachMoistenItems(arrow, (int) (10*qualityFactor()));
		moistenEffective(owner);
        GLog.i(StringsManager.getVar(R.string.Potion_ArrowMoisten));
		return quantity;
	}

	protected void moistenRottenFood(RottenFood scroll, Char owner) {
		moistenUseless(owner);
	}
	
	protected void moistenScroll(Scroll scroll, Char owner) {
		moistenUseless(owner);
	}
	
	protected void moistenArrow(Arrow arrow, Char owner) {
		moistenUseless(owner);
	}
	
	private void moistenUseless(@NotNull Char owner) {

		detach(owner.getBelongings().backpack );
        GLog.i(StringsManager.getVar(R.string.Potion_MoistenUseless));
		owner.doOperate(TIME_TO_MOISTEN );
	}
	
	protected void moistenEffective(@NotNull Char owner) {
		detach(owner.getBelongings().backpack );
		identify();
		owner.doOperate(TIME_TO_MOISTEN );
	}

	@Override
	public int overlayIndex() {
		if(!isIdentified()) {
			return super.overlayIndex();
		}
		return labelIndex;
	}

	@Override
	public String bag() {
		return PotionBelt.class.getSimpleName();
	}
}
