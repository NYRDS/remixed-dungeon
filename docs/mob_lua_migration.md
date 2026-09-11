# Mob Java → Lua Migration Procedure

How to move a mob from a java class to json data + lua script while keeping
old saves loadable. Long-term goal: every entity is json/lua; only the core
engine stays java.

Strategy (revised 2026-09-10, replaces the shell-class/alias design):

1. **One-time engine change**: saves resolve mobs by `entityKind` string
   through the existing factories (`MobFactory`), instead of
   `Class.forName` on the stored FQN. The FQN stays in the bundle and
   remains the fallback for old saves.
2. **Per-mob migration** then needs no save code at all: add json + lua,
   delete the java class and its registration — the factory automatically
   serves old and new saves the data-defined mob, because it is keyed by the
   entity kind, which is just the class simple name.

Reference examples of fully data-defined mobs (no java class): `BlackRat`,
`Snail`, `DeepSnail`, `Bee`, `BlackCat` — each is `mobsDesc/<Kind>.json` +
`scripts/mobs/<Kind>.lua`, instantiated as `CustomMob` by the json scan in
`MobFactory`.

Migrated from java 2026-09-11 as the first Step-B test batch: **`Bat`**
(json stats + `resistances:["Leech"]` + lua `attackProc` heal),
**`Worm`** (json stats + `immunities` + lua `attackProc` Roots/Poison procs;
note `Random.Int(a,b)` is `[a,b)` — lua port is `math.random(a,b-1)`),
**`Kobold`** (pure json, no lua needed). `Pickaxe`'s bat-blood quest check
now goes by kind (`MobFactory.BAT`). Verified: legacy pre-migration save (FQN-only) restores
all three via derived-kind with hp/str/fraction identical, fresh spawns and
death-loot drops work, lua attackProc round-trips (`/debug` harness).

Second batch 2026-09-11: **`ColdSpirit`** (json + Frost immunity + lua
`attackProc` — java's `Freezing.affect(pos)` also cleared fire/froze heaps
on the victim's cell; the lua port keeps only the Char-facing Frost proc),
**`PseudoRat`** (pure json + `aiState:"Hunting"`; sprite-kind int is
obsolete — `spriteDesc` frames replace it), **`SpiritOfPain`** (json +
lua `act` self-damage 6/turn; `carcassChance:0` and `exp:0` in json).
Construction sites switched to factory: `SuspiciousRat` (wererat
transform), `TreacherousSpirit` + `HeartOfDarkness$Buff` (summon procs).
Verified live: stats parity (50/320/80 ht), Hunting/SLEEPING default
states, `act` hook self-destruct timing, level-save round-trip keeping
hp/state/pos, level `.dat` stores `entityKind` + `CustomMob`.

## Save mechanics (why this works)

- Today each mob is stored in the level bundle as a nested `Bundlable` with
  `__className` = fully-qualified java class name; `Bundle.get()` does
  `Class.forName` → `newInstance()` → `restoreFromBundle()`. If the class is
  missing, the mob is **silently skipped** (logged, save loads, mob gone) —
  the data-loss risk this procedure eliminates.
- The entity kind of a java mob is `getClass().getSimpleName()` — never
  persisted. `MobFactory` registers every java mob under its simple name and
  every json def under its file name; `mobByName(kind)` returns the java
  class if registered, else `new CustomMob(kind)` (loads
  `mobsDesc/<Kind>.json` + `scriptFile`). So `kind` alone is sufficient to
  construct any mob, java or data.
- Deriving the kind from a legacy FQN is faithful: strip the package
  (substring after the last `.`), then after any `$` for nested classes
  (`...mobs.King$Undead` → `Undead`). The result is exactly the factory key
  by construction.
- Restore chain (`Char.restoreFromBundle`): `fillMobStats(true)` re-reads
  `mobsDesc/<Kind>.json` and re-attaches `scriptFile`; then hp/ht/lvl/fraction
  and buffs are applied from the bundle; then lua `loadData` and `fillStats`
  run. The lua script binds lazily by kind (`scripts/mobs/<Kind>`, Dummy
  fallback). Lua state round-trips via `LUA_DATA` (`saveData`/`loadData`,
  serpent-serialized `self.data`).
- Mob restore sites in saves: `Level` `MOBS` collection and `Dungeon` `PETS`
  collection. Nothing else deserializes `Mob` instances.

## Step A — kind-based save resolution (one-time engine work)

**Implemented 2026-09-11** (tested with Bat/Worm/Kobold, see Step B examples):

- `Bundlable` gained default `getEntityKind()`/`getEntitySystem()` (null);
  `Char`→"mob", `Item`→"item", `Buff`→"buff", `LevelObject`→"levelObject".
  `Bundle.put(Bundlable)`/`put(key, Collection)` write both tags next to
  `__className` (shared `bundleFor` helper).
- `Bundle.get()` resolution order: tagged kind → system's resolver;
  else derived kind (FQN tail) through the resolvers in fixed priority
  mob→item→buff→levelObject; else the untouched `Class.forName` fallback.
  Resolvers live in `Bundle.registerEntityResolver`, filled by a `Dungeon`
  static block (`MobFactory::tryByName` etc.).
- Strict gates added: `MobFactory.tryByName` (hasMob), `ItemFactory.tryByName`
  + `hasItem` (java registration | `scripts/items/<kind>.lua` | Carcass prefix
  — never the CustomItem/Gold tail), `BuffFactory.tryByName`
  (hasBuffForName), `LevelObjectsFactory.tryByName` (isValidObjectClass).
- A5: `PdAnnotationProcessor` now emits the current field value as the
  implicit unpack default (`bundle.optString("mobClass", typedArg.mobClass)`)
  — factory-pinned identity fields survive legacy bundles.
- Ordering fix found by the test: `Dungeon.loadGame` restored PETS *before*
  `Potion/Wand/Scroll/Ring.restore`, so any pet whose loot def builds a
  potion crashed the restore (java Kobold was unrecoverable this way:
  `new PotionOfFrost()` in its ctor → `Potion.handler` null NPE). Handlers
  now restore before pets. Related: `Pickaxe` identified bats by
  `instanceof Bat` — now `getEntityKind().equals("Bat")` so the quest works
  with the data-defined mob.

A1. Write two fields next to `__className`: `entityKind` (= `getEntityKind()`)
    and `entitySystem` (`"mob"`, `"item"`, `"buff"`, `"levelObject"`, …).
    The system tag keeps categories from colliding — the same kind string
    may exist in several systems. Implementation: default methods
    `getEntityKind()`/`getEntitySystem()` on `Bundlable` (null default),
    overridden by the base classes; `Bundle.put(Bundlable)` and
    `put(key, Collection)` write both. Char covers mobs (its `entityKind`
    already exists via `Actor`); Item, Buff, LevelObject, Blob, Heap
    already have `getEntityKind()` — they only need the system constant.

A2. Resolution with fallback, dispatching on the system tag. Bundle gains a
    registry `Map<String, Function<String,Bundlable>>` filled at game boot
    (`"mob" → MobFactory::tryByName`, `"item" → ItemFactory::tryByName`,
    `"buff" → BuffFactory::tryByName`, `"levelObject" → …`). Element
    resolution order:

    1. `entitySystem`+`entityKind` present → look up the system's factory;
       recognized kind → instance. Direct `bundle.get(key)` sites (Mob
       loot, Bones) resolve the same way — no per-site resolver threading.
    2. else derive kind from legacy `__className` (strip package, strip
       `$`-tail) → same factory — but only accept resolutions backed by a
       java registration or an existing data def (see A3 gate); otherwise
       fall through.
    3. fallback: today's exact `Class.forName(__className)` path — keeps
       heroes, blobs, levels, scripted actors, journal records and any
       unregistered class working unchanged.

    After construction (either path), run the same
    `BundleHelper.UnPack` + `restoreFromBundle` sequence as today.

A3. Non-throwing factory entries with a strict gate. The resolver must
    return null (→ FQN fallback), never guess:

    - `MobFactory.tryByName(kind)`: null unless `hasMob(kind)`
      (`mobByName` throws on unknown; and a no-def `CustomMob(kind)` must
      never be constructed from a bare kind). Mind the challenge filters
      inside `hasMob` — a filtered kind (e.g. `ArmoredStatue` under
      No armor) returning null is fine, the FQN fallback preserves today's
      behavior.
    - `ItemFactory.tryByName(kind)`: same shape. **Do not route the
      resolver through `itemByName` unguarded** — its tail falls back to
      `CustomItem(kind)` and, on failure, to Gold, silently replacing any
      unrecognized item. Gate: java registration, `scripts/items/<kind>.lua`
      existing, or the `Carcass of <Mob>` prefix. Add `ItemFactory.hasItem`.
    - `BuffFactory`: `getBuffByName` exists but its tail yields
      `CustomBuff`/`DummyBuff` — gate with `hasBuffForName`.
    - `LevelObjectsFactory.objectByName` — gate with its registration
      check.

    The gate makes legacy (no `entityKind`) bundles safe: a derived kind
    resolves only into a java class or a real data def — never into a
    def-less CustomMob or the Gold tail. Explicit `entityKind` (new saves)
    may use the full factory including composite kinds like
    `Carcass of Rat`.

A4. Call sites. With the system tag dispatching inside `Bundle`, no site
    changes are required — collections (`Level` MOBS/OBJECTS/BLOBS/HEAPS,
    `Dungeon` PETS, `Char` BUFFS, `Heap`/`Bag` ITEMS) and direct gets
    (Mob LOOT, Bones ITEM) all flow through `Bundle.get`. Mobs: `Level`
    MOBS + `Dungeon` PETS; items: Heap, Bag, Mob LOOT, Bones.

A5. Processor fix — required before any java class is deleted. Generated
    `BundleHelper.UnPack` restores `@Packable` fields with a hardcoded
    default: `mobClass = bundle.optString("mobClass", "Unknown")`. A legacy
    java-mob bundle routed through the factory has no `mobClass` field, so
    the kind pinned by `new CustomMob(kind)` would be clobbered to
    `"Unknown"`. Fix in `PdAnnotationProcessor.generateDirectUnpackCode`:
    when no explicit `defaultValue` is set, generate the **current field
    value** as the default (`bundle.optString($S, typedArg.$L)`). For a
    freshly constructed object that is identical to today's behavior; for
    factory-pinned identity fields it preserves them. Regenerates
    automatically on compile.

Why old saves need no conversion: a pre-`entityKind` save stores `...mobs.Rat`;
derived kind `Rat` → factory. If `Rat` is still registered → same java mob
as before, byte-identical behavior. If `Rat` was already migrated →
`CustomMob("Rat")` → json stats + lua behavior. If the derivation misses
(unregistered kind), the FQN fallback keeps the old behavior. Downgrade
compat: old app versions ignore the extra fields and read the
FQN as before.

## Saved-entity systems inventory

Every Bundlable category that reaches a save, and how it resolves:

| System (tag)     | Factory                      | Restore sites                          | Notes |
|------------------|------------------------------|----------------------------------------|-------|
| `mob`            | MobFactory                   | Level MOBS, Dungeon PETS               | target of this doc |
| `item`           | ItemFactory                  | Heap ITEMS, Bag ITEMS, Mob LOOT, Bones | Gold tail must be gated (A3) |
| `buff`           | BuffFactory (exists)         | Char BUFFS                             | java + `scripts/buffs/*.lua` customs; tail yields CustomBuff/DummyBuff — gate |
| `levelObject`    | LevelObjectsFactory (exists) | Level OBJECTS                          | deco, signs, barrels, plants, trap objects |
| `blob`           | none yet                     | Level BLOBS                            | few classes, engine-ish; FQN fallback until data-fied |
| `heap`           | none needed                  | Level HEAPS                            | single concrete class |
| script actors    | none needed                  | Level SCRIPTS                          | ScriptedActor carries its script id in-data already |
| journal records  | none needed                  | JOURNAL/RECORDS/LOGBOOK                | engine data, not gameplay entities |
| `level` / hero   | none needed                  | direct gets                            | single classes, stay java |

Traps exist twice: terrain triggers (`ITrigger` classes, not bundled) and
`LevelObject` trap objects (bundled, covered by the factory above).

## Coverage audit (2026-09-10)

Transitive closure over all `Mob`/`Item` descendants in
`RemixedDungeon/src/main/java`, checked against factory registrations and
data-def scans (`mobsDesc/*.json`, `scripts/items/*.lua`):

- Mobs: 108 concrete descendants. All registered or json-defined except
  `TreacherousSpirit` (spawned by AzuterronNPC) and `ImpShopkeeper`
  (LastShopLevel shops) — **registered 2026-09-10**. `CustomMob`/
  `MultiKindMob` are base classes; the nested `WandOfFlock$Sheep` is
  registered manually as `Sheep`.
- Items: 185 concrete descendants. All registered or lua-defined except
  `ChaosBlade` (chaos event → inventory) — **was java-side, not lua;
  registered 2026-09-10**; `Carcass` (dropped on mob death, saved in
  heaps) is fine once resolution reads `entityKind` — its kind is the
  composite `Carcass of <Mob>` and `itemByName` already handles the
  prefix. The rest
  of the unregistered list are base classes never instantiated directly
  (`Armor`, `Ring`, `Potion`, `Weapon`, `Key`, `Bag`, `Seed`, …), UI-only
  (`ItemPlaceholder`), reconstructed-not-bundled (`Backpack` — its
  contents store flat into the Belongings bundle), the data path itself
  (`CustomItem`), and dead code (`TitanSword`, zero references).
- No duplicate registrations, no simple-name collisions within mobs or
  items, no kind shared between the mob and item factories. No entities in
  other modules.

## Step B — migrate one mob

Java rule (2026-09-11): java code referencing an entity kind uses the named
constant in its factory class (`MobFactory.BAT`, `ItemFactory.GOLD`,
`LevelObjectsFactory.PEDESTAL`, `BuffFactory.POISON`) — never a magic
string. Add the constant if it doesn't exist yet; the literal lives only at
the constant definition.

B0. **Survey**: all java construction sites (`new Rat(`, `Rat.class`, nested
    classes like `King.Undead`, `WandOfFlock.Sheep` constructed by engine
    code — switch them to `MobFactory.mobByName(kind)`). Check for an
    existing partial `mobsDesc/<Kind>.json` (java `fillMobStats` reads it)
    and `scripts/mobs/<Kind>.lua` (binds by kind even for java mobs).

B1. **Author the json def** `mobsDesc/<Kind>.json` — explicit authored
    stats, no derived heuristics:

```json
{
  "name": "Rat_Name", "name_objective": "Rat_Name_Objective",
  "description": "Rat_Desc", "gender": "Rat_Gender",
  "attackSkill": 8, "defenseSkill": 3,
  "dmgMin": 1, "dmgMax": 3, "dr": 0,
  "ht": 8, "exp": 2, "maxLvl": 4, "str": 10,
  "baseSpeed": 1, "attackDelay": 1, "attackRange": 1,
  "viewDistance": 3, "walkingType": "NORMAL",
  "spriteDesc": "spritesDesc/Rat.json",
  "scriptFile": "scripts/mobs/Rat",
  "lootChance": 0.125, "loot": "MeatRation",
  "canBePet": true, "friendly": false, "flying": false,
  "fraction": "DUNGEON"
}
```

    `spriteDesc`/string ids are optional if conventional locations exist.
    `fraction`, `canBePet`, `friendly`, `immortal`, `movable` are re-read
    from json on every load, even for old saves — json is authoritative.

B2. **Move behavior to lua** `scripts/mobs/<Kind>.lua`:

```lua
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    stats = function(mob) end,           -- runs on ctor AND every load; keep idempotent
    attackProc = function(mob, enemy, dmg) return dmg end,
    defenceProc = function(mob, enemy, dmg) return dmg end,
    act = function(mob) return true end,
    move = function(mob, cell) return true end,
    die = function(mob, cause) return false end,
    spawn = function(mob, level) end,
    interact = function(mob, chr) end,
    zapProc = function(mob, enemy, dmg) return dmg end,  -- IZapper mobs
    zapMiss = function(mob, enemy) end,
    selectCell = function(mob) end,
    actions = function(mob, hero) return {} end,
    execute = function(mob, hero, action) end,
    priceForSell = function(mob, item) return 0 end,
    priceForBuy = function(mob, item) return 0 end,
}
```

    Each callback replaces the corresponding java override (`attackProc` →
    `Mob.attackProc`, etc.); the engine dispatches into the script from
    `Mob`/`Char` hook points, the `mob` library bridges names.

    **State rules — save compat of this step:**

    - Only `self.data` survives saving (serpent-serialized into
      `LUA_DATA`). Upvalues and non-packed java fields are lost on load.
    - `stats` runs on construction **and on every restore**. It must be
      idempotent and deterministic: never roll random stats there without
      persisting the roll in `self.data` — an elite variant would silently
      re-roll on every load.

B3. **Delete the java class and its `registerMobClass` entry.** No shell,
    no alias, no save-code: Step A routes every save version to
    `CustomMob(kind)`. Keep the kind string and class simple name stable
    forever — they are the save and mod identity. Until the class is
    actually deleted, an incremental variant is fine: keep the class, add
    json+lua, move methods over one by one (script binds by kind and the
    engine calls its hooks).

## Verification checklist

1. Build: `:RemixedDungeonDesktop:compileJava` (after Step A, this also
   regenerates `BundleHelper`).
2. **Old-save fixture**: before starting, create a save containing the mob
   (debug spawn), copy it out of the rundir. After each step,
   `/debug/continue_game` from rundir: mob restores with correct name,
   sprite, hp/ht, fraction/pet state, loot, behavior. Debug saves are
   cleartext — grep them for `entityKind`/`__className` to confirm what is
   stored.
3. New-game spawn of the kind; kill it (loot/carcass), let it path, attack.
4. Logs on load: `load: <kind> <id>` vs `skip:` (a `skip:` or an
   EventCollector exception naming the class = restore broken),
   `No mob def: <kind>` = json missing.
5. `MobFactory.allMobs()` smoke (mob viewer) — catches missing sprite/json
   across all kinds at once.
6. Lua state round-trip: set state at spawn, save, load, verify.
7. After Step A: load a pre-change save and diff behavior; then delete one
   low-risk java mob class and confirm its old-save fixture still restores
   (now via factory→CustomMob).
8. `spotlessApply` (imports only); match the file's dominant indent — repo
   has no unified indent style.
