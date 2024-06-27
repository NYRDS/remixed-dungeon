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

package com.nyrds.platform.audio;

public enum Sample  {

	INSTANCE;


	public void reset() {
}

	public void pause() {
	}

	public void resume() {
	}

	private void load(String asset) {
	}


	public int play(String id) {
		return 0;
	}

	public int play(String id, float volume) {
		return 0;
	}

	public int play(String id, float leftVolume, float rightVolume, float rate) {
		return 0;
	}

	public void enable(boolean value) {
	}

}
