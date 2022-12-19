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

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.windows.WndShopOptions;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Regeneration;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.ElmoParticle;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.items.food.OverpricedRation;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ShopkeeperSprite;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;



public class Shopkeeper extends NPC {

	{
		spriteClass = ShopkeeperSprite.class;
		movable = false;
		addImmunity(Regeneration.class);
	}

	public static int countFood(Bag backpack) {
        int ret = 0;

        for (Item item : backpack) {
            if (item instanceof Food) {
                ret+=item.quantity();
            }
        }
        return ret;
    }

    @Override
    public boolean act() {

		ItemUtils.throwItemAway(getPos());

		getSprite().turnTo( getPos(), Dungeon.hero.getPos() );
		spend( TICK );
		return true;
	}
	
	@Override
	public void damage(int dmg, @NotNull NamedEntityKind src ) {
		destroy();

		getSprite().killAndErase();
		CellEmitter.get( getPos() ).burst( ElmoParticle.FACTORY, 6 );
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public boolean interact(final Char hero) {

		int attempts = 0;

		final Bag backpack = getBelongings().backpack;

		if(ModdingMode.inRemixed() && GameLoop.getDifficulty() < 2) {
			if (countFood(backpack) < 3) {
				var foodSupply = new OverpricedRation();
				foodSupply.quantity(5);
				collect(foodSupply);
			}
		}

		while(backpack.items.size() < backpack.getSize() + 2 && attempts < 100) {
			CharUtils.generateNewItem(this);
			attempts++;
		}

		Collections.shuffle(backpack.items);

		GameScene.show(
				new WndShopOptions(this, hero));
		return true;
	}


	@Override
	public boolean collect(@NotNull Item item) {
		final Hero hero = Dungeon.hero;

		if(item instanceof Bag && hero != null) {
			if(Dungeon.hero.getItem(item.getEntityKind()).valid()) {
				return false;
			}
		}

		if(level()!=null) {
			item = Treasury.getLevelTreasury().check(item);
		} else {
			item = Treasury.get().check(item);
		}

		item.collect(this);

		return true;
	}

	@Override
	public boolean useBags() {
		return false;
	}

}
