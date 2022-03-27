package com.nyrds.pixeldungeon.mobs.common;

import androidx.annotation.Keep;

import com.nyrds.Packable;
import com.nyrds.lua.LuaEngine;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.mobs.Fraction;
import com.watabou.pixeldungeon.actors.mobs.WalkingType;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.luaj.vm2.LuaValue;

import java.util.ArrayList;

import lombok.SneakyThrows;

/**
 * Created by mike on 11.04.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class CustomMob extends MultiKindMob implements IZapper {

	private float attackDelay = 1;

	@Packable
	private String mobClass = "Unknown";

	private boolean canBePet = false;

	private int     attackRange     = 1;

	private boolean friendly;
	private boolean immortal = false;

	//For restoreFromBundle
	@Keep
	public CustomMob() {
	}

	public CustomMob(String mobClass) {
		this.mobClass = mobClass;
		fillMobStats(false);
		script.run("fillStats");
	}

	@Override
	protected float _attackDelay() {
		return attackDelay;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(dmgMin, dmgMax);
	}

	@Override
	public int dr() {
		return dr;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);

		bundle.put(LuaEngine.LUA_DATA, script.run("saveData").checkjstring());
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		fillMobStats(true);

		super.restoreFromBundle(bundle);

		String luaData = bundle.optString(LuaEngine.LUA_DATA,null);
		if(luaData!=null) {
			script.run("loadData",luaData);
		}
		script.run("fillStats");
	}

	@Override
	public void beckon(int cell) {
		if(!friendly && movable) {
			super.beckon(cell);
		}
	}

	@Override
	public String getEntityKind() {
		return mobClass;
	}

	@Override
	public boolean canBePet() {
		return canBePet;
	}

	@Override
    public boolean canAttack(@NotNull Char enemy) {

		if(friendly(enemy)) {
			return false;
		}

		int enemyPos = enemy.getPos();
		int distance = level().distance(getPos(), enemyPos);

        return distance <= attackRange && Ballistica.cast(getPos(), enemyPos, false, true) == enemyPos;
    }

	@Override
	public boolean friendly(@NotNull Char chr) {
		return friendly || super.friendly(chr);
	}


	public ArrayList<String> actions(Char hero) {
		ArrayList<String> actions = CharUtils.actions(this, hero);

		LuaValue ret = script.run("actionsList", hero);
		LuaEngine.forEach(ret, (key,val)->actions.add(val.tojstring()));

		return actions;
	}

	@Override
	public void damage(int dmg, @NotNull NamedEntityKind src) {
		if(immortal) {
			return;
		}

		super.damage(dmg, src);
	}

	@Override
	public String getDescription() {
		if(!Util.isDebug()) {
			return super.getDescription();
		}
		return super.getDescription() + "\n"
				+ Utils.format("kind: %d", kind);
	}

	@SneakyThrows
	private void fillMobStats(boolean restoring) {
		JSONObject classDesc = getClassDef();

		baseDefenseSkill = classDesc.optInt("defenseSkill", baseDefenseSkill);
		baseAttackSkill = classDesc.optInt("attackSkill", attackSkill);

		exp = classDesc.optInt("exp", exp);
		maxLvl = classDesc.optInt("maxLvl", maxLvl);
		dmgMin = classDesc.optInt("dmgMin", dmgMin);
		dmgMax = classDesc.optInt("dmgMax", dmgMax);

		dr = classDesc.optInt("dr", dr);

		baseSpeed = (float) classDesc.optDouble("baseSpeed", baseSpeed);
		attackDelay = (float) classDesc.optDouble("attackDelay", attackDelay);

		name = StringsManager.maybeId(classDesc.optString("name", mobClass+"_Name"));
		name_objective = StringsManager.maybeId(classDesc.optString("name_objective", mobClass+"_Name_Objective"));
		description = StringsManager.maybeId(classDesc.optString("description", mobClass+"_Desc"));
		gender = Utils.genderFromString(StringsManager.maybeId(classDesc.optString("gender", mobClass+"_Gender")));

		spriteClass = classDesc.optString("spriteDesc", "spritesDesc/Rat.json");

		flying = classDesc.optBoolean("flying", flying);

		setViewDistance(classDesc.optInt("viewDistance", getViewDistance()));

		walkingType = Enum.valueOf(WalkingType.class, classDesc.optString("walkingType","NORMAL"));

        defenceVerb = StringsManager.maybeId(classDesc.optString("defenceVerb", StringsManager.getVars(R.array.Char_StaDodged)[gender]));

		canBePet = classDesc.optBoolean("canBePet",canBePet);

		attackRange = classDesc.optInt("attackRange",attackRange);
		isBoss = classDesc.optBoolean("isBoss",isBoss);

		String scriptFile = classDesc.optString("scriptFile","");
		if(!scriptFile.isEmpty()) {
			script = new LuaScript(scriptFile, this);
			script.asInstance();
		}

		friendly = classDesc.optBoolean("friendly",friendly);
		movable = classDesc.optBoolean("movable",movable);
		immortal = classDesc.optBoolean("immortal",immortal);

		kind = classDesc.optInt("var", kind);

		JsonHelper.readStringSet(classDesc, Char.IMMUNITIES, immunities);
		JsonHelper.readStringSet(classDesc, Char.RESISTANCES, resistances);

		if(!restoring) {
			setFraction(Enum.valueOf(Fraction.class, classDesc.optString("fraction","DUNGEON")));
			hp(ht(classDesc.optInt("ht", 1)));
			fromJson(classDesc);
		}
	}

}
