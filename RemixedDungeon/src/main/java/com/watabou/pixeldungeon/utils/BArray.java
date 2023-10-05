
package com.watabou.pixeldungeon.utils;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BArray {

	public static boolean[] and( boolean[] a, boolean[] b, boolean[] result ) {
		
		int length = a.length;
		
		if (result == null) {
			result = new boolean[length];
		}
		
		for (int i=0; i < length; i++) {
			result[i] = a[i] && b[i];
		}
		
		return result;
	}


	public static boolean[] and_not( boolean[] a, boolean[] b, boolean[] result ) {

		int length = a.length;

		if (result == null) {
			result = new boolean[length];
		}

		for (int i=0; i < length; i++) {
			result[i] = a[i] && !b[i];
		}

		return result;
	}

	public static boolean[] or( boolean[] a, boolean[] b, boolean[] result ) {
		
		int length = a.length;
		
		if (result == null) {
			result = new boolean[length];
		}
		
		for (int i=0; i < length; i++) {
			result[i] = a[i] || b[i];
		}
		
		return result;
	}
	
	public static boolean[] not(@NotNull boolean[] a, @Nullable boolean[] result ) {
		
		int length = a.length;
		
		if (result == null) {
			result = new boolean[length];
		}
		
		for (int i=0; i < length; i++) {
			result[i] = !a[i];
		}
		
		return result;
	}
	
	public static boolean[] is( int[] a, boolean[] result, int v1 ) {
		
		int length = a.length;
		
		if (result == null) {
			result = new boolean[length];
		}
		
		for (int i=0; i < length; i++) {
			result[i] = a[i] == v1;
		}
		
		return result;
	}
	
	public static boolean[] isOneOf( int[] a, boolean[] result, int... v ) {
		
		int length = a.length;
		int nv = v.length;
		
		if (result == null) {
			result = new boolean[length];
		}
		
		for (int i=0; i < length; i++) {
			result[i] = false;
			for (int value : v) {
				if (a[i] == value) {
					result[i] = true;
					break;
				}
			}
		}
		
		return result;
	}
	
	public static boolean[] isNot( int[] a, boolean[] result, int v1 ) {
		
		int length = a.length;
		
		if (result == null) {
			result = new boolean[length];
		}
		
		for (int i=0; i < length; i++) {
			result[i] = a[i] != v1;
		}
		
		return result;
	}
	
	public static boolean[] isNotOneOf( int[] a, boolean[] result, int... v ) {
		
		int length = a.length;
		int nv = v.length;
		
		if (result == null) {
			result = new boolean[length];
		}
		
		for (int i=0; i < length; i++) {
			result[i] = true;
			for (int value : v) {
				if (a[i] == value) {
					result[i] = false;
					break;
				}
			}
		}
		
		return result;
	}
}
