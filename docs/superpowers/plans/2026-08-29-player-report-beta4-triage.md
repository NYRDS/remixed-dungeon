# Player Report 32.4.beta.4 — Code Triage (2026-08-29)

Review of 23-item RU player report against master `9e0cc922f` (= beta.4, identical code — nothing already fixed on master). No code changes made; this is analysis only.

Each item: verdict, evidence (file:line), root cause, fix sketch.

**Verdict legend:** CONFIRMED (code path fully traced) / LIKELY (mechanism solid, trigger unverified) / NEEDS RUNTIME / NEEDS INFO (screenshot or player data missing) / BY DESIGN.

---

## Summary table

| # | Issue | Verdict | Root cause in one line |
|---|---|---|---|
| 1 | Pet reaches clicked tile first | CONFIRMED | Actor tie-break is HashSet order; hero has no priority |
| 2 | Ghost clickable bag after "give to pet" | CONFIRMED | snap-3el regression: `updateItems()` called on already-hidden bag |
| 3 | Spider nest minion dupe | NEEDS RUNTIME | No spider-specific clone path exists in code |
| 4 | Pet stuck in closed door after swap | CONFIRMED | `Door.leave()` checks heaps only, fires after pet placed |
| 5 | Revive into chasm → hang, rollback, pets lost | CONFIRMED (3 sub-bugs) | No pit check on spawn; save counter leaks on exception; followers only in memory |
| 6 | Hero-phrased log for enemy potion | CONFIRMED | `PotionOfHealing.apply` logs unconditionally |
| 7 | Telekinesis doesn't discharge traps | CONFIRMED | Description stale: traps are LevelObjects, wand opts out |
| 8 | Swarm split + fire = freeze | CONFIRMED | Split writes hp < 0 past death guard → dead mob wedges turn loop silently |
| 9 | Bats avoid trap cells | CONFIRMED | `Trap.nonPassable()` ignores `isFlying()` |
| 10 | Psi-spider remains unsellable | CONFIRMED | `ht()=5`, `5/6 == 0` int division → price 0 → filtered |
| 11 | Roots works on exploding skulls | CONFIRMED | `ExplodingSkull`/`RunicSkull` missing `flying = true` |
| 12 | Untranslated string | LIKELY | Hardcoded English `RPD.glog()` literals (chess, palace, Carcass UI) |
| 13 | Corpse + black skull = body cloning | CONFIRMED | Resurrect fires *before* carcass drop; corpse not consumed |
| 14 | Skull pets gain XP, necromancy pets don't | CONFIRMED | XP gate is `undead` flag; skull pets aren't marked undead |
| 15 | Frost spirit can be frozen | LIKELY BUG | `ColdSpirit` has zero immunities (other elementals have own-element immunity) |
| 16 | Statue regenerates removed armor | CONFIRMED | `getItem()` is a lazy generator, refills empty slot forever |
| 17 | Statue STR follows armor requirement | CONFIRMED | `STR(max(12, armor.requiredSTR()))` on every `getItem()` |
| 18 | Multiplication armor = item dupe | CONFIRMED | `makeClone()` = full bundle round-trip, equipment included |
| 19 | Dwarf tokens 100g each | BY DESIGN / new farm | Price upstream-verbatim; beta.2 palace created big token source |
| 20 | Chess: succubus became queen | CONFIRMED | BlackSkull resurrects piece into its cell; board resolves by cell, not identity |
| 21 | Chess: illegal move accepted | LIKELY display desync | Engine legality sound; picture desyncs from board (root = 20) |
| 22 | Well of transmutation dead | CONFIRMED | Missing `else`: rejected item still dries the well; persists in save |
| 23 | Boss can't attack behind walls | NEEDS INFO + CONFIRMED stall | No wall-spawn in base game; blocked-boss AI stall + `King.canAttack` defect real |

---

## Details

### 1 — Pet reaches the clicked tile before the hero — CONFIRMED

**Scheduling (primary):**
- Tap: `Hero.handle(cell)` (`actors/hero/Hero.java:426-456`) → `nextAction` (`actors/Char.java:1036-1046`) → `Actor.next()` sets `current = null` — no tile reservation.
- `Actor.processTurnBased` (`actors/Actor.java:358-399`) acts every non-hero actor whose `time` ≤ hero's before the hero's turn (`:386-391`).
- `nextActor()` (`actors/Actor.java:479-494`) sorts a snapshot of `all` (a `HashSet`, `:253`) by `Float.compare(a1.time, a2.time)` — exact ties broken arbitrarily by HashSet iteration order.
- Ties are the steady state: idle turn-based hero spends nothing (`Char.java:2042-2046`), hero and pet both spend `1/speed()` per step (`Hero.java:1102`, `Char.java:1530-1537`). Any actor add/remove reshuffles HashSet → winner flips → "sometimes".

**Rendering (secondary, pets-only):** `Mob.moveSprite` (`actors/mobs/Mob.java:255-263`) snaps via `place(to)` (no slide) when `!getSprite().isVisible() || !Dungeon.isPathVisible(from,to)`, or starts a second tween (stutter) when visible. Visibility inputs stale: `placeTo` uses `Dungeon.visible` computed for the hero's *previous* position (FoV refreshes in `Hero.onMotionComplete → observe()` `Hero.java:651-656`; `GameScene.update` runs `Actor.process` at `GameScene.java:601` *before* observe at `:612`). Hero never goes through `doStepTo`, so only pets show it.

**Fix:** (a) hero-first tie-break in `nextActor()` (or monotonic insert-seq tiebreaker; optionally −ε for just-clicked hero); (b) drop/gate the redundant trailing `moveSprite()` in `Char.doStepTo`, compute sprite visibility against post-move FoV. Regression: `tests/http_api/test_turn_economy.py`.

### 2 — Bag window invisible but still clickable after "give to pet" — CONFIRMED (regression shipped in beta.4)

Chain (`windows/WndItem.java:73-89`):
1. `acton.act(owner)` → `PetInventoryManager.openPetSelect` → `heroBag.hide()` (`mechanics/PetInventoryManager.java:161-164`) → `Window.hide`/`destroy` clears children, removes camera, unregisters keys (`ui/Window.java:120-134`); `WndBag.destroy` nulls statics (`windows/WndBag.java:427-435`).
2. `CommonActions.hideBagOnAction(...)` returns false (`mechanics/CommonActions.java:37-39`) → "keep bag open, refresh" branch → `bag.getActiveDialog() == null` → `bag.updateItems()` called on the destroyed window.
3. `WndBag.updateItems()` (`WndBag.java:237-245`) creates new `ItemButton`s on an orphaned window; each Button's `TouchArea hotArea` registers in `Touchscreen.event` (`noosa/ui/Button.java:31-51`, `noosa/TouchArea.java:29`) and is never destroyed.
4. `Touchscreen.event` is stack-mode Signal = newest-first → ghost buttons dispatched first, swallow taps; not drawn (not in scene tree) but hit-test via stale `Window.camera` which still does math (`noosa/Visual.java:246-256`).

Workarounds match: clicking a ghost slot → `ItemButton.onClick` → `wndBag.hide()` → cleanup; resize/relogin rebuilds scene.

**Fix:** guard refresh when `getParent() == null` (in `WndItem.onClick` or `WndBag.updateItems()` no-op when orphaned).

### 3 — Spider nest minion duplication — NEEDS RUNTIME

- Spider nest is pure Java: `com/nyrds/pixeldungeon/spiders/levels/SpiderLevel.java` (mobs via `SpiderSpawner.java:11-30`), all spider spawns go through `Bestiary`/`MobFactory` — cannot clone a player minion. No Lua level script.
- Full minion-clone machinery: `Mob.makeClone` (`Mob.java:578`), `Mob.split` (`Mob.java:386`; callers: `Multiplicity.java:37`, `Moongrace.java:37`, `Swarm.java:50`), `Mob.resurrect` (`Mob.java:415`; caller: `BlackSkull.java:45`), `Carcass.reanimate` (`items/Carcass.java:223`), `ScrollOfMirrorImage.java:30`. None reachable from the spider level.
- Old dup guard `Dungeon.switchLevel` (`Dungeon.java:343-355`, `f375a53b5` "30.1.fix.11") has 2 gaps: the `hero.initialAlies` loop (`Dungeon.java:357-359`) has **no** guard; guard dedupes by id only — two distinct objects of the same minion (bundle round-trip) not detected. `Char` doesn't override `equals`/`hashCode`.
- Spider branch is the only place with 5 revisited branch levels + a self-loop (`levelsDesc/Dungeon.json` `6s→7s→…→10s→6s/9s`), so any transition-path dupe surfaces there first.

**Fix:** instrument `Level.mobsFollowLevelChange` (`levels/Level.java:386-407`) with `levelId` + follower id/kind logging; extend the guard to dedupe by `getOwnerId()+getEntityKind()+getPos()`; guard `initialAlies` too. Then reproduce with HTTP API.

### 4 — Swap with pet standing in doorway leaves door closed — CONFIRMED

`Char.swapPosition` (`actors/Char.java:1729-1755`): pet placed on hero's cell (the doorway) via `placeTo(chPos)`, *then* hero's `placeTo(myPos)` runs `Door.leave(oldPos)` — `levels/features/Door.java:25-33` checks `getHeap(pos) == null` only, never occupants → sets `Terrain.DOOR` (closed) **under the standing pet**, fires `Dungeon.observe()` so LoS/light recompute as blocked. Pet not flying → its `placeTo` took the `press()` branch, never `Door.enter` (`Char.java:1139-1145`); `ensureOpenDoor()` no-ops (map already `OPEN_DOOR`, `Char.java:1747-1750`).

**Fix:** `Door.leave(pos)` no-op when `Actor.findChar(pos) != null` (mirror the heap guard).

### 5 — Revive minion into chasm → hang; restart rolls back, all minions gone — CONFIRMED (3 sub-bugs)

(a) **Resurrect into chasm, pet dies instantly:** `RaiseDead.lua:17-21` records death cell (chasm cell); `cast` (`RaiseDead.lua:40-56`) checks only `cellValid(mobPos)`, never `level.pit`; `Level.spawnMob` (`levels/Level.java:903-937`) presses pet cells → `Level.press` (`:1390-1399`) → `Chasm.mobFall` → `mob.die(new Chasm())`. 15 mana spent, "success" returned, no minion; `onDie` hook re-records the chasm cell → repeats. (Chasm is `AVOID|PIT`, not `PASSABLE` — but `findPath` ORs `avoid` at `Dungeon.java:924`, which is how mobs step into chasms.)

(b) **The hang:** every `loadingOrSaving.incrementAndGet()` is unwrapped — `Dungeon.save` (`Dungeon.java:589-593`), `Dungeon.init` (`:184`, `:218`), `Dungeon.newLevel` (`:243`, `:259`). `GameLoop.onFrame` (`game/GameLoop.java:248, 264`) skips the whole update while counter > 0; `GameScene.update` returns early on `Dungeon.isLoading()` (`GameScene.java:581`). One exception inside a save (e.g. Lua error from `serializeLevelData`, `Dungeon.java:520-521`) leaks the counter → permanent frozen picture, no crash. (Ruled out: unbounded actor loop — `Mob.act` has a bounded no-spend guard `Mob.java:212-226`.) Hang trigger needs a live repro to name the exact exception.

(c) **Rollback + pet loss:** no per-turn save (`Dungeon.save` only from `InterlevelScene` ×4, `GameScene.pause`, `WndInGameMenu`, `Amulet`). `Level.mobsFollowLevelChange` (`Level.java:386-406`) strips followers *before* `Dungeon.saveCurrentLevel()`; `InterlevelScene.fall` (`:274-289`) re-saves the level without pets; followers exist only in memory across the transition (`Char.getPets()` derives from `level().mobs`, `Char.java:1832-1839`). Killing the hung process ⇒ old level, zero pets, hero rolled back. Side effect: chasm death skips `getBelongings().dropAll()` (`Mob.java:370-379`) — pet's items silently destroyed.

**Fix:** (1) refuse/redirect pet spawn on `level.pit` (use `getNearestTerrain` like `Dungeon.spawnPet`); (2) wrap all `loadingOrSaving` increment/decrement in try/finally; (3) persist followers in the game bundle or save level before stripping.

### 6 — Enemy potion use logs hero-phrased message — CONFIRMED

`PotionOfHealing.apply` (`items/potions/PotionOfHealing.java:28-32`): `heal(hero, 1f); GLog.p(PotionOfHealing_Apply);` — unconditional, no drinker parameter. String `values/strings_all.xml:1635` "Your wounds heal completely." / RU `values-ru/strings_all.xml:1634`. Mob drink path: `ai/Hunting.java:32-35` → `MobItemAi.tryUseItem` (scores `PotionOfHealing`+`AC_DRINK`, `MobItemAi.java:98-103`) → `Item.execute` → `Potion._execute AC_DRINK` (`Potion.java:122-146`) → `drink` → `onThrow(self-cell)` → `apply(thrower)`. The mob announcement one frame earlier *is* parameterised (`Mob_ItemUse`, `MobItemAi.java:62-65`).

**Fix:** parameterise / branch on `hero instanceof Hero`; reuse `Mob_ItemUse` pattern. Audit other `GLog.*` in items reachable from `MobItemAi`.

### 7 — Wand of Telekinesis doesn't discharge traps — CONFIRMED (description stale)

- Description promises it: `WandOfTelekinesis_Info` (`values/strings_all.xml:2398`, RU "разряжая ловушки" `values-ru:2407`), used at `WandOfTelekinesis.java:122-125`.
- `WandOfTelekinesis.Effect` implements `Presser` but `affectLevelObjects()` returns **false** (`:127-131`); `Level.press` (`levels/Level.java:1417-1422`) only calls `levelObject.bump(actor)` when that's true.
- `Trap.bump(Presser)` (`nyrds/levels/objects/Trap.java:122-142`) matches Hero/LevelObject/Mob/Item only — a bare Presser does nothing.
- All real traps are LevelObjects (`LevelTools.java:212-279`, `CommonLevel.placeTraps` drops `Trap.makeSimpleTrap` without touching `map[]`; terrain traps DEPRECATED per `TerrainFlags.java:58-74`) → the legacy terrain-trap switch (`Level.java:1425-1461`) is dead code.
- Second no-op hook: `levelObject.push(getOwner())` (`:97-100`) — `LevelObject.pushable()` defaults false, `Trap` never overrides.

**Fix:** `affectLevelObjects() == true` + generic-Presser branch in `Trap.bump`, or iterate beam cells calling `Trap.activate(null)` in `onZap`.

### 8 — Swarm split + fire death = game freeze — CONFIRMED (silent infinite loop)

- `Swarm.defenseProc` (`actors/mobs/Swarm.java:33-46`): bare `hp(hp() - cloneHp)` (`:41`); `Char.hp(int)` is a field write with **no death check** (`Char.java:1422-1424`).
- `Char.damage(...)` early-returns `if (!isAlive())` at `:814` — **before** the death check at `:839-841`. If `defenseProc` (runs during attack, before `enemy.damage(...)` — see `Mob.zap` `Mob.java:540-543`) drives hp ≤ 0, `die()`/`destroy()` never run → dead Swarm stays in `Actor.all` + `level().mobs`.
- `Mob.act` returns without `spend()` when dead (`Mob.java:190-195`); `nextActor()` has no liveness filter (`Actor.java:479-495`) → selected forever. The `checkedAct` invariant skips dead chars (`Char.java:292-310`, `isAlive()` condition) → **no crash report, just a hang**. Matches "froze, restart with rollback".
- Negative-damage hole: `hp() >= damage + 2` guard passes for negative damage (buff/armor `defenceProc` can drive damage negative, `Char.java:715-730`); `cloneHp = max((hp()-damage)/2, 1)` (`Mob.java:390`) → hp −1.
- Fire amplifies: `Burning.act` keeps ticking (water detach gated on `!isFlying()`, `Swarm.java:23` flying=true); `makeClone` bundle round-trip packs buffs (`Char.java:353`) → every clone inherits fresh Burning → several splitting burning swarms.

**Fix:** clamp in `Swarm.defenseProc` (go through damage path); death-check in `Char.hp(int)` for on-stage chars; skip/remove dead actors in `nextActor()`/`processTurnBased`; drop harmful buffs in `Mob.split()`.

### 9 — Bats refuse trap cells — CONFIRMED

`Trap.nonPassable` (`nyrds/levels/objects/Trap.java:244-249`): `ch instanceof Mob → !secret && uses > 0`, ignores flight. `Dungeon.markObjects` (`Dungeon.java:830-853`): `:844` applies `nonPassable` **unconditionally** — defeats `ignoreDanger` (= `isFlying() || amok`, `:913`); the `avoid` branch at `:848` is correctly gated. Both path entry points call it: `Dungeon.findPath` (`:939`), `Dungeon.flee` (`:956`). Chain: `Bat.java:22` flying → `Mob.getCloser/getFurther` → no path → `_doStep` false → `Hunting.act` flips to Wandering (`ai/Hunting.java:43-45`). Flyers don't even trigger traps (`Char.placeTo` `:1136-1147` gates `press` on `!isFlying()`) — blocking them is pure loss. Terrain side is handled (`Dungeon.findPath:934` ORs `avoid` for flyers); it's the LevelObject branch only.

**Fix:** `Trap.nonPassable` adds `!ch.isFlying()`, or gate `Dungeon.java:844` with `!ignoreDanger`.

### 10 — Psi-spider remains unsellable — CONFIRMED

`Carcass.price()` (`items/Carcass.java:276-278`): `src.ht() / 6 * quantity() * upgradeMultiplier()` — int division first. `SpiderMind` ("пси-паук") has `ht(5)` (`mobs/spiders/SpiderMind.java:37`) → 0. Sell list filters `item.price() > 0` (`windows/ItemButton.java:133`); `Carcass.price()` never calls `adjustPrice()` (which owns the ≥1 clamp, `Item.java:554-556`). Any mob with ht < 6 gives permanently unsellable remains.

**Fix:** `return Math.max(1, src.ht() * quantity() * upgradeMultiplier() / 6);` or route through `adjustPrice`.

### 11 — Roots works on exploding skulls — CONFIRMED

`Roots.attachTo` correctly refuses flying (`actors/buffs/Roots.java:18-23`); `Buff.prolong` → `attachTo` honors it. `ExplodingSkull` (`nyrds/mobs/necropolis/ExplodingSkull.java:14-16`) and `RunicSkull` never set `flying = true` (no `mobsDesc/ExplodingSkull.json` either). Contrast `EnslavedSoul.java:39` which does.

**Fix:** `flying = true;` in both initializers (optionally `addImmunity(Roots.class)` belt-and-braces). Also stops them pressing traps/terrain.

### 12 — Untranslated string — LIKELY (candidates; screenshot missing)

RU has all 3009 keys of EN (`values-ru/strings_all.xml`); only legit non-translatable keys identical. So it's a bypass of l10n. Candidates, contextual order:
- `assets/scripts/actors/Chess.lua:207` "You Lose!", `:240` "You Win!", `:276` "Stalemate!...", `:657` "Click on the board — it is your move, Hero" — hardcoded `RPD.glog()` literals (`maybeId` falls back to raw literal).
- `assets/scripts/actors/PalaceServants.lua:37` "The palace servants turn on you!"; `assets/scripts/mobs/NeutralKing.lua:18` "The King turns on you!".
- `items/Carcass.java:190` — English `"item"/"items"` substituted into translated RU `Carcass_DissectResult`; `Carcass.java:203` hardcoded title "Select an item".
- `Codex_Story_25` — only untranslated Codex story entry (byte-identical in RU).

**Fix:** add keys + switch Lua to `RPD.glog("<key>")` (pattern exists: `RaiseDead.lua:65`); plural-aware string for Carcass; translate Codex_Story_25.

### 13 — Corpse + black skull = body cloning — CONFIRMED

`Mob.die` order: equipped-item callbacks at `Mob.java:342` (`forEachEquipped → item.charDied` → `BlackSkull.charDied` `items/necropolis/BlackSkull.java:34-60` → `mob.resurrect(hero)` at `:45`) run **before** the carcass drop at `:370-376` (`Random.Float(1) <= carcassChance`, default 0.5, `Mob.java:78`). `Mob.resurrect` (`Mob.java:415-427`) spawns a fresh pet at the same cell, never touches a corpse; `Mob.revive` only nulls `carcassRef` and nothing calls it. Second half confirmed: pet death drops a carcass — the roll has no `isPet` guard (compare `Statue.java:37`, `EnslavedSoul.java:35` which set `carcassChance = 0`).

**Fix:** suppress the carcass roll when the death produced a resurrection (flag in `resurrect(Char)`), and/or never drop carcass for owned pets (`getOwnerId() != getId()`); optionally consume the existing `Carcass` at the cell on resurrect.

### 14 — Skull pets gain XP, necromancy pets don't — CONFIRMED (flag mismatch, design decision needed)

- Per-hit grant: `Char.attack` → `if (ModQuirks.mobLeveling && this instanceof Mob) earnExp(1)` (`Char.java:511-515`) — no pet/undead check.
- Sole gate: `Char.earnExp` early-returns on `undead` (`Char.java:2207-2210`).
- Necromancy sets `setUndead(true)` (`items/Carcass.java:226`, `scripts/spells/RaiseDead.lua:55`) → zero combat XP. Black skull path (`Mob.resurrect`, `Mob.java:418`) doesn't → behaves like `ScrollOfDomination` minions (`items/scrolls/ScrollOfDomination.java:36-45`), levels up.
- `isPet`/`undead` do orthogonal jobs; neither means "gains combat XP".

**Fix (design call):** decide the intended pet-XP rule; narrow option — make black-skull-raised minions consistent with necromancy (undead), or decouple XP from `undead` with an explicit pets-gain/don't-gain rule.

### 15 — Frost spirit can be frozen — LIKELY BUG

`ColdSpirit` ("Дух мороза", `nyrds/mobs/icecaves/ColdSpirit.java:16-31`): no `addImmunity`/`addResistance` at all → `immunities()` empty → `Frost.attachTo` paralyzes it (via `Buff.attachTo` immunity check `Buff.java:105-113`). It *deals* freezing (`:34-40`). Convention: `WaterElemental.java:41` (Frost), `FireElemental.java:35-39` (Fire/Burning/…), `IceElemental` (Roots/Paralysis/Stun/ToxicGas) — every elemental immune to own element.

**Fix:** `addImmunity(Frost.class)` minimum (+ Bleeding / PsionicBlast per convention).

### 16 — Enslaved statue regenerates armor — CONFIRMED

`ArmoredStatue.getItem()` (`nyrds/mobs/common/ArmoredStatue.java:46-73`): empty ARMOR slot unconditionally generates a fresh random treasury armor + random glyph (`Treasury…random`, `inscribe(Glyph.random())`); same in `Statue.java:101-132` (weapon variant; comment `:122-126` explains lazy generation to avoid double-spend). Triggered by every sprite rebuild (`Char.getSprite` when null → `Actor.init:302`, `GameScene:265`, `Actor.java:434`) and every description tap (`Statue.getDescription` `:83,86,90` → `CharDescTab.java:48-50`). Extraction is a normal unequip (`EquipableItem.java:31-33, 100-118`) → lands in pet backpack → trivially extracted via `PetInventoryManager.takeItemFromPet`.

**Fix:** `@Packable boolean gearGranted` set on first materialisation; empty-slot branch no-op once set. Same in `Statue.java`.

### 17 — Statue STR follows equipped armor's STR requirement — CONFIRMED (statue-only)

`STR(Math.max(12, item.requiredSTR()))` runs on every `getItem()`: `Statue.java:130`, `ArmoredStatue.java:71` — against whatever is currently equipped. Constructors never set STR → statue STR is entirely gear-derived. `Char.STR(int)` is a plain setter (`Char.java:2182-2184`). Knock-on: `Belongings.encumbranceCheck` (`actors/hero/Belongings.java:363-375`) compares requirement vs `effectiveSTR()` → self-fulfilling, statue wears anything with zero penalty. Heroes unaffected (`Hero.java:142`, `Hero.effectiveSTR:162-165`).

**Fix:** set STR once at construction (or from first generated item); delete the sync line from both `getItem()`s.

### 18 — Multiplication armor on statue = item dupe farm — CONFIRMED

`Multiplicity` glyph (`items/armor/glyphs/Multiplicity.java:19-42`) is in the standard random pool (`items/armor/Armor.java:244-249`) and `ArmoredStatue` inscribes a random glyph (`ArmoredStatue.java:63-65`). Proc: `defenceProc` → `((Mob) defender).split(cell, damage)` → `Mob.makeClone` (`Mob.java:578-594`) = full `storeInBundle`/`restoreFromBundle`; `Char.storeInBundle` packs belongings (`Char.java:347-357`), `Belongings.storeInBundle` packs backpack + equipped slots (`actors/hero/Belongings.java:244-247`, `:191-203`) → clone gets its own copy of armor + glyph + upgrades. Clone is a hero-owned controllable pet (`Mob.java:587-591`); killing it drops the copied armor (`Mob.java:378`). Combined with 16 (empty slot refills with fresh random glyph) + 17 (no penalty) = unlimited enchanted-armor farm.

**Fix:** strip belongings in `makeClone` when cloning via `split` (necromancy precedent: `Carcass.java:231` `pet.getBelongings().clear()`), or `Multiplicity` falls back to `MirrorImage` for non-Hero defenders.

### 19 — Dwarf tokens sell for 100g — BY DESIGN (price) / LIKELY farm (source)

`DwarfToken.price()` = `quantity() * 100` (`items/quest/DwarfToken.java:26-28`) — upstream-verbatim (`85d810f14`). Sell = `item.price()` via `WndTradeItem.java:227-241` → `Char.java:2131-2132`. Source: `Monk.java:66-68` / `Golem.java:44-46` die → `Imp.Quest.process` (`Imp.java:193-201`) — one token per kill, no cap, while quest open. Beta.2 added the palace: 2-4 neutral real `Monk`/`Golem` servants (`CityBossLevel.java:197, 213-216`) + King summons (`King.java:139, 145-149`) — `Imp.Quest.process` excludes nothing (the `King.java:147` comment blocks XP only). Sink: alchemy `Earthroot.Seed + DwarfToken → PotionOfStrength` (`scripts/alchemy_recipes.lua:111-117`).

**Fix options (design call):** gate the drop (skip undead / CityBossLevel), or `price()` → 0 (unsellable; quest + alchemy remain sinks), or lower price and pay real value at turn-in.

### 20 — Chess: resurrected piece corrupts the board — CONFIRMED

`movePiece` (`scripts/actors/Chess.lua:103-122`): kills the target by cell (`:118` `target:die(mob)`) then mover `setPos(to)` (`:119`). Inside `die()`, `Mob.die` → equipped callbacks → `BlackSkull.charDied` (`BlackSkull.java:34-59`, `mob.canBePet()` is true for everything — `Mob.java:522-525`) → `Mob.resurrect` (`Mob.java:414-427`) spawns a brand-new pet Succubus **at the death cell, before the warlock's `setPos` lands**. Two actors in one cell (Actor chars is a multimap, `Actor.java:262`; `findChar` returns arbitrary first of a HashSet, `Actor.java:582-595`). All piece resolution is positional: click gating by engine board char (`Chess.lua:671-676`), per-frame retint paints whatever mob sits on a piece square (`Chess.lua:402-423`). The `pieces` table is write-only — no identity registry anywhere. Unlucky branch: succubus retinted as queen and moved by `movePiece`; real warlock stranded on a square the engine thinks is empty → "not your piece" forever. Lucky branch: pet succubus roams the board killing pieces out-of-band → `movePiece: no mob on cell … skip` (`:105-107`) after the engine already committed.

**Fix:** (a) gate BlackSkull out of chess (level property check in `BlackSkull.charDied`, or make chess pieces non-`canBePet`); (b) maintain a real `chessCell → mob` registry in the `pieces` table; `movePiece`/`retintPieces` look up by identity, ignore untracked mobs.

### 21 — Chess: "illegal move" accepted — LIKELY display desync (root = 20)

Engine path is sound: player moves built from `sunfish.legal_moves_uci` (`Chess.lua:687-693`) and applied via `sunfish.move` (`:724`, non-legal ⇒ "illegal move" `:746`); `sunfish.move` = pseudo-legal `genMoves` + `is_legal` (`scripts/stuff/chess/sunfish.lua:2182-2219`, validation `:2213`); `is_legal` handles double check, single check, pins, en-passant, king moves, castling (`:1028-1148`); search gates every move with `m_is_legal` (`:1696`, `:1908`, `:1920`); book plays through `move` (`:2342`); the unvalidated `bkm_light` shortcut is gated to 3-piece endings where self-check is impossible (`:2362`, `:2384`, `:2240-2284`).

Seen symptom = picture desync: engine applies a legal move; `animateMove`/`movePiece` resolve positionally via `findChar` and move the wrong mob or skip (`:105-107`) when the mob↔square map is corrupted (bug 20, or hero/pet attacking pieces). Engine game stays legal; visible board diverges.

Secondary finding: `sunfish.stm_is_black` (`sunfish.lua:2113-2126`) inverted for how `Chess.lua:741` calls it (passes `chess:rotate()`) → mirrored book moves, validated and normally rejected → opening book effectively dead for AI replies. Harmless but the book never plays.

**Fix:** fix 20 first; harden `movePiece`/`animateMove` to refuse animating when `findChar(from)` isn't the tracked piece; periodic reconciliation of mobs on `.` squares; fix `stm_is_black`.

### 22 — Well of transmutation dead after rejected item — CONFIRMED (regression)

`WaterOfTransmutation.affectItem` (`actors/blobs/WaterOfTransmutation.java:44-67`): `Item transmutedItem = item;` with type branches and **no terminal else** → non-transmutable item returns itself → `WellWater.affect` (`WellWater.java:72-91`) sees `newItem == oldItem` but returns true anyway (`:87`) → `well.lua:48-55` sets `self.data.water = nil` → permanently dry; `well.lua:init` (`:13-38`) re-seeds only if `water` non-empty → **persists in save**. Upstream had `else { return null; }` (see `git show 85d810f14`); broken by `7f86d62a4` ("WellOfTransmutation will change spell…"), kept by `704553809`. Matches the player's "threw a non-transmutable item earlier". Minor related: `ItemUtils.throwItemAway` (`nyrds/items/ItemUtils.java:24-35`) silently deletes the item if no adjacent valid cell.

**Fix:** `transmutedItem = null` + terminal `else { return null; }` (upstream semantics); `changeWeapon` returns null for unhandled weapon types; optionally don't delete items in `throwItemAway`.

### 23 — Boss can't attack hero behind spawned walls — NEEDS INFO + CONFIRMED stall

No wall-spawning ability exists in base code: runtime `Terrain.WALL` writes are confined to level builders + painter/pickaxe scripts; only mob terrain writes are DM-300 deco (`DM300.java:104`), Crystal embers, EarthElemental regrowth; the only boss-level runtime change is `BossLevel.seal()` (one arena door, `BossLevel.java:41-45, 147`). Likely a mod boss, or the player means summoned statues/pillars (`levelObjects/statue.json`, `pile_of_stones.json` — layer-0 blockers per `WalkingType.java:33-39`). Need level/boss/mod name.

Confirmed stall mechanism: `Hunting.act` (`ai/Hunting.java:38-46`) — `canAttack` false → `doStepTo` → `findPath` returns `INVALID_CELL` (unreachable/occupied, `Dungeon.java:925-941`) → flip to `Wandering` + `randomDestination()` → endless wander; no ranged/melee fallback, appears to do nothing forever (no hard freeze — always spends).

Adjacent confirmed defect: `King.canAttack` (`actors/mobs/King.java:94-99`) returns only `getPos() == targetPedestal` while `canTryToSummon()` is true — the Dwarf King **refuses melee on an adjacent hero** for most of the fight (`maxArmySize` grows as HP drops, `:108-132`).

**Fix:** `King.canAttack` → `adjacent(enemy) || (canTryToSummon() && getPos() == targetPedestal)`; `Hunting` fallback attack when step fails and enemy is in reach; keep real enemy target on stall.

---

## Cross-cutting themes

1. **BlackSkull resurrect** is the root of 13, 20, and the chess desync 21. One gate (`canBePet` / level check) kills three bugs.
2. **`makeClone` bundle round-trip** dupes equipment (18) and inherits harmful buffs (8).
3. **Traps→LevelObject migration** left two stale consumers: telekinesis (7) and flying pathing (9).
4. **Statue `getItem()` generator** — 16 and 17, same two methods.
5. **Silent-hang class**: dead actors wedging the turn loop (8) and leaking the `loadingOrSaving` counter (5) both freeze without a crash report. Worth a global invariant.

## Suggested priority

1. **5** — data loss class (pet roster deleted, save counter leak)
2. **18, 16, 17** — enchanted-armor dupe farm, one small PR
3. **8** — silent freeze, reproducible, systemic fix value
4. **2** — shipped regression, one-line guard
5. **22, 4, 10, 11, 9, 6, 13** — small confirmed fixes
6. **20/21** — needs the chess registry refactor (bigger)
7. **3, 23, 12** — need runtime data / screenshot / player info first
8. **1, 14, 15, 19** — design decisions (tie-break policy, pet XP, elemental immunity, token economy)
