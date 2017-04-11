package com.nyrds.pixeldungeon.mobs.common;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mike on 11.04.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class CustomMob extends MultiKindMob {

	private final String DEFENSE_SKILL = "defenseSkill";
	private final String EXP           = "exp";
	private final String MAX_LVL       = "maxLvl";
	private final String DMG_MIN       = "dmgMin";
	private final String DMG_MAX       = "dmgMax";
	private final String ATTACK_SKILL  = "attackSkill";
	private final String DR            = "dr";
	

	private int dmgMin, dmgMax;
	private int attackSkill;
	private int dr;

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(dmgMin, dmgMax);
	}

	@Override
	public int attackSkill(Char target) {
		return attackSkill;
	}

	@Override
	public int dr() {
		return dr;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);

		bundle.put(DEFENSE_SKILL, defenseSkill);
		bundle.put(EXP, exp);
		bundle.put(MAX_LVL, maxLvl);
		bundle.put(DMG_MIN, dmgMin);
		bundle.put(DMG_MAX, dmgMax);
		bundle.put(ATTACK_SKILL, attackSkill);
		bundle.put(DR, dr);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

		defenseSkill = bundle.optInt(DEFENSE_SKILL, defenseSkill);
		exp = bundle.optInt(EXP, exp);
		maxLvl = bundle.optInt(MAX_LVL, maxLvl);
		dmgMin = bundle.optInt(DMG_MIN, dmgMin);
		dmgMax = bundle.optInt(DMG_MAX, dmgMax);
		attackSkill = bundle.optInt(ATTACK_SKILL, attackSkill);
		dr = bundle.optInt(DR, dr);
	}

	@Override
	public void fromJson(JSONObject mobDesc) throws JSONException, InstantiationException, IllegalAccessException {
		super.fromJson(mobDesc);

		defenseSkill = mobDesc.optInt(DEFENSE_SKILL, defenseSkill);
		exp = mobDesc.optInt(EXP, exp);
		maxLvl = mobDesc.optInt(MAX_LVL, maxLvl);
		dmgMin = mobDesc.optInt(DMG_MIN, dmgMin);
		dmgMax = mobDesc.optInt(DMG_MAX, dmgMax);
		attackSkill = mobDesc.optInt(ATTACK_SKILL, attackSkill);
		dr = mobDesc.optInt(DR, dr);
	}
}
