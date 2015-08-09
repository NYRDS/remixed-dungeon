package com.nyrds.pixeldungeon.spiders.levels;

import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
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
	}
	
	public void setShape(int shape) {
		this.shape = shape;
	}
	
	public void setInterior(int interior) {
		this.interior = interior;
	}
	
	public boolean digChamber(Level level, boolean realyDig) {
		
		int k = r;

		for (int i = -k; i < k + 1; ++i) {
			for (int j = -k; j < k + 1; ++j) {
				if (level.cellValid(x + i, y + j)) {

					boolean empty = false;
					switch (shape) {
					case 0:
						empty = Math.abs(i - j) < k;
						break;
						
					case 1:
						empty = Math.abs(j - i) < k;
						break;
						
					case 2:
						empty = Math.abs(i + j) < k;
						break;

					case 3:
						empty = Math.abs(i) + Math.abs(j) < k;
						break;

					case 4:
						empty = Math.abs(i * j) < k;
						break;
						
					case 5:
						empty = i*i + j*j < k*k;
						break;
					}

					if (empty) {
						int cellId = level.cell(x + i, y + j);
						
						if(realyDig) {
							level.map[cellId] = Terrain.EMPTY;
							
							switch(Random.Int(3)){
							case 0:
								level.map[cellId] = Terrain.WATER;
							break;
							case 1:
								level.map[cellId] = Terrain.HIGH_GRASS;
							break;
							}
						}else {
							if(level.map[cellId]==Terrain.EMPTY) {
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}

	
}
