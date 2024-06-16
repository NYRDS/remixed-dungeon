package com.watabou.pixeldungeon;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.GamesInProgress.Info;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;

import java.io.File;

public class SaveUtils {

	private static final String AUTO_SAVE = "autoSave";
	private static final String PREV_SAVE = "prevSave";

	static private boolean hasClassTag(HeroClass cl, String fname) {
		return fname.contains(cl.tag());
	}

	public static void loadGame(String slot, HeroClass heroClass) {
		
		GLog.toFile("Loading: class :%s slot: %s", heroClass.toString(), slot);
		Dungeon.deleteGame(true);
		copyFromSaveSlot(slot, heroClass);

		InterlevelScene.Do(InterlevelScene.Mode.CONTINUE);

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
		
		return Utils.EMPTY_STRING;
	}
	
	public static boolean slotUsed(String slot, HeroClass cl) {
		String[] slotFiles = FileSystem.getInternalStorageFile(slot)
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

	public static boolean isRelatedTo(String path,HeroClass cl) {
		return ( path.endsWith(".dat") && hasClassTag(cl, path) ) || path.endsWith(gameFile(cl)) || path.endsWith(Bones.getBonesFile());
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
	
	private static void copyFromSaveSlot(String slot, HeroClass heroClass) {
		String[] files = FileSystem.getInternalStorageFile(slot).list();
		if(files == null) {
			return;
		}
		
		for (String file : files) {
			if (isRelatedTo(file, heroClass)) {

				String from = FileSystem.getInternalStorageFile(slot + "/" + file).getAbsolutePath();
				String to = FileSystem.getInternalStorageFile(file).getAbsolutePath();
				FileSystem.copyFile(from, to);
			}
		}
	}

	public static void deleteSaveFromSlot(String slot, HeroClass cl) {
		File slotDir = FileSystem.getInternalStorageFile(slot)
				.getAbsoluteFile();

		File[] slotFiles = slotDir.listFiles();

		if (slotFiles != null) {
			for (File file : slotFiles) {
				String path = file.getAbsolutePath();
				if (isRelatedTo(path, cl)) {
					if(!file.delete()) {
						GLog.toFile("Failed to delete file: %s !", path);
					}
				}
			}
		}
	}

	public static void copySaveToSlot(String slot, HeroClass cl) {
		deleteSaveFromSlot(slot, cl);

		String[] files = Game.instance().fileList();

		for (String file : files) {
			if (isRelatedTo(file, cl)) {
				
				String from = FileSystem.getInternalStorageFile(file).getAbsolutePath();
				String to = FileSystem.getInternalStorageFile(slot + "/" + file).getAbsolutePath();

				FileSystem.copyFile(from,to);
			}
		}
	}

	public static void deleteLevels(HeroClass cl) {
		String[] files = Game.instance().fileList();

		for (String file : files) {
			if (file.endsWith(".dat") && hasClassTag(cl, file)) {
				if(!Game.instance().deleteFile(file)){
					GLog.toFile("Failed to delete file: %s !", file);
				}
			}
		}
	}

	public static void deleteGameFile(HeroClass cl) {
		String gameFile = gameFile(cl);
		Game.instance().deleteFile(gameFile);
	}

	public static String gameFile(HeroClass cl) {
		return cl.tag()+".dat";
	}

	public static String depthFileForLoad(HeroClass cl, int depth, String levelKind, String levelId) {
		String newFormat = depthFileForSave(cl, depth, levelKind, levelId);
		if(FileSystem.getInternalStorageFile(newFormat).exists()) {
			return newFormat;
		}
		
		return Utils.format(levelKind + "_" + _depthFile(cl), depth);
	}

	public static String depthFileForSave(HeroClass heroClass, int levelDepth, String levelKind, String levelId) {
		return Utils.format(levelKind + "_" + levelId + "_" + _depthFile(heroClass), levelDepth);
	}
	
	private static String _depthFile(HeroClass cl) {
		return cl.tag()+"%d.dat";
	}

	static public String buildSlotFromTag(String tag) {
		return ModdingMode.activeMod() + "_" + tag + "_" + GameLoop.getDifficulty();
	}

	public static String getAutoSave() {
		return buildSlotFromTag(AUTO_SAVE);
	}

	public static String getPrevSave() {
		return buildSlotFromTag(PREV_SAVE);
	}

	public static void preview(Info info, Bundle bundle) {
		info.depth = bundle.getInt(Dungeon.DEPTH);
		if (info.depth == -1) {
			info.depth = bundle.getInt(Statistics.DEEPEST); // FIXME
		}

		Bundle heroBundle = bundle.getBundle(Dungeon.HERO);
		info.level = heroBundle.getInt(Char.LEVEL);
		info.difficulty  = heroBundle.getInt(Hero.DIFFICULTY);
	}
}
