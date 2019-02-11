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
package com.watabou.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.levels.TownShopLevel;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.VBox;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.rings.RingOfHaggler;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.ItemSlot;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import androidx.annotation.NonNull;

public class WndTradeItem extends Window {

	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 18;
	
	private WndBag owner;

	private VBox vbox = new VBox();

	public WndTradeItem( final Item item, WndBag owner ) {
		
		super();
		
		this.owner = owner; 
		
		float pos = createDescription( item, false );

		add(vbox);

		if (item.quantity() == 1) {
			
			RedButton btnSell = new RedButton( Utils.format( Game.getVar(R.string.WndTradeItem_Sell), item.price() ) ) {
				@Override
				protected void onClick() {
					sell( item );
					hide();
				}
			};
			btnSell.setSize( WIDTH, BTN_HEIGHT );
			vbox.add( btnSell );

		} else {
			
			int priceAll= item.price();
			RedButton btnSell1 = new RedButton( Utils.format( Game.getVar(R.string.WndTradeItem_Sell1), priceAll / item.quantity() ) ) {
				@Override
				protected void onClick() {
					sellOne( item );
					hide();
				}
			};
			btnSell1.setSize(WIDTH, BTN_HEIGHT );
			vbox.add( btnSell1 );
			RedButton btnSellAll = new RedButton( Utils.format( Game.getVar(R.string.WndTradeItem_SellAll), priceAll ) ) {
				@Override
				protected void onClick() {
					sell( item );
					hide();
				}
			};
			btnSellAll.setSize(WIDTH, BTN_HEIGHT );
			vbox.add( btnSellAll );
		}
		
		RedButton btnCancel = new RedButton( Game.getVar(R.string.WndTradeItem_Cancel) ) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		btnCancel.setSize( WIDTH, BTN_HEIGHT );
		vbox.add( btnCancel );

		vbox.setPos(0, pos+GAP);

		resize( WIDTH, (int) vbox.bottom());
	}
	
	public WndTradeItem( final Heap heap, boolean canBuy ) {
		
		super();
		
		Item item = heap.peek();
		
		float pos = createDescription( item, true );

		add(vbox);

		int price = price( item );
		
		if (canBuy) {
			
			RedButton btnBuy = new RedButton( Utils.format( Game.getVar(R.string.WndTradeItem_Buy), price ) ) {
				@Override
				protected void onClick() {
					hide();
					buy( heap );
				}
			};
			btnBuy.setSize(WIDTH, BTN_HEIGHT );
			btnBuy.enable( price <= Dungeon.hero.gold());
			vbox.add( btnBuy );
			
			RedButton btnCancel = new RedButton( Game.getVar(R.string.WndTradeItem_Cancel) ) {
				@Override
				protected void onClick() {
					hide();
				}
			};
			btnCancel.setSize( WIDTH, BTN_HEIGHT );
			vbox.add( btnCancel );
		}

		vbox.setPos(0, pos+GAP);
		resize( WIDTH, (int)vbox.bottom());
	}

	public WndTradeItem(final Item item, boolean canBuy) {
		float pos = createDescription( item, true );

		add(vbox);

		int price = price( item );

		if (canBuy) {

			RedButton btnBuy = new RedButton( Utils.format( Game.getVar(R.string.WndTradeItem_Buy), price ) ) {
				@Override
				protected void onClick() {
					hide();
					buy( item );
				}
			};
			btnBuy.setSize(WIDTH, BTN_HEIGHT );
			btnBuy.enable( price <= Dungeon.hero.gold());
			vbox.add( btnBuy );

			RedButton btnCancel = new RedButton( Game.getVar(R.string.WndTradeItem_Cancel) ) {
				@Override
				protected void onClick() {
					hide();
				}
			};
			btnCancel.setSize( WIDTH, BTN_HEIGHT );
			vbox.add( btnCancel );
		}

		vbox.setPos(0, pos+GAP);
		resize( WIDTH, (int)vbox.bottom());

	}

	@Override
	public void hide() {
		
		super.hide();
		
		if (owner != null) {
			owner.hide();
			Shopkeeper.sell();
		}
	}
	
	private float createDescription( Item item, boolean forSale ) {
		
		// Title
		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( item ) );
		titlebar.label( forSale ? 
			Utils.format( Game.getVar(R.string.WndTradeItem_Sale), item.toString(), price( item ) ) :
			Utils.capitalize( item.toString() ) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );
		
		// Upgraded / degraded
		if (item.levelKnown && item.level() > 0) {
			titlebar.color( ItemSlot.UPGRADED );	
		} else if (item.levelKnown && item.level() < 0) {
			titlebar.color( ItemSlot.DEGRADED );	
		}
		
		// Description
		Text info = PixelScene.createMultiline( item.info(), GuiProperties.regularFontSize() );
		info.maxWidth(WIDTH);
		info.x = titlebar.left();
		info.y = titlebar.bottom() + GAP;
		add( info );
		
		return info.y + info.height();
	}
	
	private void sell( Item item ) {
		
		Hero hero = Dungeon.hero;
		
		if (item.isEquipped( hero ) && !((EquipableItem)item).doUnequip( hero, false )) {
			return;
		}
		item.detachAll( hero.belongings.backpack );
		
		int price = item.price();
		
		new Gold( price ).doPickUp( hero );
		GLog.i( Game.getVar(R.string.WndTradeItem_Sold), item.name(), price );

		placeItemInShop(item);
	}

	private void placeItemInShop(Item item) {
		if (Dungeon.level instanceof TownShopLevel) {
			if(!item.cursed && item.price() > 10 ) {
				TownShopLevel shopLevel = (TownShopLevel) Dungeon.level;
				shopLevel.itemForSell(item);
			}
		}
	}

	private void sellOne( @NonNull Item  item ) {
		
		if (item.quantity() <= 1) {
			sell( item );
		} else {
			
			Hero hero = Dungeon.hero;
			
			item = item.detach( hero.belongings.backpack );
			int price = item.price();
			
			new Gold( price ).doPickUp( hero );
			GLog.i( Game.getVar(R.string.WndTradeItem_Sold), item.name(), price );

			placeItemInShop(item);
		}
	}
	
	private int price( @NonNull  Item item ) {
		// This formula is not completely correct...
		int price = item.price() * 5 * (Dungeon.depth / 5 + 1);
		if (Dungeon.hero.hasBuff( RingOfHaggler.Haggling.class ) && price >= 2) {
			price /= 2;
		}
		return price;
	}

	private void buy( Item item ) {

		Hero hero = Dungeon.hero;

		int price = price( item );
		hero.spendGold(price);

		GLog.i( Game.getVar(R.string.WndTradeItem_Bought), item.name(), price );

		if (!item.doPickUp( hero )) {
			Dungeon.level.drop( item, hero.getPos() ).sprite.drop();
		}

		Item newItem;
		do {
			newItem = Generator.random();
		} while (newItem instanceof Gold);

		placeItemInShop(newItem);
	}

	private void buy( Heap heap ) {
		
		Hero hero = Dungeon.hero;
		Item item = heap.pickUp();
		
		int price = price( item );
		hero.spendGold(price);

		GLog.i( Game.getVar(R.string.WndTradeItem_Bought), item.name(), price );
		
		if (!item.doPickUp( hero )) {
			Dungeon.level.drop( item, heap.pos ).sprite.drop();
		}

		Item newItem;
		do {
			newItem = Generator.random();
		} while (newItem instanceof Gold);

		placeItemInShop(newItem);
	}
}
