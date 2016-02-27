package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.pixeldungeon.levels.com.nyrds.pixeldungeon.levels.Tools;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Shadow;
import com.watabou.pixeldungeon.actors.mobs.Wraith;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 13.02.2016
 */
public class ShadowLord extends Boss {

	public ShadowLord() {
		hp(ht(260));
		defenseSkill = 34;

		EXP = 56;

		lootChance = 0.5f;
	}

	@Override
	public boolean isAbsoluteWalker() {
		return true;
	}

	public void spawnShadow() {
		int cell = Dungeon.level.getSolidCellNextTo(getPos());

		if (cell != -1) {
			Mob mob = new Shadow();

			mob.state = mob.WANDERING;
			GameScene.add(Dungeon.level, mob, 2);
			WandOfBlink.appear(mob, cell);
		}
	}

	public void spawnWraith() {
		int cell = Dungeon.level.getEmptyCellNextTo(getPos());

		if (cell != -1) {
			Mob mob = new Wraith();

			mob.state = mob.WANDERING;
			GameScene.add(Dungeon.level, mob, 2);
			WandOfBlink.appear(mob, cell);
		}
	}

	public void twistLevel() {
		Tools.buildSquareMaze(Dungeon.level,4);
		return;
		/*
		for (int i = -5; i <= 5; i++) {
			for (int j = -5; j <= 5; j++) {
				if (i * i + j * j <= 25 && Math.random() < 0.25f) {
					int x = getPos() % Dungeon.level.getWidth();
					int y = getPos() / Dungeon.level.getHeight();
					x += i;
					y += j;
					if (Dungeon.level.cellValid(x, y)) {
						int cell = Dungeon.level.cell(x, y);
						if (Actor.findChar(cell) == null) {
							if ((TerrainFlags.flags[Dungeon.level.map[cell]] & TerrainFlags.PASSABLE) != 0) {

								Dungeon.level.set(cell, Terrain.WALL);
								if (Math.random() < 0.1f) {
									Dungeon.level.set(cell, Terrain.WALL_DECO);
								}
								GameScene.updateMap(cell);
								CellEmitter.get(cell).start(FlameParticle.FACTORY, 0.1f, 3);
								continue;
							}

							if ((TerrainFlags.flags[Dungeon.level.map[cell]] & TerrainFlags.SOLID) != 0) {
								Dungeon.level.set(cell, Terrain.EMPTY);
								if (Math.random() < 0.1f) {
									Dungeon.level.set(cell, Terrain.EMPTY_DECO);
								}
								GameScene.updateMap(cell);
								CellEmitter.get(cell).start(FlameParticle.FACTORY, 0.1f, 3);
							}
						}
					}
				}
			}
		}
		*/
	}

	@Override
	public void move(int step) {
		super.move(step);

		//if (Math.random() < 0.1f) {
			twistLevel();
		//	return;
		//}
	}


	@Override
	protected boolean canAttack( Char enemy ) {

		return Dungeon.level.distance(getPos(), enemy.getPos()) < 4 && Ballistica.cast(getPos(), enemy.getPos(), false, true) == enemy.getPos();
	}

	@Override
	protected boolean doAttack( Char enemy ) {

		if (Math.random() < 0.1f) {
			spawnShadow();
			return true;
		}

		if (Math.random() < 0.1f) {
			spawnWraith();
			return true;
		}

		if (Dungeon.level.distance( getPos(), enemy.getPos() ) <= 1) {
			return super.doAttack( enemy );
		} else {

			getSprite().zap( enemy.getPos() );

			spend( 1 );

			if (hit( this, enemy, true )) {
				enemy.damage(damageRoll(), this);
			}
			return true;
		}
	}


	@Override
	public int damageRoll() {
		return Random.NormalIntRange(1, 2);
	}

	@Override
	public int attackSkill(Char target) {
		return 1;
	}

	@Override
	public int dr() {
		return 2;
	}

}
