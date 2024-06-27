package com.nyrds.pixeldungeon.spiders.levels;

import com.nyrds.pixeldungeon.items.Treasury;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.plants.Seed;
import com.watabou.utils.Random;

public class Chamber {
	final int x;
	final int y;
	final int r;

	int shape;
	int interior;

	public Chamber(int x, int y, int r) {
		this.x = x;
		this.y = y;
		this.r = r;
		
		this.shape = Random.IntRange(0, 5);
		this.interior = Random.IntRange(0, 2);
	}
	
	public void setShape(int shape) {
		this.shape = shape;
	}
	
	public void setInterior(int interior) {
		this.interior = interior;
	}
	
	public boolean digChamber(Level level, boolean realyDig) {
		
		for (int i = -r; i < r + 1; ++i) {
			for (int j = -r; j < r + 1; ++j) {
				if (level.cellValid(x + i, y + j)) {

					boolean empty = false;
					switch (shape) {
					case 0:
						empty = Math.abs(i - j) < r;
						break;
						
					case 1:
						empty = Math.abs(j - i) < r;
						break;
						
					case 2:
						empty = Math.abs(i + j) < r;
						break;

					case 3:
						empty = Math.abs(i) + Math.abs(j) < r;
						break;

					case 4:
						empty = Math.abs(i * j) < r;
						break;
						
					case 5:
						empty = i*i + j*j < r*r;
						break;
					}
					
					if (empty) {
						int cellId = level.cell(x + i, y + j);
						
						if(realyDig) {
							decorateCell(level, cellId);
						}else {
							if(level.map[cellId] != Terrain.WALL) {
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}

	private void decorateCell(Level level, int cellId) {

		int[] map = level.map;

		map[cellId] = Terrain.EMPTY;
		
		switch(interior){
			case 0:		//simple cave
				switch(Random.Int(3)){
				case 0:
					map[cellId] = Terrain.WATER;
				break;
				case 1:
					map[cellId] = Terrain.HIGH_GRASS;
				break;
				}
			break;
			
			case 1:		//garden
				map[cellId] = Terrain.HIGH_GRASS;
				if(Random.Int(5)==0) {
					Item seedCandidate = Treasury.getLevelTreasury().random(Treasury.Category.SEED);
					if(seedCandidate instanceof Seed) {
						level.plant((Seed) seedCandidate, cellId);
					}
				}
			break;
			
			case 2:		//water
				map[cellId] = Terrain.WATER;
				if(Random.Int(5)==0) {
					map[cellId] = Terrain.GRASS;
				}
			break;
		}
	}

	
}
