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
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.VBox;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.rings.RingOfHaggler;
import com.watabou.pixeldungeon.scenes.GameScene;
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

	private VBox vbox = new VBox();

	@NonNull
	private Shopkeeper shopkeeper;
	private Hero       customer;

	private static final int[] tradeQuantity = {1, 5, 10, 50, 100, 500, 1000};

	public WndTradeItem(final Item item, @NonNull Shopkeeper shopkeeper, boolean buy) {

		super();

		this.shopkeeper = shopkeeper;
		this.customer   = Dungeon.hero;

		add(vbox);

		if(buy) {
			makeBuyWnd(item);
		} else {
			makeSellWnd(item);
		}
	}

	@Deprecated
	public WndTradeItem(final Heap heap, boolean canBuy) {
		
		super();

		this.customer   = Dungeon.hero;

		Item item = heap.peek();
		
		float pos = createDescription( item, true );

		add(vbox);

		int price = price( item, true );
		
		if (canBuy) {
			
			RedButton btnBuy = new RedButton( Utils.format( Game.getVar(R.string.WndTradeItem_Buy), price ) ) {
				@Override
				protected void onClick() {
					hide();
					buy( heap );
				}
			};
			btnBuy.setSize(WIDTH, BTN_HEIGHT );
			btnBuy.enable( price <= customer.gold());
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

	private void makeSellWnd(final Item item) {
		float pos = createDescription( item, false);

		vbox.clear();
		int priceAll = price( item, false);

		if (item.quantity() == 1) {

			RedButton btn = new RedButton( Utils.format( Game.getVar(R.string.WndTradeItem_Sell), priceAll ) ) {
				@Override
				protected void onClick() {
					sell( item,1 );
				}
			};
			btn.setSize( WIDTH, BTN_HEIGHT );
			vbox.add( btn );

		} else {

			for (int i = 0;i< tradeQuantity.length;++i) {
				if (item.quantity() > tradeQuantity[i]) {
					final int finalI = i;
					final int priceFor = priceAll/item.quantity() * tradeQuantity[i];
					RedButton btnSellN = new RedButton(Utils.format(Game.getVar(R.string.WndTradeItem_SellN),
							tradeQuantity[finalI],
							priceFor))
					{
						@Override
						protected void onClick() {
							sell(item, tradeQuantity[finalI]);
						}
					};
					btnSellN.setSize(WIDTH, BTN_HEIGHT);
					vbox.add(btnSellN);
				}
			}

			RedButton btnSellAll = new RedButton( Utils.format( Game.getVar(R.string.WndTradeItem_SellAll), priceAll ) ) {
				@Override
				protected void onClick() {
					sell( item, item.quantity() );
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

	private void makeBuyWnd(final Item item) {
		float pos = createDescription( item, true);

		vbox.clear();

		int priceAll = price( item, true );

		if (item.quantity() == 1) {

			RedButton btnBuy = new RedButton( Utils.format( Game.getVar(R.string.WndTradeItem_Buy), priceAll ) ) {
				@Override
				protected void onClick() {
					buy( item,1 );
				}
			};
			btnBuy.setSize( WIDTH, BTN_HEIGHT );
			btnBuy.enable(priceAll<=customer.gold());
			vbox.add( btnBuy );

		} else {
			for (int i = 0; i < tradeQuantity.length; ++i) {
				if (item.quantity() > tradeQuantity[i]) {
					final int priceFor = priceAll / item.quantity() * tradeQuantity[i];
					final int finalI = i;
					RedButton btnBuyN = new RedButton(Utils.format(Game.getVar(R.string.WndTradeItem_BuyN),
							tradeQuantity[finalI],
							priceFor)) {
						@Override
						protected void onClick() {
							buy(item, tradeQuantity[finalI]);
						}
					};
					btnBuyN.enable(priceFor <= customer.gold());
					btnBuyN.setSize(WIDTH, BTN_HEIGHT);
					vbox.add(btnBuyN);
				}
			}

			RedButton btnBuyAll = new RedButton(Utils.format(Game.getVar(R.string.WndTradeItem_BuyAll), priceAll)) {
				@Override
				protected void onClick() {
					buy(item, item.quantity());
				}
			};

			btnBuyAll.setSize(WIDTH, BTN_HEIGHT);
			btnBuyAll.enable(priceAll <= customer.gold());
			vbox.add(btnBuyAll);
		}
		RedButton btnCancel = new RedButton(Game.getVar(R.string.WndTradeItem_Cancel)) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		btnCancel.setSize(WIDTH, BTN_HEIGHT);
		vbox.add(btnCancel);
		vbox.setPos(0, pos+GAP);

		resize( WIDTH, (int)vbox.bottom());
	}

	private float createDescription( Item item, boolean buying ) {
		
		// Title
		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( item ) );
		titlebar.label( buying ?
			Utils.format( Game.getVar(R.string.WndTradeItem_Sale), item.toString(), price( item, true ) ) :
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

	private int price( @NonNull  Item item, boolean buying) {
		int price = item.price();
		if(buying) {
			price *=  5 * (Dungeon.depth / 5 + 1);
			if (Dungeon.hero.hasBuff(RingOfHaggler.Haggling.class) && price >= 2) {
				price /= 2;
			}
		}
		else {
			if(Dungeon.hero.hasBuff(RingOfHaggler.Haggling.class)) {
				price *= 1.5f;
			}
		}
		return price;
	}

	private void buy( @NonNull Item item, final int quantity ) {
		item = item.detach(shopkeeper.getBelongings().backpack, quantity);

		int price = price( item, true );
		customer.spendGold(price);

		GLog.i( Game.getVar(R.string.WndTradeItem_Bought), item.name(), price );

		if (!item.doPickUp( customer )) {
			Dungeon.level.drop( item, customer.getPos() ).sprite.drop();
		}

		Item leftover = shopkeeper.getBelongings().getItem(item.getClassName());
		if(leftover != null) {
			hide();
			GameScene.show(new WndTradeItem(leftover,shopkeeper,true));
		} else {
			shopkeeper.generateNewItem();
			hide();
		}
	}

	private void sell( Item item, final int quantity) {

		if (item.isEquipped( customer ) && !((EquipableItem)item).doUnequip( customer, false )) {
			hide();
			return;
		}

		item = item.detach( customer.getBelongings().backpack, quantity );
		shopkeeper.placeItemInShop(item);
		hide();

		int price = price(item, false);

		new Gold( price ).doPickUp( customer );
		GLog.i( Game.getVar(R.string.WndTradeItem_Sold), item.name(), price );

		Item leftover = customer.getBelongings().getItem(item.getClassName());
		if(leftover != null && leftover.quantity() > 0) {
			GameScene.show(new WndTradeItem(leftover,shopkeeper,false));
		}

	}

	@Deprecated
	private void buy( Heap heap ) {
		
		Hero hero = Dungeon.hero;
		Item item = heap.pickUp();
		
		int price = price( item, true);
		hero.spendGold(price);

		GLog.i( Game.getVar(R.string.WndTradeItem_Bought), item.name(), price );
		
		if (!item.doPickUp( hero )) {
			Dungeon.level.drop( item, heap.pos ).sprite.drop();
		}
	}

	@Override
	public void hide() {
		super.hide();
		if(WndBag.getInstance()!=null) {
			WndBag.getInstance().updateItems();
		}
	}
}
