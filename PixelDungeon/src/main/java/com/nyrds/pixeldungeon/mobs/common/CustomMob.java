package com.nyrds.pixeldungeon.mobs.common;

import com.watabou.noosa.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.json.JSONObject;

/**
 * Created by mike on 11.04.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class CustomMob extends MultiKindMob {

	private final String MOB_CLASS = "mobClass";

	private int dmgMin, dmgMax;
	private int attackSkill;
	private int dr;

	private float speed = 1, attackDelay = 1;

	private String mobClass = "Unknown";

	//For restoreFromBundle
	public CustomMob() {}

	public CustomMob(String mobClass) {
		this.mobClass = mobClass;
		fillMobStats();
	}

	@Override
	public float speed() {
		return speed;
	}

	@Override
	protected float attackDelay() {
		return attackDelay;
	}

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

		bundle.put(MOB_CLASS,mobClass);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		mobClass = bundle.getString(MOB_CLASS);
		fillMobStats();

		super.restoreFromBundle(bundle);
	}

	@Override
	protected void readCharData() {
		super.readCharData();
	}

	private void fillMobStats() {
		ensureActualClassDef();

		JSONObject classDesc = defMap.get(getMobClassName());

		defenseSkill = classDesc.optInt("defenseSkill", defenseSkill);
		attackSkill  = classDesc.optInt("attackSkill", attackSkill);

		exp    = classDesc.optInt("exp", exp);
		maxLvl = classDesc.optInt("maxLvl", maxLvl);
		dmgMin = classDesc.optInt("dmgMin", dmgMin);
		dmgMax = classDesc.optInt("dmgMax", dmgMax);

		dr = classDesc.optInt("dr", dr);

		speed       = (float) classDesc.optDouble("speed", speed);
		attackDelay = (float) classDesc.optDouble("attackDelay", attackDelay);

		name            = StringsManager.maybeId(classDesc.optString("name",name));
		name_objective  = StringsManager.maybeId(classDesc.optString("name_objective",name));
		description     = StringsManager.maybeId(classDesc.optString("description",description));
		gender          = Utils.genderFromString(classDesc.optString("gender",""));

		spriteClass = classDesc.optString("spriteDesc","spritesDesc/Rat.json");

		hp(ht(classDesc.optInt("ht",1)));
	}

	@Override
	public String getMobClassName() {
		return mobClass;
	}

}
