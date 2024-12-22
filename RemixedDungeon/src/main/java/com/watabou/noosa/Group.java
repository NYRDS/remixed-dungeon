/*
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

package com.watabou.noosa;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

import lombok.SneakyThrows;

public class Group extends Gizmo {

	static int totalGizmo;
	static int nullGizmo;

	@NotNull
	protected final ArrayList<Gizmo> members = new ArrayList<>();

	private boolean sorted = false;
	public boolean sort = false;

	public Group() {
	}

	@Override
	public void destroy() {
		clear();
	}


	@Override
	public void update() {
		if (sort && !sorted) {
			sort();
		}
		//members.removeAll(Collections.singleton(null));
		//members.removeIf(Objects::isNull); needs Android N
		for (int i = 0; i < members.size(); i++) {
			Gizmo g = members.get(i);

/*
			totalGizmo++;
			if(g==null) {
				nullGizmo++;
			}
*/
			if ( g!= null && g.alive && g.isActive()) {
				g.update();
			}
		}
	}

	@Override
	public void draw() {
		for (int i = 0; i < members.size(); i++) {
			Gizmo g = members.get(i);
			if (g != null && g.alive && g.getVisible()) {
				g.draw();
			}
		}
	}

	@Override
	public void kill() {
		// A killed group keeps all its members,
		// but they get killed too
		for (int i = 0; i < members.size(); i++) {
			Gizmo g = members.get(i);
			if (g != null && g.alive) {
				g.kill();
			}
		}

		super.kill();
	}

	public Gizmo add(Gizmo g) {

		if (g.getParent() == this) {
			return g;
		}

		members.add(g);
		g.setParent(this);
		sorted = false;
		return g;
	}

	public Gizmo addAfter(Gizmo g, Gizmo after) {
		int i = members.indexOf(after);
		if (i == -1) {
			return add(g);
		} else {
			members.add(i + 1, g);
			g.setParent(this);
			sorted = false;
			return g;
		}
	}
	@SneakyThrows
	public Gizmo recycle(@NotNull Class<? extends Gizmo> c) {

		Gizmo g = getFirstAvailable(c);
		if (g != null) {
			return g;
		}

		return add(c.newInstance());
	}

	// Real removal
	public void remove(Gizmo g) {
		if (members.remove(g)) {
			g.setNullParent();
		}
		sorted = false;
	}

	public void removeAll() {
		for(Gizmo g:members) {
			g.setNullParent();
		}
		members.clear();
	}


	private Gizmo getFirstAvailable(@NotNull Class<? extends Gizmo> c) {

		for (Gizmo g: members) {
			if (g != null && !g.alive && g.getClass() == c) {
				return g;
			}
		}

		return null;
	}

	protected int countLiving() {

		int count = 0;

		for (Gizmo g: members) {
			if (g != null && g.alive) {
				count++;
			}
		}

		return count;
	}

	public void clear() {
		for (int i = 0; i < getLength(); i++) {
			Gizmo g = members.get(i);
			if (g != null) {
				g.destroy();
			}
		}
		members.clear();
	}

	public void bringToFront(Gizmo g) {
		members.remove(g);
		members.add(g);
		g.setParent(this);
	}

	public void sendToBack(Gizmo g) {
		members.remove(g);
		members.add(0, g);
		g.setParent(this);
	}

	public int getLength() {
		return members.size();
	}

	public Gizmo getMember(int i) {
		return members.get(i);
	}

	/** @noinspection ComparatorCombinators*/
	public void sort() {
		members.removeAll(Collections.singleton(null));
		Collections.sort(members, (a,b)-> a.layer - b.layer);
		sorted = true;
	}

	//Testing stuff

	public int findByClass(@NotNull Class<? extends Object> c, int offset) {
		for (int i = offset; i < getLength(); i++) {
			Gizmo g = members.get(i);
			if (g.isActive() && c.isAssignableFrom(g.getClass())) {
				return i;
			}
		}
		return -1;
	}
}
