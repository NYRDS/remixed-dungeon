# Remote-Controlled Chars (Slice 1) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remotely controlled game chars ("puppets") — real mobs in a new `RemoteControlled` AI state, driven by id over the debug HTTP API with a command-pump timeline.

**Architecture:** Puppets are ordinary `Mob`s (friend stance = `makePet(hero)`, foe = untouched) in a new AI state that never acts autonomously, executes injected `curAction`, and reverts to default AI after N idle world turns. Commands go through the game's own tap resolution (`CharUtils.actionForCell`) and the `Actor` time queue; the hero burns wait-ticks while a puppet acts, keeping turn-based fairness. Spec: `docs/superpowers/specs/2026-08-30-remote-controlled-chars-design.md`.

**Tech Stack:** Java (libGDX desktop + Android), NanoHTTPD debug API, Python `requests` test harness (`tests/http_api/`).

**Key repo facts for the implementer:**
- `MobAi` states are **shared singletons** keyed by `getClass().getSimpleName().toUpperCase()` — per-char data must live on the `Mob`, never in the state class.
- The turn-based world clock parks at the hero (`Actor.processTurnBased` early-returns when hero is current with no action). A puppet only advances when something spends time.
- All game mutation from HTTP handlers must run on the GL thread via `GameLoop.pushUiTask` + `CountDownLatch` (see `handleDebugCreateMob` in `DebugEndpoints.java` for the pattern).
- Compile checks: `./gradlew -p RemixedDungeonDesktop compileJava` and `./gradlew -c settings.android.gradle :RemixedDungeon:compileAndroidGooglePlayDebugJavaWithJavac`.
- Live tests: `cd tests/http_api && python3 test_<name>.py --start-server` (starts the game, ~4 min).

---

### Task 1: `RemoteControlled` AI state + Mob watchdog fields

**Files:**
- Create: `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/RemoteControlled.java`
- Modify: `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/mobs/Mob.java`
- Modify: `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/MobAi.java` (static block, after `registerAiState(KillOrder.class);`)

- [ ] **Step 1: Create the AI state class**

```java
package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.actions.CharAction;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

/**
 * Remote-control state: the char is driven from outside (debug HTTP API).
 * No autonomy at all - executes the injected curAction if present,
 * idles otherwise. Reverts to the pre-remote AI after remoteRevertAfter
 * idle world turns (watchdog, 0 = never).
 * Spec: docs/superpowers/specs/2026-08-30-remote-controlled-chars-design.md
 */
public class RemoteControlled extends MobAi implements AiState {

	@Override
	public void act(@NotNull Char me) {
		if (!(me instanceof Mob)) {
			// caveman: state is mob-only, but stay safe and cheap
			me.spend(Char.TICK);
			return;
		}
		Mob mob = (Mob) me;

		CharAction action = me.getCurAction();
		if (action == null) {
			// idle: count quiet world turns, revert after the limit
			if (mob.remoteRevertAfter > 0 && ++mob.remoteIdleTurns >= mob.remoteRevertAfter) {
				GLog.i("%s reverts from remote control", mob.getEntityKind());
				mob.revertRemoteControl();
				return;
			}
			me.spend(Char.TICK);
			return;
		}

		// active command - it is activity, watchdog reset
		mob.remoteIdleTurns = 0;
		action.act(me);
		// Mob.act handles the no-time-spent case (retries / spends TICK)
	}

	@Override
	public String status(Char me) {
		return "remote controlled";
	}

	@Override
	public void gotDamage(Char me, NamedEntityKind src, int dmg) {
		// caveman: no revenge, no chase. the driver decides everything.
	}
}
```

- [ ] **Step 2: Add watchdog fields + revert logic + bundle persistence to `Mob`**

In `Mob.java`, add fields near the top of the class body (after `private Carcass carcassRef;`):

```java
	// caveman: remote-control watchdog - see RemoteControlled state.
	// remoteRevertAfter persists in the bundle; counters do not (reset on load).
	public static final String REMOTE_REVERT_AFTER = "remoteRevertAfter";

	@Packable(defaultValue = "0")
	public int remoteRevertAfter = 0;

	public transient int remoteIdleTurns = 0;
	public String remoteRevertStateTag = null;
	public boolean remoteReverted = false;
```

Add a method to `Mob` (next to `releasePet()`):

```java
	public void revertRemoteControl() {
		remoteIdleTurns = 0;
		remoteReverted = true;
		setCurAction(null);
		String tag = remoteRevertStateTag != null ? remoteRevertStateTag : "WANDERING";
		remoteRevertStateTag = null;
		remoteRevertAfter = 0;
		setState(MobAi.getStateByTag(tag));
	}
```

In `Mob.storeInBundle` (already overridden), add after `bundle.put(FRACTION, fraction.ordinal());`:

```java
		bundle.put(REMOTE_REVERT_AFTER, remoteRevertAfter);
```

In `Mob.restoreFromBundle` (already overridden), add after the `fraction = ...` line:

```java
		remoteRevertAfter = bundle.optInt(REMOTE_REVERT_AFTER, 0);
		remoteIdleTurns = 0; // watchdog counts live world turns only
```

- [ ] **Step 3: Register the state**

In `MobAi.java` static block, after `registerAiState(KillOrder.class);` add:

```java
		registerAiState(RemoteControlled.class);
```

Add the import next to the other `com.nyrds.pixeldungeon.ai` imports if the file uses none (all these classes are in the same package — no import needed).

- [ ] **Step 4: Check `@Packable` import on Mob**

`Mob.java` needs `import com.nyrds.Packable;` if not already present (check the imports; `Char.java` uses the same annotation).

- [ ] **Step 5: Compile both platforms**

Run: `./gradlew -p RemixedDungeonDesktop compileJava --console=plain -q` → expect exit 0.
Run: `./gradlew -c settings.android.gradle :RemixedDungeon:compileAndroidGooglePlayDebugJavaWithJavac --console=plain -q` → expect exit 0.

- [ ] **Step 6: Commit**

```bash
git add RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/RemoteControlled.java \
        RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/mobs/Mob.java \
        RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/MobAi.java
git commit -m "feat: RemoteControlled AI state + remote watchdog fields on Mob (remote chars 1/3)"
```

---

### Task 2: Remote endpoints — spawn / possess / release / list

**Files:**
- Modify: `RemixedDungeon/src/main/java/com/nyrds/platform/app/DebugEndpoints.java` (append before the final closing brace)
- Modify: `RemixedDungeon/src/main/java/com/nyrds/platform/app/BaseWebServer.java` (route map)

- [ ] **Step 1: Add a shared helper + the four handlers to `DebugEndpoints`**

Add imports at the top of `DebugEndpoints.java`:

```java
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.RemoteControlled;
```

Append before the final closing brace of the class:

```java
	// ---- remote-controlled chars (see docs/superpowers/specs/2026-08-30-remote-controlled-chars-design.md) ----

	private static boolean isRemote(Mob mob) {
		return mob.getState().getTag().equals(MobAi.getStateByClass(RemoteControlled.class).getTag());
	}

	private static Mob findMobById(int id) {
		if (Dungeon.level == null) {
			return null;
		}
		for (Mob mob : Dungeon.level.mobs) {
			if (mob.getId() == id) {
				return mob;
			}
		}
		return null;
	}

	private static void applyStance(Mob mob, String stance) {
		if ("friend".equalsIgnoreCase(stance) && Dungeon.hero != null) {
			mob.makePet(Dungeon.hero);
		}
		// "foe" = untouched dungeon mob - nothing to do
	}

	// GET /debug/remote/spawn?type=Rat&x=&y=&stance=&revertAfter=
	public static NanoHTTPD.Response handleRemoteSpawn(NanoHTTPD.IHTTPSession session) {
		try {
			String query = session.getQueryParameterString();
			String type = null;
			String stance = "foe";
			int x = -1, y = -1;
			int revertAfter = 10;

			if (query != null && !query.isEmpty()) {
				for (String param : query.split("&")) {
					if (param.startsWith("type=")) {
						type = java.net.URLDecoder.decode(param.substring(5), "UTF-8");
					} else if (param.startsWith("stance=")) {
						stance = java.net.URLDecoder.decode(param.substring(7), "UTF-8");
					} else if (param.startsWith("revertAfter=")) {
						try {
							revertAfter = Integer.parseInt(java.net.URLDecoder.decode(param.substring(12), "UTF-8"));
						} catch (NumberFormatException ignored) {
						}
					} else if (param.startsWith("x=")) {
						try {
							x = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8"));
						} catch (NumberFormatException ignored) {
						}
					} else if (param.startsWith("y=")) {
						try {
							y = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8"));
						} catch (NumberFormatException ignored) {
						}
					}
				}
			}

			if (type == null || type.isEmpty()) {
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
					createErrorResponse("Missing type parameter").toString());
			}
			if (Dungeon.level == null) {
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
					createErrorResponse("Game state not initialized - start a game first").toString());
			}
			if ("friend".equalsIgnoreCase(stance) && Dungeon.hero == null) {
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
					createErrorResponse("stance=friend needs a hero").toString());
			}

			final String finalType = type;
			final String finalStance = stance;
			final int finalX = x, finalY = y;
			final int finalRevertAfter = revertAfter;
			final int[] mobId = new int[1];
			final String[] error = new String[1];

			java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
			GameLoop.pushUiTask(() -> {
				try {
					Mob mob = MobFactory.mobByName(finalType);
					if (mob == null) {
						error[0] = "Unknown mob type: " + finalType;
						return;
					}
					int cell = (finalX >= 0 && finalY >= 0)
						? finalX + finalY * Dungeon.level.getWidth()
						: Dungeon.level.randomPassableCell();
					mob.setPos(cell);
					mob.remoteRevertStateTag = mob.getState().getTag();
					Dungeon.level.spawnMob(mob);
					applyStance(mob, finalStance);
					mob.remoteRevertAfter = finalRevertAfter;
					mob.remoteIdleTurns = 0;
					mob.setState(MobAi.getStateByClass(RemoteControlled.class));
					mobId[0] = mob.getId();
				} catch (Exception e) {
					error[0] = "spawn failed: " + e.getMessage();
					GLog.n(error[0]);
				} finally {
					latch.countDown();
				}
			});

			if (!latch.await(5, java.util.concurrent.TimeUnit.SECONDS)) {
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
					createErrorResponse("Timeout waiting for remote spawn").toString());
			}
			if (error[0] != null) {
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
					createErrorResponse(error[0]).toString());
			}

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("id", mobId[0]);
			response.put("stance", finalStance);
			response.put("revertAfter", finalRevertAfter);
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
				new JSONObject(response).toString());
		} catch (Exception e) {
			GLog.w("Error in handleRemoteSpawn: " + e.getMessage());
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
				createErrorResponse("Internal error: " + e.getMessage()).toString());
		}
	}

	// GET /debug/remote/possess?id=&stance=&revertAfter=
	public static NanoHTTPD.Response handleRemotePossess(NanoHTTPD.IHTTPSession session) {
		try {
			String query = session.getQueryParameterString();
			int id = -1;
			String stance = null;
			int revertAfter = -1;

			if (query != null && !query.isEmpty()) {
				for (String param : query.split("&")) {
					if (param.startsWith("id=")) {
						try {
							id = Integer.parseInt(java.net.URLDecoder.decode(param.substring(3), "UTF-8"));
						} catch (NumberFormatException ignored) {
						}
					} else if (param.startsWith("stance=")) {
						stance = java.net.URLDecoder.decode(param.substring(7), "UTF-8");
					} else if (param.startsWith("revertAfter=")) {
						try {
							revertAfter = Integer.parseInt(java.net.URLDecoder.decode(param.substring(12), "UTF-8"));
						} catch (NumberFormatException ignored) {
						}
					}
				}
			}

			final int finalId = id;
			final String finalStance = stance;
			final int finalRevertAfter = revertAfter;
			final String[] error = new String[1];

			java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
			GameLoop.pushUiTask(() -> {
				try {
					Mob mob = findMobById(finalId);
					if (mob == null) {
						error[0] = "No mob with id " + finalId;
						return;
					}
					if (isRemote(mob)) {
						return; // already remote - nothing to do
					}
					mob.remoteRevertStateTag = mob.getState().getTag();
					if (finalStance != null) {
						applyStance(mob, finalStance);
					}
					if (finalRevertAfter >= 0) {
						mob.remoteRevertAfter = finalRevertAfter;
					} else if (mob.remoteRevertAfter <= 0) {
						mob.remoteRevertAfter = 10;
					}
					mob.remoteIdleTurns = 0;
					mob.setState(MobAi.getStateByClass(RemoteControlled.class));
				} catch (Exception e) {
					error[0] = "possess failed: " + e.getMessage();
					GLog.n(error[0]);
				} finally {
					latch.countDown();
				}
			});

			if (!latch.await(5, java.util.concurrent.TimeUnit.SECONDS)) {
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
					createErrorResponse("Timeout waiting for possess").toString());
			}
			if (error[0] != null) {
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "application/json",
					createErrorResponse(error[0]).toString());
			}

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("id", finalId);
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
				new JSONObject(response).toString());
		} catch (Exception e) {
			GLog.w("Error in handleRemotePossess: " + e.getMessage());
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
				createErrorResponse("Internal error: " + e.getMessage()).toString());
		}
	}

	// GET /debug/remote/release?id=
	public static NanoHTTPD.Response handleRemoteRelease(NanoHTTPD.IHTTPSession session) {
		try {
			String query = session.getQueryParameterString();
			int id = -1;
			if (query != null && !query.isEmpty()) {
				for (String param : query.split("&")) {
					if (param.startsWith("id=")) {
						try {
							id = Integer.parseInt(java.net.URLDecoder.decode(param.substring(3), "UTF-8"));
						} catch (NumberFormatException ignored) {
						}
					}
				}
			}

			final int finalId = id;
			final String[] error = new String[1];

			java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
			GameLoop.pushUiTask(() -> {
				try {
					Mob mob = findMobById(finalId);
					if (mob == null) {
						error[0] = "No mob with id " + finalId;
						return;
					}
					mob.revertRemoteControl();
				} catch (Exception e) {
					error[0] = "release failed: " + e.getMessage();
					GLog.n(error[0]);
				} finally {
					latch.countDown();
				}
			});

			if (!latch.await(5, java.util.concurrent.TimeUnit.SECONDS)) {
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
					createErrorResponse("Timeout waiting for release").toString());
			}
			if (error[0] != null) {
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "application/json",
					createErrorResponse(error[0]).toString());
			}

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("id", finalId);
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
				new JSONObject(response).toString());
		} catch (Exception e) {
			GLog.w("Error in handleRemoteRelease: " + e.getMessage());
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
				createErrorResponse("Internal error: " + e.getMessage()).toString());
		}
	}

	// GET /debug/remote/list
	public static NanoHTTPD.Response handleRemoteList(NanoHTTPD.IHTTPSession session) {
		try {
			if (Dungeon.level == null) {
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
					createErrorResponse("Game state not initialized - start a game first").toString());
			}

			org.json.JSONArray list = new org.json.JSONArray();
			int width = Dungeon.level.getWidth();
			for (Mob mob : Dungeon.level.mobs) {
				if (!isRemote(mob)) {
					continue;
				}
				JSONObject entry = new JSONObject();
				entry.put("id", mob.getId());
				entry.put("type", mob.getEntityKind());
				entry.put("x", mob.getPos() % width);
				entry.put("y", mob.getPos() / width);
				entry.put("stance", mob.isPet() ? "friend" : "foe");
				list.put(entry);
			}

			Map<String, Object> response = new HashMap<>();
			response.put("count", list.length());
			response.put("remote", list);
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
				new JSONObject(response).toString());
		} catch (Exception e) {
			GLog.w("Error in handleRemoteList: " + e.getMessage());
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
				createErrorResponse("Internal error: " + e.getMessage()).toString());
		}
	}
```

- [ ] **Step 2: Register routes in `BaseWebServer`**

In the static route block, after the `put("/debug/move_to", ...)` line:

```java
        debugEndpoints.put("/debug/remote/spawn", DebugEndpoints::handleRemoteSpawn);
        debugEndpoints.put("/debug/remote/possess", DebugEndpoints::handleRemotePossess);
        debugEndpoints.put("/debug/remote/release", DebugEndpoints::handleRemoteRelease);
        debugEndpoints.put("/debug/remote/list", DebugEndpoints::handleRemoteList);
```

- [ ] **Step 3: Compile both platforms**

Run: `./gradlew -p RemixedDungeonDesktop compileJava --console=plain -q` → exit 0.
Run: `./gradlew -c settings.android.gradle :RemixedDungeon:compileAndroidGooglePlayDebugJavaWithJavac --console=plain -q` → exit 0.

- [ ] **Step 4: Commit**

```bash
git add RemixedDungeon/src/main/java/com/nyrds/platform/app/DebugEndpoints.java \
        RemixedDungeon/src/main/java/com/nyrds/platform/app/BaseWebServer.java
git commit -m "feat: /debug/remote spawn/possess/release/list endpoints (remote chars 2/3)"
```

---

### Task 3: `char_status` endpoint + `remote` flag in `observe`

**Files:**
- Modify: `RemixedDungeon/src/main/java/com/nyrds/platform/app/DebugEndpoints.java`
- Modify: `RemixedDungeon/src/main/java/com/nyrds/platform/app/BaseWebServer.java`

- [ ] **Step 1: Add `handleCharStatus` to `DebugEndpoints`** (append near the remote handlers)

```java
	// GET /debug/char_status?id= - hero_status shape for any char on the level
	public static NanoHTTPD.Response handleCharStatus(NanoHTTPD.IHTTPSession session) {
		try {
			String query = session.getQueryParameterString();
			int id = -1;
			if (query != null && !query.isEmpty()) {
				for (String param : query.split("&")) {
					if (param.startsWith("id=")) {
						try {
							id = Integer.parseInt(java.net.URLDecoder.decode(param.substring(3), "UTF-8"));
						} catch (NumberFormatException ignored) {
						}
					}
				}
			}

			if (Dungeon.level == null) {
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
					createErrorResponse("Game state not initialized - start a game first").toString());
			}

			Mob mob = findMobById(id);
			if (mob == null) {
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "application/json",
					createErrorResponse("No mob with id " + id).toString());
			}

			int pos = mob.getPos();
			int width = Dungeon.level.getWidth();
			var action = mob.getCurAction();
			String actionName = (action == null) ? "idle" : action.getClass().getSimpleName();

			// one-shot: the driver learns the watchdog fired
			boolean reverted = mob.remoteReverted;
			mob.remoteReverted = false;

			String jsonString = String.format(
				"{\"alive\":%b,\"hp\":%d,\"ht\":%d,\"pos\":%d,\"x\":%d,\"y\":%d," +
					"\"action\":\"%s\",\"levelId\":\"%s\",\"depth\":%d," +
					"\"type\":\"%s\",\"fraction\":\"%s\",\"remote\":%b,\"reverted\":%b,\"revertAfter\":%d}",
				mob.isAlive(),
				mob.hp(),
				mob.ht(),
				pos, pos % width, pos / width,
				actionName,
				DungeonGenerator.getCurrentLevelId(),
				Dungeon.depth,
				mob.getEntityKind(),
				mob.fraction().name(),
				isRemote(mob),
				reverted,
				mob.remoteRevertAfter
			);

			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonString);
		} catch (Exception e) {
			GLog.w("Error in handleCharStatus: " + e.getMessage());
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
				createErrorResponse("Internal error: " + e.getMessage()).toString());
		}
	}
```

- [ ] **Step 2: Add the `remote` flag to `observe` mob entries**

In `handleDebugObserve`, inside the mob loop, after `mobJson.put("owned", ...)` add:

```java
                mobJson.put("remote", isRemote(mob));
```

- [ ] **Step 3: Register route**

In `BaseWebServer` static block, after the `/debug/remote/list` line:

```java
        debugEndpoints.put("/debug/char_status", DebugEndpoints::handleCharStatus);
```

- [ ] **Step 4: Compile both platforms**

Same two compile commands as Task 1 Step 5. Expect exit 0 both.

- [ ] **Step 5: Commit**

```bash
git add RemixedDungeon/src/main/java/com/nyrds/platform/app/DebugEndpoints.java \
        RemixedDungeon/src/main/java/com/nyrds/platform/app/BaseWebServer.java
git commit -m "feat: /debug/char_status + remote flag in observe (remote chars 2/3)"
```

---

### Task 4: `move_to?char=` — tap semantics for any remote char + command pump

**Files:**
- Modify: `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/DebugEndpoints.java` — wrong path, the handler lives in `RemixedDungeon/src/main/java/com/nyrds/platform/app/DebugEndpoints.java` (`handleDebugMoveTo`)

- [ ] **Step 1: Generalize `handleDebugMoveTo`**

Replace the whole body of `handleDebugMoveTo` with:

```java
	public static NanoHTTPD.Response handleDebugMoveTo(NanoHTTPD.IHTTPSession session) {
		try {
			String query = session.getQueryParameterString();
			int x = -1, y = -1;
			int cell = -1;
			int charId = -1;

			if (query != null && !query.isEmpty()) {
				for (String param : query.split("&")) {
					if (param.startsWith("x=")) {
						x = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8"));
					} else if (param.startsWith("y=")) {
						y = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8"));
					} else if (param.startsWith("cell=")) {
						cell = Integer.parseInt(java.net.URLDecoder.decode(param.substring(5), "UTF-8"));
					} else if (param.startsWith("char=")) {
						charId = Integer.parseInt(java.net.URLDecoder.decode(param.substring(5), "UTF-8"));
					}
				}
			}

			if (Dungeon.hero == null || Dungeon.level == null) {
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
					createErrorResponse("Game not initialized - start a game first").toString());
			}

			final com.watabou.pixeldungeon.actors.Char actor;
			if (charId < 0) {
				actor = Dungeon.hero;
			} else {
				Mob puppet = findMobById(charId);
				if (puppet == null) {
					return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "application/json",
						createErrorResponse("No mob with id " + charId).toString());
				}
				if (!isRemote(puppet)) {
					return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
						createErrorResponse("Char " + charId + " is not remote-controlled (use /debug/remote/possess)").toString());
				}
				actor = puppet;
			}

			final boolean heroPath = (actor == Dungeon.hero);
			if (heroPath ? !actor.isReady() : actor.getCurAction() != null) {
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.CONFLICT, "application/json",
					createErrorResponse("Char is busy, poll char_status/hero_status until action==idle").toString());
			}

			if (x >= 0 && y >= 0) {
				cell = x + y * Dungeon.level.getWidth();
			}

			if (cell < 0 || !Dungeon.level.cellValid(cell)) {
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
					createErrorResponse("Missing or invalid coordinates. Provide x&y or cell").toString());
			}

			final int targetCell = cell;

			java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
			final String[] error = new String[1];
			final String[] preview = new String[1];

			GameLoop.pushUiTask(() -> {
				try {
					// preview the resolution for the response, then tap for real.
					Dungeon.level.updateFieldOfView(actor);
					var action = com.watabou.pixeldungeon.actors.CharUtils.actionForCell(actor, targetCell, Dungeon.level);
					preview[0] = action.toString();

					if (!action.valid()) {
						error[0] = "No action possible at cell " + targetCell;
						return;
					}

					if (!heroPath) {
						// caveman: puppet whitelist - Descend/Ascend would transition
						// the HERO's interlevel state, PickUp/OpenChest are
						// hero-belongings paths. Move/Attack/Interact family only.
						Class<?> actionClass = action.getClass();
						boolean supported =
							actionClass == com.nyrds.pixeldungeon.ml.actions.Move.class
							|| actionClass == com.nyrds.pixeldungeon.ml.actions.Attack.class
							|| actionClass == com.nyrds.pixeldungeon.ml.actions.Interact.class
							|| actionClass == com.nyrds.pixeldungeon.ml.actions.InteractObject.class
							|| actionClass == com.nyrds.pixeldungeon.ml.actions.Unlock.class;
						if (!supported) {
							error[0] = "Action not supported for remote char: " + action.getClass().getSimpleName();
							return;
						}
					}

					if (heroPath) {
						Dungeon.hero.handle(targetCell);
					} else {
						actor.nextAction(action);
					}

					// caveman: command-pump - let the world run while the char acts.
					// turn-based clock parks at the hero, so the hero waits one
					// tick per loop (same primitive as /debug/wait_ticks). bounded.
					int guard = 0;
					while (guard++ < 20 && actor.isAlive() && actor.getCurAction() != null) {
						Hero hero = Dungeon.hero;
						if (hero != null && hero.isAlive() && hero.getCurAction() == null && !Dungeon.realtime()) {
							hero.spendAndNext(Actor.TICK);
						}
						Actor.processTurnBased(0f);
					}
				} catch (Exception e) {
					error[0] = "Error in move_to: " + e.getMessage();
					GLog.n(error[0]);
				} finally {
					latch.countDown();
				}
			});

			if (!latch.await(15, java.util.concurrent.TimeUnit.SECONDS)) {
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
					createErrorResponse("Timeout waiting for move_to").toString());
			}

			if (error[0] != null) {
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
					createErrorResponse(error[0]).toString());
			}

			int respX = targetCell % Dungeon.level.getWidth();
			int respY = targetCell / Dungeon.level.getWidth();

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("cell", targetCell);
			response.put("x", respX);
			response.put("y", respY);
			response.put("resolved", preview[0]);
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
				new JSONObject(response).toString());
		} catch (Exception e) {
			GLog.w("Error in handleDebugMoveTo: " + e.getMessage());
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
				createErrorResponse("Internal error: " + e.getMessage()).toString());
		}
	}
```

Add missing imports to `DebugEndpoints.java`:

```java
import com.watabou.pixeldungeon.actors.hero.Hero;
```

(`Actor` is already imported; check `Mob`, `Map`, `HashMap`, `JSONObject` are — they are.)

- [ ] **Step 2: Compile both platforms**

Same two compile commands. Expect exit 0 both.

- [ ] **Step 3: Run the existing LLM control suite (hero path must not regress)**

Run: `cd tests/http_api && python3 test_llm_control.py --start-server`
Expected: `Passed: 41, Failed: 0`.

- [ ] **Step 4: Commit**

```bash
git add RemixedDungeon/src/main/java/com/nyrds/platform/app/DebugEndpoints.java
git commit -m "feat: move_to?char= - tap semantics for remote chars + command pump (remote chars 3/3)"
```

---

### Task 5: Test suite `test_remote_chars.py`

**Files:**
- Create: `tests/http_api/test_remote_chars.py`

- [ ] **Step 1: Write the suite**

Create `tests/http_api/test_remote_chars.py` — copy the `TestRunner` scaffolding (start_server/stop_server/_wait_for_game/check/get/raw_get/poll helpers) verbatim from `tests/http_api/test_llm_control.py` and use this test body:

```python
    def test_friend_puppet_walk(self):
        r = self.raw_get("/debug/remote/spawn?type=Rat&stance=friend&revertAfter=0")
        self.check("friend: spawn", r.get("success") is True and r.get("id", 0) > 0, str(r))
        pid = r.get("id")
        if not pid:
            return
        st = self.get(f"/debug/char_status?id={pid}")
        self.check("friend: char_status remote", st.get("remote") is True, str(st))
        self.check("friend: fraction HEROES", st.get("fraction") == "HEROES", str(st))
        self.check("friend: idle", st.get("action") == "idle", str(st))
        self.puppets.append(pid)

        # straight-line walkable target from puppet position
        m = self.get("/debug/get_map")
        w = m["width"]
        x, y = st["x"], st["y"]
        target = None
        for d in (3, 2):
            for dx, dy in ((d, 0), (-d, 0), (0, d), (0, -d)):
                tx, ty = x + dx, y + dy
                if 0 <= tx < w and 0 <= ty < m["height"] and m["passable"][ty][tx] == "1":
                    target = (tx, ty)
                    break
            if target:
                break
        self.check("friend: target found", target is not None)
        if not target:
            return
        r = self.raw_get(f"/debug/move_to?char={pid}&x={target[0]}&y={target[1]}")
        self.check("friend: move accepted", r.get("success") is True, str(r))
        self.check("friend: resolved Move", "Move" in str(r.get("resolved", "")), str(r.get("resolved")))
        final = self.poll_char(pid, 30)
        self.check("friend: reached target",
                   final and (final["x"], final["y"]) == target,
                   f"want {target} got {(final.get('x'), final.get('y')) if final else None}")

    def test_foe_puppet_attacks_hero(self):
        hs = self.get("/debug/hero_status")
        r = self.raw_get(f"/debug/remote/spawn?type=Rat&stance=foe&x={hs['x'] + 1}&y={hs['y']}&revertAfter=0")
        pid = r.get("id")
        self.check("foe: spawn adjacent", r.get("success") is True and pid, str(r))
        if not pid:
            return
        self.puppets.append(pid)
        st = self.get(f"/debug/char_status?id={pid}")
        self.check("foe: fraction DUNGEON", st.get("fraction") == "DUNGEON", str(st))

        before = self.get("/debug/hero_status")["hp"]
        r = self.raw_get(f"/debug/move_to?char={pid}&x={hs['x']}&y={hs['y']}")
        self.check("foe: resolved Attack", "Attack" in str(r.get("resolved", "")), str(r.get("resolved")))

        deadline = time.time() + 30
        after = before
        while time.time() < deadline:
            after = self.get("/debug/hero_status")["hp"]
            if after < before:
                break
            time.sleep(0.5)
        self.check("foe: hero took damage", after < before, f"hp {before} -> {after}")

    def test_multi_puppet_interleave(self):
        ids = []
        for i in range(2):
            r = self.raw_get("/debug/remote/spawn?type=Rat&stance=friend&revertAfter=0")
            if r.get("success"):
                ids.append(r["id"])
                self.puppets.append(r["id"])
        self.check("multi: two puppets spawned", len(ids) == 2, str(ids))
        if len(ids) < 2:
            return
        # both idle-able and addressable independently
        for pid in ids:
            st = self.get(f"/debug/char_status?id={pid}")
            self.check(f"multi: puppet {pid} status", "error" not in st and st.get("remote") is True, str(st))
        lst = self.get("/debug/remote/list")
        listed = {e["id"] for e in lst.get("remote", [])}
        self.check("multi: both in remote/list", set(ids) <= listed, str(lst))

    def test_reload_persistence(self):
        r = self.raw_get("/debug/remote/spawn?type=Rat&stance=friend&revertAfter=0")
        pid = r.get("id")
        self.check("reload: spawned", bool(pid), str(r))
        if not pid:
            return
        self.puppets.append(pid)
        rl = self.get("/debug/reload_game")
        if not rl.get("success"):
            self.check("reload: reload_game ok", False, str(rl))
            return
        time.sleep(2)
        st = self.get(f"/debug/char_status?id={pid}")
        self.check("reload: puppet alive after reload", st.get("alive") is True, str(st))
        self.check("reload: still remote", st.get("remote") is True, str(st))

    def test_descend_follow(self):
        # friend puppet follows the hero through stairs (pet-transfer path)
        r = self.raw_get("/debug/remote/spawn?type=Rat&stance=friend&revertAfter=0")
        pid = r.get("id")
        if not pid:
            self.check("descend: spawned", False, str(r))
            return
        self.puppets.append(pid)
        before = self.get("/debug/hero_status").get("levelId")
        d = self.client.descend_to("1")
        if not d.get("success"):
            self.check("descend: hero descended", False, str(d))
            return
        deadline = time.time() + 20
        lvl = before
        while time.time() < deadline:
            lvl = self.get("/debug/hero_status").get("levelId")
            if lvl != before:
                break
            time.sleep(0.5)
        self.check("descend: hero on new level", lvl != before, f"{before} -> {lvl}")
        st = self.get(f"/debug/char_status?id={pid}")
        self.check("descend: puppet followed", st.get("alive") is True and st.get("levelId") == lvl,
                   f"puppet lvl={st.get('levelId')} hero lvl={lvl}")

    def test_watchdog_revert(self):
        r = self.raw_get("/debug/remote/spawn?type=Rat&stance=friend&revertAfter=3")
        pid = r.get("id")
        if not pid:
            self.check("watchdog: spawned", False, str(r))
            return
        self.puppets.append(pid)
        # burn world turns so the puppet's idle counter elapses
        for _ in range(6):
            self.raw_get("/debug/wait_ticks?ticks=3")
            time.sleep(0.2)
        deadline = time.time() + 15
        st = {}
        while time.time() < deadline:
            st = self.get(f"/debug/char_status?id={pid}")
            if st.get("reverted") or st.get("remote") is False:
                break
            self.raw_get("/debug/wait_ticks?ticks=3")
            time.sleep(0.2)
        self.check("watchdog: reverted flag", st.get("reverted") is True, str(st))
        self.check("watchdog: no longer remote", st.get("remote") is False, str(st))

    def run_test(self):
        result = self.client.start_game("WARRIOR", 0)
        if "error" in result:
            print(f"  FAIL: could not start game: {result.get('error')}")
            self.failed += 1
            return
        if not self._wait_for_game():
            print("  FAIL: game did not initialize")
            self.failed += 1
            return
        time.sleep(1)

        self.puppets = []
        self.test_friend_puppet_walk()
        self.test_foe_puppet_attacks_hero()
        self.test_multi_puppet_interleave()
        self.test_reload_persistence()
        self.test_descend_follow()
        self.test_watchdog_revert()
```

Also add to `TestRunner.__init__`: `self.puppets: List[int] = []`, and these helpers (copy `get`/`raw_get`/`poll_idle` style from `test_llm_control.py`):

```python
    def poll_char(self, pid: int, timeout=30):
        deadline = time.time() + timeout
        final = None
        while time.time() < deadline:
            final = self.get(f"/debug/char_status?id={pid}")
            if final.get("action") == "idle" or not final.get("alive"):
                break
            time.sleep(0.2)
        return final
```

- [ ] **Step 2: Run the suite**

Run: `pkill -f 'webserver=808[0]' 2>/dev/null; sleep 3; cd tests/http_api && python3 test_remote_chars.py --start-server`
Expected: all checks PASS, `Failed: 0`. Known-flaky candidates if the environment differs: `descend: puppet followed` (pet-transfer timing — retry once before investigating) and `foe: hero took damage` (rat may miss — the 30s loop covers repeated attacks).

- [ ] **Step 3: Run the regression suites**

Run: `pkill -f 'webserver=808[0]' 2>/dev/null; sleep 3; python3 test_llm_control.py --start-server` → `Failed: 0`.
Run: `pkill -f 'webserver=808[0]' 2>/dev/null; sleep 3; python3 test_path_traversal.py --start-server` → `Failed: 0`.

- [ ] **Step 4: Commit**

```bash
git add tests/http_api/test_remote_chars.py
git commit -m "test: remote chars suite - walk, foe-vs-hero, multi, reload, follow, watchdog"
```

---

### Task 6: Docs + push

**Files:**
- Modify: `tests/http_api/README.md` (LLM/Agent section table)
- Modify: `docs/superpowers/specs/2026-08-30-remote-controlled-chars-design.md` (status line)

- [ ] **Step 1: README — add rows to the LLM/Agent table**

```markdown
| `/debug/remote/spawn` | `type`, `x`, `y`, `stance` (`friend`\|`foe`), `revertAfter` | Spawn a puppet (remote-controlled mob) → `{id}` |
| `/debug/remote/possess` | `id`, `stance`, `revertAfter` | Flip an existing mob to remote control |
| `/debug/remote/release` | `id` | Drop remote control, restore previous AI |
| `/debug/remote/list` | - | List remote-controlled chars |
| `/debug/char_status` | `id` | `hero_status` shape for any char + `type`, `fraction`, `remote`, `reverted` |
```

And extend the `move_to` row description with: `char=<id>` targets a remote-controlled char instead of the hero.

- [ ] **Step 2: Spec status line**

Change `Status: approved direction (approach B — direct drive), slice 1 pending implementation plan` to `Status: slice 1 implemented`.

- [ ] **Step 3: Commit and push (stash ritual for the runtime artifact)**

```bash
git add tests/http_api/README.md docs/superpowers/specs/2026-08-30-remote-controlled-chars-design.md
git commit -m "docs: remote chars endpoints in debug API reference; spec status slice 1"
git stash push RemixedDungeon/src/main/assets/levelsDesc/Dungeon_debug.json
git pull --rebase
git stash pop
git push
```

Close the beads issue for this work (create if none exists yet: `bd create --title="Remote-controlled chars slice 1" ...`) after the push.
