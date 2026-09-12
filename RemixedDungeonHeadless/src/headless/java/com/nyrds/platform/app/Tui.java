package com.nyrds.platform.app;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.actions.Ascend;
import com.nyrds.pixeldungeon.ml.actions.Attack;
import com.nyrds.pixeldungeon.ml.actions.Descend;
import com.nyrds.pixeldungeon.ml.actions.Move;
import com.nyrds.pixeldungeon.ml.actions.PickUp;
import com.nyrds.pixeldungeon.ml.actions.Unlock;
import com.nyrds.platform.game.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.utils.GLog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

// headless TUI: stdin REPL over the same primitives the HTTP debug API uses
public class Tui {

	private static final Map<String, int[]> DIRS = new HashMap<>();

	static {
		DIRS.put("n", new int[]{0, -1});
		DIRS.put("s", new int[]{0, 1});
		DIRS.put("w", new int[]{-1, 0});
		DIRS.put("e", new int[]{1, 0});
		DIRS.put("nw", new int[]{-1, -1});
		DIRS.put("ne", new int[]{1, -1});
		DIRS.put("sw", new int[]{-1, 1});
		DIRS.put("se", new int[]{1, 1});
		// vi keys
		DIRS.put("h", new int[]{-1, 0});
		DIRS.put("j", new int[]{0, 1});
		DIRS.put("k", new int[]{0, -1});
		DIRS.put("l", new int[]{1, 0});
		DIRS.put("y", new int[]{-1, -1});
		DIRS.put("u", new int[]{1, -1});
		DIRS.put("b", new int[]{-1, 1});
		DIRS.put("n2", new int[]{0, 1});
	}

	private final Game game;
	private final BufferedReader in;

	public Tui(Game game) {
		this.game = game;
		this.in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
	}

	public void run() {
		System.out.println("Remixed Dungeon headless TUI. 'help' for commands.");
		System.out.print("rpd> ");
		System.out.flush();
		try {
			String line;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					try {
						if (!execute(line)) {
							return;
						}
					} catch (Exception e) {
						System.out.println("error: " + e.getMessage());
					}
				}
				System.out.print("rpd> ");
				System.out.flush();
			}
		} catch (IOException e) {
			GLog.w("tui stdin closed: %s", e.getMessage());
		}
	}

	// returns false to quit
	private boolean execute(String line) {
		String[] parts = line.split("\\s+", 2);
		String cmd = parts[0].toLowerCase();
		String arg = parts.length > 1 ? parts[1] : "";

		switch (cmd) {
			case "quit":
			case "exit":
				Game.exit(0);
				return false;
			case "help":
				help();
				return true;
			case "map":
				printMap();
				return true;
			case "status":
			case "st":
				printStatus();
				return true;
			case "mobs":
				printMobs();
				return true;
			case "inv":
				printInv();
				return true;
			case "log":
				for (String msg : GLog.getRecentMessagesSinceLastCall()) {
					System.out.println(msg);
				}
				return true;
			case "move":
			case "m":
				move(arg);
				return true;
			case "attack":
			case "a":
				attack(arg);
				return true;
			case "pick":
				action(new PickUp(Dungeon.hero.getPos()));
				return true;
			case "unlock":
				action(new Unlock(cellFromDir(arg)));
				return true;
			case "descend":
			case "down":
				action(new Descend(stairsCell(true)));
				return true;
			case "ascend":
			case "up":
				action(new Ascend(stairsCell(false)));
				return true;
			case "wait":
				waitTurns(parseOr(arg, 1));
				return true;
			case "auto":
				waitTurns(parseOr(arg, 10));
				return true;
			case "save":
				GameLoop.pushUiTaskAndWait(() -> Dungeon.save(true));
				System.out.println("saved");
				return true;
			default:
				System.out.println("unknown command: " + cmd + " ('help' lists commands)");
				return true;
		}
	}

	private void help() {
		System.out.println("map            ASCII map (visible + mapped cells)");
		System.out.println("status         hero vitals, depth, position");
		System.out.println("mobs           visible mobs");
		System.out.println("inv            backpack");
		System.out.println("log            recent game log");
		System.out.println("move <dir>     step once (n/s/e/w/ne/nw/se/sw or hjklyub)");
		System.out.println("move x,y       move toward cell x,y");
		System.out.println("attack <dir>   attack adjacent cell");
		System.out.println("pick           pick up under hero");
		System.out.println("unlock <dir>   unlock adjacent door");
		System.out.println("descend/ascend take the stairs");
		System.out.println("wait [n]       idle n turns");
		System.out.println("auto [n]       same as wait (autoplay n turns)");
		System.out.println("save           force a save");
		System.out.println("quit           exit");
	}

	private void move(String arg) {
		if (arg.contains(",")) {
			String[] xy = arg.split(",");
			int x = Integer.parseInt(xy[0].trim());
			int y = Integer.parseInt(xy[1].trim());
			int cell = x + y * Dungeon.level.getWidth();
			action(new Move(cell));
			return;
		}
		int[] d = DIRS.get(arg.toLowerCase());
		if (d == null) {
			System.out.println("dir? n/s/e/w/ne/nw/se/sw or x,y");
			return;
		}
		action(new Move(cellFromDir(arg)));
	}

	private int cellFromDir(String arg) {
		int[] d = DIRS.get(arg.toLowerCase());
		if (d == null) {
			return Dungeon.hero.getPos();
		}
		Level level = Dungeon.level;
		int pos = Dungeon.hero.getPos();
		int x = pos % level.getWidth() + d[0];
		int y = pos / level.getWidth() + d[1];
		return level.cellValid(x, y) ? x + y * level.getWidth() : pos;
	}

	private void attack(String arg) {
		int cell = cellFromDir(arg);
		Char target = null;
		for (Mob mob : Dungeon.level.mobs) {
			if (mob.getPos() == cell) {
				target = mob;
				break;
			}
		}
		if (target == null) {
			System.out.println("nobody there");
			return;
		}
		Char finalTarget = target;
		action(new Attack(finalTarget));
	}

	private int stairsCell(boolean down) {
		Level level = Dungeon.level;
		int pos = Dungeon.hero.getPos();
		if (down && level.isExit(pos)) {
			return pos;
		}
		if (!down && pos == level.entrance) {
			return pos;
		}
		// nearest visible stair
		int best = -1;
		int bestDist = Integer.MAX_VALUE;
		for (int cell = 0; cell < level.getLength(); cell++) {
			boolean stair = down ? level.isExit(cell) : cell == level.entrance;
			if (stair && Dungeon.visible[cell]) {
				int dist = level.distance(pos, cell);
				if (dist < bestDist) {
					bestDist = dist;
					best = cell;
				}
			}
		}
		return best >= 0 ? best : pos;
	}

	private void action(com.nyrds.pixeldungeon.ml.actions.CharAction action) {
		if (Dungeon.hero == null || Dungeon.level == null) {
			System.out.println("no game running");
			return;
		}
		GameLoop.pushUiTask(() -> Dungeon.hero.nextAction(action));
	}

	private void waitTurns(int turns) {
		for (int i = 0; i < turns; i++) {
			if (Dungeon.hero == null) {
				System.out.println("no game running");
				return;
			}
			if (!Dungeon.hero.isAlive()) {
				System.out.println("hero died while waiting");
				return;
			}
			GameLoop.pushUiTaskAndWait(() -> Dungeon.hero.spendAndNext(1f));
		}
		System.out.println("waited " + turns + " turns");
	}

	private void printStatus() {
		if (Dungeon.hero == null || Dungeon.level == null) {
			System.out.println("no game running");
			return;
		}
		Hero hero = Dungeon.hero;
		Level level = Dungeon.level;
		int pos = hero.getPos();
		System.out.printf("%s  hp %d/%d  depth %d (%s)  pos %d,%d  lvl %d  exp/next %d%n",
				hero.className(),
				hero.hp(), hero.ht(),
				Dungeon.depth,
				com.nyrds.pixeldungeon.utils.DungeonGenerator.getCurrentLevelId(),
				pos % level.getWidth(), pos / level.getWidth(),
				hero.lvl(), hero.expToLevel());
	}

	private void printMobs() {
		if (Dungeon.level == null) {
			System.out.println("no game running");
			return;
		}
		boolean any = false;
		for (Mob mob : Dungeon.level.mobs) {
			if (Dungeon.visible[mob.getPos()]) {
				any = true;
				Level level = Dungeon.level;
				int pos = mob.getPos();
				System.out.printf("%-20s hp %3d/%-3d at %d,%d%s%n",
						mob.name(),
						mob.hp(), mob.ht(),
						pos % level.getWidth(), pos / level.getWidth(),
						mob.friendly(Dungeon.hero) ? " (friendly)" : "");
			}
		}
		if (!any) {
			System.out.println("nothing visible");
		}
	}

	private void printInv() {
		if (Dungeon.hero == null) {
			System.out.println("no game running");
			return;
		}
		int i = 1;
		for (Item item : Dungeon.hero.getBelongings().backpack.items) {
			System.out.printf("%2d) %-24s %s%n", i++, item.name(), item.status() != null ? "[" + item.status() + "]" : "");
		}
	}

	private void printMap() {
		if (Dungeon.level == null) {
			System.out.println("no game running");
			return;
		}
		Level level = Dungeon.level;
		int width = level.getWidth();
		int height = level.getHeight();
		int heroPos = Dungeon.hero != null ? Dungeon.hero.getPos() : -1;

		StringBuilder sb = new StringBuilder();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int cell = y * width + x;
				if (cell == heroPos) {
					sb.append('@');
					continue;
				}
				boolean known = Dungeon.visible[cell] || level.mapped[cell];
				if (!known) {
					sb.append(' ');
					continue;
				}
				sb.append(terrainChar(level, cell));
			}
			sb.append('\n');
		}
		System.out.print(sb);
	}

	private char terrainChar(Level level, int cell) {
		if (Dungeon.visible[cell]) {
			for (Mob mob : Dungeon.level.mobs) {
				if (mob.getPos() == cell) {
					return mob.friendly(Dungeon.hero) ? 'p' : 'g';
				}
			}
			if (level.isExit(cell)) {
				return '>';
			}
			if (cell == level.entrance) {
				return '<';
			}
			if (level.getHeap(cell) != null) {
				return '*';
			}
		}

		int t = level.map[cell];
		switch (t) {
			case Terrain.CHASM: return '^';
			case Terrain.GRASS: return '"';
			case Terrain.HIGH_GRASS: return '&';
			case Terrain.WALL:
			case Terrain.WALL_DECO:
				return '#';
			case Terrain.DOOR:
			case Terrain.LOCKED_DOOR:
				return '+';
			case Terrain.OPEN_DOOR: return '/';
			case Terrain.ENTRANCE: return '<';
			case Terrain.EXIT: return '>';
			case Terrain.WATER: return '~';
			case Terrain.EMBERS: return '\u00B7';
			case Terrain.SECRET_DOOR: return '#';
			default:
				return level.passable[cell] ? '.' : '#';
		}
	}

	private static int parseOr(String arg, int def) {
		try {
			return Integer.parseInt(arg);
		} catch (Exception e) {
			return def;
		}
	}
}
