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
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.nyrds.pixeldungeon.levels.objects.Trap;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.IZapper;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Badges.Badge;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.TomeOfMastery;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Tengu extends Boss implements IZapper {

	private static final int JUMP_DELAY = 5;
	
	public Tengu() {
		
		hp(ht(120));
		exp = 20;
		baseDefenseSkill = 20;
		baseAttackSkill  = 20;
		dmgMin = 8;
		dmgMax = 15;
		dr = 5;

		addResistance( ToxicGas.class );
		addResistance( Poison.class );

		final HeroClass heroClass = Dungeon.heroClass;

		if ( heroClass != HeroClass.NECROMANCER && heroClass != HeroClass.GNOLL){
			collect(new TomeOfMastery());
		}

		if( heroClass == HeroClass.GNOLL) {
			collect(ItemFactory.itemByName("TenguLiver"));
		}

		collect(new SkeletonKey());
	}
	
	private int timeToJump = JUMP_DELAY;

	@Override
	public void die(@NotNull NamedEntityKind cause) {
		super.die(cause);
		
		Badges.validateBossSlain(Badge.BOSS_SLAIN_2);

        say(StringsManager.getVar(R.string.Tengu_Info1));
	}
	
	@Override
	public boolean getCloser(int target) {
		if (level().fieldOfView[target]) {
			jump();
			return true;
		} else {
			return super.getCloser( target );
		}
	}
	
	@Override
    public boolean canAttack(@NotNull Char enemy) {
		return Ballistica.cast( getPos(), enemy.getPos(), false, true ) == enemy.getPos();
	}
	
	@Override
	public void doAttack(Char enemy) {
		timeToJump--;
		if (timeToJump <= 0 && adjacent(enemy)) {
			jump();
		} else {
			super.doAttack( enemy );
		}
	}
	
	private void jump() {
		timeToJump = JUMP_DELAY;

		final Level level1 = level();

		for (int i = 0; i < 4; i++) {
			int trapPos = level1.getRandomTerrain((level, cell) -> level.getTopLevelObject(cell) instanceof Trap);

			if (level1.cellValid(trapPos)) {
				Trap tr = (Trap) level1.getTopLevelObject(trapPos);
				tr.reactivate(LevelObjectsFactory.POISON_TRAP, 1);
				ScrollOfMagicMapping.discover( trapPos );
			} else {
				break;
			}
		}

		ArrayList<Integer> candidates = new ArrayList<>();
		int enemyPos = getEnemy().getPos();
		for(int i = 0; i< level1.getLength(); ++i) {
			if(level1.fieldOfView[i]
					&& level1.passable[i]
					&& !level1.adjacent( i, enemyPos )
					&& Actor.findChar(i) == null) {
				candidates.add(i);
			}
		}

		if(candidates.isEmpty()) {
			PotionOfHealing.heal(this, 0.1f);
			spend( 1 / speed() );
			return;
		}

		int newPos = candidates.get(Random.index(candidates));

		getSprite().move( getPos(), newPos );
		move( newPos );
		
		if (CharUtils.isVisible(this)) {
			CellEmitter.get( newPos ).burst( Speck.factory( Speck.WOOL ), 6 );
			Sample.INSTANCE.play( Assets.SND_PUFF );
		}
		
		spend( 1 / speed() );
	}
	
	@Override
	public void notice() {
		super.notice();
        String tenguYell = StringsManager.getVar(R.string.Tengu_Info2);
		final Hero hero = Dungeon.hero;
		final HeroClass heroClass = hero.getHeroClass();

		if (heroClass.getGender() == Utils.FEMININE) {
            tenguYell = StringsManager.getVar(R.string.Tengu_Info3);
		}
		yell(Utils.format(tenguYell, heroClass.title()));
	}	
}
