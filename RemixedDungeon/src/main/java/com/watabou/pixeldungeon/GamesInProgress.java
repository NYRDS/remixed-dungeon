
package com.watabou.pixeldungeon;

import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;

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
				SaveUtils.preview( info, bundle.get() );
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
			Info info = checkFile(SaveUtils.gameFile( cl ));
			state.put( cl, info );
			return info;
		}
	}

	public static void set(Hero hero, int depth) {
		Info info = new Info();
		info.depth = depth;
		info.level = hero.lvl();
		info.difficulty = hero.getDifficulty();
		state.put( hero.getHeroClass(), info );
	}
	
	public static void delete( HeroClass cl ) {
		state.put( cl, null );
	}
	
	public static class Info {
		public int depth;
		public int level;
		public int difficulty;
	}
}
