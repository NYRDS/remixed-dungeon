/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.items.wands;

import com.nyrds.android.util.Scrambler;
import com.nyrds.pixeldungeon.items.common.UnknownItem;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.ItemStatusHandler;
import com.watabou.pixeldungeon.items.KindOfWeapon;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.rings.RingOfPower.Power;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class Wand extends KindOfWeapon implements UnknownItem {

	private static final String AC_ZAP = "Wand_ACZap";

	private static final float TIME_TO_ZAP = 1f;

	private int maxCharges = Scrambler.scramble(initialCharges());
	private int curCharges = Scrambler.scramble(maxCharges());
	
	protected Char wandUser;

	private Charger charger;

	private boolean curChargeKnown = false;

	protected boolean hitChars    = true;
	protected boolean hitObjects  = false;

	private   boolean directional = true;

	private static final Class<?>[] wands = { WandOfTeleportation.class,
			WandOfSlowness.class, WandOfFirebolt.class, WandOfPoison.class,
			WandOfRegrowth.class, WandOfBlink.class, WandOfLightning.class,
			WandOfAmok.class, WandOfTelekinesis.class, WandOfFlock.class,
			WandOfDisintegration.class, WandOfAvalanche.class };

	private static final Integer[] images = { ItemSpriteSheet.WAND_HOLLY,
			ItemSpriteSheet.WAND_YEW, ItemSpriteSheet.WAND_EBONY,
			ItemSpriteSheet.WAND_CHERRY, ItemSpriteSheet.WAND_TEAK,
			ItemSpriteSheet.WAND_ROWAN, ItemSpriteSheet.WAND_WILLOW,
			ItemSpriteSheet.WAND_MAHOGANY, ItemSpriteSheet.WAND_BAMBOO,
			ItemSpriteSheet.WAND_PURPLEHEART, ItemSpriteSheet.WAND_OAK,
			ItemSpriteSheet.WAND_BIRCH };

	private static ItemStatusHandler<Wand> handler;

	private String wood;

	@SuppressWarnings("unchecked")
	public static void initWoods() {
		handler = new ItemStatusHandler<>((Class<? extends Wand>[]) wands,images);
	}

	public static void save(Bundle bundle) {
		handler.save(bundle);
	}

	@SuppressWarnings("unchecked")
	public static void restore(Bundle bundle) {
		handler = new ItemStatusHandler<>((Class<? extends Wand>[]) wands, images, bundle);
	}

	public Wand() {
		setDefaultAction(AC_ZAP);
		animation_class = WAND_ATTACK;
		
		try {
			image = handler.index(this);
			wood = Game.getVars(R.array.Wand_Wood_Types)[ItemStatusHandler.indexByImage(image,images)];

		} catch (Exception e) {
			// Wand of Magic Missile or Wand of Icebolt
		}
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (curCharges() > 0 || !curChargeKnown) {
			actions.add(AC_ZAP);
		}

		actions.remove(AC_EQUIP);
		actions.remove(AC_UNEQUIP);
		
		if (hero.getHeroClass() == HeroClass.MAGE
			|| hero.getSubClass() == HeroSubClass.SHAMAN) {
			
			if(hero.getBelongings().weapon == this) {
				actions.add(AC_UNEQUIP); 
			} else {
				actions.add(AC_EQUIP);
			}
		}
		return actions;
	}

	@Override
	public void deactivate(Char ch) {
		onDetach();
	}

	@Override
	public void activate(Char chr) {
		charge(chr);
	}

	@Override
	public void execute(Hero hero, String action) {
		if (action.equals(AC_ZAP)) {
			setUser(hero);
			wandUser = hero;
			curItem = this;
			GameScene.selectCell(zapper);
			return;
		}

		super.execute(hero, action);
	}

	public void zapCell(Char chr, int cell) {
		setUser(chr);
		wandUser = chr;
		getDestinationCell(chr.getPos(),cell);
		onZap(cell);
	}
	
	protected abstract void onZap(int cell);

	@Override
	public boolean collect(Bag container) {
		if (super.collect(container)) {
			if (container.owner != null) {
				charge(container.owner);
			}
			return true;
		} else {
			return false;
		}
	}

	public void charge(Char owner) {
		(charger = new Charger()).attachTo(owner);
	}

	@Override
	public void onDetach() {
		stopCharging();
	}

	public void stopCharging() {
		if (charger != null) {
			charger.detach();
			charger = null;
		}
	}

	public int effectiveLevel() {
		if (charger != null) {
			Power power = charger.target.buff(Power.class);
			return power == null ? super.level() : Math.max(super.level() + power.level(), 0);
		} else {
			return super.level();
		}
	}

	public boolean isKnown() {
		return handler.isKnown(this);
	}

	public void setKnown() {
		if (!isKnown()) {
			handler.know(this);
		}

		Badges.validateAllWandsIdentified();
	}

	@Override
	public Item identify() {

		setKnown();
		curChargeKnown = true;
		super.identify();

        QuickSlot.refresh();

        return this;
	}

	@NotNull
    @Override
	public String toString() {

		StringBuilder sb = new StringBuilder(super.toString());

		String status = status();
		if (!status.equals(Utils.EMPTY_STRING)) {
			sb.append(" (" + status + ")");
		}

		return sb.toString();
	}

	@Override
	public String name() {
		return isKnown() ? name : Utils.format(R.string.Wand_Name, wood);
	}

	@Override
	public String info() {
		StringBuilder info = new StringBuilder(isKnown() ? desc()
				: Utils.format(R.string.Wand_Wood, wood));
		if (Dungeon.hero.getHeroClass() == HeroClass.MAGE
				|| Dungeon.hero.getSubClass() == HeroSubClass.SHAMAN) {
			damageRoll(Dungeon.hero);
			info.append("\n\n");
			if (isLevelKnown()) {
				info.append(Utils.format(R.string.Wand_Damage, MIN + (MAX - MIN) / 2));
			} else {
				info.append(Game.getVar(R.string.Wand_Weapon));
			}
		}
		return info.toString();
	}

	@Override
	public boolean isIdentified() {
		return super.isIdentified() && isKnown() && curChargeKnown;
	}

	@Override
	public String status() {
		if (isLevelKnown()) {
			return (curChargeKnown ? curCharges() : "?") + "/" + maxCharges();
		} else {
			return Utils.EMPTY_STRING;
		}
	}

	@Override
	public Item upgrade() {

		super.upgrade();

		maxCharges(Math.max(Math.min(maxCharges()+1, 9),maxCharges()));
		curCharges(Math.max(curCharges(), maxCharges()));

        QuickSlot.refresh();

        return this;
	}

	@Override
	public Item degrade() {
		super.degrade();

		maxCharges(Math.max(maxCharges()-1, 0));
		curCharges(Math.min(curCharges(), maxCharges()));

        QuickSlot.refresh();

        return this;
	}

	protected int initialCharges() {
		return 2;
	}

	public void mobWandUse(Char user, final int tgt) {
		wandUser = user;

		final int cell = getDestinationCell(user.getPos(),tgt);

		fx(cell, () -> onZap(cell));
		
	}
	
	protected void fx(int cell, Callback callback) {
		MagicMissile.blueLight(wandUser.getSprite().getParent(), wandUser.getPos(), cell,
				callback);
		Sample.INSTANCE.play(Assets.SND_ZAP);
	}

	protected void wandUsed() {
		curCharges(curCharges() - 1);
        QuickSlot.refresh();

        getUser().spendAndNext(TIME_TO_ZAP);
	}

	@Override
	public Item random() {
		if (Random.Float() < 0.5f) {
			upgrade();
			if (Random.Float() < 0.15f) {
				upgrade();
			}
		}

		return this;
	}

	public static boolean allKnown() {
		return handler.known().size() == wands.length;
	}

	@Override
	public int price() {
		int price = 50;
		if (cursed && cursedKnown) {
			price /= 2;
		}
		if (isLevelKnown()) {
			if (level() > 0) {
				price *= (level() + 1);
			} else if (level() < 0) {
				price /= (1 - level());
			}
		}
		if (price < 1) {
			price = 1;
		}
		return price;
	}

	private static final String MAX_CHARGES = "maxCharges";
	private static final String CUR_CHARGES = "curCharges";
	private static final String CUR_CHARGE_KNOWN = "curChargeKnown";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(MAX_CHARGES, maxCharges());
		bundle.put(CUR_CHARGES, curCharges());
		bundle.put(CUR_CHARGE_KNOWN, curChargeKnown);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		maxCharges(bundle.getInt(MAX_CHARGES));
		curCharges(bundle.getInt(CUR_CHARGES));
		curChargeKnown = bundle.getBoolean(CUR_CHARGE_KNOWN);
	}

	protected void wandEffect(final int cell) {
		setKnown();

		QuickSlot.target(curItem, Actor.findChar(cell));

		if (curCharges() > 0) {

			getUser().busy();

			fx(cell, () -> {
				onZap(cell);
				wandUsed();
			});

			Invisibility.dispel(getUser());
		} else {

			getUser().spendAndNext(TIME_TO_ZAP);
			GLog.w(Game.getVar(R.string.Wand_Fizzles));
			setLevelKnown(true);

			if (Random.Int(5) == 0) {
				identify();
			}

            QuickSlot.refresh();
        }

	}

	protected static CellSelector.Listener zapper = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {

			if (target != null) {
				if (target == getUser().getPos()) {
					GLog.i(Game.getVar(R.string.Wand_SelfTarget));
					return;
				}

				final Wand curWand = (Wand) Wand.curItem;
				final int cell = curWand.getDestinationCell(getUser().getPos(),target);
				getUser().getSprite().zap(cell);
				curWand.wandEffect(cell);
			}
		}

		@Override
		public String prompt() {
			return Game.getVar(R.string.Wand_Prompt);
		}
	};

	protected int getDestinationCell(int src, int target) {
		return Ballistica.cast(src, target, directional, hitChars, hitObjects);
	}

	public int curCharges() {
		return Scrambler.descramble(curCharges);
	}

	public void curCharges(int curCharges) {
		this.curCharges = Scrambler.scramble(curCharges);
	}

	public int maxCharges() {
		return Scrambler.descramble(maxCharges);
	}

	public void maxCharges(int maxCharges) {
		this.maxCharges = Scrambler.scramble(maxCharges);
	}

	protected class Charger extends Buff {
		private static final float TIME_TO_CHARGE = 40f;

		@Override
		public boolean dontPack(){
			return true;
		}

		@Override
		public boolean attachTo(Char target) {
			super.attachTo(target);
			delay();

			return true;
		}

		@Override
		public boolean act() {

			if (curCharges() < maxCharges()) {
				curCharges(curCharges() + 1);
                QuickSlot.refresh();
            }

			delay();

			return true;
		}

		protected void delay() {
			float time2charge = target.getHeroClass() == HeroClass.MAGE ? TIME_TO_CHARGE
					/ (float) Math.sqrt(1 + effectiveLevel())
					: TIME_TO_CHARGE;
			spend(time2charge);
		}
	}

	public boolean affectTarget() {
		return true;
	}

	@Override
	public int damageRoll(Hero owner) {
		int tier = 1 + effectiveLevel() / 3;
		MIN = tier + owner.skillLevel();
		MAX = (tier * tier - tier + 10) / 2 + owner.skillLevel()*tier + effectiveLevel();

		return super.damageRoll(owner);
	}

	@Override
	public void fromJson(JSONObject itemDesc) throws JSONException {
		super.fromJson(itemDesc);

		maxCharges(Math.min(initialCharges()+level(), 9));
		curCharges(maxCharges());

		curCharges(itemDesc.optInt("charges",curCharges()));
		maxCharges(itemDesc.optInt("maxCharges",maxCharges()));
	}

	@Override
	public String getVisualName() {
		return "Wand";
	}
}
