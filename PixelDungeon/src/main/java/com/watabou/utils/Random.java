/*
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

package com.watabou.utils;

import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.Map;

public class Random {

	public static float Float( float min, float max ) {
		return (float)(min + Math.random() * (max - min)); 
	}
	
	public static float Float( float max ) {
		return (float)(Math.random() * max);
	}
	
	public static float Float() {
		return (float)Math.random();
	}
	
	public static int Int( int max ) {
		return max > 0 ? (int)(Math.random() * max) : 0;
	}
	
	public static int Int( int min, int max ) {
		return min + (int)(Math.random() * (max - min));
	}
	
	public static int IntRange( int min, int max ) {
		return min + (int)(Math.random() * (max - min + 1));
	}
	
	public static int NormalIntRange( int min, int max ) {
		return min + (int)((Math.random() + Math.random()) * (max - min + 1) / 2f);
	}

	public static int chances( Float[] floats ) {
		
		int length = floats.length;
		
		float sum = floats[0];
		for (int i=1; i < length; i++) {
			sum += floats[i];
		}
		
		float value = Float( sum );
		sum = floats[0];
		for (int i=0; i < length; i++) {
			if (value <= sum) {
				return i;
			}
			sum += floats[i + 1];
		}
		
		return 0;
	}
	
	public static int chances( float[] floats ) {
		
		int length = floats.length;
		
		float sum = floats[0];
		for (int i=1; i < length; i++) {
			sum += floats[i];
		}
		
		float value = Float( sum );
		sum = floats[0];
		for (int i=0; i < length; i++) {
			if (value <= sum) {
				return i;
			}
			sum += floats[i + 1];
		}
		
		return 0;
	}

	@SuppressWarnings("unchecked")
	public static <K> K chances( Map<K,Float> chances ) {
		
		int size = chances.size();

		K[] values = (K[])chances.keySet().toArray();

		float[] probs = new float[size];
		float sum = 0;
		for (int i=0; i < size; i++) {
			probs[i] = chances.get( values[i] );
			sum += probs[i];
		}
		
		float value = Float( sum );
		
		sum = probs[0];
		for (int i=0; i < size; i++) {
			if (value <= sum) {
				return values[i];
			}
			sum += probs[i + 1];
		}
		
		return values[size-1];
	}
	
	public static int index( Collection<?> collection ) {
		return (int)(Math.random() * collection.size());
	}
	
	public static<T> T oneOf( T... array ) {
		return array[(int)(Math.random() * array.length)];
	}
	
	public static<T> T element( T[] array ) {
		return element( array, array.length );
	}
	
	public static<T> T element( T[] array, int max ) {
		return array[(int)(Math.random() * max)];
	}
	
	@Nullable
	@SuppressWarnings("unchecked")
	public static<T> T element( Collection<? extends T> collection ) {
		int size = collection.size();
		return size > 0 ? 
			(T)collection.toArray()[Int( size )] : 
			null;
	}
}
