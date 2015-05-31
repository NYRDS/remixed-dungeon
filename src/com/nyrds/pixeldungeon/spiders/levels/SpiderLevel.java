package com.nyrds.pixeldungeon.spiders.levels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Bones;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

public class SpiderLevel extends Level {

	{
		color1 = 0x801500;
		color2 = 0xa68521;
	}
	
	@Override
	public String tilesTex() {
		return Assets.TILES_HALLS;
	}
	
	@Override
	public String waterTex() {
		return Assets.WATER_HALLS;
	}
	
	List<Chamber> chambers = new ArrayList<Chamber>();
	
	protected void createChambers() {
		
		chambers.add(new Chamber(Level.getWidth()/2, Level.getWidth()/2, 2 ));
		for(int i = 0;i<6; ++i) {
			
			int cx = Random.Int(1,Level.getWidth()-1);
			int cy = Random.Int(1,Level.getWidth()-1);
			
			chambers.add(new Chamber ( cx, cy, 1));
		}
		
		digChamber(chambers.get(0));
		
		for( int i = 1; i < chambers.size() ; i++ ) {
			digChamber(chambers.get(i));
			connectChambers(chambers.get(i-1), chambers.get(i));
		}
	}
	
	private void digChamber(Chamber chamber) {
		int k = chamber.r;
		
		for (int i = -k; i < k + 1; ++i) {
			for (int j = -k; j < k + 1; ++j) {
				if( cellValid(chamber.x + i, chamber.y + j) ) {
					if(Math.abs(i*j) < k*k) {
						map[cell(chamber.x + i, chamber.y + j)] = Terrain.EMPTY;
					}
				}
			}
		}
		
	}

	private void connectChambers(Chamber a, Chamber b) {
		int x = a.x;
		int y = a.y;
		
		while( x != b.x || y != b.y ) {
			int dx = (int) Math.signum(x - b.x);
			int dy = (int) Math.signum(y - b.y);
			
			if(cellValid(x,y)) {
				map[cell(x,y)] = Terrain.EMPTY;
			}
			
			if(dx != 0 && map[cell(x - dx, y)] == Terrain.EMPTY) {
				x = x - dx;
				continue;
			}
			
			if(dy != 0 && map[cell(x,y-dy)] == Terrain.EMPTY) {
				y = y - dy;
				continue;
			}
			
			switch( Random.Int(10) ) {
			case 0:
			case 1:
			case 2:
			case 3:
				if( dx != 0 && cellValid(x - dx, y)) {
					x -= dx;
				}
				break;
			case 4:
			case 5:
			case 6:
			case 7:
				if( dy != 0 && cellValid(x, y - dy)) {
					y -= dy;
				}
				break;
			case 8:
				if( dx != 0 && cellValid(x + dx, y)) {
					x += dx;
				}
				break;
			case 9:
				if( dy != 0 && cellValid(x, y + dy)) {
					y += dy;
				}
				break;
			}
		}
	}
	
	@Override
	protected boolean build() {
		Arrays.fill( map, Terrain.WALL );
		
		createChambers();
		
		Chamber ent = chambers.get(chambers.size()-1);
		entrance = cell(ent.x, ent.y);
		map[entrance] = Terrain.ENTRANCE;
		
		Chamber ext = chambers.get(chambers.size()-2);
		exit = cell(ext.x, ext.y);
		map[exit] = Terrain.EXIT;

		feeling = Feeling.NONE;
		
		return true;
	}

	@Override
	protected void decorate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int nMobs() {
		return 2 + Dungeon.depth % 5 + Random.Int( 3 );
	}
	
	@Override
	protected void createMobs() {
		int nMobs = nMobs();
		for (int i=0; i < nMobs; i++) {
			Mob mob = createMob();
			mobs.add( mob );
			Actor.occupyCell( mob );
		}
	}
	
	@Override
	protected void createItems() {
		
		int nItems = 3;
		while (Random.Float() < 0.3f) {
			nItems++;
		}
		
		for (int i=0; i < nItems; i++) {
			Heap.Type type = Heap.Type.SKELETON;
			drop( Generator.random(), randomRespawnCell() ).type = type;
		}

		for (Item item : itemsToSpawn) {
			int cell = randomRespawnCell();
			if (item instanceof ScrollOfUpgrade) {

				while (map[cell] == Terrain.FIRE_TRAP || map[cell] == Terrain.SECRET_FIRE_TRAP) {
					cell = randomRespawnCell();
				}
			}

			drop( item, cell ).type = Heap.Type.HEAP;
		}
		
		Item item = Bones.get();
		if (item != null) {
			drop( item, randomRespawnCell() ).type = Heap.Type.SKELETON;
		}
	}
}
