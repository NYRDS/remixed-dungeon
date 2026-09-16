package com.nyrds.pixeldungeon.mobs.common;

import androidx.annotation.Keep;
import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.platform.audio.MusicManager;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.mobs.Fraction;
import com.watabou.pixeldungeon.actors.mobs.WalkingType;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.HeroSpriteDef;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.luaj.vm2.LuaValue;

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

	// boss battle track, played while the boss is Hunting (java Boss parity)
	@Nullable
	private String battleMusic = "";

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
		LuaValue scripted = getScript().run("onDr");
		if (scripted.isnumber()) {
			return scripted.toint();
		}
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
			// scripted NPCs (RatKing) may punch through the blanket immunity
			if (!getScript().runOptional("onAllowBuff", false, buff)) {
				return false;
			}
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

		if (isBoss && !battleMusic.isEmpty() && getState() instanceof Hunting) {
			MusicManager.INSTANCE.play(battleMusic, true);
		}
		super.act();
	}

	// java Boss die-flow parity: level music back, banner, open the sealed stair
	@Override
	public void die(@NotNull NamedEntityKind cause) {
		if (isBoss) {
			GameScene.playLevelMusic();
			GameScene.bossSlain();
			level().unseal();
		}
		super.die(cause);
	}

	// boss intro yells etc; sprite alert already played by the base
	@Override
	public void notice() {
		super.notice();
		getScript().runOptionalNoRet("onNotice");
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

		// script replaces the range+LOS check entirely (pumped Goo reach, ray attacks)
		LuaValue scripted = getScript().run("onCanAttack", enemy);
		if (scripted.isboolean()) {
			return scripted.toboolean();
		}

		int enemyPos = enemy.getPos();
		int distance = level().distance(getPos(), enemyPos);

        return distance <= attackRange && Ballistica.cast(getPos(), enemyPos, false, true) == enemyPos;
    }

	// script takes the attack entirely (Goo pump: spends and poses itself)
	@Override
	public void doAttack(Char enemy) {
		if (getScript().runOptional("onDoAttack", Boolean.FALSE, enemy)) {
			return;
		}
		super.doAttack(enemy);
	}

	@Override
	public int attackSkill(Char target) {
		LuaValue scripted = getScript().run("onAttackSkill", target);
		if (scripted.isnumber()) {
			return scripted.toint();
		}
		return super.attackSkill(target);
	}

	@Override
	public int damageRoll() {
		LuaValue scripted = getScript().run("onDamageRoll");
		if (scripted.isnumber()) {
			return scripted.toint();
		}
		return super.damageRoll();
	}

	// dynamic stats of owner-scaled summons (Deathling) live in the script
	@Override
	public int defenseSkill(@NotNull Char enemy) {
		LuaValue scripted = getScript().run("onDefenseSkill", enemy);
		if (scripted.isnumber()) {
			return scripted.toint();
		}
		return super.defenseSkill(enemy);
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

		// scripted mobs may consume the hit (RatKing anger gate)
		if (getScript().runOptional("onBlockDamage", false, dmg, src)) {
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
		if (isBoss) {
			// java Boss ctor semantics: uncapturable, death/psionic-blast proof
			canBePet = false;
			addResistance(Death.class);
			addResistance(ScrollOfPsionicBlast.class);
		}

		battleMusic = classDesc.optString("battleMusic", "");
		if (!battleMusic.isEmpty() && !ModdingMode.isSoundExists(battleMusic)) {
			battleMusic = classDesc.optString("battleMusicFallback", "");
		}

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

			if (isBoss) {
				// bosses carry the SkeletonKey that drops with their gear
				collect(new SkeletonKey());
			}
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

		// java Boss fixup parity: a save predating the key must not brick the stair
		if (isBoss && getBelongings().getItem(SkeletonKey.class) == null) {
			collect(new SkeletonKey());
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

	// Mob stores the flag as a field; the isBoss() method lives on Char only
	@LuaInterface
	@Override
	public boolean isBoss() {
		return isBoss;
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
