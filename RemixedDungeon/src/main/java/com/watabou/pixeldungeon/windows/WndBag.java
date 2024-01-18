
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.bags.Keyring;
import com.watabou.pixeldungeon.items.bags.PotionBelt;
import com.watabou.pixeldungeon.items.bags.Quiver;
import com.watabou.pixeldungeon.items.bags.ScrollHolder;
import com.watabou.pixeldungeon.items.bags.SeedPouch;
import com.watabou.pixeldungeon.items.bags.WandHolster;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.elements.Tab;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;



public class WndBag extends WndTabbed {

	private final int panelWidth;
	private final Text txtTitle;
	private Text txtSubTitle;

	public static WndBag getInstance() {
		return instance;
	}

	public Listener getListener() {
		return listener;
	}

	public Mode getMode() {
		return mode;
	}

	public enum Mode {
		ALL,
		UNIDENTIFED,
		UPGRADEABLE,
		QUICKSLOT,
		FOR_SALE,
		WEAPON,
		ARMOR,
		WAND,
		SEED,
		INSCRIBABLE,
		MOISTABLE, 
		FUSEABLE, 
		UPGRADABLE_WEAPON,
		FOR_BUY,
		ARROWS
	}


	private static final int COLS_P	= 4;
	private static final int COLS_L	= 6;

	protected static final int SLOT_SIZE	= 28;
	protected static final int SLOT_MARGIN	= 1;
	
	private static final int TAB_WIDTH_P	= 18;
	private static final int TAB_WIDTH_L	= 26;
		
	private final Listener listener;
	private final WndBag.Mode mode;
	private final String title;

	protected int count;
	protected int col;
	protected int row;
	
	private final int nCols;
	private final int nRows;

	private float titleBottom;

	private static Mode lastMode;
	private static Bag lastBag;

	private static WndBag instance;

	private final Belongings stuff;

	public WndBag(Belongings stuff, @NotNull Bag bag, Listener listener, Mode mode, String title) {
		
		super();

		stuff.getOwner().interrupt();

		nCols = RemixedDungeon.landscape() ? COLS_L : COLS_P;
		nRows = (Belongings.BACKPACK_SIZE + 4 + 1) / nCols + ((Belongings.BACKPACK_SIZE + 4 + 1) % nCols > 0 ? 1 : 0);
		
		this.listener = listener;
		this.mode = mode;
		this.title = title;
		this.stuff = stuff;
		
		lastMode = mode;

		if(bag==null) {
			bag = stuff.backpack;
		}

		lastBag = bag;

		panelWidth = SLOT_SIZE * nCols + SLOT_MARGIN * (nCols - 1);

		txtTitle = PixelScene.createMultiline( title != null ? title : Utils.capitalize( bag.name() ), GuiProperties.titleFontSize());
		txtTitle.maxWidth(panelWidth);
		txtTitle.hardlight( TITLE_COLOR );
		txtTitle.setX(PixelScene.align((panelWidth - txtTitle.width()) / 2));
		if(txtTitle.getX() <0) {
			txtTitle.setX(0);
		}
		txtTitle.setY(0);
		add( txtTitle );

		placeItems( bag );
		
		resize( 
			panelWidth,
			(int) (SLOT_SIZE * nRows + SLOT_MARGIN * (nRows - 1) + titleBottom + SLOT_MARGIN) );

		if(stuff.getOwner() instanceof Hero) {
			Bag[] bags = {
					stuff.backpack,
					stuff.getItem( PotionBelt.class ),
					stuff.getItem( SeedPouch.class ),
					stuff.getItem( ScrollHolder.class ),
					stuff.getItem( WandHolster.class ),
					stuff.getItem( Keyring.class ),
					stuff.getItem( Quiver.class ) };

			for (Bag b : bags) {
				if (b != null) {
					BagTab tab = new BagTab(this, b);
					if (RemixedDungeon.landscape()) {
						tab.setSize(TAB_WIDTH_L, tabHeight());
					} else {
						tab.setSize(TAB_WIDTH_P, tabHeight());
					}

					add(tab);
					if(b==bag){
						select(tab);
					}
				}
			}
		}

		if(instance!=null) {
			instance.hide();
		}

		instance = this;
	}
	
	public static WndBag lastBag(@NotNull Char owner, Listener listener, Mode mode, String title ) {

		Belongings belongings = owner.getBelongings();

		if (mode == lastMode && lastBag != null && 
			belongings.backpack.contains( lastBag )) {
			
			return new WndBag(belongings, lastBag, listener, mode, title );
			
		} else {
			switch (mode) {
				case WAND:
					return new WndBag(belongings, belongings.getItem(WandHolster.class), listener, mode, title );

				case SEED:
					return new WndBag(belongings, belongings.getItem(SeedPouch.class), listener, mode, title );

				case ARROWS:
					return new WndBag(belongings, belongings.getItem(Quiver.class), listener, mode, title );

				default:
					return new WndBag(belongings, belongings.backpack, listener, mode, title );
			}
		}
	}

	public void updateItems() {
		clearItems();
		placeItems(lastBag);

		Window activeDialog = getActiveDialog();
		if(activeDialog != null) {
			bringToFront(activeDialog);
		}
	}

	private void placeEquipped(Item item, Belongings.Slot slot, int image) {
		if(item != ItemsList.DUMMY) {
			placeItem(item);
			return;
		}

		if(stuff.slotBlocked(slot)) {
			placeItem(new ItemPlaceholder(ItemPlaceholder.LOCKED));
			return;
		}

		placeItem(new ItemPlaceholder(image));

	}

	public void setItemsActive(boolean state) {
		for (var child: members) {
			if(child instanceof ItemButton) {
				child.setActive(state);
			}
		}
	}

	private void clearItems() {
		row = col = count = 0;
		var childsToRemove = new ArrayList<ItemButton>();
		for (var child: members) {
			if(child instanceof ItemButton) {
				childsToRemove.add((ItemButton) child);
			}
		}

		for(var child:childsToRemove) {
			remove(child);
		}
	}

	private void placeItems(Bag container) {
		titleBottom = txtTitle.bottom();

		if(mode==Mode.FOR_BUY) {
			if(txtSubTitle != null) {
				txtSubTitle.killAndErase();
			}
			txtSubTitle = PixelScene.createMultiline( Utils.format(R.string.WndBag_BuySubtitle, Dungeon.hero.gold()) , GuiProperties.titleFontSize());
			txtSubTitle.maxWidth(panelWidth);
			txtSubTitle.hardlight( TITLE_COLOR );
			txtSubTitle.setX(PixelScene.align((panelWidth - txtSubTitle.width()) / 2));
			if(txtSubTitle.getX() <0) {
				txtSubTitle.setX(0);
			}
			txtSubTitle.setY(titleBottom);
			add( txtSubTitle );
			titleBottom = txtSubTitle.bottom();
		}

		// Equipped items
		if(stuff.getOwner() instanceof Hero) {
			placeEquipped(stuff.getItemFromSlot(Belongings.Slot.WEAPON),   Belongings.Slot.WEAPON,    ItemPlaceholder.RIGHT_HAND);
			placeEquipped(stuff.getItemFromSlot(Belongings.Slot.ARMOR),    Belongings.Slot.ARMOR,     ItemPlaceholder.BODY);
			placeEquipped(stuff.getItemFromSlot(Belongings.Slot.LEFT_HAND), Belongings.Slot.LEFT_HAND, ItemPlaceholder.LEFT_HAND);
			placeEquipped(stuff.getItemFromSlot(Belongings.Slot.ARTIFACT),    Belongings.Slot.ARTIFACT,  ItemPlaceholder.ARTIFACT);
			placeEquipped(stuff.getItemFromSlot(Belongings.Slot.LEFT_ARTIFACT),    Belongings.Slot.LEFT_ARTIFACT,  ItemPlaceholder.ARTIFACT);
		}

		// Unequipped items
		for (Item item : container.items) {
			if(! (item instanceof Gold)) {
				placeItem(item);
			}
		}
		
		// Empty slots
		//int margin = stuff.getOwner() instanceof Hero ?  4 :  5;
		int margin = 5;
		while (count - margin < container.getSize()) {
			placeItem( null );
		}

		// Gold
		if (stuff.getOwner() instanceof Hero) {
			row = nRows - 1;
			col = nCols - 1;

			Gold gold = stuff.getItem(Gold.class);
			if(gold!=null) {
				placeItem(gold);
			}
		}
	}
	
	private void placeItem(final Item item) {

		if(row >=nRows) {
			return;
		}

		int x = col * (SLOT_SIZE + SLOT_MARGIN);
		int y = (int) (titleBottom + SLOT_MARGIN + row * (SLOT_SIZE + SLOT_MARGIN));

		ItemButton btnItem = new ItemButton(this, item );
		btnItem.setPos(x,y);
		add( btnItem );
		
		if (++col >= nCols) {
			col = 0;
			row++;
		}
		
		count++;
	}
	
	@Override
	public void onMenuPressed() {
		if (listener == null) {
			hide();
		}
	}
	
	@Override
	public void onBackPressed() {
		if (listener != null) {
			listener.onSelect( null, Dungeon.hero);
		}
		super.onBackPressed();
	}
	
	@Override
	public void onClick( Tab tab ) {
		super.onClick(tab);

		BagTab bagTab = (BagTab) tab;

		txtTitle.text( title != null ? title : Utils.capitalize(bagTab.bag.name() ));
		txtTitle.setX(PixelScene.align((panelWidth - txtTitle.width()) / 2));

		if(txtTitle.getX() <0) {
			txtTitle.setX(0);
		}

		lastBag = bagTab.bag;

		clearItems();
		placeItems(bagTab.bag);
	}
	
	@Override
	protected int tabHeight() {
		return 24;
	}

	public boolean hideOnSelect() {
		return  !(mode == Mode.FOR_SALE || mode == Mode.FOR_BUY);
	}

	public interface Listener {
		void onSelect(Item item, Char selector);
	}
}
