
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.items.weapon.enchantments.Piercing;
import com.watabou.pixeldungeon.items.weapon.enchantments.Swing;
import com.watabou.pixeldungeon.items.weapon.melee.KindOfBow;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.MissileSprite;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndOptions;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaValue;

public class MissileWeapon extends Weapon {

    {
        stackable = true;
        setLevelKnown(true);
        setDefaultAction(AC_THROW);
    }

    @Override
    public ArrayList<String> actions(Char hero) {
        ArrayList<String> actions = super.actions(hero);
        if (!Utils.isOneOf(hero.getHeroClass(), HeroClass.HUNTRESS, HeroClass.ROGUE, HeroClass.GNOLL)) {
            actions.remove(CommonActions.AC_EQUIP);
            actions.remove(CommonActions.AC_UNEQUIP);
        }
        return actions;
    }

    @Override
    protected void onThrow(int cell, @NotNull Char thrower, Char target) {
        if (kindWeapon && dropsToFloorOnThrow && target != null && target.valid() && target != thrower) {
            thrower.shoot( target, this );
            dropTo( cell, thrower);
            return;
        }
        if (kindWeapon && incendiary && (target == null || !target.valid() || target == thrower)) {
            // IncendiaryDart: seed a fire blob on a flammable landing cell
            if (thrower.level().flammable[cell]) {
                GameScene.add(com.watabou.pixeldungeon.actors.blobs.Blob.seed( cell, 4, com.watabou.pixeldungeon.actors.blobs.Fire.class ));
            } else {
                super.onThrow( cell, thrower, target);
            }
            return;
        }
        if (target == null || !target.valid() || target == thrower) {
            super.onThrow(cell, thrower, target);
        } else {
            if (!thrower.shoot(target, this)) {
                miss(cell, thrower, target);
            }
        }
    }

    protected void miss(int cell, Char thrower, Char enemy) {

        if (kindWeapon && returnsToHand) {
            circleBack( cell, thrower);
            return;
        }

        if (this instanceof Arrow) {
            Arrow arrow = (Arrow) this;
            if (arrow.firedFrom != null) {
                arrow.firedFrom.onMiss(thrower);
            }
        }

        super.onThrow(cell, thrower, enemy);
    }

    @Override
    public void attackProc(Char attacker, Char defender, int damage) {

        // data-kind proc first, then the base relay + hand-throw detach
        int baseDamage = damage;
        if (kindWeapon && kindScript.hasMethod("attackProc")) {
            baseDamage = kindScript.runOptional("attackProc", damage, attacker, defender, damage);
        }

        super.attackProc(attacker, defender, baseDamage);

        if (kindWeapon && returnsToHand && attacker.rangedWeapon == this) {
            circleBack( defender.getPos(), attacker);
        }

        if (this instanceof Arrow) {
            Arrow arrow = (Arrow) this;
            KindOfBow firedFrom = arrow.firedFrom;
            if (firedFrom != null && firedFrom.isEnchanted()) {
                firedFrom.getEnchantment().proc(firedFrom, attacker, defender, baseDamage);
            }
        }

        if (!attacker.rangedWeapon.valid() && stackable) {
            if (!attacker.getBelongings().isEquipped(this)) {
                if (quantity() == 1) {
                    doUnequip(attacker, false, false);
                } else {
                    detach(null);
                }
            }
        }
    }

    @Override
    public boolean doEquip(@NotNull final Char hero) {
        if (notUsableInMelee()) {

            final WndBag wndBag = WndBag.getInstance();
            if (wndBag == null) {
                return false;
            }

            wndBag.setItemsActive(false);

            wndBag.add(
                    new WndOptions(StringsManager.getVar(R.string.MissileWeapon_Missiles),
                            StringsManager.getVar(R.string.MissileWeapon_Sure),
                            StringsManager.getVar(R.string.MissileWeapon_Yes),
                            StringsManager.getVar(R.string.MissileWeapon_No)) {

						@Override
                        public void onSelect(int index) {
                            if (index == 0) {
                                MissileWeapon.super.doEquip(hero);
                            }
                            wndBag.updateItems();
                        }

                        // the bag list is disabled while this dialog is up: re-enable on
                        // every way out, not just the buttons (outside tap exits via hide())
                        @Override
                        public void hide() {
                            wndBag.setItemsActive(true);
                            super.hide();
                        }
                    }
            );
        } else {
            return MissileWeapon.super.doEquip(hero);
        }
        return false;
    }

    @Override
    public Item random() {
        if (kindWeapon && kindScript.hasMethod("randomQty")) {
            LuaValue lo = kindScript.run("randomQty", 0);
            LuaValue hi = kindScript.run("randomQty", 1);
            quantity(com.watabou.utils.Random.Int(lo.toint(), hi.toint()));
            return this;
        }
        return this;
    }

    protected boolean notUsableInMelee() {
        if (kindWeapon) {
            return !kindUsableInMelee;
        }
        return true;
    }

    @Override
    public boolean isUpgradable() {
        if (kindWeapon) {
            return kindUpgradable;
        }
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public String info() {

        String descText = desc();
        if (kindWeapon) {
            descText = kindScript.runOptional("info", descText);
        }
        StringBuilder info = new StringBuilder(descText);

        info.append(Utils.format(R.string.MissileWeapon_Info1, MIN + (MAX - MIN) / 2));
        info.append(" ");

        final Char hero = Dungeon.hero;

        if (hero.getBelongings().backpack.items.contains(this)) {
            int effectiveSTR = hero.effectiveSTR();
            int requiredSTR = requiredSTR();
            if (requiredSTR > effectiveSTR) {
                info.append(Utils.format(R.string.MissileWeapon_Info2, name));
            }
            if (requiredSTR < effectiveSTR) {
                info.append(Utils.format(R.string.MissileWeapon_Info3, name));
            }
        }

        if (isEquipped(hero)) {
            info.append(Utils.format(R.string.MissileWeapon_Info4, name));
        }

        return info.toString();
    }

    @Override
    public boolean isFliesStraight() {
        if (kindWeapon) {
            return kindScript.runOptional("isFliesStraight", super.isFliesStraight());
        }
        return true;
    }

    @Override
    public String getVisualName() {
        if (kindWeapon) {
            return kindScript.runOptional("visualName", super.getVisualName());
        }
        return "none";
    }

    @Override
    public int image() {
        if (kindWeapon && kindScript.hasMethod("image")) {
            return kindScript.run("image").toint();
        }
        return super.image();
    }

    @Override
    public String bag() {
        return "Quiver";
    }

	// --- data-kind surface (was one java class per missile) ---

	protected LuaScript kindScript;
	protected boolean kindWeapon;
	protected String missileKind;

	private boolean dropsToFloorOnThrow;
	private boolean incendiary;
	private boolean returnsToHand;
	private boolean rerollForbiddenEnchants;
	private boolean kindUpgradable;
	private boolean kindUsableInMelee = true;
	private float kindTime2equip = -1;

	@NotNull
	private Belongings.Slot throwSlot = Belongings.Slot.NONE;

	public MissileWeapon() {
	}

	public MissileWeapon(@NotNull String kind) {
		this(kind, "scripts/lib/emptykind");
	}

	public MissileWeapon(@NotNull String kind, String fallbackScript) {
		missileKind = kind;
		kindWeapon = true;
		kindScript = new LuaScript("scripts/items/" + kind, fallbackScript, this);

		name = getClassParam("Name", StringsManager.getVar(R.string.Item_Name), false);
		info = getClassParam("Info", StringsManager.getVar(R.string.Item_Info), false);

		if (!kindScript.hasMethod("desc")) {
			return;
		}
		LuaValue desc = kindScript.run("desc").checktable();

		setSTR(desc.rawget("STR").optint(10));
		MIN = desc.rawget("min").optint(1);
		MAX = desc.rawget("max").optint(4);
		DLY = (float) desc.rawget("dly").optdouble(1);

		int imageOverride = desc.rawget("image").optint(-1);
		if (imageOverride >= 0) {
			image = imageOverride;
		}
		LuaValue imageFileOverride = desc.rawget("imageFile");
		if (!imageFileOverride.isnil()) {
			imageFile = imageFileOverride.tojstring();
		}

		dropsToFloorOnThrow  = desc.rawget("dropsToFloorOnThrow").optboolean(false);
		incendiary           = desc.rawget("incendiary").optboolean(false);
		returnsToHand        = desc.rawget("returnsToHand").optboolean(false);
		rerollForbiddenEnchants = desc.rawget("rerollForbiddenEnchants").optboolean(false);
		kindUpgradable       = desc.rawget("upgradable").optboolean(false);
		kindUsableInMelee    = !desc.rawget("notUsableInMelee").optboolean(false);
		kindTime2equip       = (float) desc.rawget("time2equip").optdouble(-1);
	}

	@Override
	public String getEntityKind() {
		return kindWeapon ? missileKind : super.getEntityKind();
	}

	@Override
	public int price() {
		if (kindWeapon && kindScript.hasMethod("pricePerUnit")) {
			return (int) (kindScript.runOptional("pricePerUnit", 2) * quantity());
		}
		if (kindWeapon && kindScript.hasMethod("price")) {
			return kindScript.runOptional("price", 100);
		}
		return super.price();
	}

	@Override
	public float time2equipBase() {
		if (kindWeapon && kindTime2equip >= 0) {
			return kindTime2equip;
		}
		return super.time2equipBase();
	}

	@Override
	public Item upgrade( boolean enchant ) {
		if (kindWeapon && returnsToHand) {
			// Boomerang: MIN+1, MAX+2 per upgrade
			MIN += 1;
			MAX += 2;
			super.upgrade( enchant );
			return this;
		}
		return super.upgrade( enchant );
	}

	@Override
	public Item degrade() {
		if (kindWeapon && returnsToHand) {
			MIN -= 1;
			MAX -= 2;
			return super.degrade();
		}
		return super.degrade();
	}

	@Override
	public Weapon enchant( Enchantment ench ) {
		if (kindWeapon && rerollForbiddenEnchants) {
			// Boomerang: no Piercing/Swing
			while (ench instanceof Piercing || ench instanceof Swing) {
				ench = Enchantment.random();
			}
		}
		return super.enchant( ench );
	}

	@Override
	public boolean isFliesFastRotating() {
		if (kindWeapon) {
			return kindScript.runOptional("isFliesFastRotating", super.isFliesFastRotating());
		}
		return super.isFliesFastRotating();
	}

	@Override
	public String getAttackAnimationClass() {
		if (kindWeapon) {
			String anim = kindScript.runOptional("anim", Utils.EMPTY_STRING);
			if (!anim.isEmpty()) {
				return anim;
			}
		}
		return super.getAttackAnimationClass();
	}

	@Override
	public void cast(@NotNull Char user, int dst ) {
		if (kindWeapon && returnsToHand) {
			throwSlot = getOwner().getBelongings().itemSlot(this);
		}
		super.cast( user, dst );
	}

	// was Boomerang.circleBack: the thrown weapon flies back to the thrower
	private void circleBack(int from, @NotNull final Char owner) {

		((MissileSprite) owner.getSprite().getParent()
				.recycle(MissileSprite.class)).reset(from, owner.getPos(),
				this, () -> {
					// First, return the item to its original inventory slot if it was equipped
					if (throwSlot != null && throwSlot != Belongings.Slot.NONE) {
						owner.getBelongings().setItemForSlot(this, throwSlot);
					} else {
						owner.collect(this);
					}

					// Then, if it was in a quickslot, place it back in the quickslot as well
					int qsIndex = getQuickSlotIndex();
					if (qsIndex >= 0) {
						Item quickslotItem = QuickSlot.getEarlyLoadItem(qsIndex);
						if (quickslotItem == null || quickslotItem == this) {
							// Quickslot is available, set the boomerang back to it
							QuickSlot.selectItem(this, qsIndex);
						}
						// If quickslot is occupied by another item, just leave it in the inventory
					}
					QuickSlot.refresh(owner);
				});
	}

}