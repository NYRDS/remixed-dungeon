
package com.watabou.pixeldungeon.items.armor;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

abstract public class ClassArmor extends Armor {

	private final int specialCostModifier = 3;

	@Packable
	public int STR;
	@Packable
	public int DR;

	{
		setLevelKnown(true);
		setCursedKnown(true);
		setDefaultAction(special());
	}
	
	public ClassArmor() {
		super( 6 );
	}
	
	public static Armor upgrade (Char owner, Armor armor ) {

		ClassArmor classArmor;

		if(owner.getSubClass() == HeroSubClass.NONE) {
			classArmor = owner.getHeroClass().classArmor();
		} else {
			classArmor = owner.getSubClass().classArmor();
		}

		classArmor.setOwner(owner);

		classArmor.STR = armor.requiredSTR();
		classArmor.DR  = armor.effectiveDr();
		
		classArmor.inscribe( armor.glyph );
		
		return classArmor;
	}

	@Override
	public int effectiveDr() {
		return DR;
	}

	@Override
	public int requiredSTR() {
		return STR;
	}

	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (hero.getSkillPoints() >= hero.getSkillPointsMax()/specialCostModifier + 1 && isEquipped( hero )) {
			actions.add( special() );
		}
		return actions;
	}
	
	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		if (action.equals(special())) {

			int cost = chr.getSkillPointsMax()/specialCostModifier;

			if (chr.getSkillPoints() < cost) {
                GLog.w(StringsManager.getVar(R.string.ClassArmor_LowMana));
				return;
			}
			if (!isEquipped(chr)) {
                GLog.w(StringsManager.getVar(R.string.ClassArmor_NotEquipped));
				return;
			}

			doSpecial(chr);
			chr.spendSkillPoints(cost);
			return;
		}

		super._execute(chr, action );
	}
	
	abstract public String special();
	abstract public void doSpecial(@NotNull Char user);
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public int price() {
		return 0;
	}
}
