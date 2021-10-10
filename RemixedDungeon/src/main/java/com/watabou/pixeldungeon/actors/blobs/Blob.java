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
package com.watabou.pixeldungeon.actors.blobs;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.util.ModError;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.BArray;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;

import lombok.SneakyThrows;

public class Blob extends Actor implements NamedEntityKind {

	public int volume;

	public    int[] cur;
	protected int[] off;

	static private int width;
	static private int height;

	public BlobEmitter emitter;

	protected Blob() {

		cur = new int[getLength()];
		off = new int[getLength()];

		volume = 0;
	}

	private static final String CUR   = "cur";
	private static final String START = "start";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);

		if (volume > 0) {

			int start;
			for (start = 0; start < getLength(); start++) {
				if (cur[start] > 0) {
					break;
				}
			}
			int end;
			for (end = getLength() - 1; end > start; end--) {
				if (cur[end] > 0) {
					break;
				}
			}

			bundle.put(START, start);
			bundle.put(CUR, trim(start, end + 1));

		}
	}

	private int[] trim(int start, int end) {
		int len = end - start;
		int[] copy = new int[len];
		System.arraycopy(cur, start, copy, 0, len);
		return copy;
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {

		super.restoreFromBundle(bundle);

		int[] data = bundle.getIntArray(CUR);

		int start = bundle.getInt(START);
		for (int i = 0; i < data.length; i++) {
			cur[i + start] = data[i];
			volume += data[i];
		}
	}

	@Override
	public boolean act() {

		spend(TICK);

		if (volume > 0) {

			volume = 0;
			evolve();

			int[] tmp = off;
			off = cur;
			cur = tmp;

		}

		return true;
	}

	public void use(BlobEmitter emitter) {
		this.emitter = emitter;
	}

	protected void evolve() {

		boolean[] notBlocking = BArray.not(Dungeon.level.solid, null);

		for (int i = 1; i < getHeight() - 1; i++) {

			int from = i * getWidth() + 1;
			int to = from + getWidth() - 2;

			for (int pos = from; pos < to; pos++) {
				if (notBlocking[pos]) {

					int count = 1;
					int sum = cur[pos];

					if (notBlocking[pos - 1]) {
						sum += cur[pos - 1];
						count++;
					}
					if (notBlocking[pos + 1]) {
						sum += cur[pos + 1];
						count++;
					}
					if (notBlocking[pos - getWidth()]) {
						sum += cur[pos - getWidth()];
						count++;
					}
					if (notBlocking[pos + getWidth()]) {
						sum += cur[pos + getWidth()];
						count++;
					}

					int value = sum >= count ? (sum / count) - 1 : 0;
					off[pos] = value;

					volume += value;
				} else {
					off[pos] = 0;
				}
			}
		}
	}

	public void seed(int cell, int amount) {
		checkSeedCell(cell);
		cur[cell] += amount;
		volume += amount;
	}

	public void seed(int x,int y, int amount) {
		seed(x+y*width,amount);
	}

	public void clearBlob(int cell) {
		volume -= cur[cell];
		cur[cell] = 0;
	}

	public String tileDesc() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@SneakyThrows
	public static <T extends Blob> T seed(int cell, int amount, Class<T> type) {
		return seed(Dungeon.level, cell, amount, type);
	}

	@LuaInterface
	@SneakyThrows
	public static <T extends Blob> T seed(Level level, int cell, int amount, Class<T> type) {
		T gas = (T) level.blobs.get(type);
		if (gas == null) {
			gas = type.newInstance();
			level.blobs.put(type, gas);
		}

		gas.seed(cell, amount);

		return gas;
	}

	public static void setWidth(int val) {
		width = val;
	}

	public static void setHeight(int val) {
		height = val;
	}

	public static int getWidth() {
		return width;
	}

	public static int getHeight() {
		return height;
	}

	public static int getLength() {
		return width * height;
	}

	@Override
	public String getEntityKind() {
		return getClass().getSimpleName();
	}

	@Override
	public String name() {
		return getEntityKind();
	}

	protected void checkSeedCell(int cell) {
		if(cell<0 || cell > this.cur.length) {
			throw new ModError(Utils.format("Bad cell %d for blob %s", cell, getEntityKind()));
		}
	}
}
