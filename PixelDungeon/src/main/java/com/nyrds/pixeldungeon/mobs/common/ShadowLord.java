package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.pixeldungeon.levels.com.nyrds.pixeldungeon.levels.Tools;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Shadow;
import com.watabou.pixeldungeon.actors.mobs.Wraith;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 13.02.2016
 */
public class ShadowLord extends Boss {

	private int cooldown = -1;

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
		Tools.buildSquareMaze(Dungeon.level, 4);
	}


	@Override
	protected boolean canAttack( Char enemy ) {
		return Dungeon.level.distance(getPos(), enemy.getPos()) < 4 && Ballistica.cast(getPos(), enemy.getPos(), false, true) == enemy.getPos();
	}

	@Override
	protected boolean doAttack( Char enemy ) {

		if (Dungeon.level.distance( getPos(), enemy.getPos() ) <= 1) {
			return super.doAttack(enemy);
		} else {

			getSprite().zap( enemy.getPos() );

			spend( 1 );

			if (hit(this, enemy, true)) {
				enemy.damage(damageRoll(), this);
			}
			return true;
		}
	}

	private void blink(int epos) {

		if(Dungeon.level.distance(getPos(),epos)==1) {
			int y = getPos() / Dungeon.level.getWidth();
			int x = getPos() % Dungeon.level.getWidth();

			int ey = epos / Dungeon.level.getWidth();
			int ex = epos % Dungeon.level.getWidth();

			int dx = x - ex;
			int dy = y - ey;

			x += 2*dx;
			y += 2*dy;

			final int tgt = Dungeon.level.cell(x,y);
			if(Dungeon.level.cellValid(tgt)) {
				final Char ch = this;
				fx(getPos(), new Callback() {
					@Override
					public void call() {
						WandOfBlink.appear(ch, tgt);
					}
				});
			}
		}
	}

	protected void fx( int cell, Callback callback ) {
		MagicMissile.purpleLight(getSprite().getParent(), getPos(), cell, callback);
		Sample.INSTANCE.play(Assets.SND_ZAP);
		getSprite().setVisible(false);
	}

	@Override
	public int defenseProc(Char enemy, int damage) {

		int dmg = super.defenseProc(enemy, damage);
		if (dmg > 0 && cooldown<0) {
			state = PASSIVE;
			blink(enemy.getPos());
			twistLevel();
			cooldown = 20;
		}
		return dmg;
	}

	@Override
	protected boolean act() {

		if(state==PASSIVE){
			cooldown--;
			if(cooldown<0){
				spawnWraith();
				spawnShadow();
				state=WANDERING;
				yell("Prepare yourself!");
			}
		}
		return super.act();
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
