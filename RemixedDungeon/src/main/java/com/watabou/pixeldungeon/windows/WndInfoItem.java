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

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
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
				title = Game.getVar(R.string.WndInfoItem_Chest);
				info = Game.getVar(R.string.WndInfoItem_WontKnow);
			} else if (heap.type == Type.TOMB) {
				title = Game.getVar(R.string.WndInfoItem_Tomb);
				info = Game.getVar(R.string.WndInfoItem_Owner);
			} else if (heap.type == Type.SKELETON) {
				title = Game.getVar(R.string.WndInfoItem_Skeleton);
				info = Game.getVar(R.string.WndInfoItem_Remains);
			} else if (heap.type == Type.CRYSTAL_CHEST) {
				title = Game.getVar(R.string.WndInfoItem_CrystalChest);
				info = Utils.format( Game.getVar(R.string.WndInfoItem_Inside), Utils.indefinite( heap.peek().name() ) );
			} else {
				title = Game.getVar(R.string.WndInfoItem_LockedChest);
				info = Game.getVar(R.string.WndInfoItem_WontKnow) +" "+ Game.getVar(R.string.WndInfoItem_NeedKey);
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
