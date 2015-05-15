package com.watabou.pixeldungeon;

import com.nyrds.android.util.ModdingMode;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.utils.Utils;

public class SaveUtils {
	private static final String RG_GAME_FILE	= "game.dat";
	private static final String RG_DEPTH_FILE	= "depth%d.dat";
	
	private static final String WR_GAME_FILE	= "warrior.dat";
	private static final String WR_DEPTH_FILE	= "warrior%d.dat";
	
	private static final String MG_GAME_FILE	= "mage.dat";
	private static final String MG_DEPTH_FILE	= "mage%d.dat";
	
	private static final String RN_GAME_FILE	= "ranger.dat";
	private static final String RN_DEPTH_FILE	= "ranger%d.dat";

	private static final String EL_GAME_FILE	= "elf.dat";
	private static final String EL_DEPTH_FILE	= "elf%d.dat";

	public static String gameFile( HeroClass cl ) {
		
		if(ModdingMode.mode()) {
			return "modding.dat";
		}
		
		switch (cl) {
		case WARRIOR:
			return WR_GAME_FILE;
		case ROGUE:
			return RG_GAME_FILE;
		case MAGE:
			return MG_GAME_FILE;
		case HUNTRESS:
			return RN_GAME_FILE;
		case ELF:
			return EL_GAME_FILE;
		default:
			return RG_GAME_FILE;
		}
	}
	
	public static String depthFile( HeroClass cl, int depth) {
		return Utils.format(_depthFile(cl), depth);
	}
	
	private static String _depthFile( HeroClass cl) {
		
		if(ModdingMode.mode()) {
			return "modding%d.dat";
		}
		
		switch (cl) {
		case WARRIOR:
			return WR_DEPTH_FILE;
		case ROGUE:
			return RG_DEPTH_FILE;
		case MAGE:
			return MG_DEPTH_FILE;
		case HUNTRESS:
			return RN_DEPTH_FILE;
		case ELF:
			return EL_DEPTH_FILE;
		default:
			return RG_DEPTH_FILE;
		}
	}

}
