package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.mobs.guts.BurningFist;
import com.nyrds.pixeldungeon.mobs.guts.Larva;
import com.nyrds.pixeldungeon.mobs.guts.RottingFist;
import com.nyrds.pixeldungeon.mobs.guts.YogsBrain;
import com.nyrds.pixeldungeon.mobs.guts.YogsEye;
import com.nyrds.pixeldungeon.mobs.guts.YogsHeart;
import com.nyrds.pixeldungeon.mobs.guts.YogsTeeth;
import com.nyrds.pixeldungeon.mobs.icecaves.IceGuardian;
import com.nyrds.pixeldungeon.mobs.icecaves.IceGuardianCore;
import com.nyrds.pixeldungeon.mobs.necropolis.Lich;
import com.nyrds.pixeldungeon.mobs.necropolis.RunicSkull;
import com.nyrds.pixeldungeon.mobs.npc.AzuterronNPC;
import com.nyrds.pixeldungeon.mobs.npc.BellaNPC;
import com.nyrds.pixeldungeon.mobs.npc.CagedKobold;
import com.nyrds.pixeldungeon.mobs.npc.FortuneTellerNPC;
import com.nyrds.pixeldungeon.mobs.npc.HealerNPC;
import com.nyrds.pixeldungeon.mobs.npc.InquirerNPC;
import com.nyrds.pixeldungeon.mobs.npc.LibrarianNPC;
import com.nyrds.pixeldungeon.mobs.npc.NecromancerNPC;
import com.nyrds.pixeldungeon.mobs.npc.PlagueDoctorNPC;
import com.nyrds.pixeldungeon.mobs.npc.ScarecrowNPC;
import com.nyrds.pixeldungeon.mobs.npc.ServiceManNPC;
import com.nyrds.pixeldungeon.mobs.npc.SociologistNPC;
import com.nyrds.pixeldungeon.mobs.npc.TownGuardNPC;
import com.nyrds.pixeldungeon.mobs.npc.TownShopkeeper;
import com.nyrds.pixeldungeon.mobs.npc.TownsfolkMovieNPC;
import com.nyrds.pixeldungeon.mobs.npc.TownsfolkNPC;
import com.nyrds.pixeldungeon.mobs.npc.TownsfolkSilentNPC;
import com.nyrds.pixeldungeon.mobs.spiders.SpiderQueen;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModdingMode;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Challenges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.mobs.DM300;
import com.watabou.pixeldungeon.actors.mobs.Eye;
import com.watabou.pixeldungeon.actors.mobs.Goo;
import com.watabou.pixeldungeon.actors.mobs.King;
import com.watabou.pixeldungeon.actors.mobs.King.Undead;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Monk;
import com.watabou.pixeldungeon.actors.mobs.Senior;
import com.watabou.pixeldungeon.actors.mobs.Tengu;
import com.watabou.pixeldungeon.actors.mobs.npcs.Blacksmith;
import com.watabou.pixeldungeon.actors.mobs.npcs.Ghost;
import com.watabou.pixeldungeon.actors.mobs.npcs.Hedgehog;
import com.watabou.pixeldungeon.actors.mobs.npcs.Imp;
import com.watabou.pixeldungeon.actors.mobs.npcs.ImpShopkeeper;
import com.watabou.pixeldungeon.actors.mobs.npcs.MirrorImage;
import com.watabou.pixeldungeon.actors.mobs.npcs.RatKing;
import com.watabou.pixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.watabou.pixeldungeon.actors.mobs.npcs.WandMaker;
import com.watabou.pixeldungeon.items.wands.WandOfFlock;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;




@LuaInterface
public class MobFactory {
	public static final String RAT = "Rat";
	public static final String BAT = "Bat";
	public static final String DEEP_SNAIL = "DeepSnail";
	public static final String SHAMAN_ELDER = "ShamanElder";
	public static final String PSEUDO_RAT = "PseudoRat";
	public static final String SPIRIT_OF_PAIN = "SpiritOfPain";
	public static final String SNAIL = "Snail";
	public static final String SHEEP = "Sheep";
	public static final String ELEMENTAL = "Elemental";
	public static final String STATUE = "Statue";
	public static final String ARMORED_STATUE = "ArmoredStatue";
	public static final String GOLDEN_STATUE = "GoldenStatue";
	public static final String MIRROR_IMAGE = "MirrorImage";
	public static final String WRAITH = "Wraith";
	public static final String WARLOCK = "Warlock";
	public static final String SPIDER_SERVANT = "SpiderServant";
	public static final String SPIDER_GUARD = "SpiderGuard";
	public static final String SPIDER_MIND_AMBER = "SpiderMindAmber";
	public static final String SPIDER_EGG = "SpiderEgg";
	public static final String SPIDER_NEST = "SpiderNest";
	public static final String JAR_OF_SOULS = "JarOfSouls";
	public static final String SKELETON = "Skeleton";
	public static final String FETID_RAT = "FetidRat";
	public static final String SUSPICIOUS_RAT = "SuspiciousRat";
	public static final String GHOST = "Ghost";
	public static final String UNDEAD = "Undead";
	public static final String SHOPKEEPER = "Shopkeeper";
	public static final String TOWN_SHOPKEEPER = "TownShopkeeper";
	public static final String MIMIC = "Mimic";
	public static final String MIMIC_PIE = "MimicPie";
	public static final String MIMIC_AMULET = "MimicAmulet";
	public static final String ALBINO = "Albino";
	public static final String SHIELDED = "Shielded";
	public static final String SHADOW = "Shadow";
	public static final String DREAD_KNIGHT = "DreadKnight";
	public static final String TREACHEROUS_SPIRIT = "TreacherousSpirit";
	public static final String GNOLL = "Gnoll";
	public static final String CRAB = "Crab";
	public static final String GOLEM = "Golem";
	public static final String MONK = "Monk";
	public static final String BANDIT = "Bandit";
	public static final String ACIDIC = "Acidic";
	public static final String SCORPIO = "Scorpio";
	public static final String FIRE_ELEMENTAL = "FireElemental";
	public static final String AIR_ELEMENTAL = "AirElemental";
	public static final String WATER_ELEMENTAL = "WaterElemental";
	public static final String EARTH_ELEMENTAL = "EarthElemental";
	public static final String PIRANHA = "Piranha";

	static private Map<String, Class<? extends Mob>> mMobsList;

	static {
		initMobsMap();

		for(String mobFile: ModdingMode.listResources("mobsDesc", (dir, name) -> name.endsWith(".json"))) {
			String mobKind = mobFile.replace(".json", Utils.EMPTY_STRING);
			if(!mMobsList.containsKey(mobKind)) {	// do not shadow built-in classes by partial json definitions
				mMobsList.put(mobKind, CustomMob.class);
			}
		}
	}

	private static void registerMobClass(Class<? extends Mob> mobClass) {
		mMobsList.put(mobClass.getSimpleName(), mobClass);
	}
	
	private static void initMobsMap() {

		mMobsList = new HashMap<>();
		registerMobClass(RatKing.class);
		registerMobClass(Goo.class);

		registerMobClass(Tengu.class);

		registerMobClass(SpiderQueen.class);

		registerMobClass(Monk.class);
		registerMobClass(DM300.class);
		registerMobClass(King.class);
		registerMobClass(Undead.class);
		registerMobClass(Senior.class);

		registerMobClass(Eye.class);
		registerMobClass(Larva.class);
		registerMobClass(BurningFist.class);
		registerMobClass(RottingFist.class);

		// Statue/ArmoredStatue/GoldenStatue kinds are data now
		// (mobsDesc/*.json) - classes deleted, kinds must never resolve to java.
		registerMobClass(YogsBrain.class);
		registerMobClass(YogsEye.class);
		registerMobClass(YogsHeart.class);
		registerMobClass(YogsTeeth.class);
		registerMobClass(ShadowLord.class);

		registerMobClass(Lich.class);
		registerMobClass(RunicSkull.class);

		registerMobClass(Crystal.class);


		registerMobClass(IceGuardian.class);
		registerMobClass(IceGuardianCore.class);

		registerMobClass(Hedgehog.class);
		registerMobClass(HealerNPC.class);
		registerMobClass(TownGuardNPC.class);
		registerMobClass(ServiceManNPC.class);
		registerMobClass(TownsfolkNPC.class);
		registerMobClass(PlagueDoctorNPC.class);
		registerMobClass(TownsfolkMovieNPC.class);
		registerMobClass(TownsfolkSilentNPC.class);
		registerMobClass(BellaNPC.class);
		registerMobClass(LibrarianNPC.class);
		registerMobClass(FortuneTellerNPC.class);
		registerMobClass(CagedKobold.class);
		registerMobClass(WandMaker.class);
		registerMobClass(Blacksmith.class);
		registerMobClass(ScarecrowNPC.class);
		registerMobClass(NecromancerNPC.class);
		registerMobClass(Imp.class);
		registerMobClass(AzuterronNPC.class);

		registerMobClass(Deathling.class);

		registerMobClass(Ghost.class);
		registerMobClass(SociologistNPC.class);
		registerMobClass(InquirerNPC.class);
		registerMobClass(Shopkeeper.class);
		registerMobClass(ImpShopkeeper.class);
		registerMobClass(TownShopkeeper.class);
		registerMobClass(MirrorImage.class);

		mMobsList.put(SHEEP, WandOfFlock.Sheep.class);
		//old mods compatibility
	}

	// legacy-mod kind aliases: kinds that existed only as compatibility shims
	private static String resolveAlias(String kind) {
		if (ELEMENTAL.equals(kind)) {
			return FIRE_ELEMENTAL;
		}
		return kind;
	}

	@Contract(pure = true)
	public static boolean hasMob(String mobClass) {
		mobClass = resolveAlias(mobClass);

		if(Dungeon.isChallenged(Challenges.NO_ARMOR) && mobClass.equals(ARMORED_STATUE)) {
			return false;
		}

		if(Dungeon.isChallenged(Challenges.NO_WEAPON) && mobClass.equals(STATUE)) {
			return false;
		}

		return mMobsList.containsKey(mobClass);
	}

	@NotNull
	public static Mob mobByName(String selectedMobClass) {

		selectedMobClass = resolveAlias(selectedMobClass);

		try {
			Class<? extends Mob> mobClass = mMobsList.get(selectedMobClass);

			if (mobClass!=null && mobClass != CustomMob.class) {
				return mobClass.newInstance();
			} else {
				return new CustomMob(selectedMobClass);
			}
		} catch (Exception e) {
			throw new TrackedRuntimeException(selectedMobClass,e);
		}
	}

	/**
	 * Gated resolver for save-restore: null unless the kind is registered or
	 * has a data def. Never constructs a def-less CustomMob from a bare kind.
	 */
	@Nullable
	public static Mob tryByName(String kind) {
		if (!hasMob(kind)) {
			return null;
		}
		return mobByName(kind);
	}

	@NotNull
	public static Mob createMob(String selectedMobClass, String jsonDesc) throws JSONException, IllegalAccessException, InstantiationException {
		var mob = mobByName(selectedMobClass);
		mob.fromJson(JsonHelper.readJsonFromString(jsonDesc));
		return mob;
	}

	public static Image avatar(String kind)  {
		return MobFactory.mobByName(kind).newSprite().avatar();
	}

	public static List<Mob> allMobs() {
		List<Mob> mobs = new ArrayList<>();

		for(String mobClass:mMobsList.keySet()) {
			GLog.debug("Spawning: %s",mobClass);
			mobs.add(mobByName(mobClass));
		}

		return mobs;
	}

	public static Set<String> getAllMobNames() {
		return new HashSet<>(mMobsList.keySet());
	}
}
