

package com.watabou.utils;

public interface Bundlable {

	void restoreFromBundle( Bundle bundle );
	void storeInBundle( Bundle bundle );
	
	boolean dontPack();
	
}
