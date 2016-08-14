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

import android.support.annotation.NonNull;

import com.nyrds.android.util.TrackedRuntimeException;

import java.util.ArrayList;

public class Group extends Gizmo {

	@NonNull
	protected ArrayList<Gizmo> members = new ArrayList<>();

	public Group() {
	}

	@Override
	public void destroy() {
		for (int i = 0; i < getLength(); i++) {
			Gizmo g = members.get(i);
			if (g != null) {
				g.destroy();
			}
		}
		members.clear();
	}

	@Override
	public void update() {
		for (int i = 0; i < getLength(); i++) {
			Gizmo g = members.get(i);
			if (g != null && g.exists && g.active) {
				g.update();
			}
		}
	}

	@Override
	public void draw() {
		for (int i = 0; i < getLength(); i++) {
			Gizmo g = members.get(i);
			if (g != null && g.exists && g.getVisible()) {
				g.draw();
			}
		}
	}

	@Override
	public void kill() {
		// A killed group keeps all its members,
		// but they get killed too
		for (int i = 0; i < getLength(); i++) {
			Gizmo g = members.get(i);
			if (g != null && g.exists) {
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
		return g;
	}

	public Gizmo addToBack(Gizmo g) {

		if (g.getParent() == this) {
			sendToBack(g);
			return g;
		}

		members.add(0, g);
		g.setParent(this);
		return g;
	}

	public Gizmo recycle(@NonNull Class<? extends Gizmo> c) {

		Gizmo g = getFirstAvailable(c);
		if (g != null) {
			return g;
		}

		try {
			return add(c.newInstance());
		} catch (Exception e) {
			throw new TrackedRuntimeException(e);
		}
	}

	// Real removal
	public void remove(Gizmo g) {
		if (members.remove(g)) {
			g.setNullParent();
		}
	}

	public Gizmo getFirstAvailable(@NonNull Class<? extends Gizmo> c) {

		for (int i = 0; i < getLength(); i++) {
			Gizmo g = members.get(i);
			if (g != null && !g.exists && g.getClass() == c) {
				return g;
			}
		}

		return null;
	}

	public int countLiving() {

		int count = 0;

		for (int i = 0; i < getLength(); i++) {
			Gizmo g = members.get(i);
			if (g != null && g.exists && g.alive) {
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

	public Gizmo bringToFront(Gizmo g) {
		if (members.contains(g)) {
			members.remove(g);
			members.add(g);
			return g;
		} else {
			return null;
		}
	}

	public Gizmo sendToBack(Gizmo g) {
		if (members.contains(g)) {
			members.remove(g);
			members.add(0, g);
			return g;
		} else {
			return null;
		}
	}

	public int getLength() {
		return members.size();
	}
}
