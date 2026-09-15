package com.nyrds.pixeldungeon.mobs.common;

import androidx.annotation.Keep;
import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.util.JsonHelper;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.mobs.Fraction;
import com.watabou.pixeldungeon.actors.mobs.WalkingType;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.HeroSpriteDef;
import com.watabou.pixeldungeon.utils.GLog;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

/**
 * Created by mike on 11.04.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class CustomMob extends MultiKindMob implements IZapper {

	private float attackDelay = 1;
	private int spriteLayer = 0;

	@Packable
	public String mobClass = "Unknown";

	private boolean canBePet = false;

	private boolean friendly;
	private boolean immortal = false;
	private boolean humanoid = false;

	// statue-style sprite: hero-layers + currently equipped item
	private boolean heroSprite = false;

	// survive Level.reset (statues stay, ordinary mobs are removed)
	private boolean persistOnReset = false;

	// NPC profile (npc: true in the mob def): static town-folk behavior —
	// steps off objects/stairs in act, never beckoned, immune to buffs, not
	// petable. Replaces the deleted java NPC base-class behavior.
	private boolean npc = false;

	//For restoreFromBundle
	@Keep
	public CustomMob() {
		super();
	}

	private boolean hasBodyParts = true;

	public CustomMob(String mobClass) {
		super();
		this.mobClass = mobClass;
		fillMobStats(false);
		getScript().run("fillStats");
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
		if (npc) {
			return;
		}
		if(!friendly && movable) {
			super.beckon(cell);
		}
	}

	@Override
	public boolean add(Buff buff) {
		if (npc) {
			return false;
		}
		return super.add(buff);
	}

	// the old java NPC.act preamble: keep the cell clean, never park on an
	// object tile or stairs, face the hero
	@Override
	public void act() {
		if (npc) {
			int pos = getPos();

			ItemUtils.throwItemAway(pos);

			LevelObject levelObject = level().getTopLevelObject(pos);
			if (levelObject != null) {
				int newPos = level().getEmptyNonStairsCellNextTo(pos);
				if (level().cellValid(newPos) && newPos != pos) {
					WandOfBlink.appear(this, newPos);
				}
			}

			if (Dungeon.hero != null) {
				getSprite().turnTo(pos, Dungeon.hero.getPos());
			}
		}
		super.act();
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
	public float speed() {
		float base = super.speed();
		// terrain-conditional speeds (water/earth elementals) live in the script
		return (float) getScript().run("onSpeed", base).optdouble(base);
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
			GLog.debug("No mob def: " + mobClass);
			return;
		}

		baseDefenseSkill = classDesc.optInt("defenseSkill", baseDefenseSkill);
		baseAttackSkill = classDesc.optInt("attackSkill", attackSkill);

		expForKill = classDesc.optInt("exp", expForKill);
		maxLvl = classDesc.optInt("maxLvl", maxLvl);
		dmgMin = classDesc.optInt("dmgMin", dmgMin);
		dmgMax = classDesc.optInt("dmgMax", dmgMax);

		dr = classDesc.optInt("dr", dr);

		baseStr = classDesc.optInt("str", baseStr);

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
		pacified = classDesc.optBoolean("pacified",pacified);

		spriteLayer = classDesc.optInt("spriteLayer",spriteLayer);

		humanoid = classDesc.optBoolean("isHumanoid", humanoid);

		heroSprite = classDesc.optBoolean("heroSprite", heroSprite);

		persistOnReset = classDesc.optBoolean("persistOnReset", persistOnReset);

		npc = classDesc.optBoolean("npc", npc);
		if (npc) {
			canBePet = false;
		}

		kind = classDesc.optInt("var", kind);
		carcassChance = (float) classDesc.optDouble("carcassChance", carcassChance);
		hasBodyParts = classDesc.optBoolean("hasBodyParts", hasBodyParts);

		JsonHelper.readStringSet(classDesc, Char.IMMUNITIES, immunities);
		JsonHelper.readStringSet(classDesc, Char.RESISTANCES, resistances);

		if(!restoring) {
			setFraction(Enum.valueOf(Fraction.class, classDesc.optString("fraction","DUNGEON")));
			hp(ht(classDesc.optInt("ht", 1)));
			fromJson(classDesc);
		}
	}

	@Override
	public int getSpriteLayer() {
		return spriteLayer;
	}

	@LuaInterface
	@Override
	public boolean isHumanoid() {
		return humanoid;
	}

	@LuaInterface
	@Override
	public boolean hasBodyParts() {
		return hasBodyParts;
	}

	@Override
	public CharSprite newSprite() {
		if (heroSprite) {
			var item = getItemFromSlot(Belongings.Slot.WEAPON);
			if (!item.valid()) {
				item = getItemFromSlot(Belongings.Slot.ARMOR);
			}
			return HeroSpriteDef.createHeroSpriteDef(item);
		}
		return super.newSprite();
	}

	@Override
	public boolean reset() {
		return persistOnReset;
	}
}
