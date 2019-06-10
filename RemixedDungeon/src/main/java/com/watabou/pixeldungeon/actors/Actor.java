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
import android.util.Log;

import com.nyrds.Packable;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
	
	protected void postpone( float time ) {
		if (this.time < now + time) {
			this.time = now + time;
		}
	}

	protected float cooldown() {
		return time - now;
	}
	
	protected void deactivate() {
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
	
	private static HashSet<Actor> all = new HashSet<>();
	private static Actor current;
	
	private static float now = 0;
	
	@SuppressLint("UseSparseArrays")
	public static Map<Integer, Char> chars = new HashMap<>();
	
	public static void clear() {
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
	
	public static void init(Level level) {
		clear();
		
		addDelayed( Dungeon.hero, -Float.MIN_VALUE );
		
		for (Mob mob : level.mobs) {
			mob.regenSprite();
			add( mob );
		}
		
		for (Blob blob : level.blobs.values()) {
			add( blob );
		}
	}
	
	public static void occupyCell( Char ch ) {
		chars.put(ch.getPos(), ch);
	}
	
	public static void freeCell( int pos ) {
		chars.remove(pos);
	}
	
	/*protected*/public void next() {
		if (current == this) {
			current = null;
		}
	}
	

	private static void processReaTime(float elapsed) {

		now += elapsed * realTimeMultiplier;

		do {
			current = null;

			chars.clear();
			float justNow = now;

			//select actor to act
			for (Actor actor : all) {
				if (actor.time < justNow) {
					current = actor;
					justNow = current.time;
				}

				//also fill chars positions
				if (actor instanceof Char) {
					Char ch = (Char) actor;
					chars.put(ch.getPos(), ch);
				}
			}

			if(current!= null) {
				Actor actor = current;
//				Log.i("Main loop", String.format("%s %4.2f %4.2f",actor.getClass().getSimpleName(),now,actor.time));

				float timeBefore = actor.time;

				actor.act();

				if(!(actor.time>timeBefore)) {
					Log.i("Main loop", String.format("%s %4.2f, time not increased!",actor.getClass().getSimpleName(),actor.time));
				}
			}

		} while (current != null);

	}
	
	public static void process(float elapsed) {
		
		if(Dungeon.realtime()) {
			processReaTime(elapsed);
			return;
		}

		// action still in progress
		if (current != null && !current.timeout()) {
			return;
		}
	
		boolean doNext;
		
		Actor toRemove = null;
		
		do {
			now = Float.MAX_VALUE;
			current = null;
			
			chars.clear();
			
			
			for (Actor actor : all) {

				//select actor to act
				if (actor.time < now) {
					now = actor.time;
					current = actor;
				}

				//fill chars
				if (actor instanceof Char) {
					Char ch = (Char)actor;

					//some old dirty hack
					if(!Dungeon.level.cellValid(ch.getPos())) {
						current = null;
						toRemove = actor;
						continue;
					}

					chars.put(ch.getPos(), ch);
				}
			}

			if(toRemove != null) {
				remove(toRemove);
				toRemove = null;
			}

			// have candidate to act
			if (current != null) {

				//Log.i("Main loop", String.format("%s %4.2f",current.getClass().getSimpleName(),current.time));

				if (current instanceof Char && ((Char)current).getSprite().isMoving) {
					// If it's character's turn to act, but its sprite 
					// is moving, wait till the movement is over
					//Log.i("Main loop","skipped");
					current = null;
					break;
				}

				//Log.i("Main loop", "act");
				doNext = current.act();

				if (doNext && !Dungeon.hero.isAlive()) {
					doNext = false;
					current = null;
				}

			} else {
				doNext = false;
			}
			
		} while (doNext);
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
		}

	}
	
	public static void remove( Actor actor ) {
		
		if (actor != null) {
			if(current == actor) {
				current = null;
			}
			
			all.remove( actor );
		}
	}
	
	public static Char findChar(int pos) {
		return chars.get(pos);
	}
	
	public static HashSet<Actor> all() {
		return all;
	}
}
