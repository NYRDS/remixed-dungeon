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

import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.levels.PredesignedLevel;
import com.nyrds.pixeldungeon.levels.TownShopLevel;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Regeneration;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.ElmoParticle;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ShopkeeperSprite;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndOptions;
import com.watabou.pixeldungeon.windows.WndTradeItem;
import com.watabou.utils.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class Shopkeeper extends NPC {

	{
		spriteClass = ShopkeeperSprite.class;
		movable = false;
		belongings = new Belongings(this);
		addImmunity(Regeneration.class);
	}

	private Belongings belongings;

	@Override
    public boolean act() {

		ItemUtils.throwItemAway(getPos());

		getSprite().turnTo( getPos(), Dungeon.hero.getPos() );
		spend( TICK );
		return true;
	}
	
	@Override
	public void damage(int dmg, @NotNull NamedEntityKind src ) {
		flee();
	}
	
	@Override
	public void add( Buff buff ) {
		flee();
	}
	
	private void flee() {
		for (Heap heap: Dungeon.level.allHeaps()) {
			if (heap.type == Heap.Type.FOR_SALE) {
				CellEmitter.get( heap.pos ).burst( ElmoParticle.FACTORY, 4 );
				heap.destroy();
			}
		}
		
		destroy();
		
		getSprite().killAndErase();
		CellEmitter.get( getPos() ).burst( ElmoParticle.FACTORY, 6 );
	}
	
	@Override
	public boolean reset() {
		return true;
	}


	@Override
	public void onSpawn(Level level) {
		super.onSpawn(level);

		if(level instanceof PredesignedLevel) {
			TownShopLevel.fillInventory(this);
		}
	}

	private WndBag.Listener sellItemSelector = new WndBag.Listener() {
		@Override
		public void onSelect( Item item ) {
			if (item != null) {

				if(item instanceof Bag && !((Bag)item).items.isEmpty()) {
					GameScene.selectItemFromBag(sellItemSelector, (Bag)item , WndBag.Mode.FOR_SALE, Game.getVar(R.string.Shopkeeper_Sell));
					return;
				}

				GameScene.show( new WndTradeItem( item, Shopkeeper.this, false) );
			}
		}
	};

	private WndBag.Listener buyItemSelector = item -> {
		if (item != null) {
			GameScene.show( new WndTradeItem( item, Shopkeeper.this, true) );
		}
	};

	@Override
	public boolean interact(final Char hero) {

		while(getBelongings().backpack.items.size() < getBelongings().backpack.size + 2) {
			generateNewItem();
		}

		Collections.shuffle(getBelongings().backpack.items);

		GameScene.show(new WndOptions(Utils.capitalize(getName()),
								Game.getVar(R.string.Shopkeeper_text),
								Game.getVar(R.string.Shopkeeper_SellPrompt),
								Game.getVar(R.string.Shopkeeper_BuyPrompt)){
			@Override
			protected void onSelect(int index) {
				WndBag wndBag = null;

				switch (index) {
					case 0:
						wndBag = new WndBag(hero.getBelongings(),hero.getBelongings().backpack,sellItemSelector,WndBag.Mode.FOR_SALE, Game.getVar(R.string.Shopkeeper_Sell));
						break;
					case 1:
						wndBag = new WndBag(belongings,belongings.backpack,buyItemSelector,WndBag.Mode.FOR_BUY, Game.getVar(R.string.Shopkeeper_Buy));
						break;
				}

				if(wndBag!=null) {
					GameScene.show(wndBag);
				}
			}
		});
		return true;
	}

	public void placeItemInShop(Item item) {
		if(!item.cursed && item.price() > 10 ) {
			addItem(item);
		}
	}

	public void generateNewItem()
	{
		Item newItem;
		do {
			newItem = level().getTreasury().random();
		} while (newItem instanceof Gold);

		placeItemInShop(newItem);
	}


	public void addItem(Item item) {
		if(item instanceof Bag && Dungeon.hero != null) {
			if(Dungeon.hero.getBelongings().getItem(item.getClassName())!=null) {
				return;
			}
		}
		item.collect(this);
	}

	@Override
	public Belongings getBelongings() {
		return belongings;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		belongings.storeInBundle(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		belongings.restoreFromBundle(bundle);
	}
}
