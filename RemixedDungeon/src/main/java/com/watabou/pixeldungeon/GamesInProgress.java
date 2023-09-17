
package com.watabou.pixeldungeon;

import com.google.common.base.Optional;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.utils.Bundle;

import java.util.HashMap;
import java.util.Map;

import lombok.val;

public class GamesInProgress {

	private static final Map<HeroClass, Info> state = new HashMap<>();
	
	public static Info checkFile(String file) {
		Info info = null;
		try {
			val bundle = Dungeon.gameBundle( file );
			if (bundle.isPresent()) {
				info = new Info();
				Dungeon.preview( info, bundle.get() );
			}
		} catch (Exception e) {
			info = null;
		}
		return info;
		
	}
	
	public static Info check( HeroClass cl ) {
		
		if (state.containsKey( cl )) {
			
			return state.get( cl );
			
		} else {
			Info info =checkFile(SaveUtils.gameFile( cl ));
			state.put( cl, info );
			return info;
		}
	}

	public static void set( HeroClass cl, int depth, int level ) {
		Info info = new Info();
		info.depth = depth;
		info.level = level;
		state.put( cl, info );
	}
	
	public static void setUnknown( HeroClass cl ) {
		state.remove( cl );
	}
	
	public static void delete( HeroClass cl ) {
		state.put( cl, null );
	}
	
	public static class Info {
		public int depth;
		public int level;
	}
}
