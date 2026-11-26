
package com.watabou.pixeldungeon.items.armor;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaValue;

public class ClassArmor extends Armor {

	private final int specialCostModifier = 3;

	@Packable
	public int STR;
	@Packable
	public int DR;

	{
		setLevelKnown(true);
		setCursedKnown(true);
	}

	public ClassArmor() {
		super( 6 );
		setDefaultAction(special());
	}

	// data-kind surface (was one java class per hero class armor)
	private String specialAction = Utils.EMPTY_STRING;
	private boolean kindBuilt;
	private int specialCostModifierKind = 3;
	private boolean leaper;
	private boolean teleporter;
	private boolean mageSpecial;
	private boolean huntressSpecial;
	private boolean elfSpecial;
	private boolean gnollSpecial;
	private boolean priestSpecial;

	public ClassArmor(@NotNull String kind) {
		this(kind, "scripts/lib/emptykind");
	}

	public ClassArmor(@NotNull String kind, String fallbackScript) {
		super(kind);
		kindBuilt = true;
		setLevelKnown(true);
		setCursedKnown(true);

		if (!kindScript.hasMethod("desc")) {
			setDefaultAction(special());
			return;
		}
		LuaValue desc = kindScript.run("desc").checktable();
		specialAction = desc.rawget("special").optjstring(Utils.EMPTY_STRING);
		STR = desc.rawget("STR").optint(0);
		DR  = desc.rawget("DR").optint(0);
		leaper           = desc.rawget("leaper").optboolean(false);
		teleporter       = desc.rawget("teleporter").optboolean(false);
		mageSpecial      = desc.rawget("mageSpecial").optboolean(false);
		huntressSpecial  = desc.rawget("huntressSpecial").optboolean(false);
		elfSpecial       = desc.rawget("elfSpecial").optboolean(false);
		gnollSpecial     = desc.rawget("gnollSpecial").optboolean(false);
		priestSpecial    = desc.rawget("priestSpecial").optboolean(false);
		setDefaultAction(special());
	}

	public String special() {
		return specialAction;
	}

	public void doSpecial(@NotNull Char user) {
		kindScript.runOptionalNoRet("doSpecial", user);
	}

	// ArmorKit flow: build the hero's class armor kind with the sacrificed
	// armor's stats + glyph (was HeroClass/HeroSubClass.classArmor newInstance)
	public static ClassArmor forKind(@NotNull String kind, @NotNull Char owner, @NotNull Armor armor) {
		ClassArmor classArmor = (ClassArmor) ItemFactory.itemByName(kind);
		classArmor.setOwner(owner);
		classArmor.STR = armor.requiredSTR();
		classArmor.DR  = armor.effectiveDr();
		classArmor.inscribe( armor.glyph );
		return classArmor;
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
