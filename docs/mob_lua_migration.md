# Mob Java → Lua Migration Procedure

How to move a mob from a java class to json data + lua script while keeping
old saves loadable. Long-term goal: every entity is json/lua; only the core
engine stays java.

Reference examples of fully data-defined mobs (no java class): `BlackRat`,
`Snail`, `DeepSnail`, `Bee`, `BlackCat` — each is `mobsDesc/<Kind>.json` +
`scripts/mobs/<Kind>.lua`, instantiated as `CustomMob` by the json scan in
`MobFactory`.

## Why save compat is non-trivial

Facts about persistence (verified in code):

- Each mob is stored in the level bundle as a nested `Bundlable` with
  `__className` = **fully-qualified java class name**. On load,
  `Bundle.get()` does `Class.forName(clName)` → `newInstance()` →
  `restoreFromBundle()` (`com.watabou.utils.Bundle`).
- The entity kind string is **not persisted** for java mobs — it is derived
  as `getClass().getSimpleName()` (`Actor.getEntityKind`). `CustomMob`
  persists its kind as the `@Packable` field `mobClass`, but old java-mob
  bundles don't have that field.
- Restore chain (`Char.restoreFromBundle`): `fillMobStats(true)` re-reads
  `mobsDesc/<Kind>.json` and re-attaches `scriptFile`; then hp/ht/lvl/fraction
  and buffs are applied from the bundle; then lua `loadData` and `fillStats`
  run. The lua script is bound lazily by kind: `scripts/mobs/<Kind>` with
  `scripts/mobs/Dummy` fallback (`Char.getScript`).
- Lua state round-trips through the save: `Char.storeInBundle` saves
  `script:saveData()`, restore calls `script:loadData(str)`. The `mob` lua
  library implements this via `self.data` serialized with serpent.
- **Failure mode:** if `Class.forName` fails (class deleted, renamed, moved
  package), the mob is silently skipped — exception is logged, the save
  loads, the mob is gone. This is the data-loss risk of a careless migration.

Consequences:

1. The **kind string is the stable identity** — bestiary, `MobFactory.mobByName(kind)`
   respawn paths, quest/level string refs all use it. Never rename it.
2. The **java class FQN must keep resolving** for every save version you
   support loading. Deleting the class requires a restore shell or alias
   (Step 4).

## Step 0 — Survey the mob

- All java construction sites: `new Rat(`, `Rat.class`, inner classes
  (`King.Undead`, `Ghost.FetidRat`, `WandOfFlock.Sheep` are constructed by
  engine code — those call sites must switch to `MobFactory.mobByName(kind)`
  before the class can shrink).
- Existing json def: many java mobs already have a partial `mobsDesc/<Kind>.json`
  (java `fillMobStats` reads it). Complete it rather than duplicate.
- Existing lua: `scripts/mobs/<Kind>.lua` may already exist (it binds by kind
  even for java mobs).

## Step 1 — Author the json def

`RemixedDungeon/src/main/assets/mobsDesc/<Kind>.json`. Stats must be explicit
authored values (see CustomMob/Mob `fillMobStats` for the consumed fields):

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

Notes:

- `spriteDesc`/name ids are optional if conventional locations exist
  (`spritesDesc/<Kind>.json`, `<Kind>_Name` string ids).
- `fraction`, `canBePet`, `friendly`, `immortal`, `movable` are re-read from
  json on every load, even for old saves — json is authoritative for them.

## Step 2 — Move behavior to lua

`RemixedDungeon/src/main/assets/scripts/mobs/<Kind>.lua`:

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

Mapping: each callback replaces the corresponding java override
(`attackProc` → `Mob.attackProc`, `defenceProc` → `defenceProc`, `act` →
state `act` logic, `die` → `die`, etc.). Engine dispatches into the script
from `Mob`/`Char` hook points; the `mob` library bridges names.

**State rules — the save-compat core of this step:**

- Only `self.data` (via `mob.storeData`/`mob.restoreData`, or plain fields in
  the `mob.init` table's `data`) survives saving — serpent-serialized into
  `LUA_DATA`. Anything else (upvalues, engine-side java fields you stopped
  persisting) is lost on load.
- `stats`/`fillStats` runs on construction **and on every restore**
  (`Char.restoreFromBundle` ends with `script:fillStats()`). It must be
  idempotent and deterministic: never roll random stats there without
  persisting the roll — an elite variant would silently re-roll on every
  load. Derive variants from `self.data` set at spawn.

## Step 3 — Slim the java class to a restore shell

Replace the class body with a shell (keep package and name — the FQN is the
save key):

```java
/**
 * Restore shell: pre-migration saves reference this class by name.
 * All stats/behavior live in mobsDesc/Rat.json + scripts/mobs/Rat.lua.
 */
public class Rat extends CustomMob {
    public Rat() {
        mobClass = "Rat";
        fillMobStats(false);
        getScript().run("fillStats");
    }
}
```

- Keep the `MobFactory.registerMobClass(Rat.class)` entry. New spawns go
  through the shell, which is just `CustomMob` pinned to the kind.
- Do this only after Step 0 call sites use kind-based construction.
- Delete overridden stat getters (`dmgMin`, `attackSkill`, …) and behavior
  methods — json+lua own them now. Keep only what no json/lua field expresses
  yet (each leftover is engine work to expose as data, not a reason to keep
  logic in java).

If a mob still has substantial engine-coupled behavior, an incremental
variant is fine: keep the java class as-is, add/complete the json def and
move selected methods to lua (the script binds by kind and the engine calls
its hooks first). Saves stay compatible at every intermediate point.

## Step 4 — Retiring the shell class (optional, later)

A shell is three lines and permanent — that is acceptable and recommended.
Removing it entirely requires:

1. `Bundle.addAlias(CustomMob.class, "<old FQN>")` so old `__className`
   resolves. The mechanism exists (`Bundle.aliases`) but is currently
   **insufficient for mobs**: old bundles lack the `mobClass` field, so the
   restored `CustomMob` cannot learn its kind. A small engine change is
   needed first — pass the aliased (original) name to the instance at
   `Bundle.get()` so it can set `mobClass`. Until that lands, keep shells.
2. A save-generation cut-off decision: aliases must live as long as loading
   saves older than the migration is supported.

Never rename or move the shell class afterwards — that is equivalent to
deleting it (same silent data loss).

## Verification checklist

1. Build: `:RemixedDungeonDesktop:compileJava`.
2. **Old-save fixture:** before starting, create a save containing the mob
   (debug spawn), copy it out of the rundir. After each step, launch from
   rundir and `/debug/continue_game`: mob must restore with correct name,
   sprite, hp/ht, fraction/pet state, loot, and behavior. Debug saves are
   cleartext — grep them for `__className` to confirm what is stored.
3. New-game spawn of the kind; kill it (loot/carcass), let it path and attack.
4. Watch logs on load: `load: <kind> <id>` vs `skip:` (a `skip:` or an
   EventCollector exception with the class name = restore broken),
   `No mob def: <kind>` = json missing.
5. `MobFactory.allMobs()` smoke (mob viewer) — catches missing sprite/json
   for every kind at once.
6. Confirm lua state round-trip: set state at spawn, save, load, verify.
7. `spotlessApply` (imports only); match the file's dominant indent — repo
   has no unified indent style.
