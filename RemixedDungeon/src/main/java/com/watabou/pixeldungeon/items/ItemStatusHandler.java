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
package com.watabou.pixeldungeon.items;

import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class ItemStatusHandler<T extends Item> {

	private Class<? extends T>[] items;
	
	private HashMap<Class<? extends T>, Integer> images;
	private HashSet<Class<? extends T>>          known;
	
	public ItemStatusHandler( Class<? extends T>[] items, Integer[] allImages ) {
		
		this.items = items;
		
		this.images = new HashMap<>();
		known       = new HashSet<>();
		
		ArrayList<Integer> imagesLeft = new ArrayList<>(Arrays.asList(allImages));
		
		for (int i=0; i < items.length; i++) {
			
			Class<? extends T> item = items[i];
			
			int index = Random.Int( imagesLeft.size() );

			images.put( item, imagesLeft.get( index ) );
			imagesLeft.remove( index );
		}
	}
	
	public ItemStatusHandler( Class<? extends T>[] items, Integer[] images, Bundle bundle ) {
		
		this.items = items;
		
		this.images = new HashMap<>();
		known       = new HashSet<>();
		
		restore( bundle, images );
	}
	
	private static final String PFX_IMAGE	= "_image";
	private static final String PFX_KNOWN	= "_known";
	
	public void save( Bundle bundle ) {
		for (int i=0; i < items.length; i++) {
			String itemName = items[i].toString();
			bundle.put( itemName + PFX_IMAGE, images.get( items[i] ) );
			bundle.put( itemName + PFX_KNOWN, known.contains( items[i] ) );
		}
	}
	
	private void restore( Bundle bundle, Integer[] allImages ) {
		ArrayList<Integer> imagesLeft = new ArrayList<>(Arrays.asList(allImages));
		
		for (int i=0; i < items.length; i++) {
			
			Class<? extends T> item = items[i];
			String itemName = item.toString();
			
			if (bundle.contains( itemName + PFX_IMAGE )) {
				Integer image = bundle.getInt( itemName + PFX_IMAGE );
				images.put( item, image );
				imagesLeft.remove( image );
				
				if (bundle.getBoolean( itemName + PFX_KNOWN )) {
					known.add( item );
				}
				
			} else {
				int index = Random.Int( imagesLeft.size() );

				images.put( item, imagesLeft.get( index ) );
				imagesLeft.remove( index );
			}
		}
	}

	public int index (T item ) {return images.get( item.getClass() );}

	public boolean isKnown( T item ) {
		return known.contains( item.getClass() );
	}
	
	@SuppressWarnings("unchecked")
	public void know( T item ) {
		known.add( (Class<? extends T>)item.getClass() );
		
		if (known.size() == items.length - 1) {
			for (int i=0; i < items.length; i++) {
				if (!known.contains( items[i] )) {
					known.add( items[i] );
					break;
				}
			}
		}
	}
	
	public HashSet<Class<? extends T>> known() {
		return known;
	}
	
	public HashSet<Class<? extends T>> unknown() {
		HashSet<Class<? extends T>> result = new HashSet<>();
		for (Class<? extends T> i : items) {
			if (!known.contains( i )) {
				result.add( i );
			}
		}
		return result;
	}

	public static int indexByImage(int image,Integer [] allImages) {
		for (int i = 0;i<allImages.length;++i) {
			if(allImages[i]==image) {
				return i;
			}
		}
		return 0;
	}
}
