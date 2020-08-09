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

package com.watabou.utils;

public class SystemTime {

	private static long now;
	private static long lastActionTime;

	public static void tick() {
		now = System.currentTimeMillis();
	}

	public static long now() {
		return now;
	}

	public static long timeSinceTick() {
		return System.currentTimeMillis() - now;
	}

	public static long getLastActionTime() {
		return lastActionTime;
	}

	public static void updateLastActionTime() {
		lastActionTime = now();
	}
}

