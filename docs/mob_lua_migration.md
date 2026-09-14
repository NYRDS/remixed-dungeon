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

Third batch 2026-09-11 (trivial tier): **`Albino`** (json + lua
`attackProc` → Bleeding at 50%; Badges rare check converted from
`instanceof` to kind), **`EnslavedSoul`** (json `undead`/`carcassChance`/
Gold loot + lua attackProc: 1-in-5, hero-only, random one of six 3-turn
debuffs; hero check via `enemy:getEntityKind() == "Hero"` — the shipped
idiom, cf. RemixedPickaxe.lua), **`ExplodingSkull`** (json + lua
attackProc self-`die` — attackProc fires only on a hit, matching the java
`attack()` override that died on `super.attack()==true`; verified live:
log "взрывающийся череп ударил тебя", hero killed, skull removed),
**`Shielded`** (pure json incl. `isHumanoid`, inherited Brute stats
authored explicitly). Live-verified via the `/debug` web API: exact
hp/ht/str parity on spawn, hunting/attack/kamikaze behavior, Bleeding
proc observed on hero. Deferred with reasons: `Acidic` (inherits
Scorpio's keep-distance kite AI), `MimicPie`+`IceElemental`
(`IDepthAdjustable` depth-scaled stats), `IceGuardian`+`IceGuardianCore`
(coupled boss pair with cross-resurrection in `die()`), `Rat`/`Gnoll`/
`Crab` (quest statics in `die()` — see Step C).

Fourth batch 2026-09-11 (depth-scaled family, per Mike's assumption
"author stats at the mob's origin depth"): **`IceElemental`** (authored at
depth 17 — ice1-4 span 15-18; lua attackProc 1/3 Slow on hero),
**`MimicPie`** (fixed depth-10 stats; CharUtils' `adjustStats(Dungeon.depth)`
no-ops on CustomMob — `Mob.adjustStats` is an empty default — so json wins),
**`Mimic`** (fixed depth-10 stats + lua gold-bleed proc:
`RPD.Dungeon.level:drop(RPD.item("Gold",gp), self:getPos())` — Level.drop
and Item ops are callable from engine scripts), **`MimicAmulet`** (fixed
depth-10; note its java defense was literally 1 — `Mob.attackSkill` is a
vestigial `final int 0` its formula multiplied; permanent Levitation via
`RPD.permanentBuff` in `stats`, the Assassin.lua pattern; SkeletonKey rides
the inventory bundle — `Mob.die` drops belongings). Mimic construction:
`Mimic.spawnAt` became `CharUtils.spawnMimicAt(pos, items)` on the factory.
Verified live: all four spawn data-defined with exact authored hp/str and
inventory (RottenPasty / SkeletonKey).
Existing lua idioms to reuse (scripts/mobs/): `RPD.affectBuff(chr,"Kind",dur)`,
`RPD.permanentBuff(chr, RPD.Buffs.X)` in `stats`, `RPD.setAi(me,"Fleeing"|"Hunting")`
— ShamanElder.lua already implements the act-policy kite in production,
`self:loot(item)` theft (ScriptedThief), `self:immunities():add` (NatureAura).
Deferred from this batch: `Wraith` (spawned from cursed heaps at any depth
and by Shadow Lord at depth ~30 — flattening unacceptable),
`AirElemental` (kite band + no-adjacent-melee gate + WindGust zap),
`WaterElemental`/`EarthElemental` (terrain-conditional speed + buff
intercepts), `Crystal` (Shadow-Lord support: random wand arsenal,
pedestal death logic, hp-based damageRoll).

Fifth batch 2026-09-11 (necropolis simple tier): **`Zombie`** (json + lua
attackProc 1/3 Poison × durationFactor), **`DeathKnight`**/`**DreadKnight**`
(json + attackProc double damage + `RPD.Sfx.DeathStroke.hit(enemy)` visual —
already bound in commonClasses; `@LuaInterface` added to `DeathStroke.hit`
so the generated interface map registers it), **`Shadow`** (pure json:
`walkingType:"WALL"`, speed 2, attackDelay 0.5, `aiState:"Wandering"`),
**`Brute`** (pure json; its java `getSubClass()` override was dead code —
only `Dungeon.hero.getSubClass()` is ever read), **`TreacherousSpirit`**
(json `canBePet:false` + HeartOfDarkness as `loot`/`lootChance:1` + lua
attackProc 1/4 summon SpiritOfPain — BeeSpawner idiom). Construction sites
switched: `ShadowLord.spawnShadow` and `AzuterronNPC.Quest.process` to the
factory (new constants `SHADOW`, `DREAD_KNIGHT`, `TREACHEROUS_SPIRIT`);
Badges `instanceof DreadKnight` → kind check. Verified live: legacy-save
fixture restores all six with exact hp/ht/str parity, zombie poison proc in
the log, both knights melee, TS summon observed (3/8 lives), zero lua errors.

Batch 5 lessons:
- **`attackProc` can fire with a null `enemy`** — java knights guarded
  `if (enemy != null)`; dropping the guard in lua produced
  `DeathStroke.hit(null)` NPEs caught live. All attackProc scripts now guard
  `enemy ~= nil` (Zombie/DeathKnight/DreadKnight/TreacherousSpirit).
- DreadKnight's 1/10 Stun proc is a **no-op in java too** (`Buff.affect`
  2-arg = no duration → FlavourBuff decays immediately); ported as-is.
- Shadow's java `speed()` override returned a constant 2, ignoring buff
  multipliers; json `baseSpeed:2` lets Slow actually slow it (minor delta,
  arguably a fix). Free-roaming Shadows don't exist in java either (only
  Shadow Lord blinks them in), so no wander behavior to preserve.
- `Mob.loot(Object,float)` rolls chance AND picks the item at construction
  (`collect`), not at death — json loot + `lootChance` matches ctor loot
  semantics for free; only Treasury *category* loot has no json form.
- Dev-overlay trap: `rundir/mods/Remixed` held per-file symlinks, so new
  `mobsDesc/*.json` were invisible to the game → legacy saves hit the
  Class.forName fallback and mobs silently skipped. `mobsDesc` and
  `scripts/mobs` in the overlay are directory symlinks now.

Deferred from batch 5: `KoboldIcemancer` — its java `zap()` deals **no
damage** (Slow-only controller); the `zapProc` hook can't suppress the base
zap damage (returning 0 still runs `defenseProc`/`damage(0)`), so it needs
the ranged-tier zap story (Warlock/Shaman/Eye batch). `ZombieGnoll` —
`resurrect()` is not `@LuaInterface` (gate blocks `self:resurrect()`); a
one-line annotation unblocks it.

Sixth batch 2026-09-13 (Step C shipped — quest-die hoist + sewer/dwarftown
tier): engine change first — `Mob.die` now opens with
`processQuestKills(cause)`, a kind-gated switch calling the quest processors
at the exact point the old overrides did (before the die body):
Rat → Scarecrow+Ghost+PlagueDoctor, Gnoll → Scarecrow+Ghost, Crab → Ghost,
Golem/Monk → Imp. **The kind gate is load-bearing**: Scarecrow counts every
processed kill (25 → done), so the switch must not fall through to arbitrary
kinds. `Imp.Quest.process` inner checks became
`getEntityKind().equals(MobFactory.MONK/GOLEM)`; `Monk` (stays java) lost its
`die()` override — the hoist covers it. `@LuaInterface` added to
`Mob.resurrect()` (the ZombieGnoll unblocker). New MobFactory constants
GNOLL/CRAB/GOLEM/MONK. Deleted java: Rat, Gnoll, Crab, Golem, ZombieGnoll
(new json defs + `spritesDesc/*.json` already existed); one straggler
converted: `RatKingCrown` `instanceof Rat` → kind check (makePet on rats).

Lua: `scripts/mobs/Rat.lua` (replaced an empty stub) and `Albino.lua` —
`act` policy: enemy has the ratter aura → `RPD.setAi(self,"Fleeing")` +
`RPD.affectBuff(self,"Terror",10)` once (java Terror.DURATION). The aura
buff kind is **`artifactBuffRatterAura`**, not the class simple name —
caught live: with the wrong string the rat kept hunting; `char_status`
(now reports a `buffs` array, and accepts `id=hero`) showed the hero's
actual buff kind. `scripts/mobs/ZombieGnoll.lua` — `die` hook:
35 % (`math.random(100) > 65`) `self:resurrect()` unless
`cause:getEntityKind() == "Burning"`, plus bone-speck emitter,
`snd_death`, Goo_StaInfo1 status, ZombieGnoll_Info log. Resurrect-in-hook
is safe: the hook runs inside `Char.die` before `destroy()`, the new mob's
`occupyCell` overwrites the dying one in the cell map, and the dying mob's
later `freeCell` is a two-arg remove that no-ops against the new occupant.

Seventh batch 2026-09-13 (caves/sewers tier + thieves): **`Scorpio`**/**`Acidic`**
(json + `attackRange:10` + lua `spawn` hook viewDistance=level+1 + `zapProc`
1/2 Cripple + `act` kite: HUNTING+no clean ranged line → FLEEING, inverse
back; the kite calls the newly exposed `CharUtils.canDoOnlyRangedAttack`, the
java `canAttack`/`getCloser→getFurther` inversion expressed as a state pair),
**`Acidic`** adds `defenceProc` acid reflect (`Random.IntRange(0,damage)` is
inclusive → `math.random(0,dmg)`), **`Spinner`** (json + `attackProc` 1/2
Poison 7-8×durationFactor → setAi Fleeing + `act` flips back when the victim
is no longer poisoned + `move` hook seeds a Web blob at the cell being left —
the hook runs before `placeTo`, so `getPos()` is still the old cell;
`RPD.placeBlob(RPD.Blobs.Web, pos, 5-6)`), **`Thief`**/**`Bandit`** (json
`attackDelay:0.5` + newly exposed `CharUtils.steal` → setAi "ThiefFleeing"
(the tag registry covers it) + defenceProc drops Gold when struck mid-flight;
Bandit adds 1/2-prolonged Blindness 5-11 + `enemy:observe()`; the "carries X"
description suffix is NOT portable — accepted cosmetic delta), **`Wraith`**
(json ht 1, flying, no body parts, immunities Death+Terror, carcass 0 +
lua `stats` re-deriving dmgMax/atk/def from `RPD.Dungeon.depth` — spawn depth
is the only depth it lives on, so restore-time re-derivation is faithful;
depth-1 values sit in the json as defaults). Wraith spawn helpers moved to
`CharUtils.spawnWraithAt/spawnWraithsAround` (Heap tomb/skeleton-cursed +
ShadowLord summon; state Hunting, 2f delay, alpha tween + curse particles
preserved). `Scorpio`'s ctor potion/meat roll lives in `stats` persisted via
`mob.restoreData(javaChar)` — the per-mob lua data table (knownMobs roundtrip
in mob.lua; callbacks receive the JAVA char as first arg, not the script
table). `Item.doDrop`, `Char.getState/observe/setViewDistance` (lombok @Setter
replaced by an explicit annotated setter), `MobAi.getTag`,
`Level.getViewDistance` newly `@LuaInterface`; Mob gained annotated
`setDmgMax/getDmgMin/getDmgMax`, Char `set/getBaseAttackSkill/set/getBaseDefenseSkill`.
`/debug/char_status` now also reports `atk`/`def` (raw bases), `dmgMin`/`dmgMax`,
`state` tag. Badges Bandit/Acidic rare-checks and AlchemyRecipes "Scorpio"
contain() switched to the new MobFactory constants. Shipped lua fix: Zombie's
poison was ported as `math.random(2,3)` but java `Random.Int(2,3)` is `[2,3)`
i.e. always 2 — now a constant 2.

Verified live: fixture restores all six with exact hp/ht/str parity
(95/17, 95/17, 50/10, 20/11, 20/11, 1/13) and raw bases
(atk/def/dmg 36/24/20-32 ×2, 20/14/12-16, 12/12/1-7 ×2, 10/50/1-3);
Wraith depth-3 fresh spawn = atk 13 def 65 dmg 1-6 exactly as java's
adjustStats(3); thief steal → THIEFFLEEING observed in logs
("украл предмет золото у тебя"), bandit steal + "** Тебя ослепили!"
(Blindness) + observe; scorpio/acidic `act` kite observed
(HUNTING↔FLEEING flips, FLEEING engaged on no-LOS); acidic zap + reflect
killed a pet golem (85→11 hp in one round); scorpio ctor roll persists
(`luaData {rolled=true}`), re-saved level stores 17 CustomMob kinds with
zero old FQNs and the rolled meat in belongings. CI python suite green:
blood_transfusion, level_navigation 9/9, doctor_spells 7/7, all_spells 38/38,
alchemy 42/42. Accepted deltas: Thief/Bandit lose the "carries X" description
suffix (no hook); Wraith `reset()` (→Wandering) not expressible — a wraith
that loses the hero walks back to its spawn cell instead of resetting in
place; scorpio/acidic old saves with an EMPTY backpack may gain the ctor roll
item once on first data-defined load (item-carrying ones are detected via the
backpack guard).

Deferred from batch 7: `KoboldIcemancer`/`Warlock`/`Shaman`/`Eye` (ranged-tier
zap story: no-damage zaps), `FireElemental`/`WaterElemental`/`EarthElemental`
(buff-add() hook), `Crystal` (wand arsenal), `Piranha` (Statistics/Badges
counters). Swarm/Skeleton/Succubus shipped as the eighth batch below.

Eighth batch 2026-09-13 (split, treasury loot, blink — the "needs a design"
trio): **`Swarm`** (json + lua `defenceProc` split: `self:split(cell,dmg)`
(newly `@LuaInterface`) + `clone:loot(item,chance)` + `setCarcassChance`,
all per-generation scaled; `generation` lives in `self.data` and reaches the
clone for free — `Mob.makeClone` round-trips the mob through a Bundle, and
`LUA_DATA` is part of that bundle, so no wiring was needed; `stats` re-derives
the scaled carcass chance on every restore from the persisted generation),
**`Skeleton`** (treasury category loot rolled in the `stats` hook following
the Scorpio pattern — `RPD.Treasury:getLevelTreasury():worstOf("WEAPON",3)`
via a new string-overload `Treasury.worstOf(String,int)` @LuaInterface;
the roll persists via `data.rolled` with the empty-backpack legacy guard;
the java loot() hero-level gate is re-checked in lua; the NecroBossLevel
exception ports as `RPD.Dungeon.level:levelKind() == "NecroBossLevel"`;
`die` hook does the bone-blast AoE — `RPD.Actor:findChar`, `self:damageRoll()`,
`ch:defenceRoll(self)`, `ch:damage(dmg,self)` — plus `RPD.playSound("snd_bones")`
and `RPD.glogn(RPD.textById("Skeleton_Killed"))`; the java `Dungeon.fail` in
die was redundant — `Char implements Doom`, so the hero-death path already
composes the identical fail message), **`Succubus`** (json + `spawn` hook
viewDistance+1 + `attackProc` 1/3 Charm via `RPD.Buffs.Charm:durationFactor`
+ `RPD.affectBuff`; the OneWayLoveBuff self-charm redirect ports as
`enemy:hasBuff("OneWayLoveBuff")` — the cursed/rose duration factors are
handled inside durationFactor; the blink lives in a NEW script hook
`getCloser` → engine `Mob.getCloser` now calls
`getScript().runOptional("onGetCloser", false, target, ignorePets)` first —
lua returns true when it took the step: `RPD.CharUtils:blinkTo` +
`data.delay = 5` + `self:spend(-1 / self:speed())` refunding `doStepTo`'s
pre-charge (the Tengu refund idiom); luaj arrays are 1-BASED so the FOV check
is `level.fieldOfView[target + 1]`). Construction sites switched:
`Lich.useSkull` blue-skull summon → `MobFactory.mobByName(MOBFactory.SKELETON)`.
Newly `@LuaInterface`: `Mob.split/loot/setCarcassChance/damageRoll/getMaxLvl`,
`Char.defenceRoll/speed/spend`. Verified live: exact stat parity
(80/10/12/5/1-4, 25/11/12/9/3-8 — skeleton correctly loses the Regeneration
buff via json `undead`, 80/13/40/25/15-25); pet-driven melee split produced
3 clones with `generation=1.0` in their `luaData`, surviving
reload → re-save; skeleton rolled a weapon and used it in AI combat
("скелет использует кинжал"); skeleton death AoE dropped an adjacent rat
8→3 hp; succubus blinked across the level (position jumps, `delay=4.0`
persisted) and charmed the hero ("Ты зачарован!"); CI python suite green:
blood_transfusion 6/6, level_navigation, doctor_spells 7/7, all_spells 38/38,
alchemy 42/42. Accepted deltas: legacy (pre-migration) saves holding a SPLIT
swarm restore with carcassChance 0.2 instead of the generation-scaled value
(the java `generation` @Packable field has no json counterpart — the clones'
own future splits still scale correctly from gen 0); skeleton kills recorded
through the generic Doom path only (message identical).

Ninth batch 2026-09-13 (ranged tier + the zap hook): engine change —
`Mob.zap` now opens with a script hook
`getScript().runOptional("onZap", false, enemy)`: lua returning true has
taken the zap entirely (hit roll, effects, death report), false falls through
to the base damage zap. `Mob.zapHit` went protected→public `@LuaInterface`
(it shows the miss status itself, so a full-replacement zap needs no extra
miss handling). `CharUtils.blinkAwayFrom(chr, enemy, dist)` added so lua can
reproduce `blinkAway(chr, new BlinkAwayFromChar(...))` without a java lambda.
Bridge rule re-learned the hard way: engine hooks and script callbacks must
NOT share a name — `invokemethod` prepends the script table, and a desc key
named like the hook (onZap) shadows the mob.lua bridge, shifting every arg by
one (self became the script table). Callbacks stay unprefixed (`zap`,
`getCloser`); the bridge `mob.onZap` lives in mob.lua like `onGetCloser`.
Also: call statics taking a Char arg with COLON syntax
(`RPD.Buffs.Weakness:duration(enemy)` — dot-call feeds ch=null through
luaj's instance slot); probe-verified colon returns the correct 40.
Migrated: **`Warlock`** (json + lua `zap` = base damage chain in lua
(zapHit → defenseProc/damage via public Char.defenseProc) + 1/2 Weakness +
checkDeathReport, `defenceProc` blink-away thresholds via
`RPD.CharUtils:blinkAwayFrom` with damage/2 (`math.floor` — java int div),
treasury POTION 0.83 roll in `stats`),
**`Shaman`** (lua `zap` = `RPD.CharUtils:lightningProc(self, pos,
damageRoll()*2)` + report, 10% zapMiss yell via `self:yell("Shaman_ZapMiss")`,
`defenceProc` two-stage flee (data.fleeState 1 at 2/3, 2 at 1/3, damage/2),
`act` resumes Hunting when >2 cells from the enemy — the java getFurther
override; SCROLL 0.33 roll),
**`KoboldIcemancer`** (lua `zap` = NO damage at all — zapHit → 1/2
`Slow(1)` + report; the whole point of the hook; POTION 0.83 roll).
All three: `attackRange:8` (java canAttack had no distance cap but is FOV
gated at viewDistance 8 — effective parity), `resistances` per java, l10n
keys pre-existed, spritesDesc jsons pre-existed. `RingOfElements` FULL-list
`Warlock.class.getSimpleName()` → `MobFactory.WARLOCK` constant.
The treasury category roll (POTION/SCROLL) uses
`RPD.Treasury:getLevelTreasury():random("POTION")` with the loot() hero-level
gate re-checked in lua and the Scorpio rolled/backpack-guard pattern.
Verified live: exact stat parity (70/15/25/18/12-20, 18/11/11/8/2-6,
70/12/25/18/15-17); icemancer zap → Slow on a pet golem with hp UNCHANGED
(no-damage ✓); warlock zap → 16 damage + Weakness buff; shaman zap → two
lightning bolts totalling 18; zero script errors after the bridge fix; CI
python suite green (6/6, nav, 7/7, 38/38, 42/42). Deferred from this batch:
**Eye** — its canAttack (enemy on an aim-through ballistica trace + pierce
beam over ALL traced chars) needs either a canAttack hook (dropped by design)
or attackRange approximations that break the pierce; stays java with the
bosses. Warlock's vestigial fx() (MagicMissile) was dead code — the zap
visual comes from the sprite anim.

Tenth batch 2026-09-13 (spider tier): **`SpiderServant`** (json + lua
`attackProc` 1/4 Poison 2×durationFactor), **`SpiderGuard`** (json `var:1` +
lua `attackProc` 1/10 Stun 3), **`SpiderMind`** (json + lua `zapProc` ally
buff: iterate `level:getCopyOfMobsArray()` — a JAVA ARRAY, luaj-indexed
1-based with `#mobs`/`mobs[i]`, NOT a List (no `:size()`); FOV check
`level.fieldOfView[pos + 1]`; `RPD.Dungeon:isCellVisible(pos)` COLON (static
taking an int arg — dot-call throws "userdata expected, got number");
buffs by kind string incl. nested `Health`/`Armor` (BuffFactory registers
nested classes under simple names) — returns 0 = no zap damage; `getCloser`
hook flee-while-hunting via the new `@LuaInterface Mob.getFurther` +
`self.enemySeen`), **`SpiderMindAmber`** (same kite + `zapProc` random
Blindness/Slow/Weakness 3 turns on TOP of base damage — returns dmg).
Java deleted: the four spider classes; `Badges` instanceof → new constants
`MobFactory.SPIDER_GUARD/SPIDER_MIND_AMBER`; `SpiderCharm` summon →
`MobFactory.mobByName(SPIDER_SERVANT)` (constant added). CI python suite
green on the batch jar (6/6, nav, 7/7, 38/38, 42/42). Verification caveat:
SpiderMind's ally-buff zap was verified component-wise (array iteration,
FOV indexing, colon statics, friendly check, zero lua errors across many
provoked zaps) but never observed end-to-end — its attack skill 10 vs any
staged ally/enemy defense kept missing (zapProc fires only on hits), and
town staging geometry kept blocking the ray. Next session: provoke one with
a dummy ally in FOV on a clear line (Statue def 4 as the zap TARGET works;
the ALLY just needs to stand in FOV off-axis).

Eleventh batch 2026-09-13 (elementals + piranha, batch 9b): engine surface first —
`Char.add` dispatches `runOptional("onAddBuff", false, buff)` before the generic
Burning tick (true = script handled the buff, base attach skipped; bridge
`mob.onAddBuff`, script key `addBuff`); `CustomMob.speed()` override dispatches
`run("onSpeed", base)` (bridge `mob.onSpeed`, script key `speed` — water ×2/×0.5,
earth ×0.5 on liquid via `RPD.TerrainFlags:is(level.map[pos+1], LIQUID)`);
`Spell.cast(Char,int)` protected→public `@LuaInterface` (all overrides were
already public); new annotations: `setSkillLevel`, `ht(int)`, `hp(int)`,
`STR(int)`, `Level.distance`, `Mob.setDmgMin/setDr/setExpForKill/getExpForKill/
setMaxLvl`. Sandbox lesson: **bindClass access is gated on the CLASS being
`@LuaInterface`-annotated** — TerrainFlags/Statistics/Random needed class-level
annotations (warn-only failures had silently zeroed the piranha counter until
annotated). commonClasses now binds Statistics/Random/TerrainFlags/Burning.
MobFactory: constants FIRE/AIR/WATER/EARTH_ELEMENTAL + PIRANHA; the legacy-mod
kind `Elemental` (was mMobsList→FireElemental.class) became `resolveAlias`
consulted by hasMob+mobByName. PoolPainter piranhas via `MobFactory.mobByName`.
Deleted java: FireElemental, AirElemental, WaterElemental, EarthElemental,
Piranha; l10n keys and spritesDesc jsons pre-existed.

Migrated: **`FireElemental`** (pure fixed-stat json + `attackProc` 1/2
`Buff:affect(enemy,"Burning",Burning:duration(enemy))` (≡ java
affect+reignite) + `addBuff`: Burning→heal(1..ht*4)+skip, Frost→damage(1..ht*2/3)+skip),
**`WaterElemental`** (Wraith-pattern: json depth-1 defaults + `stats` re-derives
from `RPD.Dungeon.depth`; speed hook; `act` heal-on-water; `attackProc` 1/2
Frost with real duration; `addBuff`: Frost→heal(exp)+skip, Burning→damage
1..ht/3+still-attach), **`EarthElemental`** (depth re-derive + liquid-speed +
`attackProc` 1/2 `RPD.placeBlob(RPD.Blobs.Regrowth, cell, max(exp,10)*15)` on
open terrain), **`AirElemental`** (depth re-derive + `setSkillLevel(3+lvl/10)`,
json `attackRange:3`, Scorpio-style distance-band kite in `act` (Hunting→Fleeing
at dist<2, back at ≥2), `zapProc` → `RPD.SpellFactory:getSpellByName("WindGust"):
cast(self, enemyPos)` — WindGust's cast was registered in SpellFactory all
along), **`Piranha`** (depth re-derive, `walkingType:"WATER"`, RawFish carry via
the Scorpio stats-guard + `self:collect`, `act` self-die on dry land,
`die` hook: `Statistics.piranhasKilled++` + `Badges:validatePiranhasKilled()`;
luaj static field write works once the class is annotated).

Discoveries: (1) **the 2-arg `Buff:affect(kindString)` attaches with left=0 →
FlavourBuff expires instantly** — ColdSpirit's batch-2 Frost proc was a latent
no-op; fixed to `RPD.affectBuff(enemy,"Frost",Frost:duration(enemy))`.
DreadKnight Stun and Worm Roots are 2-arg in the ORIGINAL java too (faithful
no-ops, left alone); Albino Bleeding works (DotBuff own timer). (2)
**Water/Fire heal-from-immune-buff is zeroed by `resist()`** (heal src = the
Frost/Burning buff the mob is immune to) — the java add()-heals were always
no-ops; ported as-is. (3) **Frost detaches on ANY damage**
(`Frost.charGotDamage→detach`) — an on-hit Frost proc is shattered by the same
swing's damage in java and lua alike; verified attach-then-shatter via probes.
(4) MultiKindMob kind variants (min(depth/5,4)) were visually inert in java
(selectKind ignores kind) — dropped. (5) New debug endpoints (permanent):
`/debug/make_pet`, `/debug/force_attack` (real `Char.attack` chain; melee path —
ranged zaps need the AI), `/debug/affect_buff` (real `Buff.affect(Char,String,
float)`); `/debug/go_to_level` sets a real Dungeon.depth unlike descend_to by
id. (6) root `scripts/` and `assets/scripts/` are hardlink twins — commit both.

Verified live: stat parity exact at depth 0 and depth 5 for all five
(fire 65/25/20/16-20; air d5 16/23/11/0-4; water d5 26/6/11/13-13; earth d5
51/2/3/10-10; piranha d5 35/30/20/5-14, speed 2); water speed 0.5 on land,
earth 1.0 (only slows in liquid); water Frost-absorb skips attach (full + hurt),
water Burning hurts AND attaches ✓, fire Frost 65→39 no attach ✓, fire
Burning-heal observed via blob-Ignite (+1, regen-capped) with no Burning
attach ✓; piranha land-death instant, counter/badge hook clean ×6; air kite
band demonstrated at both edges (fled when staged inside dist<2, approached
then held at dist 2). CI python suite green on the batch jar: blood 6/6, nav,
doctor 7/7, spells 38/38, alchemy 42/42. Accepted deltas / leftovers: air
gust-push not observed end-to-end (SpellFactory lua cast component-proven);
water in-water heal needs a water tile; piranha `reset()→true` (never returns
to spawn) not expressible — it now swims home; air skillLevel frozen at
authored 3 (java 3+lvl/10); level 1 json defaults equal depth-1 formulas
exactly. Fire Burning proc observed killing golems after the DeathStroke
colon fix (below).

"As intended" pass (same day, Mike's call on the no-ops): DreadKnight 1/10
Stun and Worm 1/7 Roots now carry real durations (`RPD.affectBuff(enemy,
"Stun", Stun:duration(enemy))` — the standard-convention 10×factor; Roots
3 turns per the WandMaker precedent) — both verified live (golem stunned;
RottingFist rooted at hit 4). Water/Fire elemental absorb-heals made real:
the absorbed kind was REMOVED from the json immunities — an immune kind is
pre-blocked at `Buff.attachTo` and never reaches `addBuff`, and `resist()`
would zero a heal sourced by that very buff — the `addBuff` branch now heals
with `src = self` and rejects the attach, so the mob stays frost/burn-proof
in effect AND heals. Water heal-on-Frost observed (11→17 = exactly exp@5),
fire Burning-heal observed (40→65 cap, no attach). Water/ColdSpirit hit
procs restored to the full `RPD.PseudoBlobs.Freezing:affect(enemyPos)`
(Freezing was already @LuaInterface and bound as PseudoBlobs.Freezing):
duration-carrying Frost + fire put out + HEAP FREEZE at the victim cell —
live-verified: MysteryMeat heap → FrozenCarpaccio, potion shattered
(Heap.freeze: potions shatter, mimics wake chilled). The Frost itself still
shatters to the same hit's damage (Frost.charGotDamage) — accepted.
Bonus latent bugs found by the proc hunt: `RPD.Sfx.DeathStroke.hit(enemy)`
(dot-call on a static → ch=null NPE) had been silently aborting BOTH
knights' attackProcs since batch 5 — colon-call fixed, double damage
observed; `Stun` was missing from the commonClasses Buffs table
(RPD.Buffs.Stun was nil). get_items now reports item kinds (the bundle
serialization lacks identity).

Verified live (batch 6): exact stat parity on all five (hp 8/12/15/85/210,
str 10/10/10/16/13, Crab speed 2); 4 resurrect rises in 8 non-burning
kills, 0 in 6 burning kills; resurrected zombie persists through
save/reload; ratter aura flees both Rat and Albino (Terror observed,
batch-3 parity gap — old java Albino inherited canAttack from Rat —
restored); every switch branch exercised by real `die()` calls;
save round-trip stores `entityKind`+`CustomMob` and restores exact
hp/str/state (even HORRIFIED). Full CI python suite green on the new
headless jar: blood_transfusion 6/6, doctor_spells 7/7 (drives the
plague-doctor hide quest through the hoist), level_navigation 9/9,
all_spells 38/38, alchemy 42/42. Debug tooling: `char_status` gained
`buffs` + hero support.

## Step C — engine work that unblocks the rest

(SHIPPED 2026-09-13 — see the sixth batch above. Kept for rationale.)

**Quest statics in `die()` (the only real blocker left).** Rat/Gnoll/Crab
call `Ghost/ScarecrowNPC/PlagueDoctorNPC Quest.process(pos)`, Golem/Monk
call `Imp.Quest.process(mob)`. The lua `quest` library is a separate
storage-based mod-quest system, not wired to these java processors. Fix:
hoist the processors out of per-mob `die()` into the central death path,
kind-gated at the call site (they self-gate on quest state already);
data-defined mobs then get quest behavior for free through
`mob.lua onDie → quest.mobDied`.

**`canAttack`/`attack` hooks are NOT needed** (decided 2026-09-11 after
reading the dispatch): `Char.act()` calls `getScript().runOptional("onAct")`
and discards the result — lua `act` is a pre-AI policy hook, not a turn
replacement. Recipes that make explicit hooks unnecessary:
- plain ranged: `attackRange` json key already drives
  `CustomMob.canAttack` (distance + ballistica) — covers Warlock, Shaman,
  Tengu base attack, Eye; extras go into `zapProc`/`defenceProc`.
- side-effect gates: `act` sets the AI state, java act executes — Rat's
  ratter-aura flee becomes "enemy has RATTER_AURA → set Fleeing + Terror".
- kiting (Scorpio): `act` sets Fleeing/Hunting by distance.
- bosses (Goo pump, King pedestals, Tengu jumps) stay java longest;
  a state-driven `attackRange` write from lua may or may not work
  (untested luaj field write) — revisit if/when they migrate.
Invariant: never spend time inside the lua `act` hook — java act owns the
clock and the debug double-spend check flags violations.

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

## Debug harness notes (desktop, 2026-09-11)

- Launch from a scratch rundir (saves stay out of the repo; saves land in
  `./saves` relative to cwd):

  ```
  cd rundir && java --add-opens java.base/java.util=ALL-UNNAMED \
    -cp "<repo>/RemixedDungeon/src/main/assets:$(cat /tmp/rpd-desktop-cp.txt)" \
    com.nyrds.pixeldungeon.desktop.DesktopLauncher --webserver --windowed --nosound
  ```

  (`/tmp/rpd-desktop-cp.txt` = runtime classpath saved from a gradle run;
  the assets dir goes on the cp so gdx internal lookups find fonts.)
- `--webserver` flag is REQUIRED for the :8080 debug API.
- Overlay for mod-file lookups: symlink game assets + desktop `d_assets/`
  + `l10ns/` + repo-root `scripts/` into `<rundir>/mods/Remixed/` — dev
  builds resolve user data relative to the CWD (snap/appimage builds use
  ~/.local/share). The repo's `data/mods/Remixed` and `mods/Remixed`
  symlinks are TRACKED and dangling — never repurpose them.
- `preferences.hjson` `fps_limit` is an INDEX into {30,60,120}, not a value.
- Useful endpoints: `/debug/start_game`, `/debug/create_mob?type=X&x=&y=`,
  `/debug/wait_ticks?ticks=n` (runs real hero turns), `/debug/get_mobs`,
  `/debug/get_hero_info`, `/debug/get_recent_logs`, `/debug/change_level?level=n`,
  `/debug/char_status?id=`. verify batch:
  stats parity via get_mobs (HP/HT/baseStr), attack behavior via
  wait_ticks + hero HP/buffs, kamikaze via get_recent_logs + mob removal.

## Lua-side interface (short)

`scripts/lib/mob.lua` bridges `mob.init{...}` callbacks to `Char` dispatch
points. Only `self.data` survives saving (serpent → `LUA_DATA`);
`stats` runs on construction AND every restore — keep it idempotent,
persist randoms into `self.data`.

| Callback | Dispatch / notes |
|---|---|
| `stats(mob)` | ctor + every restore (after bundle stats) |
| `act(mob) → bool` | pre-AI policy: return value discarded, java AI still runs; never spend time here |
| `attackProc(mob, enemy, dmg) → dmg` | fires only on a hit |
| `defenceProc(mob, enemy, dmg) → dmg` | |
| `damage(mob, dmg, src)` | after taking damage |
| `die(mob, cause) → bool` | then `quest.mobDied` + onDie callbacks run |
| `move(mob, cell) → bool` | |
| `spawn(mob, level)` | on level entry |
| `interact(mob, chr)` | return not false = handled |
| `zapProc(mob, enemy, dmg) → dmg` / `zapMiss(mob, enemy)` | ranged attacks |
| `actions(mob, hero)` / `execute(mob, hero, action)` / `selectCell(mob)` | custom actions |
| `priceForSell/priceForBuy(mob, item)`, `buyMode/sellMode` | shopkeepers |

Engine-script idioms in use (`scripts/mobs/`): `RPD.affectBuff(chr,"Kind",dur)`
· `RPD.permanentBuff(chr, RPD.Buffs.X)` (permanent buffs in `stats`)
· `RPD.removeBuff` · `RPD.setAi(me,"Hunting"/"Fleeing"/"Wandering")` —
the act-policy kite (ShamanElder.lua) · `RPD.item("Gold",n)` +
`RPD.Dungeon.level:drop(item,pos)` (Mimic) · `self:loot(item)` theft
(ScriptedThief) · `self:immunities():add(...)` (NatureAura) ·
`RPD.MobFactory:mobByName(kind)` + `level:spawnMob(mob)` (BeeSpawner) ·
hero check: `enemy:getEntityKind() == "Hero"`. Item/Level methods are
callable from engine scripts (the `@LuaInterface` gate binds mod scripts).

Twelfth batch 2026-09-14 (spawner/summoner tier + guts): engine surface first —
`Actor`-time spawners port as act-hook tick counters (java `postpone(n)`
delays the mob's own turn; the lua `act` hook cannot spend, so
`data.ticks` counting acts reproduces the same wall clock — Sleeping-state
acts come slower than 1/tick, so effective delays stretch ~1.5x). New
json key `"pacified"` on CustomMob (fillMobStats optBoolean; gates
canAttack like the java field — SuspiciousRat/JarOfSouls need it). New
`@LuaInterface`: `Char.playAttack(int)`, `Mob.remove()` (the quiet
self-removal: Char.die without Mob.die drops/carcass), `CharSprite.zap(int)`,
`Plant` class + both `effect` overloads, `MobSpawner` class +
`spawnRandomMob` (bound as `RPD.MobSpawner`; statics via COLON call).
`LevelObjectsFactory` was bound in commonClasses but never EXPORTED in the
RPD table — added (`RPD.LevelObjectsFactory`; a sandboxed script got nil
+ "attempt to index nil with key 'objectByName'"). commonClasses also
exports `MobSpawner` now. Construction sites switched: `Ghost.Quest.process`
FetidRat → factory (new constant FETID_RAT; inner class deleted), 
`SpiderSpawner.spawnEgg/spawnNest` → factory (SPIDER_EGG, SPIDER_NEST),
`MobSpawner.spawnJarOfSouls` → factory (JAR_OF_SOULS). YogsBrain→Nightmare
and SpiderQueen→SpiderEgg already spawned by string kind — no change needed.
Deleted java: Nightmare, SuspiciousRat, SpiderEgg, SpiderNest,
SpiderExploding, JarOfSouls, Ghost$FetidRat.

Migrated: **`Nightmare`** (json + lua attackProc 1/10 Roots 3 / 1/10
`Stun:duration` — real durations per the as-intended pass; `act` forces
Hunting every tick), **`SuspiciousRat`** (pacified json + `act`:
first enemySeen tick shows the twitch status (Goo_StaInfo1) + sprite zap,
4 enemy-seen acts later spawns PSEUDO_RAT at own cell + snd_cursed +
self-die; java spent 4 ticks at once — counter is same wall clock),
**`FetidRat`** (json RatSkull loot 1.0 + Paralysis immunity + Wandering;
defenceProc re-seeds ParalyticGas 20 under itself — the 20-volume gas is
as weak as java's), **`SpiderEgg`** (movable:false + Sleeping; act
counter 20 → `RPD.MobSpawner:spawnRandomMob(level,pos,25)`, self:remove()
on success — remove = quiet vanish, NO loot drop, unlike die; 20%
treasury SEED roll in stats per the Skeleton pattern), **`SpiderNest`**
(same counter 20, limit 20, never removes; PotionOfHealing loot via json),
**`SpiderExploding`** (kamikaze: attackProc rolls its plant INTO data in
stats (java rolled a MultiKindMob kind per instance; kind is visually
inert per batch 11), `LevelObjectsFactory:objectByName(plant):effect(
enemyPos, enemy, self)` + self:die — Earthroot Armor buff observed on a
zapped hero), **`JarOfSouls`** (undead + pacified + hasBodyParts:false +
movable:false; act: while enemySeen, every 15th act playAttack +
limitless `spawnRandomMob` (limit -1) — java postponed 15, same pacing).

Batch 12 lessons:
- **Sleeping mobs wake only probabilistically** — Sleeping.act
  chooseEnemy rolls `Random.Int((dist + stealth)/attention)==0`
  (attention 0.5 base) and needs the shared `level.fieldOfView` to see
  the hero; wake-by-damage (seekRevenge) is the reliable staged path.
- **`level.fieldOfView` is ONE shared array, overwritten by every
  acting char** — a script reading it is only correct during its own
  mob's act (AI-driven zap), never from an out-of-turn call.
- **`test_damage` bypasses defenceProc** (raw `Char.damage`) — defenceProc
  hooks need a real attack (`force_attack`/`force_zap`) to fire.
- Spot a script that never attached vs one whose hook never fired via
  `luaData` writes: no stats-time writes = indistinguishable.

New permanent debug endpoints: `/debug/mob_brain?id=` (state, enemy
id/kind/pos, enemySeen, canAttack, ballistica trace with per-cell
passable/losBlock/char), `/debug/force_zap?attacker=&target=` (drives the
real `Mob.zap` chain — onZap hook, zapHit, zapProc — without AI
scheduling; note the FOV array is stale in that context).

Leftovers closed this session (batches 10-11 tails):
- **SpiderMind ally-buff zap observed end-to-end**: AI-driven zap at a
  pet Statue (MindVision on the SM to force the wake; clean 2-cell line
  on the sewer floor) → zapProc prolonged Blessed/Armor on the friendly
  rat in FOV. The FOV check reads the SM's own array during its act —
  correct as shipped.
- **Air gust push observed**: hero pushed one cell away from an
  AI-zapping AirElemental (no other mover). WindGust by design pushes
  only chars BETWEEN caster and aim cell, never the aim cell itself —
  the original java zapProc cast identically, so the port is faithful.
- **Water heal observed**: damaged pet WaterElemental on a water tile
  healed +1/turn (exp-scaled, regen-capped).

Verification (batch 12): stat parity exact on all seven (80/17/26/24,
140/17/25/25, 15/10/12/5, 2/10/1/1, 10/10/1/2, 5/11/125/1, 70/17/1/5 —
hp/str/atk/def, dmg ranges + states matching the java ctors incl.
Wandering FetidRat + JarOfSouls and Sleeping eggs/nest/rat); egg hatched
a bestiary rat and removed itself, nest spawned and stayed, JarOfSouls
spawned on its 15-tick gate, SuspiciousRat twitched then became a
PseudoRat (320 ht), SpiderExploding burst on its hero hit leaving the
Earthroot Armor buff, Nightmare landed Roots on a pet golem, FetidRat
defenceProc probe fired; save round-trip preserved damaged hp, positions,
states and LUA_DATA (ticks counters, plant roll) with zero `skip:` lines.
CI python suite green on the new headless jar: blood 6/6, navigation,
doctor 7/7, all_spells 38/38, alchemy 42/42.

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
