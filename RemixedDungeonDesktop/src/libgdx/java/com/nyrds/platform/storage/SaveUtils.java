package com.nyrds.platform.storage;

import static com.nyrds.pixeldungeon.ml.BuildConfig.SAVES_PATH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.util.ModdingBase;
import com.watabou.pixeldungeon.Bones;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.GamesInProgress;
import com.watabou.pixeldungeon.GamesInProgress.Info;
import com.watabou.pixeldungeon.Statistics;
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

	static FileHandle local(String filename) {
		return Gdx.files.local(SAVES_PATH + File.separator + filename);
	}

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

	public static Info slotInfo(String slot, HeroClass cl) {
		if(slotUsed(slot, cl)) {
			
			String localName = slot +"/"+ gameFile(cl);
			
			return GamesInProgress.checkFile(localName);
		}
		
		return null;
	}
	
	public static boolean slotUsed(String slot, HeroClass cl) {
		FileHandle[] slotFiles = local(slot).list();
		if (slotFiles == null) {
			return false;
		}

		for (FileHandle file : slotFiles) {
			if (file.name().endsWith(gameFile(cl))) {
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
		FileHandle[] files = local(slot).list();

		if(files == null) {
			return;
		}
		
		for (FileHandle file : files) {
			if (isRelatedTo(file.name(), heroClass)) {

				String from = local(slot + File.separator + file.name()).file().getAbsolutePath();
				String to = local(file.name()).file().getAbsolutePath();
				FileSystem.copyFile(from, to);
			}
		}
	}

	public static void deleteSaveFromSlot(String slot, HeroClass cl) {

		File slotDir = local(slot).file().getAbsoluteFile();

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

		String[] files = local("").file().list();

		for (String file : files) {
			if (isRelatedTo(file, cl)) {
				
				String from = local(file).file().getAbsolutePath();
				String to = local(slot + File.separator + file).file().getAbsolutePath();

				FileSystem.copyFile(from,to);
			}
		}
	}

	public static void deleteLevels(HeroClass cl) {
		FileHandle[] files = local("").list();

		for (FileHandle file : files) {
			String path = file.path();
			if (path.endsWith(".dat") && hasClassTag(cl, path)) {
				file.delete();
			}
		}
	}

	public static void deleteGameFile(HeroClass cl) {
		String gameFile = gameFile(cl);
		local("").child(gameFile).delete();
	}

	public static String gameFile(HeroClass cl) {
		return cl.tag()+".dat";
	}

	public static String modDataFile() {
		return "ModDataFor_"+ ModdingBase.activeMod() + ".dat";
	}

	public static String depthFileForSave(HeroClass heroClass, int levelDepth, String levelKind, String levelId) {
		return Utils.format(levelKind + "_" + levelId + "_" + _depthFile(heroClass), levelDepth);
	}
	
	private static String _depthFile(HeroClass cl) {
		return cl.tag()+"%d.dat";
	}

	static public String buildSlotFromTag(String tag, int difficulty)  {
		return ModdingBase.activeMod() + "_" + tag + "_" + difficulty;
	}

	public static String getAutoSave() {
		return getAutoSave(GameLoop.getDifficulty());
	}

	public static String getPrevSave() {
		return getAutoSave(GameLoop.getDifficulty());
	}

	public static String getAutoSave(int dif) {
		return buildSlotFromTag(AUTO_SAVE, dif);
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
