package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.AppodealRewardVideo;
import com.nyrds.pixeldungeon.support.RewardVideoAds;
import com.nyrds.pixeldungeon.windows.WndMovieTheatre;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Bundle;

public class ServiceManNPC extends NPC {

	private int filmsSeen = 0;
	final private String FILMS_SEEN = "films_seen";

	public ServiceManNPC() {
		AppodealRewardVideo.initCinemaRewardVideo();
	}
	
	@Override
	public int defenseSkill( Char enemy ) {
		return 1000;
	}
	
	@Override
	public String defenseVerb() {
		return Game.getVar(R.string.Ghost_Defense);
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(FILMS_SEEN,filmsSeen);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		filmsSeen = bundle.optInt(FILMS_SEEN,0);
	}

	@Override
	protected Char chooseEnemy() {
		return DUMMY;
	}
	
	@Override
	public void damage( int dmg, Object src ) {
	}
	
	@Override
	public void add( Buff buff ) {
	}

	@Override
	public boolean reset() {
		return true;
	}

	public void reward() {
		filmsSeen++;
		Dungeon.hero.collect(new Gold(150));
	}

	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo( getPos(), hero.getPos() );

		if(filmsSeen >= 5){
			say(Game.getVar(R.string.ServiceManNPC_Limit));
			return true;
		}

		if(RewardVideoAds.isReady()) {
			GameScene.show(new WndMovieTheatre(this));
		} else {
			say(Game.getVar(R.string.ServiceManNPC_NotReady));
		}

		return true;
	}

}
