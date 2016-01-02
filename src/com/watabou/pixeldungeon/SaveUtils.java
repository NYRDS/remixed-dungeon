package com.watabou.pixeldungeon;

import java.io.File;

import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.ModdingMode;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.GamesInProgress.Info;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

public class SaveUtils {
	private static final String RG_GAME_FILE = "rogue.dat";
	private static final String RG_DEPTH_FILE = "rogue%d.dat";

	private static final String WR_GAME_FILE = "warrior.dat";
	private static final String WR_DEPTH_FILE = "warrior%d.dat";

	private static final String MG_GAME_FILE = "mage.dat";
	private static final String MG_DEPTH_FILE = "mage%d.dat";

	private static final String RN_GAME_FILE = "ranger.dat";
	private static final String RN_DEPTH_FILE = "ranger%d.dat";

	private static final String EL_GAME_FILE = "elf.dat";
	private static final String EL_DEPTH_FILE = "elf%d.dat";

	static private boolean hasClassTag(HeroClass cl, String fname) {
		switch (cl) {
		case ROGUE:
			return fname.contains("rogue");
		case WARRIOR:
			return fname.contains("warrior");
		case HUNTRESS:
			return fname.contains("ranger");
		case MAGE:
			return fname.contains("mage");
		case ELF:
			return fname.contains("elf");
		default:
			throw new RuntimeException("unknown hero class!");
		}
	}

	public static void loadGame(String slot, HeroClass heroClass) {
		
		GLog.toFile("Loading: class :%s slot: %s", heroClass.toString(), slot);
		
		copyFromSaveSlot(slot, heroClass);
		
		InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
		Game.switchScene(InterlevelScene.class);
		Dungeon.heroClass = heroClass;
	}

	public static String slotInfo(String slot, HeroClass cl) {
		if(slotUsed(slot, cl)) {
			
			String localName = slot +"/"+ gameFile(cl);
			
			Info info = GamesInProgress.checkFile(localName);
			
			if(info!= null) {
				return Utils.format("d: %2d   l: %2d", info.depth,
						info.level);
			}
		}
		
		return "";
	}
	
	public static boolean slotUsed(String slot, HeroClass cl) {
		String[] slotFiles = FileSystem.getInteralStorageFile(slot)
				.getAbsoluteFile().list();
		if (slotFiles == null) {
			return false;
		}

		for (String file : slotFiles) {
			if (file.endsWith(gameFile(cl))) {
				return true;
			}
		}

		return false;
	}

	private static boolean isRelatedTo(String path,HeroClass cl) {
		return ( path.endsWith(".dat") && hasClassTag(cl, path) ) || path.endsWith(gameFile(cl));
	}

	public static void copyAllClassesToSlot(String slot) {
		for(HeroClass hc: HeroClass.values()) {
			copySaveToSlot(slot,hc);
		}
	}
	
	public static void copyAllClassesFromSlot(String slot) {
		for(HeroClass hc: HeroClass.values()) {
			copyFromSaveSlot(slot,hc);
		}
	}
	
	public static void deleteGameAllClasses() {
		for(HeroClass hc: HeroClass.values()) {
			deleteLevels(hc);
			deleteGameFile(hc);
		}
	}
	
	public static void copyFromSaveSlot(String slot, HeroClass heroClass) {
		Dungeon.deleteGame(true);
		
		String[] files = FileSystem.getInteralStorageFile(slot).list();

		for (String file : files) {
			if (isRelatedTo(file, heroClass)) {

				String from = FileSystem.getInteralStorageFile(slot + "/" + file).getAbsolutePath();
				String to = FileSystem.getInteralStorageFile(file).getAbsolutePath();

				GLog.toFile("restoring file: %s, (%s -> %s)", file, from, to);

				FileSystem.copyFile(from, to);
			}
		}
	}
	
	public static void copySaveToSlot(String slot, HeroClass cl) {
		GLog.toFile("Saving: class :%s slot: %s", cl.toString(), slot);
		
		File slotDir = FileSystem.getInteralStorageFile(slot)
				.getAbsoluteFile();
		
		File[] slotFiles = slotDir.listFiles();
		
		if (slotFiles != null) {
			for (File file : slotFiles) {
				String path = file.getAbsolutePath();
				if (isRelatedTo(path, cl)) {
					GLog.toFile("deleting %s", path);
					if(!file.delete()) {
						GLog.toFile("Failed to delete file: %s !", path);
					}
				}
			}
		}

		String[] files = Game.instance().fileList();

		for (String file : files) {
			if (isRelatedTo(file, cl)) {
				
				String from = FileSystem.getInteralStorageFile(file).getAbsolutePath();
				String to = FileSystem.getInteralStorageFile(slot + "/" + file).getAbsolutePath();
				
				GLog.toFile("storing file: %s, (%s -> %s)", file, from, to);
				
				FileSystem.copyFile(from,to);
			}
		}
	}

	public static void deleteLevels(HeroClass cl) {
		GLog.toFile("Deleting levels: class :%s", cl.toString());
		
		String[] files = Game.instance().fileList();

		for (String file : files) {
			if (file.endsWith(".dat") && hasClassTag(cl, file)) {
				GLog.toFile("deleting: %s", file);
				if(!Game.instance().deleteFile(file)){
					GLog.toFile("Failed to delete file: %s !", file);
				}
			}
		}
	}

	public static void deleteGameFile(HeroClass cl) {
		String gameFile = gameFile(cl);
		GLog.toFile("Deleting gamefile: class :%s file: %s", cl.toString(), gameFile);
		Game.instance().deleteFile(gameFile);
	}

	public static String gameFile(HeroClass cl) {

		if (ModdingMode.mode()) {
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
			throw new RuntimeException("unknown hero class!");
		}
	}

	public static String depthFile(HeroClass cl, int depth, String levelKind) {
		return Utils.format(levelKind + "_" + _depthFile(cl), depth);
	}

	private static String _depthFile(HeroClass cl) {

		if (ModdingMode.mode()) {
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
			throw new RuntimeException("unknown hero class!");
		}
	}
}
