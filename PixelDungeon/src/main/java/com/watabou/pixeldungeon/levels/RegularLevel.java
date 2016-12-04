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
package com.watabou.pixeldungeon.levels;

import com.nyrds.pixeldungeon.levels.CustomLevel;
import com.nyrds.pixeldungeon.levels.objects.Barrel;
import com.nyrds.pixeldungeon.levels.objects.Sign;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.watabou.pixeldungeon.Bones;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.pixeldungeon.levels.Room.Type;
import com.watabou.pixeldungeon.levels.painters.ExitPainter;
import com.watabou.pixeldungeon.levels.painters.Painter;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Graph;
import com.watabou.utils.Random;
import com.watabou.utils.Rect;
import com.watabou.utils.SparseArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public abstract class RegularLevel extends CustomLevel {

	protected HashSet<Room> rooms;

	private SparseArray<Room> exits = new SparseArray<>();

	protected Room roomEntrance;

	public int secretDoors;

	protected boolean placeEntranceAndExit() {
		int distance;
		int retry = 0;
		int minDistance = (int) Math.sqrt(rooms.size());

		Room roomExit;

		do {
			do {
				roomEntrance = Random.element(rooms);
			} while (roomEntrance.width() < 4 || roomEntrance.height() < 4);

			do {
				roomExit = Random.element(rooms);
			} while (roomExit == roomEntrance || roomExit.width() < 4 || roomExit.height() < 4);

			Graph.buildDistanceMap(rooms, roomExit);
			distance = roomEntrance.distance();

			if (retry++ > 10) {
				return false;
			}

		} while (distance < minDistance);

		roomEntrance.type = Type.ENTRANCE;
		roomExit.type = Type.EXIT;

		setRoomExit(roomExit);

		return true;
	}

	@Override
	protected boolean build() {

		if (!initRooms()) {
			return false;
		}

		if (!placeEntranceAndExit()) {
			return false;
		}

		placeSecondaryExits();

		buildPath(roomEntrance, exitRoom(0));

		ArrayList<Room> connectedRooms = new ArrayList<>();
		connectedRooms.add(roomEntrance);

		for (int i = 0; i<DungeonGenerator.exitCount(levelId);++i) {
			connectedRooms.add(exitRoom(i));
		}

		int isolatedCounter = 0;
		int roomCounter = 0;
		for (Room r: rooms) {
			if(!connectedRooms.contains(r)) {
				roomCounter++;
				Room connectedRoom = Random.element(connectedRooms);
				if (r.isRoomIsolatedFrom(connectedRoom)) {
					buildPath(connectedRoom,r);
					isolatedCounter++;
					GLog.i("%s isolated rooms: %d | %d ",r.type.toString(), isolatedCounter, roomCounter);
				} else {
					if(connectedRoom.width()>=3 || connectedRoom.height()>=3) {
						connectedRooms.add(r);
					}
				}
			}
		}

		if (Dungeon.shopOnLevel()) {
			Room shop = null;
			for (Room r : roomEntrance.connected.keySet()) {
				if (r.width() >= 5 && r.height() >= 5) {
					shop = r;
					break;
				}
			}

			if (shop == null) {
				return false;
			} else {
				shop.type = Room.Type.SHOP;
			}
		}

		assignRoomType();


		paint();
		paintWater();
		paintGrass();

		placeTraps();

		return true;
	}

	private void buildPath(Room from, Room to) {
		Graph.buildDistanceMap(rooms, to);
		List<Room> path = Graph.buildPath(from, to);
		if(path!=null) {
			Room room = from;
			for (Room next : path) {
				if (!room.isRoomIsolatedFrom(to)) {
					break;
				}
				room.price(room.price()*2);
				room.connect(next);
				room = next;
			}
		}
	}


	protected Room exitRoom(int index) {
		return exits.get(index);
	}

	protected void placeSecondaryExits() {
		int exitCount = DungeonGenerator.exitCount(levelId);

		for (int i = 1; i < exitCount; ++i) {
			Room secondaryExit;
			do {
				secondaryExit = Random.element(rooms);
			} while (secondaryExit.type != Type.NULL ||
					secondaryExit.width() < 4 ||
					secondaryExit.height() < 4
					);
			secondaryExit.type = Type.EXIT;
			exits.put(i, secondaryExit);

			buildPath(exitRoom(i-1),exitRoom(i));
		}
	}

	protected void assignRemainingRooms() {
		int count = 0;
		for (Room r : rooms) {
			if (r.type == Type.NULL) {
				int connections = r.connected.size();
				if (connections == 0) {

				} else if (Random.Int(connections * connections) == 0) {
					r.type = Type.STANDARD;
					count++;
				} else {
					r.type = Type.TUNNEL;
				}
			}
		}

		while (count < 4) {
			Room r = randomRoom(Type.TUNNEL, 1);
			if (r != null) {
				r.type = Type.STANDARD;
				count++;
			}
		}
	}

	protected void assignRoomConnectivity(Room r) {
		HashSet<Room> neighbours = new HashSet<>();
		for (Room n : r.neighbours) {
			if (!r.connected.containsKey(n) &&
					!Room.SPECIALS.contains(n.type) &&
					n.type != Type.PIT) {

				neighbours.add(n);
			}
		}
		if (neighbours.size() > 1) {
			r.connect(Random.element(neighbours));
		}
	}

	protected int nTraps() {
		return Dungeon.depth <= 1 ? 0 : Random.Int(1, rooms.size() + Dungeon.depth);
	}

	protected boolean initRooms() {

		rooms = new HashSet<>();
		split(new Rect(0, 0, getWidth() - 1, getHeight() - 1));

		if (rooms.size() < 8) {
			return false;
		}

		Room[] ra = rooms.toArray(new Room[rooms.size()]);
		for (int i = 0; i < ra.length - 1; i++) {
			for (int j = i + 1; j < ra.length; j++) {
				ra[i].addNeighbor(ra[j]);
			}
		}

		return true;
	}

	protected void assignRoomType() {

		List<Room.Type> specials = new ArrayList<>(Room.SPECIALS);

		int specialRooms = 0;

		for (Room r : rooms) {
			if (r.type == Type.NULL &&
					r.connected.size() == 1) {

				if (specials.size() > 0 &&
						r.width() > 3 && r.height() > 3 &&
						Random.Int(specialRooms * specialRooms + 2) == 0) {

					if (pitRoomNeeded) {

						r.type = Type.PIT;
						pitRoomNeeded = false;

						specials.remove(Type.ARMORY);
						specials.remove(Type.CRYPT);
						specials.remove(Type.LABORATORY);
						specials.remove(Type.LIBRARY);
						specials.remove(Type.STATUE);
						specials.remove(Type.TREASURY);
						specials.remove(Type.VAULT);
						specials.remove(Type.WEAK_FLOOR);

					} else if (Dungeon.depth % 5 == 2 && specials.contains(Type.LABORATORY)) {

						r.type = Type.LABORATORY;

					} else if (Dungeon.depth >= Dungeon.transmutation && specials.contains(Type.MAGIC_WELL)) {

						r.type = Type.MAGIC_WELL;

					} else {

						int n = specials.size();
						r.type = specials.get(Math.min(Random.Int(n), Random.Int(n)));
						if (r.type == Type.WEAK_FLOOR) {
							weakFloorCreated = true;
						}
					}

					Room.useType(r.type);
					specials.remove(r.type);
					specialRooms++;

				} else if (Random.Int(2) == 0) {
					assignRoomConnectivity(r);
				}
			}
		}
		assignRemainingRooms();
	}

	protected void paintWater() {
		boolean[] lake = water();
		for (int i = 0; i < getLength(); i++) {
			if (map[i] == Terrain.EMPTY && lake[i]) {
				map[i] = Terrain.WATER;
			}
		}
	}

	protected void paintGrass() {
		boolean[] grass = grass();

		if (getFeeling() == Feeling.GRASS) {

			for (Room room : rooms) {
				if (room.type != Type.NULL && room.type != Type.PASSAGE && room.type != Type.TUNNEL) {
					grass[(room.left + 1) + (room.top + 1) * getWidth()] = true;
					grass[(room.right - 1) + (room.top + 1) * getWidth()] = true;
					grass[(room.left + 1) + (room.bottom - 1) * getWidth()] = true;
					grass[(room.right - 1) + (room.bottom - 1) * getWidth()] = true;
				}
			}
		}

		for (int i = getWidth() + 1; i < getLength() - getWidth() - 1; i++) {
			if (map[i] == Terrain.EMPTY && grass[i]) {
				int count = 1;
				for (int n : NEIGHBOURS8) {
					if (grass[i + n]) {
						count++;
					}
				}
				map[i] = (Random.Float() < count / 12f) ? Terrain.HIGH_GRASS : Terrain.GRASS;
			}
		}
	}

	protected abstract boolean[] water();

	protected abstract boolean[] grass();


	protected int minRoomSize = 7;
	protected int maxRoomSize = 9;

	protected void split(Rect rect) {

		int w = rect.width();
		int h = rect.height();

		if (w > maxRoomSize && h < minRoomSize) {

			int vw = Random.Int(rect.left + 3, rect.right - 3);
			split(new Rect(rect.left, rect.top, vw, rect.bottom));
			split(new Rect(vw, rect.top, rect.right, rect.bottom));

		} else if (h > maxRoomSize && w < minRoomSize) {

			int vh = Random.Int(rect.top + 3, rect.bottom - 3);
			split(new Rect(rect.left, rect.top, rect.right, vh));
			split(new Rect(rect.left, vh, rect.right, rect.bottom));

		} else if ((Math.random() <= (minRoomSize * minRoomSize / rect.square()) && w <= maxRoomSize && h <= maxRoomSize) || w < minRoomSize || h < minRoomSize) {

			rooms.add((Room) new Room().set(rect));

		} else {

			if (Random.Float() < (float) (w - 2) / (w + h - 4)) {
				int vw = Random.Int(rect.left + 3, rect.right - 3);
				split(new Rect(rect.left, rect.top, vw, rect.bottom));
				split(new Rect(vw, rect.top, rect.right, rect.bottom));
			} else {
				int vh = Random.Int(rect.top + 3, rect.bottom - 3);
				split(new Rect(rect.left, rect.top, rect.right, vh));
				split(new Rect(rect.left, vh, rect.right, rect.bottom));
			}

		}
	}

	private void paintRoom(Room r) {
		placeDoors(r);
		r.type.paint(this, r);
	}

	protected void paint() {

		ExitPainter.resetCounter();

		for (Room r : rooms) {
			if (r.type != Type.NULL && r.type != Type.SEWER_BOSS_EXIT && r.type != Type.PRISON_BOSS_EXIT && r.type != Type.EXIT) {
				paintRoom(r);
			} else {
				if (getFeeling() == Feeling.CHASM && Random.Int(2) == 0) {
					Painter.fill(this, r, Terrain.WALL);
				}
			}
		}

		for (int i = 0; i < exits.size(); ++i) {
			Room room = exits.get(i);
			if (room.type != Type.SEWER_BOSS_EXIT && room.type != Type.PRISON_BOSS_EXIT) {
				paintRoom(room);
			}
		}

		for (Room r : rooms) {
			if (r.type == Type.SEWER_BOSS_EXIT || r.type == Type.PRISON_BOSS_EXIT) {
				paintRoom(r);
			}
		}

		for (Room r : rooms) {
			paintDoors(r);
		}
	}

	private void placeDoors(Room r) {
		for (Room n : r.connected.keySet()) {
			Room.Door door = r.connected.get(n);
			if (door == null) {

				Rect i = r.intersect(n);
				if (i.width() == 0) {
					door = new Room.Door(
							i.left,
							Random.Int(i.top + 1, i.bottom));
				} else {
					door = new Room.Door(
							Random.Int(i.left + 1, i.right),
							i.top);
				}

				r.connected.put(n, door);
				n.connected.put(r, door);
			}
		}
	}

	protected void paintDoors(Room r) {
		for (Room n : r.connected.keySet()) {

			if (joinRooms(r, n)) {
				continue;
			}

			Room.Door d = r.connected.get(n);
			int door = d.x + d.y * getWidth();

			switch (d.type) {
				case EMPTY:
					map[door] = Terrain.EMPTY;
					break;
				case TUNNEL:
					map[door] = tunnelTile();
					break;
				case REGULAR:
					if (Dungeon.depth <= 1) {
						map[door] = Terrain.DOOR;
					} else {
						boolean secret = (Dungeon.depth < 6 ? Random.Int(12 - Dungeon.depth) : Random.Int(6)) == 0;
						map[door] = secret ? Terrain.SECRET_DOOR : Terrain.DOOR;
						if (secret) {
							secretDoors++;
						}
					}
					break;
				case UNLOCKED:
					map[door] = Terrain.DOOR;
					break;
				case HIDDEN:
					map[door] = Terrain.SECRET_DOOR;
					break;
				case BARRICADE:
					map[door] = Random.Int(3) == 0 ? Terrain.BOOKSHELF : Terrain.BARRICADE;
					break;
				case LOCKED:
					map[door] = Terrain.LOCKED_DOOR;
					break;
			}
		}
	}

	protected boolean joinRooms(Room r, Room n) {

		if (r.type != Room.Type.STANDARD || n.type != Room.Type.STANDARD) {
			return false;
		}

		Rect w = r.intersect(n);
		if (w.left == w.right) {

			if (w.bottom - w.top < 3) {
				return false;
			}

			if (w.height() == Math.max(r.height(), n.height())) {
				return false;
			}

			if (r.width() + n.width() > maxRoomSize) {
				return false;
			}

			w.top += 1;
			w.bottom -= 0;

			w.right++;

			Painter.fill(this, w.left, w.top, 1, w.height(), Terrain.EMPTY);

		} else {

			if (w.right - w.left < 3) {
				return false;
			}

			if (w.width() == Math.max(r.width(), n.width())) {
				return false;
			}

			if (r.height() + n.height() > maxRoomSize) {
				return false;
			}

			w.left += 1;
			w.right -= 0;

			w.bottom++;

			Painter.fill(this, w.left, w.top, w.width(), 1, Terrain.EMPTY);
		}

		return true;
	}

	@Override
	public int nMobs() {
		return 3 + Dungeon.depth % 5 + Random.Int(4);
	}

	protected void placeEntranceSign() {
		while (true) {
			int pos = roomEntrance.random(this);
			if (pos != entrance) {
				Sign sign = new Sign(pos, Dungeon.tip(this));
				addLevelObject(sign);
				break;
			}
		}
	}

	protected void placeBarrels(int num) {
		for (int i = 0; i < num; i++) {
			int pos = getRandomTerrainCell(Terrain.EMPTY);
			if (cellValid(pos)) {
				addLevelObject(new Barrel(pos));
			}
		}
	}

	@Override
	public int randomRespawnCell() {
		int count = 0;
		int cell;

		while (true) {

			if (++count > 10) {
				return -1;
			}

			Room room = randomRoom(Room.Type.STANDARD, 10);
			if (room == null) {
				continue;
			}

			cell = room.random(this);
			if (!Dungeon.visible[cell] && Actor.findChar(cell) == null && passable[cell]) {
				return cell;
			}

		}
	}

	@Override
	public int randomDestination() {

		int cell;

		while (true) {

			Room room = Random.element(rooms);
			if (room == null) {
				continue;
			}

			cell = room.random(this);
			if (passable[cell]) {
				return cell;
			}

		}
	}

	@Override
	protected void createItems() {

		int nItems = 3;
		while (Random.Float() < 0.3f) {
			nItems++;
		}

		for (int i = 0; i < nItems; i++) {
			drop(Generator.random(), randomDropCell()).type = Random.chances(Heap.regularHeaps);
		}

		for (Item item : itemsToSpawn) {
			int cell = randomDropCell();
			if (item instanceof ScrollOfUpgrade) {

				while (map[cell] == Terrain.FIRE_TRAP || map[cell] == Terrain.SECRET_FIRE_TRAP) {
					cell = randomDropCell();
				}
			}

			drop(item, cell).type = Heap.Type.HEAP;
		}

		Item item = Bones.get();
		if (item != null) {
			drop(item, randomDropCell()).type = Heap.Type.SKELETON;
		}
	}

	protected Room randomRoom(Room.Type type, int tries) {
		for (int i = 0; i < tries; i++) {
			Room room = Random.element(rooms);
			if (room.type == type) {
				return room;
			}
		}
		return null;
	}

	public Room room(int pos) {
		for (Room room : rooms) {
			if (room.type != Type.NULL && room.inside(pos)) {
				return room;
			}
		}

		return null;
	}

	protected int randomDropCell() {
		while (true) {
			Room room = randomRoom(Room.Type.STANDARD, 1);
			if (room != null) {
				int pos = room.random(this);
				if (passable[pos]) {
					return pos;
				}
			}
		}
	}

	@Override
	public int pitCell() {
		for (Room room : rooms) {
			if (room.type == Type.PIT) {
				return room.random(this);
			}
		}

		return super.pitCell();
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("rooms", rooms);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

		rooms = new HashSet<>(bundle.getCollection("rooms", Room.class));
		for (Room r : rooms) {
			if (r.type == Type.WEAK_FLOOR) {
				weakFloorCreated = true;
				break;
			}
		}
	}

	protected Room getRoomExit() {
		return exitRoom(0);
	}

	protected void setRoomExit(Room roomExit) {
		exits.put(0, roomExit);
	}
}
