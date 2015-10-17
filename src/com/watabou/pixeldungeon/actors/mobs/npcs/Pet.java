package com.watabou.pixeldungeon.actors.mobs.npcs;

import java.util.HashSet;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.utils.Random;

public class Pet<MobType extends Mob> extends NPC {
	
	private MobType mob;
	
	public Pet(MobType mob) {
		state = HUNTING;
		enemy = DUMMY;
		
		this.mob = mob;
	}

	@Override
	public CharSprite sprite() {
		return mob.sprite();
	}
	
	@Override
	public int defenseSkill(Char enemy) {
		return mob.defenseSkill(enemy);
	};
	
	@Override
	public int attackSkill( Char target ) {
		return mob.attackSkill(target);
	}
	
	@Override
	public int damageRoll() {
		return mob.damageRoll();
	}
	
	@Override
	public int defenseProc(Char enemy, int damage) {
		return mob.defenseProc(enemy, damage);
	}
	
	@Override
	public int attackProc( Char enemy, int damage ) {
		return mob.attackProc(enemy, damage);
	}
	
	protected Char chooseEnemy() {
		
		if (enemy == DUMMY || !enemy.isAlive()) {
			HashSet<Mob> enemies = new HashSet<Mob>();
			for (Mob mob:Dungeon.level.mobs) {
				if (mob.hostile && Dungeon.level.fieldOfView[mob.pos]) {
					enemies.add( mob );
				}
			}
			
			enemy = enemies.size() > 0 ? Random.element( enemies ) : DUMMY;
		}
		
		return enemy;
	}
	
	@Override
	public boolean interact(final Hero hero) {
		
		int curPos = pos;
		
		moveSprite( pos, hero.pos );
		move( hero.pos );
		
		hero.getSprite().move( hero.pos, curPos );
		hero.move( curPos );
		
		hero.spend( 1 / hero.speed() );
		hero.busy();
		
		return true;
	}
	
	@Override
	public String description() {
		return mob.description();
	}
	
	@Override
	public String getName() {
		return mob.getName();
	}
	
	@Override
	public int getGender() {
		return mob.getGender();
	}
}