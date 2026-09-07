package com.nyrds.platform.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.utils.GameControl;
import com.nyrds.pixeldungeon.utils.Position;
import com.nyrds.platform.storage.SaveUtils;
import com.nyrds.platform.util.PUtil;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.TitleScene;

import org.teavm.jso.JSBody;

/**
 * Web debug entrypoints: query params that skip the menu flow.
 * <p>
 * ?ep=newgame[&hero=WARRIOR&difficulty=2][&level=<levelId>][&x=<n>&y=<n>|&cell=<n>]
 * — boot straight into a fresh dungeon (deletes the current game, like New
 * Game; intro story is skipped). The title screen is still built first so all
 * boot-time init happens as usual, then the new game is triggered as if the
 * user had clicked through the menus.
 * <p>
 * Optional placement: level is a levelsDesc id (e.g. Town); x&y (or cell)
 * place the hero on that level via Hero.teleportTo — same level blinks the
 * hero there, a different levelId goes through InterlevelScene(RETURN).
 * x/y without level place on the starting level.
 */
final class DebugEntryPoints {

	private static final int MAX_GAME_SCENE_POLLS = 900;

	@JSBody(params = {}, script = "return window.location.search;")
	private static native String locationSearch();

	static void scheduleFromUrl() {
		String search = locationSearch();
		if (search == null || search.isEmpty()) {
			return;
		}

		if ("continue".equals(param(search, "ep"))) {
			scheduleContinue();
			return;
		}

		if (!"newgame".equals(param(search, "ep"))) {
			return;
		}

		String hero = param(search, "hero");
		if (hero == null) {
			hero = "WARRIOR";
		}
		hero = hero.toUpperCase();

		int difficulty = 2;
		String sDiff = param(search, "difficulty");
		if (sDiff != null) {
			try {
				difficulty = Integer.parseInt(sDiff);
			} catch (NumberFormatException e) {
				PUtil.slog("game", "entrypoint: bad difficulty '" + sDiff + "', using 2");
			}
		}

		final String fLevel = param(search, "level");

		// &save=1: run the real Dungeon.save once the hero is in the game
		// (debug verification of the web save pipeline)
		final boolean fSave = "1".equals(param(search, "save"));

		int x = -1, y = -1, cell = -1;
		try {
			String sx = param(search, "x");
			String sy = param(search, "y");
			if (sx != null && sy != null) {
				x = Integer.parseInt(sx);
				y = Integer.parseInt(sy);
			} else if (sx != null || sy != null) {
				PUtil.slog("game", "entrypoint: x and y must be given together, ignoring them");
			}
			String sCell = param(search, "cell");
			if (sCell != null) {
				cell = Integer.parseInt(sCell);
			}
		} catch (NumberFormatException e) {
			PUtil.slog("game", "entrypoint: bad placement coordinates, ignoring them");
			x = -1;
			y = -1;
			cell = -1;
		}

		final int fX = x, fY = y, fCell = cell;
		final boolean place = fLevel != null || fCell >= 0 || fX >= 0;
		final String fHero = hero;
		final int fDifficulty = difficulty;

		if (place || fSave) {
			PUtil.slog("game", "entrypoint: newgame hero=" + hero + " difficulty=" + difficulty
					+ (place ? " level=" + fLevel + " x=" + fX + " y=" + fY + " cell=" + fCell : "")
					+ (fSave ? " save=1" : ""));
		} else {
			PUtil.slog("game", "entrypoint: newgame hero=" + hero + " difficulty=" + difficulty);
		}

		GameLoop.pushUiTask(new Runnable() {

			private int polls;
			private boolean fired;

			@Override
			public void run() {
				bumpPollCount();
				if (GameLoop.scene() == null) {
					// NOT pushUiTask: reposting into the uiTask queue from
					// inside its own drain loop spins the drain forever.
					// Gdx runnable queue is copied-then-drained per frame.
					Gdx.app.postRunnable(this);
					return;
				}
				if (!fired) {
					if (!(GameLoop.scene() instanceof TitleScene)) {
						return; // something else took over - stand down
					}
					// uiTasks run between frames, so TitleScene.create() has fully returned here
					PUtil.slog("EP2", "firing startNewGame " + fHero + " d" + fDifficulty);
					GamePreferences.intro(false);
					GameControl.startNewGame(fHero, fDifficulty, false);
					PUtil.slog("EP2", "startNewGame returned");
					fired = true;
					if (!place && !fSave) {
						return;
					}
					Gdx.app.postRunnable(this);
					return;
				}
				// placement/save phase: wait for GameScene with a valid hero
				if (!(GameLoop.scene() instanceof GameScene)) {
					if (GameLoop.scene() instanceof TitleScene || ++polls > MAX_GAME_SCENE_POLLS) {
						PUtil.slog("EP2", "entrypoint: game scene never came up, placement skipped");
						return;
					}
					Gdx.app.postRunnable(this);
					return;
				}
				if (Dungeon.hero == null || Dungeon.hero.invalid() || Dungeon.level == null) {
					if (++polls > MAX_GAME_SCENE_POLLS) {
						PUtil.slog("EP2", "entrypoint: hero never became valid, placement skipped");
						return;
					}
					Gdx.app.postRunnable(this);
					return;
				}
				if (fSave) {
					try {
						PUtil.slog("EP2", "saving game");
						Dungeon.save(false);
						PUtil.slog("EP2", "game saved");
						FileHandle[] rootList = Gdx.files.local("/").list();
						StringBuilder names = new StringBuilder();
						for (FileHandle f : rootList) {
							names.append(f.path()).append("(").append(f.name()).append(") ");
						}
						PUtil.slog("EP2", "root list [n=" + rootList.length + "]: " + names);
						PUtil.slog("EP2", "slotUsed = " + SaveUtils.slotUsed(
								SaveUtils.getAutoSave(), Dungeon.heroClass));
					} catch (Exception e) {
						PUtil.slog("EP2", "save failed: " + e.getMessage());
					}
					if (!place) {
						return;
					}
				}
				placeHero(fLevel, fX, fY, fCell);
			}
		});
	}

	private static void scheduleContinue() {
		PUtil.slog("game", "entrypoint: continue");
		GameLoop.pushUiTask(new Runnable() {

			private int polls;

			@Override
			public void run() {
				bumpPollCount();
				if (!(GameLoop.scene() instanceof TitleScene)) {
					if (++polls > MAX_GAME_SCENE_POLLS) {
						PUtil.slog("EP2", "continue: title scene never came up");
						return;
					}
					Gdx.app.postRunnable(this);
					return;
				}
				// GameLoop.difficulty is only set by the difficulty-select UI
				// (or ?ep=newgame) - scan the sane range instead of trusting it
				for (int difficulty = 0; difficulty <= 5; difficulty++) {
					String slot = SaveUtils.buildSlotFromTag("autoSave", difficulty);
					for (HeroClass heroClass : HeroClass.values()) {
						if (SaveUtils.slotUsed(slot, heroClass)) {
							PUtil.slog("EP2", "continue: loading " + slot + " for " + heroClass);
							// deleteGame inside loadGame reads Dungeon.heroClass -
							// the menu flow sets it before loading, so must we
							Dungeon.heroClass = heroClass;
							GameLoop.setDifficulty(difficulty);
							SaveUtils.loadGame(slot, heroClass);
							return;
						}
					}
				}
				PUtil.slog("EP2", "continue: no autosave found");
			}
		});
	}

	private static void placeHero(String levelId, int x, int y, int cell) {
		try {
			String target = levelId != null ? levelId : Dungeon.level.levelId;
			Position position;
			if (x >= 0 && y >= 0) {
				position = new Position(target, x, y);
			} else if (cell >= 0) {
				position = new Position(target, cell);
			} else {
				position = new Position(target, -1);
			}
			PUtil.slog("EP2", "placing hero at " + target + " x=" + x + " y=" + y + " cell=" + cell);
			Dungeon.hero.teleportTo(position);
			PUtil.slog("EP2", "hero placed");
		} catch (Exception e) {
			PUtil.slog("EP2", "placement failed: " + e.getMessage());
		}
	}

	// browser debug: count entrypoint task invocations (window.__epPoll)
	@org.teavm.jso.JSBody(script =
			"window.__epPoll = (window.__epPoll || 0) + 1;")
	private static native void bumpPollCount();

	private static String param(String search, String name) {
		String prefix = name + "=";
		for (String part : search.substring(1).split("&")) {
			if (part.startsWith(prefix)) {
				return part.substring(prefix.length());
			}
		}
		return null;
	}
}
