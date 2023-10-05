
package com.watabou.pixeldungeon;

import com.nyrds.platform.util.TrackedRuntimeException;
import com.watabou.noosa.NinePatch;

public class Chrome {

	public enum  Type {
		TOAST,
		TOAST_TR,
		WINDOW,
		BUTTON,
		TAG,
		GEM,
		SCROLL,
		TAB_SET,
		TAB_SELECTED,
		TAB_UNSELECTED,
		QUICKSLOT,
		ACTION_BUTTON
	}
	
	public static NinePatch get( Type type ) {
		switch (type) {
		case WINDOW:
			return new NinePatch( Assets.getChrome(), 0, 0, 22, 22, 7 );
		case TOAST:
			return new NinePatch( Assets.getChrome(), 22, 0, 18, 18, 5 );
		case TOAST_TR:
			return new NinePatch( Assets.getChrome(), 40, 0, 18, 18, 5 );
		case BUTTON:
			return new NinePatch( Assets.getChrome(), 58, 0, 4, 4, 1 );
		case TAG:
			return new NinePatch( Assets.getChrome(), 22, 18, 16, 14, 3 );
		case GEM:
			return new NinePatch( Assets.getChrome(), 0, 32, 32, 32, 13 );
		case SCROLL:
			return new NinePatch( Assets.getChrome(), 32, 32, 32, 32, 5, 11, 5, 11 );
		case TAB_SET:
			return new NinePatch( Assets.getChrome(), 64, 0, 22, 22, 7, 7, 7, 7 );
		case TAB_SELECTED:
			return new NinePatch( Assets.getChrome(), 64, 22, 10, 14, 4, 7, 4, 6 );
		case TAB_UNSELECTED:
			return new NinePatch( Assets.getChrome(), 74, 22, 10, 14, 4, 7, 4, 6 );
		case QUICKSLOT:
			return new NinePatch( Assets.getChrome(), 107, 44, 20, 20, 3 );
		case ACTION_BUTTON:
			return new NinePatch( Assets.getChrome(), 65, 49, 14, 14, 2 );
		}
		throw new TrackedRuntimeException("wrong chrome type");
	}
}
