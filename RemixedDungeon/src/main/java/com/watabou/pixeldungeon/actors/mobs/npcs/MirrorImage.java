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
package com.watabou.pixeldungeon.actors.mobs.npcs;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.platform.EventCollector;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.HeroSpriteDef;

import org.jetbrains.annotations.NotNull;

public class MirrorImage extends Mob {

	public MirrorImage() {
		setState(MobAi.getStateByClass(Hunting.class));

        addImmunity( ToxicGas.class );
		addImmunity( Paralysis.class );
        addImmunity( Burning.class );
        hp(ht(1));
	}

	public MirrorImage(@NotNull Hero hero) {
		this();

		baseAttackSkill = hero.attackSkill(hero);
		baseDefenseSkill = hero.defenseSkill(hero);
		dmgMin = hero.damageRoll();
		dmgMax = dmgMin * 2;

		makePet(this, hero.getId());

		look = hero.getHeroSprite().getLayersDesc();
	}

	@Packable
	private String[] look = new String[0];
	@Packable
	private String deathEffect;


	@Override
	public int attackProc(@NotNull Char enemy, int damage ) {
		int dmg = super.attackProc( enemy, damage );

		destroy();
		getSprite().die();
		
		return dmg;
	}

	@Override
	public CharSprite newSprite() {
		if(look.length > 0 && deathEffect!=null && !deathEffect.isEmpty()) {
			return HeroSpriteDef.createHeroSpriteDef(look, deathEffect);
		} else { // first sprite generation
			if(Dungeon.hero != null) {
				look = Dungeon.hero.getHeroSprite().getLayersDesc();
				deathEffect = Dungeon.hero.getHeroSprite().getDeathEffect();
			} else { // dirty hack here
				EventCollector.logException("MirrorImage sprite created before hero");
				Hero hero = new Hero();
				HeroSpriteDef spriteDef = HeroSpriteDef.createHeroSpriteDef(hero);
				look = spriteDef.getLayersDesc();
				deathEffect = spriteDef.getDeathEffect();
			}

			return newSprite();
		}
	}
}