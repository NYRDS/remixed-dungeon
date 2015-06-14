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
package com.watabou.pixeldungeon.actors.buffs;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Thief;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;

public class Buff extends Actor {
	
	public Char target;
	
	interface itemAction{
		public Item   act(Item srcItem);
		public String actionText(Item srcItem);
		public void   carrierFx();
	};
	
	public boolean attachTo( Char target ) {

		if (target.immunities().contains( getClass() )) {
			return false;
		}
		
		this.target = target;
		target.add( this );
		
		return true;
	}
	
	public void detach() {
		target.remove( this );
	}
	
	@Override
	public boolean act() {
		deactivate();
		return true;
	}
	
	public int icon() {
		return BuffIndicator.NONE;
	}
	
	public static<T extends Buff> T affect( Char target, Class<T> buffClass ) {
		T buff = target.buff( buffClass );
		if (buff != null) {
			return buff;
		} else {
			try {
				buff = buffClass.newInstance();
				buff.attachTo( target );
				return buff;
			} catch (Exception e) {
				return null;
			}
		}
	}
	
	public static<T extends FlavourBuff> T affect( Char target, Class<T> buffClass, float duration ) {
		T buff = affect( target, buffClass );
		buff.spend( duration );
		return buff;
	}
	
	public static<T extends FlavourBuff> T prolong( Char target, Class<T> buffClass, float duration ) {
		T buff = affect( target, buffClass );
		buff.postpone( duration );
		return buff;
	}
	
	public static void detach( Buff buff ) {
		if (buff != null) {
			buff.detach();
		}
	}
	
	public static void detach( Char target, Class<? extends Buff> cl ) {
		detach( target.buff( cl ) );
	}
	
	private void collectOrDropItem(Item item){
		if(!item.collect( ((Hero)target).belongings.backpack )){
			Dungeon.level.drop(item, target.pos).sprite.drop();
		}	
	}
	
	protected void applyToCarriedItems(itemAction action ){
		if (target instanceof Hero) {
			
			Item item = ((Hero)target).belongings.randomUnequipped();
			
			if(item == null){
				return;
			}
			
			Item srcItem = item.detach(((Hero)target).belongings.backpack);
			
			item = action.act(srcItem);
			
			if(item == srcItem){ //item unaffected by buff
				collectOrDropItem(item);
				return;
			}
			
			String actionText = null;
			
			if(item == null){
				actionText = action.actionText(srcItem);
				action.carrierFx();
			}
			else{
				if(!srcItem.equals(item)){
					actionText = action.actionText(srcItem);
					collectOrDropItem(item);

					action.carrierFx();
				}
			}
			
			if(actionText != null){
				GLog.w(actionText);
			}
			
		} else if (target instanceof Thief){
			if (((Thief)target).item == null)
			{
				return;
			}
			((Thief)target).item = action.act(((Thief)target).item);
			action.carrierFx();
			//target.sprite.emitter().burst( ElmoParticle.FACTORY, 6 );
		}
	}
}
