/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.actors;

import android.annotation.SuppressLint;

import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.android.util.TrackedRuntimeException;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.SystemTime;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import lombok.var;

public abstract class Actor implements Bundlable {
	
	public static final float TICK	= 1f;
	private static float realTimeMultiplier = 1f;

	@Packable
	private float time;

	public static void setRealTimeMultiplier(float realTimeMultiplier) {
		Actor.realTimeMultiplier = realTimeMultiplier;
	}

	protected abstract boolean act();

	public void spend( float time ) {
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
		time = Float.MAX_VALUE;
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

		current = null;
	}
	
	public static void fixTime() {
		
		if (Dungeon.hero != null && all.contains( Dungeon.hero )) {
			Statistics.duration += now;
		}
		
		float min = Float.MAX_VALUE;
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
//			Log.i("Main loop", String.format("skip: %s %4.1f", current, current.time));
			return;
		}

		Actor actor;

//		Log.i("Main loop", "start");
		while ((actor=getNextActor(Float.MAX_VALUE)) != null) {

//			Log.i("Main loop", String.format("%s %4.2f",actor.getClass().getSimpleName(),actor.time));

			if (actor instanceof Char && ((Char)actor).getSprite().doingSomething()) {
//				Log.i("Main loop", "in action");
				// If it's character's turn to act, but its sprite
				// is moving, wait till the movement is over
				return;
			}

			current = actor;

			if (actor.act() || !Dungeon.hero.isAlive()) {
//				Log.i("Main loop", String.format("%s next",actor.getClass().getSimpleName()));
				actor.next();
			} else {
//				Log.i("Main loop", String.format("%s break",actor.getClass().getSimpleName()));

				break;
			}

			if(SystemTime.timeSinceTick() > 40) {
//				Log.i("Main loop", String.format("%s timeout",actor.getClass().getSimpleName()));

				break;
			}
		}
	}

	public static void process(float elapsed) {
		if(Dungeon.realtime()) {
			processReaTime(elapsed);
		} else {
			processTurnBased();
		}
	}

	public static Actor getNextActor(float upTo) {
		Actor toRemove = null;
		Actor next     = null;

		chars.clear();

		//Log.i("Main loop","getNextActor");

		for (Actor actor : all) {
			//select actor to act
			if (actor.time < upTo) {
				upTo = actor.time;
				next = actor;
			}

			//fill chars
			if (actor instanceof Char) {
				Char ch = (Char)actor;

				//filter out badly placed chars, is this still happens?
				if(!ch.level().cellValid(ch.getPos())) {
					actor.next();
					toRemove = actor;
					ch.getSprite().killAndErase();
					continue;
				}

				chars.put(ch.getPos(), ch);
			}
		}

		Actor.now = upTo;

		remove(toRemove);
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
	public static Char getRandomChar(int pos) {
		return Random.element(chars.values());
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
}
