
package com.watabou.pixeldungeon.items;

import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class ItemStatusHandler<T extends Item> {

	private final Class<? extends T>[] items;
	
	private final HashMap<Class<? extends T>, Integer> images;
	private final HashSet<Class<? extends T>>          known;
	
	public ItemStatusHandler( Class<? extends T>[] items, Integer[] allImages ) {
		
		this.items = items;
		
		this.images = new HashMap<>();
		known       = new HashSet<>();
		
		ArrayList<Integer> imagesLeft = new ArrayList<>(Arrays.asList(allImages));

		for (Class<? extends T> item : items) {

			assignRandomImage(imagesLeft, item);
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
		for (Class<? extends T> item : items) {
			String itemName = item.toString();
			bundle.put(itemName + PFX_IMAGE, images.get(item));
			bundle.put(itemName + PFX_KNOWN, known.contains(item));
		}
	}
	
	private void restore( Bundle bundle, Integer[] allImages ) {
		ArrayList<Integer> imagesLeft = new ArrayList<>(Arrays.asList(allImages));

		for (Class<? extends T> item : items) {

			String itemName = item.toString();

			if (bundle.contains(itemName + PFX_IMAGE)) {
				Integer image = bundle.getInt(itemName + PFX_IMAGE);

				if(imagesLeft.contains(image)) {

					images.put(item, image);
					imagesLeft.remove(image);
				} else {
					assignRandomImage(imagesLeft, (Class<? extends T>) item);
				}
				if (bundle.getBoolean(itemName + PFX_KNOWN)) {
					known.add(item);
				}

			} else {
				assignRandomImage(imagesLeft, item);
			}
		}
	}

	private void assignRandomImage(@NotNull ArrayList<Integer> imagesLeft, Class<? extends T> item) {
		int index = Random.Int(imagesLeft.size());

		images.put(item, imagesLeft.get(index));
		imagesLeft.remove(index);
	}

	public int index (T item ) {return images.get( item.getClass() );}

	public boolean isKnown( T item ) {
		return known.contains( item.getClass() );
	}
	
	@SuppressWarnings("unchecked")
	public void know( T item ) {
		known.add( (Class<? extends T>)item.getClass() );
		
		if (known.size() == items.length - 1) {
			for (Class<? extends T> aClass : items) {
				if (!known.contains(aClass)) {
					known.add(aClass);
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
