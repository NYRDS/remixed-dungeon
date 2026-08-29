# Bugs From Player Report — Remaining Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the four remaining bugs from the RU beta player report + audit: webserver NPE under `--debug`, WndPetSelect click-through, NPC healing particles visible through fog of war, and Turn-timeout infinite stall.

**Architecture:** Four independent fixes, each with a verification path. Two are engine-level (logging null-guard, actor scheduling), two are UI-level (touch dispatch ordering, particle visibility gating). Each task is self-contained and committable.

**Tech Stack:** Java (Gradle multiproject), Python HTTP test harness (`tests/http_api/`), beads issue tracker (`bd`).

**Beads:** snap-5jp (webserver NPE), snap-3el (click-through), snap-o9u (rendering glitches — only the FoW healing-particles part is in scope), snap-zog (free-turn audit findings — only the Turn-timeout stall is in scope).

**Verification invariants:**

- The game's turn engine has a DEBUG invariant in `Char.checkedAct()` (RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/Char.java:303): an actor that spends no time in `act()` throws `TrackedRuntimeException`. The hero is exempt **only when idle** (`getCurAction() == null`). Keep this true.
- Test server lifecycle: `tests/http_api/test_*.py --start-server` starts `./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer --args="--webserver=8080 --windowed"`, waits for `/ready`, runs, kills by process group. Takes 2–4 minutes mostly for gradle.
- `curl -s http://localhost:8080/debug/get_recent_logs` returns the in-game log — use it to check for `TrackedRuntimeException` and Lua errors after scripted interactions.

---

### Task 1: Webserver NPE under `--debug` (snap-5jp)

**Root cause (verified):** `PUtil.slog` (desktop) → libgdx `Logger.info` → `Gdx.app.log`, and `Gdx.app` is null until the GDX application initializes. `BaseWebServer`'s constructor (called from `main()` before GDX init) logs via `GLog.debug`, which under `--debug` routes to `PUtil.slog` → NPE → server fails to start.

**Files:**
- Modify: `RemixedDungeonDesktop/src/libgdx/java/com/nyrds/platform/util/PUtil.java:16-19`

- [ ] **Step 1: Reproduce the crash**

```bash
timeout 60 ./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer --args="--webserver=8090 --windowed --debug" 2>&1 | grep -m1 "Gdx.app"
```

Expected: `Cannot invoke "com.badlogic.gdx.Application.log(String, String)" because "com.badlogic.gdx.Gdx.app" is null`

- [ ] **Step 2: Add null-guard to desktop PUtil.slog**

Replace the whole `slog` method in `RemixedDungeonDesktop/src/libgdx/java/com/nyrds/platform/util/PUtil.java`:

```java
    static public void slog(String tag, String txt) {
        // caveman: Gdx.app null before libgdx init. early log calls (webserver
        // startup, pre-init errors) must not crash - fall back to stdout.
        if (com.badlogic.gdx.Gdx.app == null) {
            System.out.println("[" + tag + "] " + txt);
            return;
        }
        logger.setLevel(Logger.INFO);
        logger.info(txt);
    }
```

- [ ] **Step 3: Verify server starts under --debug**

```bash
timeout 120 ./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer --args="--webserver=8090 --windowed --debug" > /tmp/debug_server.log 2>&1 &
sleep 90
curl -s http://localhost:8090/ready
```

Expected: `{"ready":true,...}` (allow up to 90s for gradle + game init; poll in a loop `for i in $(seq 1 30); do curl -s http://localhost:8090/ready && break; sleep 3; done`)

Then also start a game and confirm the DEBUG invariants run (they only execute in debug builds):

```bash
curl -s "http://localhost:8090/debug/start_game?class=WARRIOR&difficulty=0"
sleep 8
curl -s "http://localhost:8090/debug/create_mob?type=Rat&x=31&y=23"
curl -s http://localhost:8090/debug/get_recent_logs | grep -c "consume no time"
```

Expected: `0` (no invariant trips on normal play)

Kill the server: `pkill -f "webserver=8090"`

- [ ] **Step 4: Commit**

```bash
git add RemixedDungeonDesktop/src/libgdx/java/com/nyrds/platform/util/PUtil.java
git commit -m "fix: desktop PUtil.slog null-guard - Gdx.app null before init crashes webserver under --debug (snap-5jp)"
```

---

### Task 2: WndPetSelect click-through (snap-3el)

**Root cause (verified):** `Touchscreen.event` is a **stackMode Signal** (`new Signal<>(true)` — `Signal.add` does `listeners.addFirst`), so the **newest** TouchArea is dispatched first. But dispatch order is only half of it: each window's full-screen blocker `TouchArea` only calls `Touchscreen.event.cancel()` when the touch starts **inside its own chrome** (`TouchArea.onSignal`: `if (hit) { if (catchTouch) Touchscreen.event.cancel(); ... }` — cancel happens only in the `hit` branch). So a tap that lands on the small `WndPetSelect` window:

1. Hits WndPetSelect's blocker (dispatched first, newest listener) — inside chrome, so it cancels. Good so far.

BUT the real leak the player hit: `WndPetSelect` is **added via `GameScene.show()` → `GameLoop.pushUiTask`** (deferred to next frame) while the hero's `WndBag` window is still open and its ItemButtons are live. `Window.onSignal` for Keys cancels the event (`Keys.event.cancel()` line 149) — but the **Touch** path has no equivalent discipline for the *button children*: `RedButton`/`ItemButton` are `TouchArea`s added to their parent window **after** the window's blocker, so within one window the buttons are dispatched before the blocker (stack mode). Across windows, the pet-select window's *buttons* are dispatched before the bag's buttons — but only if the pet-select's buttons' hit-test passes. The player's report ("tapping the pet button hits the potion behind it") means the pet-select buttons were **not receiving the hit** — because `VHBox` layout positions (`actions.setPos(...)`) are computed in window-local coordinates, but `TouchArea.onSignal` hit-tests use `overlapsScreenPoint` → `camera()` → parent chain → the window's `camera`. Two windows create **two Cameras** (`Window()` constructor: `camera = new Camera(...)` + `Camera.add(camera)`), and a button inside `WndPetSelect` hit-tests against `WndPetSelect.camera`, while the underlying `WndBag`'s buttons hit-test against `WndBag.camera`. Both cameras overlap on screen; the top window's button that fails its own hit-test (marginal taps, or layout not yet applied since `pushUiTask` defers the add — `actions.setPos` runs at construction but `resize` may reallocate) lets the touch fall through to the bag's button which passes its hit-test and consumes the item.

The robust fix is at dispatch level: **a window's blocker must cancel touch events that fall inside its chrome even when the tap misses every child button** — the blocker already does this — AND **the underlying window must not see touches at all while a modal dialog is above it**. The blocker's `cancel()` only stops *later-dispatched listeners in the same dispatch pass* — it does. So remaining hole: touches that start **outside** the top window's chrome. Those legitimately go to the game scene — and also to the bag's buttons underneath (outside pet-select chrome but over the bag's item grid). `Window.onTouchDown` in the blocker then calls `onBackPressed()` on the **top** window only. The bag window never sees an event that would dismiss it, but its buttons are still live and earlier-or-later in dispatch order depending on add order.

Simplest correct fix: when a modal `Window` is shown via `GameScene.show()`, **deactivate the previous top-level window's touch areas** — concretely, hide any open bag window when `WndPetSelect`/`WndPetInventoryOptions`/`WndPetBag` opens (they're modal choosers). This matches `WndBag.updateItems()`'s existing `bringToFront(activeDialog)` intent.

**Files:**
- Modify: `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/windows/WndBag.java` (hide on modal pet dialog open)
- Modify: `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/mechanics/PetInventoryManager.java:157-159` (openPetSelect hides hero bag)

- [ ] **Step 1: Add hideHeroBagIfOpen helper + call from openPetSelect**

In `PetInventoryManager.java`, change `openPetSelect`:

```java
    @LuaInterface
    public static void openPetSelect(@NotNull Hero hero, @Nullable Item itemToGive) {
        // caveman: hide hero bag window - modal pet chooser on top, taps must not
        // fall through to item buttons underneath (player report: clicking pet
        // button gave potion instead)
        WndBag heroBag = WndBag.getHeroBagInstance();
        if (heroBag != null) {
            heroBag.hide();
        }
        GameScene.show(new WndPetSelect(hero, itemToGive));
    }
```

Check imports in `PetInventoryManager.java` — `com.watabou.pixeldungeon.windows.WndBag` must be imported (it already imports `WndPetSelect` from the same package; add `import com.watabou.pixeldungeon.windows.WndBag;` if missing).

Note `giveItemToPet`/`takeItemFromPet` already refresh `WndBag.getHeroBagInstance()` (PetInventoryManager.java:89-91) — after `hide()` the instance is destroyed; check `WndBag.getHeroBagInstance()` returns the static field even after hide. If it does, those refresh calls would act on a dead window. Guard them:

```java
        if (WndBag.getHeroBagInstance() != null) {
            WndBag.getHeroBagInstance().updateItems();
        }
```

(they are already inside `if (WndBag.getHeroBagInstance() != null)` guards — verify, keep as is if so).

- [ ] **Step 2: Verify heroBagInstance is nulled on hide**

In `WndBag.java`, find where `heroBagInstance = this;` is set (line 203). Check `hide()`/`destroy()` — if the static is not cleared, add to the window's `destroy()`:

```java
    @Override
    public void destroy() {
        if (heroBagInstance == this) {
            heroBagInstance = null;
        }
        if (instance == this) {
            instance = null;
        }
        super.destroy();
    }
```

(Only add fields that aren't already cleared; match existing code — `instance` clearing may already exist, check first and don't duplicate.)

- [ ] **Step 3: Manual smoke via HTTP API**

Start the server (Task 1 pattern, port 8080), start game, then simulate the flow — there is no endpoint to open UI windows, so verify by **code inspection + compile** plus a game-session error check:

```bash
./gradlew :RemixedDungeon:compileJava -q
```

Expected: no errors.

Real-device/in-game manual check (needs the desktop window): open inventory → click item → "Give to pet" → pet chooser appears → tap pet button → item transfers, no item "used" from behind. If no display available, mark Task 2 verified-by-inspection and note it in the commit message.

- [ ] **Step 4: Commit**

```bash
git add RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/mechanics/PetInventoryManager.java RemixedDungeon/src/main/java/com/watabou/pixeldungeon/windows/WndBag.java
git commit -m "fix: hide hero bag when pet-select modal opens - taps no longer fall through to item buttons (snap-3el)"
```

---

### Task 3: NPC healing particles visible through fog of war (snap-o9u, part 3)

**Root cause (verified):** Buff visual effects attach regardless of the owner's visibility. `Char.add(Buff)` (Char.java:1046) calls `buff.attachVisual()` whenever `isOnStage()` — no visibility check. For mobs, `Char.placeTo` (Char.java:1142) correctly sets `getSprite().setVisible(Dungeon.isCellVisible(cell) && invisible >= 0)`, so the *sprite* is hidden in fog, but particle emitters attached to the sprite (healing, regeneration) keep emitting — particles are drawn by their own draw pass and don't inherit the sprite's `visible` flag.

**Files:**
- Modify: `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/Char.java:1046-1071` (`add(Buff)` — gate `attachVisual` on visibility)

- [ ] **Step 1: Gate attachVisual on cell visibility**

In `Char.add(Buff buff)`, change the tail:

```java
        buffsUpdatedCount++;

        if (!isOnStage()) {
            return true;
        }

        // caveman: hidden char (fog of war, invisible) shows buff particles to
        // player - emitters draw own pass, sprite visibility does not stop them.
        if (Dungeon.isCellVisible(getPos())) {
            buff.attachVisual();
        }
        return true;
```

(`Dungeon` is already imported in Char.java — verify.)

- [ ] **Step 2: Check Regeneration-style buffs for always-on emitters**

```bash
grep -rn "attachVisual" RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/buffs/Regeneration.java RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/mechanics/buffs/*.java 2>/dev/null | head
```

If any buff's `attachVisual` starts a looping emitter, confirm the emitter is attached to `target.getSprite()` (parented to the sprite, still hidden by our gate since we skip attach). No further change needed — the gate at attach time covers it.

- [ ] **Step 3: Compile**

```bash
./gradlew :RemixedDungeon:compileJava -q
```

Expected: no errors.

- [ ] **Step 4: In-game verification via debug API**

Start server (Task 1 pattern), start game, create a mob far from hero (random cell), then check the game log for emitter errors:

```bash
curl -s "http://localhost:8090/debug/start_game?class=WARRIOR&difficulty=0"
sleep 8
curl -s "http://localhost:8090/debug/create_mob?type=Rat"
sleep 2
curl -s http://localhost:8090/debug/get_recent_logs | grep -ci "exception"
```

Expected: `0`. (Visual confirmation needs eyes on the window; code path is: hidden → no emitter at all.)

- [ ] **Step 5: Commit**

```bash
git add RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/Char.java
git commit -m "fix: buff particles no longer attach for chars outside field of view (snap-o9u part)"
```

---

### Task 4: Turn-timeout can stall the hero forever (snap-zog, finding from report)

**Root cause (verified):** `Hero.timeout()` (Hero.java:956-967) fires only when `Dungeon.moveTimeout()` is finite (user setting / mod quirk). It calls `spend(TIME_TO_REST)` and returns true, which lets `processTurnBased` re-run the hero. But `timeout()` early-returns **false** while `getSprite().isPlayingExtraAnimation()` — if that animation never completes (extra animations are Lua/mod-driven), the loop `current == hero && !current.timeout()` → return, retries every frame, and the hero never acts again. Also, with `moveTimeout == POSITIVE_INFINITY` (default!) the idle hero simply never re-schedules — fine — but the chess level (`chess level - wip` commit) runs `ScriptedActor.onStep()` per **frame**, not per turn, implying frame-driven gameplay where a stuck `timeout()==false` freezes everything.

The narrow, defensible fix: **also break out when the extra animation has stalled** — add a wall-clock guard. If the animation has been "playing" for more than N seconds (say 10), log it and allow the timeout.

**Files:**
- Modify: `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/hero/Hero.java:955-967`

- [ ] **Step 1: Add stall guard to timeout()**

Replace `timeout()`:

```java
    private long extraAnimStallStart = 0;

    @Override
    protected boolean timeout() {
        if (SystemTime.now() - SystemTime.getLastActionTime() > Dungeon.moveTimeout()) {
            if (getSprite().isPlayingExtraAnimation()) {
                // caveman: stuck extra animation (mod lua gone wrong) must not
                // freeze hero forever. after 10s wall clock, force the timeout.
                long now = SystemTime.now();
                if (extraAnimStallStart == 0) {
                    extraAnimStallStart = now;
                    return false;
                }
                if (now - extraAnimStallStart < 10_000) {
                    return false;
                }
                EventCollector.logException("hero extra animation stalled >10s, forcing turn timeout");
                extraAnimStallStart = 0;
                // fall through: force timeout below
            } else {
                extraAnimStallStart = 0;
            }
            SystemTime.updateLastActionTime();
            spend(TIME_TO_REST);
            return true;
        }
        return false;
    }
```

Check imports: `EventCollector` (com.nyrds.platform.EventCollector) — verify it's imported in Hero.java; add if missing.

- [ ] **Step 2: Compile**

```bash
./gradlew :RemixedDungeon:compileJava -q
```

Expected: no errors.

- [ ] **Step 3: Regression — turn economy test still passes**

```bash
cd tests/http_api && timeout 400 python3 test_turn_economy.py --start-server 2>&1 | tail -8
```

Expected: `Passed: 1  Failed: 0` (attack cost 1.0 tick). The timeout change must not affect the no-timeout default path.

- [ ] **Step 4: Commit**

```bash
git add RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/hero/Hero.java
git commit -m "fix: hero no longer stalls forever when extra animation never completes - 10s wall-clock guard on timeout (snap-zog part)"
```

---

### Task 5: Close out beads + push

- [ ] **Step 1: Close beads issues**

```bash
bd close snap-5jp --reason="fixed: PUtil.slog null-guard, verified --debug server starts"
bd close snap-3el --reason="fixed: hero bag hidden under pet-select modal"
bd update snap-o9u --notes="healing-particles part fixed (attachVisual gated on FoV); delayed-draw and sprite-overlap remain open"
bd update snap-zog --notes="timeout-stall part fixed (10s guard); free Taunt and missile-callback spend remain open by design"
```

- [ ] **Step 2: Push everything**

```bash
git pull --rebase
git push
bd dolt push
```

Expected: clean push; if `git pull --rebase` complains about local changes to `Dungeon_debug.json`/`wiki-data` (runtime artifacts from test runs), `git stash push RemixedDungeon/src/main/assets/levelsDesc/Dungeon_debug.json wiki-data` first, then pull, push, `git stash pop`.

---

## Self-Review

**Spec coverage:** Player report items — turn skips (fixed in prior commit, regression-tested in Task 4 Step 3), "Считай этого [миньон]" message (fixed prior), attack button skip (same root cause), gas stunlock (same root cause), pet steals clicked tile (same root cause), order-given turn skip (same root cause), delayed NPC draw + sprite overlap (explicitly deferred — snap-o9u stays open for those two), healing visible in fog (Task 3), pet-select click-through (Task 2). Audit findings — webserver NPE (Task 1), timeout stall (Task 4), free Taunt (deferred by design), missile-callback spend loss (deferred, edge case). ✓

**Placeholder scan:** No TBD/TODO; every step has code or an exact command. ✓

**Type consistency:** `SystemTime.now()` returns `long` ms (used same as existing code in `timeout()`); `EventCollector.logException(String)` matches usage in `Mob.java:251`; `WndBag.getHeroBagInstance()` static accessor exists (WndBag.java:110); `Dungeon.isCellVisible(int)` used identically at Char.java:1142. ✓
