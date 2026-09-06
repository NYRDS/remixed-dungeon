package com.nyrds.platform.game;

import com.badlogic.gdx.Gdx;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.utils.GameControl;
import com.nyrds.platform.util.PUtil;
import com.watabou.pixeldungeon.scenes.TitleScene;

import org.teavm.jso.JSBody;

/**
 * Web debug entrypoints: query params that skip the menu flow.
 * <p>
 * ?ep=newgame[&hero=WARRIOR&difficulty=2] — boot straight into a fresh dungeon
 * (deletes the current game, like New Game; intro story is skipped). The title
 * screen is still built first so all boot-time init happens as usual, then the
 * new game is triggered as if the user had clicked through the menus.
 */
final class DebugEntryPoints {

	@JSBody(params = {}, script = "return window.location.search;")
	private static native String locationSearch();

	static void scheduleFromUrl() {
		String search = locationSearch();
		if (search == null || search.isEmpty()) {
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

		final String fHero = hero;
		final int fDifficulty = difficulty;

		PUtil.slog("game", "entrypoint: newgame hero=" + fHero + " difficulty=" + fDifficulty);

		GameLoop.pushUiTask(new Runnable() {
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
				if (!(GameLoop.scene() instanceof TitleScene)) {
					return; // something else took over - stand down
				}
				// uiTasks run between frames, so TitleScene.create() has fully returned here
				PUtil.slog("EP2", "firing startNewGame " + fHero + " d" + fDifficulty);
				GamePreferences.intro(false);
				GameControl.startNewGame(fHero, fDifficulty, false);
				PUtil.slog("EP2", "startNewGame returned");
			}
		});

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
