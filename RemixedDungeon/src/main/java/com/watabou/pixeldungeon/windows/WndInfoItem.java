
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Heap.Type;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.ItemSlot;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.elements.GenericInfo;

import org.jetbrains.annotations.NotNull;

public class WndInfoItem extends Window {

	public WndInfoItem( Heap heap ) {
		
		super();
		
		if (heap.type == Heap.Type.HEAP) {
			
			Item item = heap.peek();

			GenericInfo.makeInfo(this,
									new ItemSprite(item),
									item.toString(),
									itemDescColor(item),
									item.info());
		} else {
			
			String title;
			String info;
			
			if (heap.type == Type.CHEST || heap.type == Type.MIMIC) {
                title = StringsManager.getVar(R.string.WndInfoItem_Chest);
                info = StringsManager.getVar(R.string.WndInfoItem_WontKnow);
			} else if (heap.type == Type.TOMB) {
                title = StringsManager.getVar(R.string.WndInfoItem_Tomb);
                info = StringsManager.getVar(R.string.WndInfoItem_Owner);
			} else if (heap.type == Type.SKELETON) {
                title = StringsManager.getVar(R.string.WndInfoItem_Skeleton);
                info = StringsManager.getVar(R.string.WndInfoItem_Remains);
			} else if (heap.type == Type.CRYSTAL_CHEST) {
                title = StringsManager.getVar(R.string.WndInfoItem_CrystalChest);
                info = Utils.format(R.string.WndInfoItem_Inside, Utils.indefinite( heap.peek().name() ) );
			} else {
                title = StringsManager.getVar(R.string.WndInfoItem_LockedChest);
                info = StringsManager.getVar(R.string.WndInfoItem_WontKnow) +" "+ StringsManager.getVar(R.string.WndInfoItem_NeedKey);
			}

			GenericInfo.makeInfo(this,
									new ItemSprite(heap),
									title,
									TITLE_COLOR,
									info);

		}
	}

	public WndInfoItem( Item item ) {
		
		super();

		GenericInfo.makeInfo(this,
								new ItemSprite(item),
								item.toString(),
								itemDescColor(item),
								item.info());
	}

	private int itemDescColor(@NotNull Item item) {
		int color = TITLE_COLOR;
		if(item.isLevelKnown()) {
			if (item.level() > 0) {
				color = ItemSlot.UPGRADED;
			}
			if (item.level() < 0) {
				color = ItemSlot.DEGRADED;
			}
		}
		return color;
	}
}
