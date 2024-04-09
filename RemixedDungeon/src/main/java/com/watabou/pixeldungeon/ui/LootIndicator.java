
package com.watabou.pixeldungeon.ui;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;

public class LootIndicator extends Tag {
	
	private ItemSlot slot;
	
	private Item lastItem = null;
	private int lastQuantity = 0;

	private final Char hero;

	public LootIndicator(Char hero) {
		super( 0x1F75CC );

		this.hero = hero;
		setSize( 24, 22 );
		
		setVisible(false);
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
		
		slot = new ItemSlot() {
			protected void onClick() {
				Dungeon.hero.handle( Dungeon.hero.getPos() );
			}
		};
		slot.showParams( false );
		add( slot );
	}
	
	@Override
	protected void layout() {
		super.layout();
		
		slot.setRect( x + 2, y + 3, width - 2, height - 6 );
	}
	
	@Override
	public void update() {
		
		if (hero.isReady()) {
			Heap heap = Dungeon.level.getHeap( hero.getPos() );
				
			if (heap != null) {
				Item item = 
						heap.type == Heap.Type.CHEST || heap.type == Heap.Type.MIMIC ? ItemSlot.CHEST : 
						heap.type == Heap.Type.LOCKED_CHEST ? ItemSlot.LOCKED_CHEST :
						heap.type == Heap.Type.TOMB ? ItemSlot.TOMB :
						heap.type == Heap.Type.SKELETON ? ItemSlot.SKELETON :
						heap.peek();
				if (item != lastItem || item.quantity() != lastQuantity) {
					lastItem = item;
					lastQuantity = item.quantity();
					
					slot.item( item );
					flash();
				}
				setVisible(true);
				
			} else {
				
				lastItem = null;
				setVisible(false);
				
			}
		}
		
		slot.enable( getVisible() && hero.isReady() );
		
		super.update();
	}
}
