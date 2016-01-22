package com.nyrds.pixeldungeon.mobs.common;

import java.util.HashMap;

import com.nyrds.pixeldungeon.mobs.elementals.AirElemental;
import com.nyrds.pixeldungeon.mobs.elementals.EarthElemental;
import com.nyrds.pixeldungeon.mobs.elementals.WaterElemental;
import com.nyrds.pixeldungeon.mobs.spiders.SpiderEgg;
import com.nyrds.pixeldungeon.mobs.spiders.SpiderExploding;
import com.nyrds.pixeldungeon.mobs.spiders.SpiderMind;
import com.nyrds.pixeldungeon.mobs.spiders.SpiderNest;
import com.nyrds.pixeldungeon.mobs.spiders.SpiderQueen;
import com.nyrds.pixeldungeon.mobs.spiders.SpiderServant;
import com.watabou.noosa.Game;
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
import com.watabou.pixeldungeon.actors.mobs.Mimic;
import com.watabou.pixeldungeon.actors.mobs.MimicPie;
import com.watabou.pixeldungeon.actors.mobs.King.Undead;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Monk;
import com.watabou.pixeldungeon.actors.mobs.Piranha;
import com.watabou.pixeldungeon.actors.mobs.Rat;
import com.watabou.pixeldungeon.actors.mobs.Scorpio;
import com.watabou.pixeldungeon.actors.mobs.Shadow;
import com.watabou.pixeldungeon.actors.mobs.Shaman;
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
import com.watabou.utils.Random;


public class MobFactory {

	static private HashMap <String, Class<? extends Mob>> mMobsList;
	
	private static void registerMobClass(Class<? extends Mob> mobClass) {
		mMobsList.put(mobClass.getSimpleName(), mobClass);
	}
	
	private static void initMobsMap() {
		
		mMobsList = new HashMap<>();
		registerMobClass(Rat.class);
		registerMobClass(Gnoll.class);
		registerMobClass(Crab.class);
		registerMobClass(Swarm.class);
		registerMobClass(Thief.class);
		registerMobClass(Skeleton.class);
		registerMobClass(Goo.class);
		
		registerMobClass(Shaman.class);
		registerMobClass(Shadow.class);
		registerMobClass(Bat.class);
		registerMobClass(Brute.class);
		registerMobClass(Tengu.class);
		
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
		
		registerMobClass(AirElemental.class);
		registerMobClass(WaterElemental.class);
		registerMobClass(EarthElemental.class);
		registerMobClass(Warlock.class);
		registerMobClass(Golem.class);
		registerMobClass(Succubus.class);
		registerMobClass(King.class);
		registerMobClass(Undead.class);
		
		registerMobClass(Eye.class);
		registerMobClass(Scorpio.class);
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
	}
	
	public static Class<? extends Mob> mobClassRandom() {
		if(mMobsList==null) {
			initMobsMap();
		}
		
		return Random.element(mMobsList.values());
	}
	
	public static Class<? extends Mob> mobClassByName(String selectedMobClass) {
		
		if(mMobsList==null) {
			initMobsMap();
		}
		
		Class<? extends Mob> mobClass = mMobsList.get(selectedMobClass);
		if(mobClass != null) {
			return mobClass;
		} else {
			Game.toast("Unknown mob: [%s], spawning Rat instead",selectedMobClass);
			return Rat.class;
		}
	}

}
