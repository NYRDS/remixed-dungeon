package com.nyrds.retrodungeon.mobs.common;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.retrodungeon.mobs.elementals.AirElemental;
import com.nyrds.retrodungeon.mobs.elementals.EarthElemental;
import com.nyrds.retrodungeon.mobs.elementals.IceElemental;
import com.nyrds.retrodungeon.mobs.elementals.WaterElemental;
import com.nyrds.retrodungeon.mobs.guts.MimicAmulet;
import com.nyrds.retrodungeon.mobs.guts.Nightmare;
import com.nyrds.retrodungeon.mobs.guts.PseudoRat;
import com.nyrds.retrodungeon.mobs.guts.SuspiciousRat;
import com.nyrds.retrodungeon.mobs.guts.Worm;
import com.nyrds.retrodungeon.mobs.guts.YogsBrain;
import com.nyrds.retrodungeon.mobs.guts.YogsEye;
import com.nyrds.retrodungeon.mobs.guts.YogsHeart;
import com.nyrds.retrodungeon.mobs.guts.YogsTeeth;
import com.nyrds.retrodungeon.mobs.guts.ZombieGnoll;
import com.nyrds.retrodungeon.mobs.icecaves.ColdSpirit;
import com.nyrds.retrodungeon.mobs.icecaves.IceGuardian;
import com.nyrds.retrodungeon.mobs.icecaves.IceGuardianCore;
import com.nyrds.retrodungeon.mobs.icecaves.Kobold;
import com.nyrds.retrodungeon.mobs.icecaves.KoboldIcemancer;
import com.nyrds.retrodungeon.mobs.necropolis.DeathKnight;
import com.nyrds.retrodungeon.mobs.necropolis.DreadKnight;
import com.nyrds.retrodungeon.mobs.necropolis.EnslavedSoul;
import com.nyrds.retrodungeon.mobs.necropolis.ExplodingSkull;
import com.nyrds.retrodungeon.mobs.necropolis.JarOfSouls;
import com.nyrds.retrodungeon.mobs.necropolis.Lich;
import com.nyrds.retrodungeon.mobs.necropolis.RunicSkull;
import com.nyrds.retrodungeon.mobs.necropolis.Zombie;
import com.nyrds.retrodungeon.mobs.npc.ArtificerNPC;
import com.nyrds.retrodungeon.mobs.npc.BellaNPC;
import com.nyrds.retrodungeon.mobs.npc.CagedKobold;
import com.nyrds.retrodungeon.mobs.npc.FortuneTellerNPC;
import com.nyrds.retrodungeon.mobs.npc.HealerNPC;
import com.nyrds.retrodungeon.mobs.npc.LibrarianNPC;
import com.nyrds.retrodungeon.mobs.npc.PlagueDoctorNPC;
import com.nyrds.retrodungeon.mobs.npc.ServiceManNPC;
import com.nyrds.retrodungeon.mobs.npc.TownGuardNPC;
import com.nyrds.retrodungeon.mobs.npc.TownsfolkMovieNPC;
import com.nyrds.retrodungeon.mobs.npc.TownsfolkNPC;
import com.nyrds.retrodungeon.mobs.npc.TownsfolkSilentNPC;
import com.nyrds.retrodungeon.mobs.spiders.SpiderEgg;
import com.nyrds.retrodungeon.mobs.spiders.SpiderExploding;
import com.nyrds.retrodungeon.mobs.spiders.SpiderMind;
import com.nyrds.retrodungeon.mobs.spiders.SpiderNest;
import com.nyrds.retrodungeon.mobs.spiders.SpiderQueen;
import com.nyrds.retrodungeon.mobs.spiders.SpiderServant;
import com.watabou.pixeldungeon.actors.mobs.Acidic;
import com.watabou.pixeldungeon.actors.mobs.Albino;
import com.watabou.pixeldungeon.actors.mobs.Bandit;
import com.watabou.pixeldungeon.actors.mobs.Bat;
import com.watabou.pixeldungeon.actors.mobs.Brute;
import com.watabou.pixeldungeon.actors.mobs.Crab;
import com.watabou.pixeldungeon.actors.mobs.DM300;
import com.watabou.pixeldungeon.actors.mobs.Elemental;
import com.watabou.pixeldungeon.actors.mobs.Eye;
import com.watabou.pixeldungeon.actors.mobs.Gnoll;
import com.watabou.pixeldungeon.actors.mobs.Golem;
import com.watabou.pixeldungeon.actors.mobs.Goo;
import com.watabou.pixeldungeon.actors.mobs.King;
import com.watabou.pixeldungeon.actors.mobs.King.Undead;
import com.watabou.pixeldungeon.actors.mobs.Mimic;
import com.watabou.pixeldungeon.actors.mobs.MimicPie;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Monk;
import com.watabou.pixeldungeon.actors.mobs.Piranha;
import com.watabou.pixeldungeon.actors.mobs.Rat;
import com.watabou.pixeldungeon.actors.mobs.Scorpio;
import com.watabou.pixeldungeon.actors.mobs.Senior;
import com.watabou.pixeldungeon.actors.mobs.Shadow;
import com.watabou.pixeldungeon.actors.mobs.Shaman;
import com.watabou.pixeldungeon.actors.mobs.Shielded;
import com.watabou.pixeldungeon.actors.mobs.Skeleton;
import com.watabou.pixeldungeon.actors.mobs.Spinner;
import com.watabou.pixeldungeon.actors.mobs.Statue;
import com.watabou.pixeldungeon.actors.mobs.Succubus;
import com.watabou.pixeldungeon.actors.mobs.Swarm;
import com.watabou.pixeldungeon.actors.mobs.Tengu;
import com.watabou.pixeldungeon.actors.mobs.Thief;
import com.watabou.pixeldungeon.actors.mobs.Warlock;
import com.watabou.pixeldungeon.actors.mobs.Wraith;
import com.watabou.pixeldungeon.actors.mobs.Yog;
import com.watabou.pixeldungeon.actors.mobs.Yog.BurningFist;
import com.watabou.pixeldungeon.actors.mobs.Yog.Larva;
import com.watabou.pixeldungeon.actors.mobs.Yog.RottingFist;
import com.watabou.pixeldungeon.actors.mobs.npcs.Ghost.FetidRat;
import com.watabou.pixeldungeon.actors.mobs.npcs.Hedgehog;
import com.watabou.pixeldungeon.actors.mobs.npcs.RatKing;
import com.watabou.utils.Random;

import java.util.HashMap;
import java.util.Map;


public class MobFactory {
	static private Map<String, Class<? extends Mob>> mMobsList;

	static {
		initMobsMap();
	}

	private static void registerMobClass(Class<? extends Mob> mobClass) {
		mMobsList.put(mobClass.getSimpleName(), mobClass);
	}
	
	private static void initMobsMap() {

		mMobsList = new HashMap<>();
		registerMobClass(Rat.class);
		registerMobClass(Albino.class);
		registerMobClass(Gnoll.class);
		registerMobClass(Crab.class);
		registerMobClass(Swarm.class);
		registerMobClass(Thief.class);
		registerMobClass(Skeleton.class);
		registerMobClass(RatKing.class);
		registerMobClass(Goo.class);

		registerMobClass(Shaman.class);
		registerMobClass(Shadow.class);
		registerMobClass(Bat.class);
		registerMobClass(Brute.class);
		registerMobClass(Tengu.class);
		registerMobClass(Bandit.class);

		registerMobClass(SpiderServant.class);
		registerMobClass(SpiderExploding.class);
		registerMobClass(SpiderMind.class);
		registerMobClass(SpiderEgg.class);
		registerMobClass(SpiderNest.class);
		registerMobClass(SpiderQueen.class);

		registerMobClass(Spinner.class);
		registerMobClass(Elemental.class);
		registerMobClass(Monk.class);
		registerMobClass(DM300.class);
		registerMobClass(Shielded.class);

		registerMobClass(AirElemental.class);
		registerMobClass(WaterElemental.class);
		registerMobClass(EarthElemental.class);
		registerMobClass(Warlock.class);
		registerMobClass(Golem.class);
		registerMobClass(Succubus.class);
		registerMobClass(King.class);
		registerMobClass(Undead.class);
		registerMobClass(Senior.class);

		registerMobClass(Eye.class);
		registerMobClass(Scorpio.class);
		registerMobClass(Acidic.class);
		registerMobClass(Yog.class);
		registerMobClass(Larva.class);
		registerMobClass(BurningFist.class);
		registerMobClass(RottingFist.class);

		registerMobClass(FetidRat.class);

		registerMobClass(Wraith.class);
		registerMobClass(Mimic.class);
		registerMobClass(MimicPie.class);
		registerMobClass(Statue.class);
		registerMobClass(Piranha.class);

		registerMobClass(MimicAmulet.class);
		registerMobClass(Worm.class);
		registerMobClass(YogsBrain.class);
		registerMobClass(YogsEye.class);
		registerMobClass(YogsHeart.class);
		registerMobClass(YogsTeeth.class);
		registerMobClass(ZombieGnoll.class);
		registerMobClass(ShadowLord.class);
		registerMobClass(Nightmare.class);
		registerMobClass(SuspiciousRat.class);
		registerMobClass(PseudoRat.class);

		registerMobClass(ArmoredStatue.class);
		registerMobClass(GoldenStatue.class);

		registerMobClass(DeathKnight.class);
		registerMobClass(DreadKnight.class);
		registerMobClass(EnslavedSoul.class);
		registerMobClass(ExplodingSkull.class);
		registerMobClass(JarOfSouls.class);
		registerMobClass(Lich.class);
		registerMobClass(RunicSkull.class);
		registerMobClass(Zombie.class);

		registerMobClass(Crystal.class);

		registerMobClass(Kobold.class);
		registerMobClass(KoboldIcemancer.class);
		registerMobClass(ColdSpirit.class);

		registerMobClass(IceElemental.class);
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
		registerMobClass(ArtificerNPC.class);

		registerMobClass(Deathling.class);
	}
	
	public static Mob mobRandom() {
		try {
			return Random.element(mMobsList.values()).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Mob mobByName(String selectedMobClass) {

		try {
			Class<? extends Mob> mobClass = mMobsList.get(selectedMobClass);
			if (mobClass != null) {
				return mobClass.newInstance();
			} else {
				return new CustomMob(selectedMobClass);
			}
		} catch (Exception e) {
			throw new TrackedRuntimeException(e);
		}
	}
}
