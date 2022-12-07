package com.nyrds.retrodungeon.spiders.levels;

import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.plants.Plant.Seed;
import com.watabou.utils.Random;

public class Chamber {
	int x;
	int y;
	int r;

	int shape    = 0;
	int interior = 0;

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
		
		level.map[cellId] = Terrain.EMPTY;
		
		switch(interior){
			case 0:		//simple cave
				switch(Random.Int(3)){
				case 0:
					level.map[cellId] = Terrain.WATER;
				break;
				case 1:
					level.map[cellId] = Terrain.HIGH_GRASS;
				break;
				}
			break;
			
			case 1:		//garden
				level.map[cellId] = Terrain.HIGH_GRASS;
				if(Random.Int(5)==0) {
					level.plant( (Seed)Generator.random(Generator.Category.SEED), cellId);
				}
			break;
			
			case 2:		//water
				level.map[cellId] = Terrain.WATER;
				if(Random.Int(5)==0) {
					level.map[cellId] = Terrain.GRASS;
				}
			break;
		}
	}

	
}
