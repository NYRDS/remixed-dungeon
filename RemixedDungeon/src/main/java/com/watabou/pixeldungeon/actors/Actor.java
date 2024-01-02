
package com.watabou.pixeldungeon.actors;

import android.annotation.SuppressLint;

import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.SystemTime;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



public abstract class Actor implements Bundlable, NamedEntityKind {
	
	public static final float TICK	= 1f;
	public static final float MICRO_TICK	= 0.001f;
	private static float realTimeMultiplier = 1f;

	private static final Map<Actor, Integer> skipCounter = new HashMap<>();

	@Packable
	private float time;

	public static void setRealTimeMultiplier(float realTimeMultiplier) {
		Actor.realTimeMultiplier = realTimeMultiplier;
	}

	protected abstract boolean act();

	public void spend( float time ) {
		//GLog.debug("%s spend %2.4f", getEntityKind(), time);
		if(Util.isDebug() && current!=this) {
			if(this instanceof Char) {
				GLog.debug("%s spends time on %s move!", getEntityKind(), current!=null?current.getEntityKind():"no one");
				//throw new TrackedRuntimeException(String.format("%s spends time on %s move!", getEntityKind(), current!=null?current.getEntityKind():"no one"));
			}
			if(this instanceof Hero && time > 5) {
				GLog.debug("hero long spend!");
			}
		}
		this.time += time;
	}

	public void postpone( float time ) {
		if (this.time < now + time) {
			this.time = now + time;
		}
	}

	protected float cooldown() {
		return time - now;
	}

	@LuaInterface
	public void deactivateActor() {
		time = Util.BIG_FLOAT;
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
	}

	@Override
	public boolean dontPack() {
		return false;
	}
	
	// **********************
	// *** Static members ***
	
	private static final HashSet<Actor> all = new HashSet<>();
	private static Actor current;
	
	private static float now = 0;
	
	@SuppressLint("UseSparseArrays")
	public static Map<Integer, Char> chars = new HashMap<>();
	
	public static void clearActors() {
		now = 0;
		chars.clear();
		all.clear();

		CharsList.reset();
		current = null;
	}
	
	public static void fixTime() {
		Hero hero = Dungeon.hero;

		if (hero.valid() && all.contains(hero)) {
			Statistics.duration += now;
		}
		
		float min = Util.BIG_FLOAT;
		for (Actor a : all) {
			if (a.time < min) {
				min = a.time;
			}
		}
		for (Actor a : all) {
			a.time -= min;
		}
		now = 0;
	}
	
	public static void init(@NotNull Level level) {
		clearActors();
		
		addDelayed( Dungeon.hero, -Float.MIN_VALUE );
		
		for (Mob mob : level.mobs) {
			mob.regenSprite();
			add( mob );
		}
		
		for (Blob blob : level.blobs.values()) {
			add( blob );
		}
	}
	
	public static void occupyCell(@NotNull Char ch ) {
		if(ch.getPos() == Level.INVALID_CELL && ! (ch instanceof DummyChar)) {
			throw new TrackedRuntimeException("trying to spawn mob in void");
		}
		chars.put(ch.getPos(), ch);
	}
	
	public static void freeCell( int pos ) {
		chars.remove(pos);
	}
	
	/*protected*/final public void next() {
		//Log.i("Main loop", String.format("next:\nNext: %s Current: %s", this, current));
		if (current == this) {
			//Log.i("Main loop", "next == current");

			current = null;
		}
	}

	private static void processReaTime(float elapsed) {

		now += elapsed * realTimeMultiplier;

		Actor next;
		while ((next=getNextActor(now)) != null) {

			//Log.i("Main loop", String.format("%s %4.2f %4.2f",next.getClass().getSimpleName(),now,next.time));

			//float timeBefore = next.time;

			current = next;
			EventCollector.setSessionData("actor", next.getEntityKind());
			next.act();
/*
			if(!(next.time>timeBefore)) {
				//Log.i("Main loop", String.format("%s %4.2f, time not increased!",next.getClass().getSimpleName(),next.time));
			}
*/
		}

	}

	public static void processTurnBased() {

		// action still in progress
		if (current != null && !current.timeout()) {
			checkSkips(current);
			return;
		}

		Actor actor;

		//Log.i("Main loop", "start");
		while ((actor=getNextActor(Util.BIG_FLOAT)) != null) {

			if (actor instanceof Char && ((Char)actor).isAlive() && ((Char)actor).getSprite().doingSomething()) {
				checkSkips(actor);
				GLog.debug("skip: %s %4.4f %x",actor.getEntityKind(), actor.time, actor.hashCode());
				// If it's character's turn to act, but its sprite
				// is moving, wait till the movement is over

				return;
			}

			resetSkips(actor);

			GLog.debug("Main actor loop: %s %4.4f %x",actor.getEntityKind(), actor.time, actor.hashCode());
			if(actor instanceof Char) {
				//GLog.debug("%s %d action %s",actor.getEntityKind(), ((Char) actor).getId(),((Char) actor).curAction);
			}

			current = actor;

			float timeBefore = actor.time;

			EventCollector.setSessionData("actor", actor.getEntityKind());

			if (actor.act() && Dungeon.hero.isAlive()) {
				//Log.i("Main loop", String.format("%s next %x",actor.getEntityKind(), actor.hashCode()));
				actor.next();
			} else {
				//Log.i("Main loop", String.format("%s next %x",actor.getEntityKind(), actor.hashCode()));
				break;
			}
/*
			if(actor.time == timeBefore && all.contains(actor)) { // don't need this check for removed actors
				var error = String.format("actor %s has same timestamp after act!", actor.getEntityKind());
				if(Util.isDebug()) {
					throw new ModError(error);
				} else {
					actor.spend(TICK);
					EventCollector.logException(error);
				}
			}
*/
			if(SystemTime.timeSinceTick() > 50) {
				break;
			}
		}
	}

	private static void resetSkips(Actor actor) {
		if(!Util.isDebug()) {
			return;
		}
		skipCounter.put(actor, 0);
	}

	private static void checkSkips(Actor actor) {
		if(!Util.isDebug()) {
			return;
		}

		if(actor.getEntityKind().equals("Hero")) {
			return;
		}

		Integer skips = skipCounter.get(actor);
		if(skips == null) {
			skips = 0;
		}
		skips++;

		if (skips > 1000) {
			GLog.debug("Stall!");
		}

		skipCounter.put(actor, skips);

		//GLog.debug("skip: %s ", skipCounter.toString());
	}

	public static void process(float elapsed) {
		if(Dungeon.realtime()) {
			processReaTime(elapsed);
		} else {
			processTurnBased();
		}
	}

	public static Actor getNextActor(float upTo) {

		var copyOfAll = all.toArray(new Actor[0]);

		Set<Char> toRemove = new HashSet<>();
		Actor next     = null;

		chars.clear();

		//Log.i("Main loop","getNextActor");

		for (Actor actor : copyOfAll) {
			if (actor instanceof Char) {
				Char ch = (Char)actor;

				if(!ch.level().cellValid(ch.getPos())) {
					actor.next();
					toRemove.add(ch);
				}
			}
		}

		for(Char ch : toRemove) {
			ch.regenSprite();
		}

		all.removeAll(toRemove);
		copyOfAll = all.toArray(new Actor[0]);


		for (Actor actor : copyOfAll) {

			boolean busy = false;

			//fill chars
			if (actor instanceof Char) {
				Char ch = (Char)actor;

				final CharSprite chSprite = ch.getSprite();

				if(chSprite.doingSomething()) {
					busy = true;
				}

				chars.put(ch.getPos(), ch);
			}

			//select actor to act
			if (actor.time < upTo) {
				upTo = actor.time;
				next = actor;
			}

			if(actor.time == upTo && !busy) {
				next = actor;
				//GLog.debug("not busy");
			}
		}

		Actor.now = upTo;

		return next;
	}

	protected boolean timeout() {
		return false;
	}

	public static void add( Actor actor ) {
		add( actor, now );
	}
	
	public static void addDelayed( Actor actor, float delay ) {
		add( actor, now + delay );
	}
	
	private static void add( Actor actor, float time ) {
		
		if (all.contains( actor )) {
			return;
		}
		
		all.add( actor );
		actor.time += time;

		if (actor instanceof Char) {
			Char ch = (Char)actor;

			CharsList.add(ch,ch.getId());

			chars.put(ch.getPos(), ch);
			all.addAll(ch.buffs());

			for(var item: ch.getBelongings()) {
				all.add(item);
			}

		}

	}
	
	public static void remove( Actor actor ) {
		
		if (actor != null) {
			actor.next();
			
			all.remove( actor );
		}
	}

	@LuaInterface
	public static boolean motionInProgress() {
		for(Actor ch : all) {
			if (ch instanceof Char){
				if (((Char) ch).getSprite().doingSomething()) {
					return true;
				}
			}
		}
		GLog.debug("no motion in progress");
		return false;
	}

	@LuaInterface
	public static Char getRandomChar() {
		var ret = Random.element(chars.values());
		GLog.debug("selected %s at %d", ret.getEntityKind(),ret.getPos());
		return ret;
	}

	@LuaInterface
	public static Char findChar(int pos) {
		return chars.get(pos);
	}

	@LuaInterface
	public boolean myMove() {
		return current == this;
	}

	public static HashSet<Actor> all() {
		return all;
	}

	@LuaInterface
	public float actorTime() {
		return time;
	}

	@LuaInterface
	static public float localTime() {
		return now;
	}

	@Override
	public String name() {
		return getEntityKind();
	}

	@Override
	public String getEntityKind() {
		return getClass().getSimpleName();
	}

	public boolean testAct() {
		return act();
	}
}
