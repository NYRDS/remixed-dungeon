package com.nyrds.retrodungeon.items.chaos;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.rings.UsableArtifact;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfWeaponUpgrade;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.weapon.melee.Bow;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class ChaosCrystal extends UsableArtifact implements IChaosItem {

	private static final String IDENTETIFY_LEVEL_KEY = "identetifyLevel";
	private static final String CHARGE_KEY = "charge";

	public static final float TIME_TO_USE = 1;

	public static final String AC_FUSE = Game.getVar(R.string.ChaosCrystal_Fuse);
	private static final String TXT_SELECT_FOR_FUSE = Game.getVar(R.string.ChaosCrystal_SelectForFuse);

	private static final int CHAOS_CRYSTALL_IMAGE = 9;
	private static final float TIME_TO_FUSE = 10;

	private int identetifyLevel = 0;
	private int charge = 0;

	public ChaosCrystal() {
		imageFile = "items/artifacts.png";
		image = CHAOS_CRYSTALL_IMAGE;
	}

	@Override
	public boolean isIdentified() {
		return identetifyLevel == 2;
	}

	@Override
	public Glowing glowing() {
		return new Glowing((int) (Math.random() * 0xffffff));
	}

	protected CellSelector.Listener chaosMark = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer cell) {
			if (cell != null) {

				if (cursed) {
					cell = getCurUser().getPos();
				}

				ChaosCommon.doChaosMark(cell, charge);
				charge = 0;
			}
			getCurUser().spendAndNext(TIME_TO_USE);
		}

		@Override
		public String prompt() {
			return Game.getVar(R.string.ChaosCrystal_Prompt);
		}
	};

	private final WndBag.Listener itemSelector = new WndBag.Listener() {
		@Override
		public void onSelect(Item item) {
			if (item != null) {

				if (item.quantity() > 1) {
					item.detach(getCurUser().belongings.backpack);
				} else {
					item.removeItemFrom(getCurUser());
				}

				removeItemFrom(getCurUser());

				getCurUser().getSprite().operate(getCurUser().getPos());
				getCurUser().spend(TIME_TO_FUSE);
				getCurUser().busy();

				if (item instanceof Scroll) {
					Item newItem = new ScrollOfWeaponUpgrade();
					getCurUser().collect(newItem);
					GLog.p(Game.getVar(R.string.ChaosCrystal_ScrollFused), newItem.name());
					return;
				}

				if (item instanceof Bow) {
					getCurUser().collect(new ChaosBow());
					GLog.p(Game.getVar(R.string.ChaosCrystal_BowFused));
					return;
				}

				if (item instanceof MeleeWeapon) {
					getCurUser().collect(new ChaosSword());
					GLog.p(Game.getVar(R.string.ChaosCrystal_SwordFused));
					return;
				}

				if (item instanceof Armor) {
					getCurUser().collect(new ChaosArmor());
					GLog.p(Game.getVar(R.string.ChaosCrystal_ArmorFused));
					return;
				}

				if (item instanceof Wand) {
					getCurUser().collect(new ChaosStaff());
					GLog.p(Game.getVar(R.string.ChaosCrystal_StaffFused));
				}


			}
		}
	};

	private void fuse(Hero hero) {
		GameScene.selectItem(itemSelector, WndBag.Mode.FUSEABLE, TXT_SELECT_FOR_FUSE);
		hero.getSprite().operate(hero.getPos());
	}

	@Override
	public void execute(final Hero ch, String action) {
		setCurUser(ch);

		if (action.equals(AC_USE)) {
			GameScene.selectCell(chaosMark);
		} else if (action.equals(AC_FUSE)) {
			fuse(ch);
		} else {

			super.execute(ch, action);
		}
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (charge == 0 || identetifyLevel == 0) {
			actions.remove(AC_USE);
		}

		if (charge >= 50 && identetifyLevel > 1) {
			actions.add(AC_FUSE);
		}
		return actions;
	}

	@Override
	public Item identify() {
		identetifyLevel++;
		return this;
	}

	@Override
	public String name() {
		switch (identetifyLevel) {
			default:
				return super.name();
			case 1:
				return Game.getVar(R.string.ChaosCrystal_Name_1);
			case 2:
				return Game.getVar(R.string.ChaosCrystal_Name_2);
		}
	}

	@Override
	public String info() {
		switch (identetifyLevel) {
			default:
				return super.info();
			case 1:
				return Game.getVar(R.string.ChaosCrystal_Info_1);
			case 2:
				return Game.getVar(R.string.ChaosCrystal_Info_2);
		}
	}

	@Override
	public String getText() {
		if (identetifyLevel > 0) {
			return Utils.format("%d/100", charge);
		} else {
			return null;
		}
	}

	@Override
	public int getColor() {
		return 0xe0a0a0;
	}

	@Override
	public void ownerTakesDamage(int damage) {
		if (damage > 0) {
			charge++;
			if (charge > 100) {
				charge = 100;
			}
		}
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);

		bundle.put(CHARGE_KEY, charge);
		bundle.put(IDENTETIFY_LEVEL_KEY, identetifyLevel);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

		charge = bundle.getInt(CHARGE_KEY);
		identetifyLevel = bundle.getInt(IDENTETIFY_LEVEL_KEY);

	}

	@Override
	public void ownerDoesDamage(Char ch,int damage) {
		if (cursed) {
			if (charge > 0) {
				ChaosCommon.doChaosMark(ch.getPos(), charge);
			}
		}
	}

	@Override
	public int getCharge() {
		return charge;
	}
}
