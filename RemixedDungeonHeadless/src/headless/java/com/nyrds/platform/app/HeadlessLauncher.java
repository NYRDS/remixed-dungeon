package com.nyrds.platform.app;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.pixeldungeon.utils.GameControl;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.game.RemixedDungeon;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.utils.GLog;

/**
 * Headless Remixed Dungeon: no graphics, no sound, no GL context.
 * A plain thread pumps GameLoop.onFrame(); scenes exist, logic ticks, nothing draws.
 *
 * Usage:
 *   --webserver[=port]   HTTP debug API (default port 8080)
 *   --tui                interactive stdin REPL (see Tui)
 *   --class=NAME         hero class for the automated start (WARRIOR, ...)
 *   --difficulty=N       difficulty 0..9
 *   --turns=N            exit after N in-game turns (exit code 0), 2 if hero died with --quit-on-death
 *   --quit-on-death      exit(2) when the hero dies
 *   --mod=NAME           active mod
 *   --fps=N              frame pacing (default 30)
 */
public class HeadlessLauncher {

	public static void main(String[] args) {
		BuildConfig.init(args);

		System.setProperty("https.protocols", "TLSv1.2");

		Flags flags = parse(args);

		if (flags.fps > 0) {
			Game.frameInterval = Math.max(1, 1000 / flags.fps);
		}
		if (flags.mod != null) {
			System.setProperty("remixed.mod", flags.mod);
			GamePreferences.activeMod(flags.mod);
		}

		GamePreferences.noSound = true;
		GameLoop.headless = true;

		RemixedDungeon game = new RemixedDungeon();
		game.create();
		game.startLoop();

		if (flags.webServerPort > 0) {
			startWebServer(flags.webServerPort);
		}

		waitForLoopReady();

		if (flags.heroClass != null) {
			String heroClass = flags.heroClass;
			int difficulty = flags.difficulty;
			GameLoop.pushUiTaskAndWait(() -> GameControl.startNewGame(heroClass, difficulty, true));
			waitForHeroReady();
		}

		if (flags.turns >= 0 || flags.quitOnDeath) {
			startMonitor(game, flags);
		}

		if (flags.tui) {
			new Tui(game).run();
		}

		// no TUI and no monitor: main thread idles, loop keeps ticking (webserver mode)
		if (!flags.tui && flags.turns < 0 && !flags.quitOnDeath) {
			try {
				Thread.currentThread().join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private static void waitForHeroReady() {
		// level load (InterlevelScene) is async - wait for the hero to actually stand in the world
		long deadline = System.currentTimeMillis() + 30000;
		while (System.currentTimeMillis() < deadline) {
			if (Dungeon.hero != null && Dungeon.level != null) {
				System.out.println("Game ready: " + Dungeon.hero.className()
						+ " on depth " + Dungeon.depth);
				return;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return;
			}
		}
		System.err.println("Headless: game start timed out");
		Game.exit(1);
	}

	private static void startWebServer(int port) {
		Thread serverThread = new Thread(() -> {
			try {
				WebServer server = new WebServer(port);
				server.start();
				System.out.println("WebServer running at: " + WebServer.getServerAddress());
			} catch (Exception e) {
				System.err.println("Failed to start WebServer: " + e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}, "WebServer-Thread");
		serverThread.setDaemon(true);
		serverThread.start();
	}

	private static void waitForLoopReady() {
		// pushUiTaskAndWait only drains once framesSinceInit > 2, give the loop a moment
		while (GameLoop.instance() == null || GameLoop.instance().framesSinceInit < 3) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private static void startMonitor(Game game, Flags flags) {
		Thread monitor = new Thread(() -> {
			int turnsPassed = 0;

			while (true) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					return;
				}

				if (Dungeon.hero != null && !Dungeon.hero.isAlive()) {
					if (flags.quitOnDeath) {
						GLog.i("Headless: hero died after %d turns", turnsPassed);
						Game.exit(2);
						return;
					}
					continue;
				}

				if (flags.turns >= 0 && Dungeon.hero != null && Dungeon.hero.isAlive()) {
					// turn-based mode: time only moves when the hero acts - actively wait one turn
					GameLoop.pushUiTaskAndWait(() -> Dungeon.hero.spendAndNext(1f));
					turnsPassed++;

					if (turnsPassed >= flags.turns) {
						GLog.i("Headless: %d turns simulated, exiting", turnsPassed);
						System.out.println("Headless: " + turnsPassed + " turns simulated, exiting");
						GameLoop.pushUiTaskAndWait(() -> Dungeon.save(true));
						Game.exit(0);
						return;
					}
				}
			}
		}, "headless-monitor");
		monitor.setDaemon(true);
		monitor.start();
	}

	private static class Flags {
		int webServerPort = -1;
		boolean tui = false;
		String heroClass = null;
		int difficulty = 0;
		int turns = -1;
		boolean quitOnDeath = false;
		String mod = null;
		int fps = -1;
	}

	private static Flags parse(String[] args) {
		Flags flags = new Flags();
		for (String arg : args) {
			if (arg == null) {
				continue;
			}
			if (arg.equals("--tui")) {
				flags.tui = true;
			} else if (arg.equals("--webserver")) {
				flags.webServerPort = 8080;
			} else if (arg.startsWith("--webserver=")) {
				flags.webServerPort = Integer.parseInt(arg.substring("--webserver=".length()));
			} else if (arg.startsWith("--class=")) {
				flags.heroClass = arg.substring("--class=".length()).toUpperCase();
			} else if (arg.startsWith("--difficulty=")) {
				flags.difficulty = Integer.parseInt(arg.substring("--difficulty=".length()));
			} else if (arg.startsWith("--turns=")) {
				flags.turns = Integer.parseInt(arg.substring("--turns=".length()));
			} else if (arg.equals("--quit-on-death")) {
				flags.quitOnDeath = true;
			} else if (arg.startsWith("--mod=")) {
				flags.mod = arg.substring("--mod=".length());
			} else if (arg.startsWith("--fps=")) {
				flags.fps = Integer.parseInt(arg.substring("--fps=".length()));
			} else if (arg.equals("--help") || arg.equals("-h")) {
				System.out.println("Remixed Dungeon headless");
				System.out.println("  --webserver[=port]  HTTP debug API");
				System.out.println("  --tui               interactive stdin REPL");
				System.out.println("  --class=NAME        hero class (WARRIOR, MAGE, ...)");
				System.out.println("  --difficulty=N      difficulty");
				System.out.println("  --turns=N           exit after N turns");
				System.out.println("  --quit-on-death     exit(2) when hero dies");
				System.out.println("  --mod=NAME          active mod");
				System.out.println("  --fps=N             frame pacing (default 30)");
				System.exit(0);
			}
		}
		return flags;
	}
}
