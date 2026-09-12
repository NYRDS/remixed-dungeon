package com.nyrds.platform.storage;

import static com.nyrds.pixeldungeon.ml.BuildConfig.SAVES_PATH;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.util.ModdingBase;
import com.watabou.pixeldungeon.Bones;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.GamesInProgress;
import com.watabou.pixeldungeon.GamesInProgress.Info;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// headless: java.io.File-based port of the desktop SaveUtils
public class SaveUtils {

	private static final String AUTO_SAVE = "autoSave";

	static File local(String filename) {
		return new File(FileSystem.getUserDataPath(SAVES_PATH) + File.separator + filename);
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
		if (slotUsed(slot, cl)) {

			String localName = slot + "/" + gameFile(cl);

			return GamesInProgress.checkFile(localName);
		}

		return null;
	}

	public static boolean slotUsed(String slot, HeroClass cl) {
		File[] slotFiles = local(slot).listFiles();
		if (slotFiles == null) {
			return false;
		}

		for (File file : slotFiles) {
			if (file.getName().endsWith(gameFile(cl))) {
				return true;
			}
		}

		return false;
	}

	public static boolean isRelatedTo(String path, HeroClass cl) {
		return (path.endsWith(".dat") && hasClassTag(cl, path)) || path.endsWith(gameFile(cl)) || path.endsWith(Bones.getBonesFile());
	}

	public static void copyAllClassesToSlot(String slot) {
		for (HeroClass hc : HeroClass.values()) {
			copySaveToSlot(slot, hc);
		}
	}

	public static void copyAllClassesFromSlot(String slot) {
		for (HeroClass hc : HeroClass.values()) {
			copyFromSaveSlot(slot, hc);
		}
	}

	public static void deleteGameAllClasses() {
		for (HeroClass hc : HeroClass.values()) {
			deleteLevels(hc);
			deleteGameFile(hc);
		}
	}

	private static void copyFromSaveSlot(String slot, HeroClass heroClass) {
		File[] files = local(slot).listFiles();

		if (files == null) {
			return;
		}

		for (File file : files) {
			if (isRelatedTo(file.getName(), heroClass)) {

				String from = local(slot + File.separator + file.getName()).getAbsolutePath();
				String to = local(file.getName()).getAbsolutePath();
				FileSystem.copyFile(from, to);
			}
		}
	}

	public static void deleteSaveFromSlot(String slot, HeroClass cl) {

		File slotDir = local(slot).getAbsoluteFile();

		File[] slotFiles = slotDir.listFiles();

		if (slotFiles != null) {
			for (File file : slotFiles) {
				String path = file.getAbsolutePath();
				if (isRelatedTo(path, cl)) {
					if (!file.delete()) {
						GLog.toFile("Failed to delete file: %s !", path);
					}
				}
			}
		}
	}

	public static void copySaveToSlot(String slot, HeroClass cl) {
		deleteSaveFromSlot(slot, cl);

		File savesDir = local("");
		if (!savesDir.exists()) {
			savesDir.mkdirs();
		}
		String[] files = savesDir.list();
		if (files == null) {
			return;
		}

		for (String file : files) {
			if (isRelatedTo(file, cl)) {

				String from = local(file).getAbsolutePath();
				String to = local(slot + File.separator + file).getAbsolutePath();

				FileSystem.copyFile(from, to);
			}
		}
	}

	public static void deleteLevels(HeroClass cl) {
		File base = local("");
		File[] files = base.listFiles();

		// caveman: snap-brm followup - the new-game purge failed silently once
		// (stale chess level board resurrected into a fresh game). log a summary
		// + failures only - per-file lines spammed users' device logs.
		int deleted = 0;
		List<String> failures = new ArrayList<>();

		if (files != null) {
			for (File file : files) {
				String path = file.getPath();
				if (path.endsWith(".dat") && hasClassTag(cl, path)) {
					if (file.delete()) {
						deleted++;
					} else {
						failures.add(path);
					}
				}
			}
		}

		GLog.toFile("deleteLevels: purged %d files for %s%s",
				deleted, cl.tag(), failures.isEmpty() ? "" : " FAILED: " + String.join(", ", failures));
	}

	public static void deleteGameFile(HeroClass cl) {
		String gameFile = gameFile(cl);
		File gf = local(gameFile);
		GLog.toFile("deleteGameFile: %s exists=%b", gf.getAbsolutePath(), gf.exists());
		gf.delete();
	}

	public static String gameFile(HeroClass cl) {
		return cl.tag() + ".dat";
	}

	public static String modDataFile() {
		return "ModDataFor_" + ModdingBase.activeMod() + ".dat";
	}

	public static String depthFileForSave(HeroClass heroClass, int levelDepth, String levelKind, String levelId) {
		return Utils.format(levelKind + "_" + levelId + "_" + _depthFile(heroClass), levelDepth);
	}

	private static String _depthFile(HeroClass cl) {
		return cl.tag() + "%d.dat";
	}

	static public String buildSlotFromTag(String tag, int difficulty) {
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
			info.depth = bundle.getInt(com.watabou.pixeldungeon.Statistics.DEEPEST); // FIXME
		}

		Bundle heroBundle = bundle.getBundle(Dungeon.HERO);
		info.level = heroBundle.getInt(Char.LEVEL);
		info.difficulty = heroBundle.getInt(Hero.DIFFICULTY);
	}
}
