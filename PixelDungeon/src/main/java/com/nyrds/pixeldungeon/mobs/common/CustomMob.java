package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
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

	private float attackDelay = 1;

	private String mobClass = "Unknown";

	//For restoreFromBundle
	public CustomMob() {
	}

	public CustomMob(String mobClass) {
		this.mobClass = mobClass;
		fillMobStats();
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

		bundle.put(MOB_CLASS, mobClass);
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

		try {
			JSONObject classDesc = defMap.get(getMobClassName());

			defenseSkill = classDesc.optInt("defenseSkill", defenseSkill);
			attackSkill = classDesc.optInt("attackSkill", attackSkill);

			exp = classDesc.optInt("exp", exp);
			maxLvl = classDesc.optInt("maxLvl", maxLvl);
			dmgMin = classDesc.optInt("dmgMin", dmgMin);
			dmgMax = classDesc.optInt("dmgMax", dmgMax);

			dr = classDesc.optInt("dr", dr);

			baseSpeed = (float) classDesc.optDouble("baseSpeed", baseSpeed);
			attackDelay = (float) classDesc.optDouble("attackDelay", attackDelay);

			name = StringsManager.maybeId(classDesc.optString("name", name));
			name_objective = StringsManager.maybeId(classDesc.optString("name_objective", name));
			description = StringsManager.maybeId(classDesc.optString("description", description));
			gender = Utils.genderFromString(classDesc.optString("gender", ""));

			spriteClass = classDesc.optString("spriteDesc", "spritesDesc/Rat.json");

			flying = classDesc.optBoolean("flying", flying);

			lootChance = (float) classDesc.optDouble("lootChance", lootChance);

			if (classDesc.has("loot")) {
				loot = ItemFactory.createItemFromDesc(classDesc.getJSONObject("loot"));
			}

			viewDistance = classDesc.optInt("viewDistance",viewDistance);

			hp(ht(classDesc.optInt("ht", 1)));

		} catch (Exception e) {
			throw new TrackedRuntimeException(e);
		}
	}

	@Override
	public String getMobClassName() {
		return mobClass;
	}

}
