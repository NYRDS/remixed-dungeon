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
package com.watabou.pixeldungeon.actors.hero;

import com.watabou.pixeldungeon.actors.Char;

public class CharAction {
	
	public int dst;
	
	public static class Move extends CharAction {
		public Move( int dst ) {
			this.dst = dst;
		}
	}
	
	public static class PickUp extends CharAction {
		public PickUp( int dst ) {
			this.dst = dst;
		}
	}
	
	public static class OpenChest extends CharAction {
		public OpenChest( int dst ) {
			this.dst = dst;
		}
	}

	public static class Interact extends CharAction {
		public Char chr;
		public Interact( Char chr) {
			this.chr = chr;
		}
	}
	
	public static class Unlock extends CharAction {
		public Unlock( int door ) {
			this.dst = door;
		}
	}
	
	public static class Descend extends CharAction {
		public Descend( int stairs ) {
			this.dst = stairs;
		}
	}
	
	public static class Ascend extends CharAction {
		public Ascend( int stairs ) {
			this.dst = stairs;
		}
	}
	
	public static class Cook extends CharAction {
		public Cook( int pot ) {
			this.dst = pot;
		}
	}
	
	public static class Attack extends CharAction {
		public Char target;
		public Attack( Char target ) {
			this.target = target;
		}
	}
}
