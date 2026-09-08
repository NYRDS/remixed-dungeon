# Minion/Pet feedback — 32.4.beta.6 — code findings & suggested fixes

Source: beta tester feedback (2026-09-08), Russian original relayed by Mike.
All paths relative to `RemixedDungeon/src/main/java` unless noted.
Line numbers refer to the working tree at the time of analysis (master, post `c068ce06d`).

## Summary table

| # | Report | Root cause (one line) | Severity |
|---|--------|----------------------|----------|
| 1 | Doors never close after leaving | Heap on tile (incl. mob carcasses) or a pet standing on the tile cancels `Door.leave`; `setPos` bypasses closing | medium |
| 2 | Lit cell stuck on map after descend/reload | Pets punch a wall-blind 3×3 hole into FoV every observe; pets spawn at the stairs | high (visual) |
| 3 | Statue duplicated after exiting town shop | Id-based dedup in `Dungeon.switchLevel` defeated by stale save copies that get fresh ids on restore | rare, high |
| 4 | Spider-nest minion duplication (unfixed) | `Moongrace.effect` splits any mob incl. hero pets; `SpiderExploding` (kind 7) applies it on hit; clones keep ownerId | high |
| 5 | Orders panel doesn't fit on screen | `WndOptions`/`WndPetBag` use fixed sizes, never clamp to `WndHelper` screen bounds, no scrolling | medium (UI) |
| 6a | Statues' STR resets to 10 on reload | `Char.baseStr` is not persisted for non-hero chars | high |
| 6b | Pets "teleport" to player on re-enter | By design: `spawnPet` next to hero on level change + `Wandering` return-to-owner walks instantly off-screen | by design / polish |
| 7 | Pet STR doesn't affect carrying | Encumbrance speed penalty exists only in `Hero.speed()`; `Char.speed()` has no term; pets get complaint buff but no penalty | medium |
| 8 | Damage cancels movement orders | `MoveOrder.gotDamage → seekRevenge` unconditionally replaces state/target, incl. DoT ticks (burning/gas) | high (UX) |
| E1 | Tap on minion = 3 different outcomes | Overloaded gesture in `CharUtils.actionForCell`; heap/object precedence outranks pet | UX |
| E2 | Ordering to player's cell = minion attacks player | `OrderCellSelector` converts any `Interact` into `Attack` | high (UX) |
| E3 | "Stay there" minions ignore enemies | `Passive.act` spends a tick and does nothing else; only `gotDamage` wakes them | design |
| E4 | Army strands in narrow corridors | Pet follow = `Wandering.returnToOwnerIfTooFar`; no path-following / nearest-reachable fallback | design |

---

## Bug reports

### 1. Doors never close after you leave the door tile

> **FIXED (2026-09-08):** the main culprit was an ordering bug: `placeTo` called
> `Door.leave(oldPos)` *before* `setPos` freed the mover's old cell, so
> `Door.leave`'s `Actor.findChar(pos) == null` guard always saw the mover itself
> and refused — doors never closed even on plain walk-out. Now every position
> change closes the door it left: `Char.setPos` calls the new `closeDoorBehind`
> *after* `occupyCell` (i.e. after `freeCell(this)`), guarded by
> `GameScene.isSceneReady()` so level gen/transitions don't observe() early.
> `placeTo`'s inline pre-close block is gone (its fly-onto-door handling stays).
> Covers walk, swap, blink (`WandOfBlink.appear` routes through `placeTo`),
> `_stepBack`, spawn repositioning and script `setPos`.
>
> Door-wedging heaps are **intentional** (design decision 2026-09-09): a heap on
> a door tile keeping it open is an established player tactic, and death drops
> (carcass, mob gear) wedge the door exactly the same way — `Mob.die` keeps
> dropping at the death cell. A player who wants the door closed picks the heap
> clean. An occupied doorway stays open by design; a door whose occupant *died*
> on it stays open until the next char passes through, then closes — unless a
> heap wedges it.
>
> Verified live on the desktop debug server (SewerLevel): closed 5 → hero steps
> on, 6 → hero leaves, back to 5 (two doors); a door stays open under a standing
> pet and closes once the char leaves.

The close path is intact and unchanged since the classic import:

- `features/Door.java:25-34` — `Door.leave(pos)` sets `Terrain.DOOR`, but **silently does nothing if a heap sits on the tile**.
- Trigger: `actors/Char.java:1122-1130` — `placeTo()` closes the door of the cell being left, for hero, mobs and pets alike.
- Open path: `levels/Level.java:1457-1460` (`Level.press`), `Char.java:1751-1755` (`ensureOpenDoor` for swaps/splits).

Why doors stay open in practice:

1. **Heap on the tile.** `Door.java:28` (`getHeap(pos) == null` check). Remixed amplifies this: mobs dying in a doorway drop a carcass heap (`actors/mobs/Mob.java:372-374`), and `dropAll` on death drops gear there too (`Mob.java:378`). The door then never closes until the player picks the heap clean — easy to misread as "doors never close".
2. **A char (typically a pet) standing on the tile.** Closing only fires when the occupant *leaves*; nothing closes a door under a standing char, and pets are re-placed next to the hero on every level entry and love to idle in doorways. `swapPosition` deliberately re-opens (`ensureOpenDoor`) and leaves the door under whoever stays.
3. **`setPos` bypasses door handling.** `Char.setPos` (`Char.java:1425-1433`) does no `Door.leave`, so teleports / `Level.spawnMob` (`Level.java:921`) / `_stepBack` leave doors open behind the mover.

**Suggested fixes**

- `Door.leave`: close when the *last* char leaves — add `Actor.findChar(pos) == null` is not needed for closing itself, but **close the door in `Char.setPos` too** (or funnel all position changes through a single `leaveOldCell` helper that handles doors).
- Stop permanent heaps from pinning doors: either let a door close under a heap (heap stays, visual overlap acceptable) or, at minimum, drop carcasses/loot on the nearest non-door cell (`Mob.die`: pick `getPos()` only if not a door tile).
- Audit `Level.spawnMob`'s unconditional `press` for pets (`Level.java:933-937`) — spawning a pet onto a closed door silently opens it.

### 2. One cell near stairs / hero's old position stays lit after descend or reload

> **FIXED (2026-09-09):** the pet pass in `Level.updateFieldOfView` no longer
> paints unconditionally. The new `updateFovForPetAt` contributes a pet's 3×3
> only while the hero actually sees the pet — the pet's own cell must already be
> in the freshly cast `fieldOfView` — and intersects both the pet cell and its
> 9 cells with `discoverable[]`. A pet parked out of sight (e.g. at the stairs
> after descend) therefore leaves no lit patch and burns nothing into
> `level.mapped`; a blind or dead hero (empty FoV) gets no pet vision at all;
> the `CharsList.DUMMY`-at-`-1` corner case is covered by the `cellValid` guard.
> Mind-vision / Huntress / Awareness passes keep their unobstructed marking
> (magical senses, by design). Verified on the desktop debug server (SewerLevel):
> an owned statue spawned 20 cells from the hero leaves its 3×3 dark
> (visible 0/9, mapped 0/9) while an adjacent pet's 3×3 stays lit (9/9); after
> `go_to_level` both pets respawn next to the hero and are fully lit, logs clean.

Not stale FoV — the hero FoV recompute is clean (`mechanics/ShadowCaster.java:44` clears the array; `Dungeon.initSizeDependentStuff` reallocates on level entry/reload).

The real mechanism — **pets contribute wall-blind vision**:

- `levels/Level.java:1540-1543` — for every pet of the hero, `updateFovForObjectAt(pet.getPos())` marks a **3×3 around the pet** (`Level.java:1490-1499`, `NEIGHBOURS9`), with **no line-of-sight check**, no `discoverable` check, and it runs even while the hero is blind or dead.
- Marks flow into `Dungeon.visible` (`Dungeon.java:813-819`), burn into `level.mapped` permanently, and make mobs in those cells drawn (`GameScene.java:973-976`).
- On descend, `Dungeon.spawnPet` places every pet on the nearest walkable tile **to the hero, i.e. at the stairs** (`Dungeon.java:367-386`) — exactly where the tester sees the stuck patch. A pet following at distance illuminates a corridor segment the hero can't see (screenshot 1).
- Edge case: a pets-list id that resolves to `CharsList.DUMMY` marks cells around `pos = -1`, i.e. cells 1 and `width±1` in the map's top-left corner (`updateFovForObjectAt(-1)`).

**Suggested fixes**

- In `updateFovForObjectAt`, require the pet's own cell to be visible/discoverable and gate the 9 cells on LOS (cheapest correct version: only mark if `fieldOfView[p]` was already true from the shadow cast, or intersect with `discoverable[]`).
- Skip marking for invalid positions (`cellValid(p)` guard also fixes the `-1` corner case).
- While auditing, same treatment for HUNTRESS 3×3 (`Level.java:1550-1556`) is worth a look, though it's by design for that class.

### 3. Statue duplicated after exiting the town shop (once, unreproducible)

> **FIXED (2026-09-08):** hero-owned pets are no longer written into level save
> files at all; they are persisted in the game bundle (`pets` key) and re-spawned
> from there on CONTINUE/resurrect. Old-format level saves migrate their pets out
> on load, a stale-copy sweep in `Dungeon.switchLevel` and kind-based dedupe in
> `Dungeon.loadLevel` guard the crash-window leftovers, and `Level.reset()` no
> longer drops hero pets. `Char.baseStr` is persisted too (fixes 6a). Verified on
> the desktop debug server: transitions/continue/reset keep exactly one pet, and
> an injected stale level-save copy resolves to exactly one pet.

Pet re-add on level change happens in exactly one place, `Dungeon.switchLevel` (`Dungeon.java:308-365`), with an id-based dedup guard (`Dungeon.java:344-352`, logs `"Removing dup"`).

The hole in the guard:

1. Town/shop levels are non-static and saved with their mob list; a periodic or transition-time save (`Dungeon.save(false)` at `scenes/InterlevelScene.java:250,279,297,318`) can retain a stale copy of the pet in the destination level's save.
2. On restore, that copy fails `CharsList.add` (duplicate id) in `Char.restoreFromBundle` (`Char.java:364-368`) and **silently receives a fresh id**, while `Level.restoreFromBundle` still adds it to `level.mobs` (`levels/Level.java:751-762` — only checks `isDestroyed`).
3. The guard in `Dungeon.switchLevel` looks up `CharsList.getById(followersId)` by the *original* id — the stale copy no longer has it, so the guard can never match, and both copies end up in the level, both hero-owned.

Narrow timing window between the two saves — consistent with "happened once".

**Suggested fixes**

- Dedup followers by `ownerId + entityKind` (or by name) instead of by id only.
- In `Level.restoreFromBundle`, drop mobs whose `CharsList.add` reported DUPLICATE_ID instead of keeping them in `level.mobs`.
- Optional: on restore, detect hero-fraction mobs with `ownerId == hero.id` that are *not* in the current follower set and remove them if the same kind arrived as a follower.

### 4. Spider-nest minion duplication — root cause found (Moongrace)

> **FIXED (2026-09-08, scoped to the exploding spider):** `SpiderExploding` now
> triggers its plant with itself as the *activator* (`Plant.effect(pos, ch,
> activator)` — activator is the victim itself when a plant is stepped on, the
> attacking mob when the effect is forced by a hit). `Moongrace` splits via
> `Mob.splitHostile` only when the activator is hostile to the pressed mob, so a
> pet hit by a Moongrace-kind spider leaves a *hostile* copy (self-owned,
> `Fraction.DUNGEON`), not a free extra minion. **Planted Moongrace keeps
> friendly clones on purpose** — players split their pets with it deliberately;
> that's a feature, not the bug. `Mob.split` and `Mob.makeClone` keep their
> generic semantics (split = clone with damage, clone = faithful copy; call
> sites re-purpose the result). Verified on the desktop debug server: a
> Moongrace-kind exploding spider killing an owned rat produced a self-owned
> DUNGEON-fraction rat clone (HP 1, split signature) — no pet duplicate.
>
> **Still open by design decision (2026-09-08):** the other two pet-clone
> delivery paths keep old behavior — `ChaosShieldLeft.lua` `cloneEnemy` (splitting
> a pet attacking a shield-bearer) and the Multiplicity glyph on pet armor still
> produce *friendly* clones, i.e. remain pet-duplication vectors. Revisit if the
> spider-nest style dup resurfaces.

`Moongrace.effect` **splits any mob that presses the plant** (`plants/Moongrace.java:32-41` → `Mob.split(cell, 0)`), and `Mob.makeClone` (`actors/mobs/Mob.java:577-594`) **copies `ownerId` for pets (lines 587-591), so the clone is also a hero-owned pet**. `Level.spawnMob` has no dedup (`levels/Level.java:903-938`).

Delivery paths for pets:

- **`SpiderExploding` kind 7 = "Moongrace"** — on melee hit it instantiates the plant and calls `plant.effect(enemy.getPos(), enemy)` (`com/nyrds/pixeldungeon/mobs/spiders/SpiderExploding.java:43-54`). Any hero minion hit by an exploding Moongrace spider is **cloned on the spot** — no flower on the floor needed. This is the spider-nest bug.
- Pet walking/spawning onto a Moongrace plant cell (`Plant.bump` via `Level.press`; `Level.spawnMob` presses for pets, `Level.java:934-936`).
- `scripts/buffs/ChaosShieldLeft.lua:91-96` — blocking a pet's attack can `enemy:split(cell,0)`.

`RespawnerActor` is exonerated: it only counts non-pets and spawns brand-new hostiles (`actors/RespawnerActor.java:27-34`).

**Suggested fixes**

- Primary guard in `Mob.split` (or `Moongrace.effect`): **never split hero-owned pets** (`isPet()` → no-op / just particles).
- If cloning should stay possible in principle, clone without ownership (`new_mob.setOwnerId(new_mob.getId())`) and/or as a hostile fraction — decide the design intent first.
- Check `SpiderExploding`'s effect choice: applying terrain-modifying plants to *pets* (and to the hero's allies) may deserve its own exclusion list.

### 5. Orders panel doesn't fit the screen

- `windows/WndOptions.java:19-64` — hard-coded width (`Window.STD_WIDTH`), `resize(STD_WIDTH, (int) vbox.height())` — **height never clamped to screen**, no scrolling.
- `windows/WndPetBag.java:37-55` extends `WndBag` which renders a fixed 23-slot grid (`WndBag.java:131,148,164-166`; 4 cols portrait → 6 rows), also unclamped. `WndPetItem`/`WndPetQuantity` same pattern.
- `WndHelper.getFullscreenHeight()` / `getLimitedWidth()` (`com/nyrds/pixeldungeon/windows/WndHelper.java`) exist and are already used by e.g. `WndTitledMessage` — pet windows just don't use them. No literal X-button found in these windows; the reported overflow is the unclamped panel itself.

**Suggested fixes**

- Route all pet windows through `WndHelper` limits (`resizeLimited`-style), and/or add scroll to `WndOptions` when content exceeds fullscreen height.
- `WndPetBag`: size the slot grid from available height (or paginate).

### 6a. All statues' STR resets to 10 on re-enter

- `Char.baseStr` (`actors/Char.java:144`, default 10) is **never persisted for non-hero chars** — `Char.storeInBundle` (`Char.java:344-358`) doesn't save it; only `Hero` saves `"STR"` separately (`actors/hero/Hero.java:175,189,212`).
- A statue's STR only exists as a side effect of `Statue.getItem()` (`actors/mobs/Statue.java:117`: `STR(Math.max(12, item.requiredSTR()))`), which runs on sprite/description creation, not reliably after load. `GoldenStatue` never sets it at all.
- After reload `effectiveSTR()` = 10 → saved heavy gear triggers the permanent Encumbrance buff (`Belongings.java:393-403`, `Char.java:326-330`) → pets yell "too heavy" and fail equip checks.

**Suggested fixes**

- Persist `baseStr` for all chars (add to `Char.storeInBundle`/`restoreFromBundle` under a new key for save compatibility).
- Alternatively/additionally: re-derive statue STR from their equipped gear on restore (`Statue.restoreFromBundle` post-hook).

### 6b. Pets "teleport" to the player on re-enter

Two mechanisms, both currently by design:

- Level *changes*: `Mob.followOnLevelChanged` (`Mob.java:132-135`) → `Dungeon.spawnPet` places each pet on `getNearestTerrain(hero.getPos(), …)` (`Dungeon.java:362-386`).
- Plain *reload*: pets restore at saved positions, but any pet in `WANDERING` immediately walks back to the owner (`com/nyrds/pixeldungeon/ai/Wandering.java:22-24` → `MobAi.returnToOwnerIfTooFar`), and off-screen steps are applied instantly with no animation (`Mob.moveSprite`, `Mob.java:255-263`) — perceived as teleport. Swapping positions with the hero sets `WANDERING` (`Mob.swapPosition`, `Mob.java:528-534`), so statues are usually in this state.
- Pets given utility items may also use `ScrollOfTeleportation`/`WandOfBlink` autonomously when fleeing (`com/nyrds/pixeldungeon/ai/MobItemAi.java:112-116, 344-352`) — matches the tester's "depends on whether I gave them things".

**Suggested fixes (polish)**

- Animate/relocatepets quietly but only within visibility, or leave a "catching up" state instead of instant relocation.
- Decide whether pets should teleport to the owner on reload at all, or restore in place.

### 7. Pet STR doesn't affect carrying

> **FIXED (2026-09-09):** the encumbrance speed term moved from `Hero.speed()`
> into `Char.speed()` — armor overload (`requiredSTR() - effectiveSTR() > 0`)
> now slows *any* carrier by `1.3^-aEnc`, pets included; the computation mirrors
> the pre-existing generic term in `Char.defenseSkill` (armor-only, same shape).
> `Hero.speed()` keeps only the Freerunner sprint, granted only when not
> encumbered (same branch semantics as before — numeric results for the hero are
> identical). Equip gating was deliberately **not** added: pets keep the same
> freedom the pet equip window gives them (equip over-STR gear), but the penalty
> is now real — slow movement, `Char.defenseSkill` evasion loss, and the
> Encumbrance complaint buff that was already attaching. `MobItemAi.autoEquip`
> keeps using `statsRequirementsSatisfied`, so auto-equip never picks gear the
> pet can't wear; only manual overload is possible, exactly like for the hero.
> Verified on the desktop debug server via the new `/debug/test_equip` endpoint
> (force-equips, bypassing the STR gate, like WndPetItem can): fresh Statue
> (STR 12) + PlateArmor level -2 (requiredSTR 19) → aEnc 7, speed 1.0 → 0.159
> (= 1.3⁻⁷, exact); hero with normal gear stays at speed 1.0. `char_status`/
> `hero_status` now also report `speed` and `str`.

- Speed penalty for overload exists **only in `Hero.speed()`** (`actors/hero/Hero.java:271-278`, `1.3^-aEnc`). Generic `Char.speed()` (`Char.java:767-771`) has no encumbrance term; `Mob` doesn't override.

> **Initial mob STR (2026-09-09, follow-up review):** mobs no longer start with
> the flat default 10. `Mob()` ctor assigns `baseStr` = `mobsDesc/<Kind>.json`
> `"baseStr"` when present, else derived from the mob's typical depth in
> `levelsDesc/Bestiary.json` (weighted mean of the depths it spawns at, rounded
> up), paced after the armor tiers met at that depth: `max(10, 7 + 2·ceil(depth/5))`
> → 10/11/13/15/17 for the T1..T5 depth bands. Explicit `STR()` calls in mob
> classes (Brute 14, Statue gear-derived) still win — subclass ctor runs after
> `super()`. Deliberate (Mike): undead keep their no-exp no-leveling gate, so
> for undead pets this initial STR is permanent; living pets add `lvl()/5` on
> top via `ModQuirks.mobLeveling` (1 exp per landed hit, Champion buff at lvl 5+).
> Verified live: Rat/Gnoll 10, Skeleton 11, Bat 13, Brute 14 (class value).
- Pets *do* get the Encumbrance buff (attached to any overloaded char, `Char.java:326-330`) → complaints, and `Char.defenseSkill` evasion penalty (`Char.java:607-613`).
- `Belongings.equip` performs **no STR check** for pets (`Belongings.java:711+`), so over-STR gear is trivially equipped via `WndPetItem`.

**Suggested fixes**

- Move the encumbrance multiplier from `Hero.speed()` into `Char.speed()` (hero path then inherits it automatically) — or override `Mob.speed()`.
- Optionally gate pet equipping on `statsRequirementsSatisfied` (already used by `MobItemAi.autoEquipScore`) with the STR fix from 6a making it meaningful.

### 8. Taking damage cancels movement orders

> **FIXED (2026-09-09):** one guard in `MobAi.seekRevenge`: pets (`isPet()`,
> HEROES fraction) ignore damage whose source is not a Char — DoT ticks pass the
> buff instance (`target.damage(dmg, this)` in Burning/Poison/Bleeding/Ooze/Hunger),
> gas and traps likewise — so orders and follow survive them. All eight AI states'
> `gotDamage` funnel into `seekRevenge`, and it is the only seekRevenge definition,
> so no per-state edits were needed. Damage from a real attacker (Char source)
> still drops the order into revenge, as before. Wild mobs untouched (guard is
> pet-only). Verified on the desktop debug server via the new `/debug/order_pet`
> + `/debug/test_damage` endpoints (they drive the real `Interact.act` →
> `CellSelector` flow and real `Char.damage` calls): pet in MoveOrder + Burning
> tick → stays MoveOrder, enemy clear; hit by a hostile Rat (Char src) → order
> dropped, revenge state; DoT while Wandering-follow → unchanged.

- Order = AI state `MoveOrder` + target cell (`ml/actions/OrderCellSelector.java:36-40`; `ai/MoveOrder.java:15-17` steps toward `me.getTarget()`).
- Every damage tick calls `gotDamage` (`Char.damage` → `Char.java:817`); `MoveOrder.gotDamage → seekRevenge` (`MoveOrder.java:23-26`; `MobAi.seekRevenge`, `ai/MobAi.java:47-66`) **unconditionally replaces state and target** — Hunting if any enemy visible, else Wandering to a respawn cell. Burning/gas ticks have no Char source, so they hit the `chooseEnemy` branch and still discard the order. Same shape in `Wandering`, `Passive`, `Hunting`, `KillOrder`.

**Suggested fixes**

- In `MoveOrder.gotDamage` (and ideally `Passive`/`Wandering` for pets): if `me.isPet()` and the damage source is not a Char (DoT/blob), keep the order; only react to real attackers.
- Optionally: pets retain orders through damage entirely; player can re-issue (matches tester expectation of "orders are absolute").

---

## Behavior / design reports (extras)

### E1. Tapping a minion does three different things

Plain tap is overloaded in `CharUtils.actionForCell` (`actors/CharUtils.java:231-289`):

- pet outside hero FoV → `Move(cell)` — hero walks;
- pet adjacent → `Interact` → `Char.interact` → `swapPosition` (hero and pet swap cells; silent no-op if swap fails);
- pet distant + visible → order mode (`Interact.act` → `hero.selectCell(OrderCellSelector)`), not a window;
- the orders/inventory **window** is only reachable via info-mode tap (`InformerCellListener`) or the toolbar button (adjacent-only);
- `handleObjectOrHeap` is checked *before* the pet (`CharUtils.java:241-243`) — a heap/level object under the pet outranks the pet.

**Suggested direction** (matches tester's ask): make plain tap on a visible owned pet always open the orders window (or walk to it — pick one); check heap precedence only in info mode. Cheap first step: move the heap/object check after the friendly-pet check.

### E2. Selecting the player as order target → minion attacks the player

> **FIXED (2026-09-09):** `OrderCellSelector.onSelect` now intercepts the case
> "ordered cell is occupied by the selector" *before* `CharUtils.actionForCell`
> runs: pet → `Wandering` (follow owner), enemy cleared, target = player cell,
> says `Mob_FollowMe` ("Following you!"/"Следую за тобой!", new string in en+ru
> `strings_all.xml`). Necessary at this level because the doc's original theory
> (the `Interact`→`Attack` conversion) is only the *secondary* path: the primary
> one is `Hero.friendly(Mob)` → `heroClass.friendlyTo(kind)` — a class-list
> lookup (`initHeroes.json` `friendlyMobs`, e.g. Gnoll/Shaman for some classes)
> that ignores ownership entirely, so for a warrior a Statue pet is "not
> friendly" and `actionForCell` returns `Attack(hero)` directly, without any
> `Interact` ever being built. The early occupancy check covers both mechanisms.
> Verified live via `/debug/order_pet` onto the hero's cell: Wandering,
> enemySet=false, "Следую за тобой!", hero HP untouched (before: KillOrder on
> the player, "Считай этого тебя мёртвым!").
>
> **Observation, open:** because `Hero.friendly` ignores ownership, other
> hero-perspective checks (targeting, AoE, interact prompts) may misbehave
> around owned pets whose kind is not in the class's `friendlyMobs` list.
> Making owned (`isPet()`) mobs always hero-friendly in `Hero.friendly` would be
> the deeper fix — needs its own pass, touches balance.

Confirmed, explicit code: `OrderCellSelector.onSelect` converts any `Interact` into `Attack` (`ml/actions/OrderCellSelector.java:44-46`); ordering to the hero's cell yields `Interact(hero)` → `KillOrder` on the player.

**Fix**: special-case `Interact` target == the hero/owner → "follow me": set `Wandering` (follow owner) and clear any standing order — exactly the tester's intuition.

### E3. "Stay there" minions ignore enemies until attacked

`ai/Passive.act` just spends a tick with `enemySeen = false` (`Passive.java:16-20`); only `gotDamage → seekRevenge` wakes them.

**Suggested direction**: give the stay-order its own state (or parameterize `Passive`): keep scanning enemies; engage in-place when in attack range (respect ranged vs melee), return to post when the enemy dies/leaves.

### E4. Army strands in corridors (>2 minions can't follow)

Follow logic = `Wandering.returnToOwnerIfTooFar` (straight-line `getCloser`); no path-following behind the hero, so in a long 1-wide corridor only the first couple keep up; the rest stall or try side routes.

**Suggested direction**: for pets with no enemy in sight, pathfind to the hero's *trail* (last cells the hero occupied) or fall back to "nearest reachable cell to the owner" instead of giving up — tester's explicit suggestion.

---

## Priority order (proposed)

1. **#4** pet cloning via `split()` (guard hero pets) — small change, kills two reproducible dup bugs.
2. **#6a** persist `baseStr` — small save-format change, removes a whole class of post-load weirdness.
3. **#2** pet FoV 3×3 → LOS-gated — small change, fixes the most visible map bug.
4. **#8** orders survive non-Char damage — small, big UX win.
5. **E2** order-to-player → follow me — small.
6. **#1** doors close under last-occupant + carcass placement — medium.
7. **#5** clamp pet windows to screen — medium (UI).
8. **#3** follower dedup by kind/owner — rare but prevents dupe.
9. **#7** encumbrance for pets — medium, touches balance.
10. **E1/E3/E4** — design decisions first, then implement.
