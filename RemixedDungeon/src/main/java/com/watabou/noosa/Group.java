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

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import lombok.SneakyThrows;

public class Group extends Gizmo {

	static int totalGizmo;
	static int nullGizmo;

	@NotNull
	protected ArrayList<Gizmo> members = new ArrayList<>();

	public Group() {
	}

	@Override
	public void destroy() {
		clear();
	}


	@Override
	public void update() {
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
			if ( g!= null && exists && g.active) {
				g.update();
			}
		}
	}

	@Override
	public void draw() {
		for (int i = 0; i < members.size(); i++) {
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

	public void addToBack(Gizmo g) {

		if (g.getParent() == this) {
			sendToBack(g);
		}

		members.add(0, g);
		g.setParent(this);
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
	}

	public void removeAll() {
		for(Gizmo g:members) {
			g.setNullParent();
		}
		members.clear();
	}


	private Gizmo getFirstAvailable(@NotNull Class<? extends Gizmo> c) {

		for (int i = 0; i < getLength(); i++) {
			Gizmo g = members.get(i);
			if (g != null && !g.exists && g.getClass() == c) {
				return g;
			}
		}

		return null;
	}

	protected int countLiving() {

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

	public void bringToFront(Gizmo g) {
		if (members.contains(g)) {
			members.remove(g);
			members.add(g);
		}
	}

	private void sendToBack(Gizmo g) {
		if (members.contains(g)) {
			members.remove(g);
			members.add(0, g);
		}
	}

	public int getLength() {
		return members.size();
	}

	public Gizmo getMember(int i) {
		return members.get(i);
	}

	//Testing stuff

	public int findByClass(@NotNull Class<? extends Gizmo> c, int offset) {
		for (int i = offset; i < getLength(); i++) {
			Gizmo g = members.get(i);
			if (c.isAssignableFrom(g.getClass())) {
				return i;
			}
		}
		return -1;
	}
}
