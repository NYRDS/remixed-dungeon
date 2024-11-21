
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Quiver;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.items.weapon.melee.KindOfBow;
import com.watabou.pixeldungeon.utils.HUtils;
import com.nyrds.util.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndOptions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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
        if (target == null || !target.valid() || target == thrower) {
            super.onThrow(cell, thrower, target);
        } else {
            if (!thrower.shoot(target, this)) {
                miss(cell, thrower, target);
            }
        }
    }

    protected void miss(int cell, Char thrower, Char enemy) {

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

        super.attackProc(attacker, defender, damage);

        if (this instanceof Arrow) {
            Arrow arrow = (Arrow) this;
            KindOfBow firedFrom = arrow.firedFrom;
            if (firedFrom != null && firedFrom.isEnchanted()) {
                firedFrom.getEnchantment().proc(firedFrom, attacker, defender, damage);
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
                    }
            );
        } else {
            return MissileWeapon.super.doEquip(hero);
        }
        return false;
    }

    @Override
    public Item random() {
        return this;
    }

    protected boolean notUsableInMelee() {
        return true;
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public String info() {

        StringBuilder info = new StringBuilder(desc());

        info.append(HUtils.format(R.string.MissileWeapon_Info1, MIN + (MAX - MIN) / 2));
        info.append(" ");

        final Char hero = Dungeon.hero;

        if (hero.getBelongings().backpack.items.contains(this)) {
            int effectiveSTR = hero.effectiveSTR();
            int requiredSTR = requiredSTR();
            if (requiredSTR > effectiveSTR) {
                info.append(HUtils.format(R.string.MissileWeapon_Info2, name));
            }
            if (requiredSTR < effectiveSTR) {
                info.append(HUtils.format(R.string.MissileWeapon_Info3, name));
            }
        }

        if (isEquipped(hero)) {
            info.append(HUtils.format(R.string.MissileWeapon_Info4, name));
        }

        return info.toString();
    }

    @Override
    public boolean isFliesStraight() {
        return true;
    }

    @Override
    public String getVisualName() {
        return "none";
    }

    @Override
    public String bag() {
        return Quiver.class.getSimpleName();
    }
}
