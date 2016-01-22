package com.nyrds.pixeldungeon.spiders.levels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nyrds.pixeldungeon.mobs.spiders.SpiderSpawner;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Bones;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.pixeldungeon.levels.CommonLevel;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

public class SpiderLevel extends CommonLevel {
	
	public SpiderLevel() {
		color1 = 0x48763c;
		color2 = 0x59994a;
	}

	@Override
	public String tilesTex() {
		return Assets.TILES_SPIDER_NEST;
	}

	@Override
	public String waterTex() {
		return Assets.WATER_SPIDERS;
	}

	List<Chamber> chambers = new ArrayList<Chamber>();

	protected void createChambers() {

		for (int i = 0; i < (Dungeon.depth - 4) * 2; ++i) {

			Chamber chamber;
			boolean canDig;
			do{
				int cx = Random.Int(1, getWidth() - 1);
				int cy = Random.Int(1, getHeight() - 1);
				chamber = new Chamber(cx, cy, Random.IntRange(2, Dungeon.depth/2));
				chamber.setShape(Random.IntRange(0, 5));
				canDig = chamber.digChamber(this,false);
			} while(!canDig);
			
			chamber.digChamber(this, true);
			
			chambers.add(chamber);
		}
		if(Math.random() > 0.5) {
			for (int i = 1; i < chambers.size(); ++i) {
				connectChambers(chambers.get(i-1), chambers.get(i));
			}
		} else {
			for (int i = 1; i < chambers.size(); ++i) {
				connectChambers(chambers.get(0), chambers.get(i));
			}
		}
	}

	private boolean isCellIs(int x, int y, int type) {
		if(!cellValid(x,y)) {
			return false;
		}
		
		if(map[cell(x,y)] == type) {
			return true;
		}
		
		return false;
	}
	
	private void connectChambers(Chamber a, Chamber b) {
		int x = a.x;
		int y = a.y;

		while (x != b.x || y != b.y) {
			int dx = (int) Math.signum(x - b.x);
			int dy = (int) Math.signum(y - b.y);
			
			if (isCellIs(x,y, Terrain.WALL) ) {
				map[cell(x, y)] = Terrain.EMPTY;
			}

			if (dx != 0 && isCellIs(x-dx,y,Terrain.EMPTY)) {
				x = x - dx;
				continue;
			}

			if (dy != 0 && isCellIs(x,y-dy,Terrain.EMPTY)) {
				y = y - dy;
				continue;
			}

			switch (Random.Int(10)) {
			case 0:
			case 1:
			case 2:
			case 3:
				if (dx != 0) {
					x -= dx;
				}
				break;
			case 4:
			case 5:
			case 6:
			case 7:
				if (dy != 0) {
					y -= dy;
				}
				break;
			case 8:
				if (dx != 0) {
					x += dx;
				}
				break;
			case 9:
				if (dy != 0) {
					y += dy;
				}
				break;
			}
		}
	}
	
	@Override
	protected boolean build() {
		Arrays.fill(map, Terrain.WALL);

		createChambers();

		Chamber ent = chambers.get(0);
		entrance = cell(ent.x, ent.y);
		map[entrance] = Terrain.ENTRANCE;

		if(Dungeon.depth != 10) {
			Chamber ext = chambers.get(chambers.size() - 1);
			exit = cell(ext.x, ext.y);
			map[exit] = Terrain.EXIT;
		}

		feeling = Feeling.NONE;
		placeTraps();
		return true;
	}

	protected int nTraps() {
		return Random.IntRange(Dungeon.depth, (int) (Math.sqrt(getLength()) + Dungeon.depth) );
	}
	
	@Override
	protected void decorate() {
		// TODO Auto-generated method stub
	}

	@Override
	public int nMobs() {
		return 0;
	}

	@Override
	protected void createMobs() {
		
		int pos = randomRespawnCell();
		
		for (int i = 0; i< Dungeon.depth * 2; i++) {
			while(Actor.findChar(pos) != null) {
				pos = randomRespawnCell();
			}
			SpiderSpawner.spawnEgg(this, pos);
		}
		
		for (int i = 0; i< Dungeon.depth / 2; i++) {
			while(Actor.findChar(pos) != null) {
				pos = randomRespawnCell();
			}
			SpiderSpawner.spawnNest(this, pos);
		}
		
		if(Dungeon.depth == 10) {
			SpiderSpawner.spawnQueen(this, randomRespawnCell());
		}

	}

	@Override
	protected void createItems() {

		int nItems = 3;
		while (Random.Float() < 0.3f) {
			nItems++;
		}

		for (int i = 0; i < nItems; i++) {
			Heap.Type type = Heap.Type.SKELETON;
			drop(Generator.random(), randomRespawnCell()).type = type;
		}

		for (Item item : itemsToSpawn) {
			int cell = randomRespawnCell();
			if (item instanceof ScrollOfUpgrade) {

				while (map[cell] == Terrain.FIRE_TRAP
						|| map[cell] == Terrain.SECRET_FIRE_TRAP) {
					cell = randomRespawnCell();
				}
			}

			drop(item, cell).type = Heap.Type.HEAP;
		}

		Item item = Bones.get();
		if (item != null) {
			drop(item, randomRespawnCell()).type = Heap.Type.SKELETON;
		}
	}

}
