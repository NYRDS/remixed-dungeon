package com.nyrds.pixeldungeon.mobs.common;

import androidx.annotation.Keep;

import com.nyrds.Packable;
import com.nyrds.lua.LuaEngine;
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

	private boolean friendly;
	private boolean immortal = false;

	//For restoreFromBundle
	@Keep
	public CustomMob() {
		super();
	}

	public CustomMob(String mobClass) {
		super();
		this.mobClass = mobClass;
		fillMobStats(false);
	}

	@Override
	protected float _attackDelay() {
		return attackDelay;
	}

	@Override
	public int dr() {
		return dr;
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

	@Override
	public void damage(int dmg, @NotNull NamedEntityKind src) {
		if(immortal) {
			return;
		}

		super.damage(dmg, src);
	}


	@SneakyThrows
	@Override
	protected void fillMobStats(boolean restoring) {
		JSONObject classDesc = getClassDef();
		if(! classDesc.keys().hasNext()) {
			return;
		}

		baseDefenseSkill = classDesc.optInt("defenseSkill", baseDefenseSkill);
		baseAttackSkill = classDesc.optInt("attackSkill", attackSkill);

		exp = classDesc.optInt("exp", exp);
		maxLvl = classDesc.optInt("maxLvl", maxLvl);
		dmgMin = classDesc.optInt("dmgMin", dmgMin);
		dmgMax = classDesc.optInt("dmgMax", dmgMax);

		dr = classDesc.optInt("dr", dr);

		baseSpeed = (float) classDesc.optDouble("baseSpeed", baseSpeed);
		attackDelay = (float) classDesc.optDouble("attackDelay", attackDelay);

		spriteClass = classDesc.optString("spriteDesc", "spritesDesc/Rat.json");

		flying = classDesc.optBoolean("flying", flying);

		setViewDistance(classDesc.optInt("viewDistance", getViewDistance()));

		walkingType = Enum.valueOf(WalkingType.class, classDesc.optString("walkingType","NORMAL"));

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
