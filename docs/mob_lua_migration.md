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

A1. Write the kind: in `Char.storeInBundle`, `bundle.put("entityKind",
    getEntityKind())`. (Later, when items migrate, generalize the same way
    for Item; Char covers Level.MOBS and Dungeon.PETS today.)

A2. Resolution with fallback. Add a resolver overload
    `getCollection(key, type, Function<String,Bundlable> byKind)` in
    `Bundle`; element resolution order:

    1. `entityKind` field present → `byKind.apply(kind)`; if the resolver
       recognizes it, use the instance.
    2. else derive kind from legacy `__className` (strip package, strip
       `$`-tail) → resolver; if unrecognized, fall through.
    3. fallback: today's exact `Class.forName(__className)` path — keeps
       heroes, blobs, buffs, levels and any unregistered class working
       unchanged.

    After construction (either path), run the same
    `BundleHelper.UnPack` + `restoreFromBundle` sequence as today.

A3. Non-throwing factory entry: `MobFactory.tryByName(kind)` returning null
    for unknown kinds (`mobByName` throws; the resolver must fall back, not
    throw). Mind the challenge filters inside `hasMob` — a filtered kind
    (e.g. `ArmoredStatue` under No armor) returning null is fine, the FQN
    fallback preserves today's behavior.

A4. Pass the resolver at both sites: `Level.restoreFromBundle` (MOBS) and
    `Dungeon` (PETS): `bundle.getCollection(MOBS, Mob.class,
    MobFactory::tryByName)`.

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
compat: old app versions ignore the extra `entityKind` field and read the
FQN as before.

## Step B — migrate one mob

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
  "ht": 8, "exp": 2, "maxLvl": 4, "baseStr": 10,
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
