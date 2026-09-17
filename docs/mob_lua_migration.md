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

## Coverage audit (2026-09-10; counts refreshed 2026-09-15 after batches 12-14)

Transitive closure over all `Mob`/`Item` descendants in
`RemixedDungeon/src/main/java`, checked against factory registrations and
data-def scans (`mobsDesc/*.json`, `scripts/items/*.lua`):

- Mobs: every concrete descendant is reachable by kind. 93 kinds are
  data-defined (`mobsDesc/*.json`, the migration target), 45 + 3 manual
  entries remain java-registered (bosses, NPCs, engine mobs — see the
  batch-15 survey for their override surfaces). Statue, ArmoredStatue,
  GoldenStatue, the batch-12 kinds and the batch-15 Yog/IceGuardian kinds
  have NO java class anymore; their old-save FQNs resolve through the
  FQN-tail route to CustomMob.
  `CustomMob`/`MultiKindMob` are base classes; the nested
  `WandOfFlock$Sheep` is registered manually as `Sheep`.
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
| `desc(mob) → string\|nil` | Char.getDescription script hook; nil/empty = json classDesc answers |
| `destroy(mob)` | from Char.destroy (journal cleanup etc.), no return |
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

Thirteenth+fourteenth batch 2026-09-15 (statues complete — the sprite
delta; Mike scoped the first push to the base statue, the two variants
followed same day once the pattern proved out): **`Statue`,
`ArmoredStatue`, `GoldenStatue`** — all three kinds data, all three java
classes DELETED. Kind went data by dropping the `registerMobClass` lines
(kinds "Statue"/"ArmoredStatue"/"GoldenStatue" resolve to CustomMob, old
saves restore via the FQN-tail route like every other batch).

Engine surface (all reusable, statue-motivated):
- json `heroSprite: true` on CustomMob → `newSprite()` builds
  `HeroSpriteDef.createHeroSpriteDef(item)` — the statue look is
  hero-layers (`hero_modern/body|head/statue.png`) carrying the equipped
  weapon (or armor, weapon slot checked first). No `spriteDesc` needed;
  the legacy `spritesDesc/Statue.json` remains as super's fallback.
- json `persistOnReset: true` → CustomMob.reset returns true (java
  Statue.reset survived Level.reset; default mobs are removed).
- script hook `description` (bridge → user `desc(mob)`) consulted at the
  TOP of `Char.getDescription`; nil/empty falls through to the json
  classDesc path — statues describe their rolled weapon, and pre-empt the
  level suffix exactly like the java override did.
- script hook `onDestroy` (bridge → user `destroy(mob)`) from
  `Char.destroy` — journal-record cleanup on removal.
- `ItemUtils.statueWeaponCandidate` / `enchantStatueWeapon` /
  `statueArmorCandidate` / `inscribeStatueArmor` @LuaInterface statics:
  the lua side has NO instanceof (this luaj fork has no
  `luajava.instanceof`), and the java grant filters are pure class logic
  (weapon: EquipableItem + goodForMelee + usableAsWeapon + !MissileWeapon
  + level>=0, enchant only MeleeWeapon; armor: EquipableItem +
  usableAsArmor + level>=0, inscribe only classed Armor). commonClasses
  exports `RPD.Slot` (Belongings$Slot enum for `setItemForSlot`).
- debug: `/debug/journal` (records+depth), `/debug/char_desc?id=`.

Statue.lua: depth-scaled stats re-derived in `stats` each load
(def/atk 4+depth, dmg depth/4+1..depth, ht 15+5*depth — json holds the
depth-1 fallbacks; `data.born` gates the fresh-spawn full heal so a
restore never re-heals: ctor-time fillStats runs BEFORE the bundle
overwrites hp anyway, the guard is refactor insurance); gear grant is
the one-shot `gearGranted` in data (slot already filled → pre-migration
save, mark and skip); `act` journals on visibility (`!isPet()` +
`CharUtils:isVisible`), `destroy` un-journals; `desc` =
`RPD.format("Statue_Desc", item:name())`, naked → plain `name()`.
ArmoredStatue.lua: def 4+2*depth, atk (9+depth)*2 − lvl() (java
overrode attackSkill flat; base attack gains +lvl, the compensation
keeps the effective roll identical), dmg 4-8 static in json, armor roll
into the ARMOR slot (sprite flag takes weapon-else-armor). GoldenStatue
.lua: base-statue formulas, weapon always GoldenSword +4 via
`RPD.ItemFactory:itemByName` (no roll), STR never gear-derived (java
parity). One deliberate delta: golden honors `gearGranted` — java
re-granted a fresh +4 sword whenever the slot emptied, the same
endless-refill farm the one-shot flag killed on the enslaved statue.

Verified live (desktop, town depth 0 + sewer depths 1-2 + save round):
base stats exact per formulas, three rolls granted 3 different weapons
(str 14/16/20), armored: hp/ht 20 def 6 atk-base 19 (=20−lvl) dmg 4-8
str 12 with ClothArmor slot + RU desc, golden: GoldenSword in slot,
stats/str 10 per java; journal add on sight and drop on kill (per-depth
records), sprite rendering weapon layers, damaged hp 15/20 + state +
LUA_DATA across go_to_level/reload_game, zero skip lines, hero/rat descs
untouched. CI: alchemy 42/42, all_spells 38/38, doctor 7/7, blood 6/6.
Not exercised organically: a random STATUE special room (painter calls
the proven `MobFactory.mobByName`; sweep of depths 3-7 rolled none).

Fifteenth batch 2026-09-15 (Yog's flesh + ice guardian; the "NPC design
pass" stays queued behind Mike's call — these five needed no design
decisions): **`Larva`, `YogsHeart`, `YogsTeeth`, `YogsBrain`,
`IceGuardian`** — all five kinds data, all five java classes DELETED.
`IceGuardianCore` deliberately STAYS java: it is a `Boss` subclass and
its die-flow (battleMusic from classDef, `GameScene.bossSlain()`,
`level().unseal()`, `Badges.validateBossSlain`) is not lua-reachable —
that is the boss-flow story for a later batch. Its `instanceof
IceGuardian` switch is now a `getEntityKind().equals(ICE_GUARDIAN)` kind
check, and `IceCavesBossLevel` builds the guard via
`MobFactory.mobByName(MobFactory.ICE_GUARDIAN)` (new constant).

Engine surface (all reusable):
- `Level.getMobs()` @LuaInterface — the level mob list as a LuaTable
  (mirrors `getLevelObjects`); enables beckon-all loops, kind scans and
  "pick a random mob" logic in scripts.
- @LuaInterface on `Char.damage(int,NamedEntityKind)` (IceGuardian feeds
  its core), `Mob.beckon(int)` (Yog damage hooks), and
  `CharUtils.spawnOnNextCell` (Heart/Brain summoners — java YogsEye still
  uses it unchanged).
- `Char.die(cause)` was already @LuaInterface — Larva self-destructs with
  `self:die(self)` inside `act`, which runs at the top of `Char.act`
  (before buffs), exactly where the java override did its work.
- `Mob.resurrect()` spawns a fresh CustomMob of the same kind (FQN-free),
  so the IceGuardian rebuild trick ports with no new plumbing.

Gotcha worth remembering: do NOT eagerly `bindClass` item classes whose
static init CONSTRUCTS an item (e.g. `PotionOfHealing` builds
`pseudoPotion` in clinit) — `commonClasses.lua` is required at boot via
`LuaEngine.<clinit>` (Dungeon.reset → TitleScene), before the item
status handlers exist; the eager bind NPE'd the title screen. The
heal-and-cleanse flow is inlined in YogsHeart.lua instead
(`other:heal(ht*0.2, self)` + four `detachBuff`). Devour (safe clinit)
is bound as `RPD.Devour`.

Script notes: YogsBrain is the Scorpio kite shape verbatim
(canDoOnlyRangedAttack HUNTING↔FLEEING flip + lightningProc in zapProc,
return 0 = no base zap damage; attackRange authored 8 like Shaman/Warlock
— java had no cap); its damage hook spawns "Nightmare" via
spawnOnNextCell (Nightmare already data). YogsTeeth rolls the three
independent procs (drain via `heal(dmg, self)`, `Bleeding` affect+level,
Devour.hit + snd_bite + ×2). Heart's defenceProc spawns "Larva" the same
way. Larva: `stats` floors lvl at 1 (java ctor `lvl(1)`, guard so
earnExp-levelled larvae — fresh or pre-migration — keep their level),
`spawn` sets Hunting (java ctor state; the only batch kind needing an
initial state), `act` bursts into {Scorpio, Worm, Eye, Scorpio} at its
own cell when lvl>=2 (mob leveling makes that live; the java
sprite-emitter curse burst is done as a `CellEmitter:center` burst —
same particles, cell-anchored).

Verified live (desktop, town + sewer depth 1): all five stat parity
exact vs the java ctors (120/17/30/20/25-30, 450/18/26/40/35-45,
350/18/46/44/50-80, 350/18/31/30/15-25, 70/14/31/30/10-15 speed 0.7 +
FrozenCarpaccio loot), Larva HUNTING at spawn; level_up probe → burst
(Larva gone, HUNTING Scorpio imago at its cell); guardian death → core
took exactly 150 → two fresh guardians (and core's own death removes
them); teeth→core hit dealt 90 (> the 80 max single hit = a bleed/devour
proc fired; drain masked by full hp); heart damage → every mob on the
level flipped WANDERING (beckon-all); wounded pet rat healed +1
(= floor(ht*0.2), pets have no natural regen); rat→heart real attack
spawned a Larva next to it; brain zap dealt exactly 18 (in 15-25,
no base damage), brain damage → Nightmare spawned; brain
HUNTING→FLEEING kite with real retreat moves. Save round-trip:
damaged larva (65/120), FLEEING brain, heart, nightmare all restored at
positions, zero skip lines. New permanent debug endpoint
`/debug/level_up?id=` (real `earnExp(1000)` — mob-leveling probe).
Suite harness (this session's find, bd bwi material): the alchemy suite
finishes 37/42 on every headless variant tried; the 5 fails are ALL
"No recipe found" for MOD-data recipes (trio→VileEssence, Zombie,
Brute). Root cause chain, fully diagnosed: (1) the headless launcher
runs UNMODDED unless `--mod=Remixed` is passed (server_command in
tests/http_api/headless_server.py now passes it, and the CI workflow
now builds the assets-bundled `headlessShadowJar` —
`RemixedDungeon-Headless.jar` — instead of the bare `shadowJar`, which
bundles no assets so the mod can't resolve); (2) with the mod active,
mod recipe registration still dies on headless because ANY Potion
instance construction NPEs (`Potion.handler` null) — the item status
handlers are only initialized by the scene/full-game init path the
headless build skips; core-json recipes load, the mod's lua/json recipe
set half-loads (4 recipes) and everything downstream cascades. The
desktop build inits handlers during normal boot, which is why it serves
all 105 recipes. Next bwi step = initialize item status handlers in the
headless boot (or make recipe validation lazy about item construction).
Also fixed en route: ServerManager.start() waits on check_server()
without a port-ownership check — any leftover game on :8080 hijacks the
suite (watch for instant READY). My diff touches no alchemy/recipe/mod
code; all five batch kinds were verified through the live debug API
instead.

Not exercised: organic IceCavesBossLevel pressHero spawn (the boss level
is unreachable from the town debug flow; the guard line compiles and
`mobByName(ICE_GUARDIAN)` is the proven create_mob path), YogsEye
stays java and its "Larva"/Yog-part strings keep resolving.

Next: batch 16 — NPC pass (design settled with Mike 2026-09-15).

Key research finding: the lua NPC toolkit ALREADY exists and is proven
in-game. `scripts/npc/` hosts lua NPC scripts bound via the `scriptFile`
json key (CustomMob loads them as instances): Bishop (2019, gold-for-bless
dialogs on `RPD.chooseOption`), PlagueDoctor (2024, five-stage quest chain
on `mob.restoreData/storeData` per-NPC state + `RPD.showQuestWindow` +
`chr:checkItem`/`collectAnimated`), plus Bard/Barman/Innkeeper/Inquirer/
ItemSelectorExample/QuestGiverDemo. Also present: `itemSelector.selectItem`
+ `RPD.BackpackMode` (backpack picking), `scripts/lib/quest` (kill-tracking
quest state: `quest.give(name,chr,{kills={...}})`; `Char.die` runs `onDie`
on the dying mob and lib/mob.lua routes it into `quest.mobDied`), and the
`RPD.Journal` binding. The NPC json profile mostly exists too —
friendly/movable/immortal/fraction/aiState/baseSpeed:0 — see
mobsDesc/BishopNPC.json.

The in-game town is `town_2` → `levelsDesc/Town_2021_03.json` (Dungeon.json
graph), which places the LUA `PlagueDoctor` kind. Mike confirmed: the lua
doctor IS the in-game doctor. Java `PlagueDoctorNPC` is placed only by the
legacy, graph-unreferenced `Town.json`; its class is dead weight except
`questCompleted()` (called from PlagueDoctor.lua's special reward — badge +
hat unlock) and Quest statics wired into Dungeon bundles and
`Mob.processQuestKills` (dead in game: `given` only ever set by the java
interact, which never runs).

Sixteenth batch, part 16a 2026-09-15 (npc profile + doctor decommission):
- CustomMob gained the `npc` json key: act() preamble from the old
  NPC base class (throw items off the cell, blink off level objects via
  getEmptyNonStairsCellNextTo — never onto stairs, face the hero),
  absolute beckon no-op, `add(Buff)` false, canBePet forced false.
  Verified live: restored doctor blocked Burning (non-npc doctor took
  it — contrast probe), blinked off a ToxicTrap spawned under it,
  makePet left it NEUTRAL/un-owned.
- `PlagueDoctorNPC.java` DELETED. The in-game doctor is the LUA
  `PlagueDoctor` kind (live graph town_2 → Town_2021_03.json); the java
  kind survived only in the graph-unreferenced legacy Town.json, whose
  placement now points at the lua kind too. questCompleted() (badge +
  hat unlock, called from PlagueDoctor.lua's special reward) moved to a
  @LuaInterface static on `PlagueDoctorMask`, bound in commonClasses as
  `RPD.PlagueDoctorMask`. Quest statics removed from Dungeon save/restore
  and the processQuestKills RAT branch (dead code: `given` was only ever
  set by the java interact). Old saves keep the "plaguedoctornpc" quest
  bundle node unread; legacy RatHide quest state is lost (accepted).
- New `mobsDesc/PlagueDoctorNPC.json` (bishop-shaped + npc:true,
  scriptFile → scripts/npc/PlagueDoctor) so old-save FQN-tail restores
  land on a working data NPC. Verified live: fixture save with a java
  PlagueDoctorNPC (spawned pre-deletion) restores as CustomMob, script
  binds, quest state auto-inits, /debug/interact (new permanent endpoint
  driving the real tap-a-mob Interact CharAction) opens the prologue and
  stores questInProgress/questVariant. PlagueDoctor.lua also got a
  questIndex nil-guard for restored NPCs and now calls
  RPD.PlagueDoctorMask:questCompleted().
- Rat-kill regression clean (RAT branch still feeds Scarecrow/Ghost
  quests); boot clean with the new commonClasses bind (two launches).

Sixteenth batch, part 16b 2026-09-15 (town crowd → data+lua, RatKing gates):

- 10 kinds migrated, 13 java classes + 3 windows deleted
  (~1100 lines): TownGuardNPC/TownsfolkNPC/TownsfolkSilentNPC/
  TownsfolkMovieNPC/LibrarianNPC (one random WndQuest phrase each —
  java `WndQuest(int...)` picks ONE phrase, so each lua keeps the id
  list and shows one via showQuestWindow), HealerNPC+WndPriest,
  FortuneTellerNPC+WndFortuneTeller, NecromancerNPC, Hedgehog, RatKing.
  Each kind: bishop-shaped mobsDesc/<Kind>.json (same kind name = level
  jsons needed NO edits; old saves restore by name) + scripts/npc/<Name>.lua.
- New CustomMob plumbing (the only java surface RatKing needed, per
  Mike ruling "damage/buff gate on RatKing.lua"): optional script vetoes
  `onAllowBuff(mob,buff)` (consulted for npc-profile mobs before the
  blanket buff-immunity return) and `onBlockDamage(mob,dmg,src)`
  (consulted before super.damage; true = hit fully consumed, no hp loss,
  no gotDamage — matches the old java RatKing.damage early-return).
  mob.lua got matching wrappers; the gate DECISIONS live in RatKing.lua
  (allowBuff = anger>=2, blockDamage = friendly hit sets anger=2).
- mob.onInteract now FORWARDS the script's return (nil = handled, for
  every legacy script; false declines → Char.interact falls through to
  attack). RatKing interact returns false once anger>=2, java parity.
- RatKing.json: NO friendly key (static friendly:true would pin
  friendly() forever and block the hostile flip) — NEUTRAL fraction
  gives the friendly behavior via super; anger flip does
  setFraction(DUNGEON) + setAi("Hunting") from lua. RPD.Fraction bound
  in commonClasses. carcassChance:0 explicit (data default is 0.5; the
  java NPC base had 0) + persistOnReset (java reset()=true).
- Healer: WndPriest math moved to `CharUtils.goldPrice(buyer,base)`
  (difficulty factor, Haggler x0.9 — exact int-cast parity), heal to
  `CharUtils.healPatient` (PotionOfHealing.heal + Hunger + the
  Brute->Gnoll-badge easter egg; potion classes stay unbound java-side,
  batch-15 clinit lesson). FortuneTeller: `CharUtils.identifyItem`
  (= ScrollOfIdentify.identify) + `countUnidentified`; pick via proven
  itemSelector.selectUnidentifiedItem; identify-all =
  belongings:identify() straight from lua. Dialogs = RPD.chooseOption
  with prices formatted into labels (Bishop pattern).
- Necromancer: PrisonLevel d7 spawn + exit-1 removal swapped to
  mobByName/getEntityKind (painter pattern); `introduced` via
  restoreData; SkeletonKey via collectAnimated. Hedgehog: HallsLevel
  d23 spawn (once-per-run static kept level-side) → mobByName; talk
  ladder + Pasty-on-4th + speed ramp via onSpeed, state in restoreData.
- InquirerNPC/SociologistNPC/BellaNPC DELETED entirely (Mike: "keep
  just sprites") — Pollfish husk, graph-dead 2018 survey downloader,
  unplaced kind. spritesDesc/*.json kept; WndSurvey deleted (sole
  consumer). Deleted kinds resolve to a harmless dummy (verified via
  create_mob — no crash), TownLibrary.json Sociologist entry stripped.
- New permanent /debug/lua_eval?code=<lua> (runs a chunk in engine
  globals on the game thread) — drove the dialog-handler math and the
  heal/identify flows without UI clicks.
- Verified live (desktop rig, --mod assets): town crowd spawns as data
  (HP/HT 1, PASSIVE, NEUTRAL), guard/librarian/necromancer/fortune/
  healer windows screenshot-verified, gold math 75g exact, heal
  8/20→20/20 with gold debit, identify 1→0, king wake/anger ladder →
  HUNTING+DUNGEON, 9-dmg hit blocked at anger ramp (HP stayed 30),
  Blessed blocked while friendly / attached when angry, crown heap on
  death, anger+awake survive save/reload, hedgehog speed 0.5→3.5 +
  Pasty heap, deleted kinds → dummy, 120 town ticks zero lua errors.
- Lessons: (1) mob.lua wrapper arg convention — user hooks receive the
  JAVA mob as first arg: `act(self)`, `speed(self,base)`,
  `blockDamage(self,dmg,src)`, `allowBuff(self,buff)`; a wrong second
  param silently receives nil (Healer act crashed on :getPos until
  fixed). (2) THIS workspace resolves game files from rundir/mods/
  Remixed/ FIRST (FileSystem base-path list) — scripts/ and mobsDesc/
  there are hardlink twins of the repo tree EXCEPT scripts/npc, which
  is a real copy: new npc luas must be cp'd into
  rundir/mods/Remixed/scripts/npc/ or the level dies with
  "Missing file" (mobsDesc resolves anyway, making it look half-broken).
  (3) Desktop town = levelsDesc/Town_2021_03_desktop.json (that's who
  references it) — android/desktop town variants differ (movie folk is
  android/legacy-only). (4) move_hero refuses unexplored targets —
  /debug/reveal_map first.

Then 16c — quest NPCs: Ghost/WandMaker/
Blacksmith/ScarecrowNPC/Imp/CagedKobold/AzuterronNPC + the shopkeeper
family (Shopkeeper/TownShopkeeper/ImpShopkeeper). Mike rulings 2026-09-15:
ALL quest logic moves lua (quest state reset on old saves is acceptable,
no java bundle-node shims); WndBlacksmith gets recreated lua-side (expose
the tools); shop windows no longer stay java — WndShopOptions is already
lua-reachable (commonClasses class table) and WndTradeItem works with any
Char shopkeeper, so data mobs run the real shop UI once their backpack is
stocked.

16c-1 SHIPPED 5535e5dae (2026-09-16): quest layer (quest.lua trySpawn +
QuestBridge) + SadGhost; Ghost/WndSadGhost/WndSadGhostNecro java deleted.
Gotchas for the remaining slices: bound-class static methods take
COLON-calls from lua; lua quest state must stay serpent-safe plain
values (rewards generated lazily at button-press); delete Mob.java
switch lines with each quest (Scarecrow+Imp lines remain until 16c-3).

16c-2 SHIPPED 00a78a4c8 (2026-09-16): Imp + WandMaker; Imp/WandMaker/
WndImp/WndWandmaker java deleted; LastShopLevel gated via QuestBridge.
isCompleted; Rotberry split to plants/ with the first Bundle.addAlias
(pre-tag Seed FQN); ItemUtils.dropAt(kind,pos,heapType) added because
Heap.Type is sandbox-unreachable.java allHeaps() returns a java List —
iterate size()/get(i), never ipairs; heap.type is a field (type:name()).
Headless launches need --add-opens java.base/java.util for palace levels
(pre-existing PalaceServants.lua); headlessShadowJar ignores symlinked
assets/scripts changes — rm the jar before rebuilding.

16c-3 SHIPPED (2026-09-16): Blacksmith/ScarecrowNPC/CagedKobold/AzuterronNPC
+ Shopkeeper/TownShopkeeper/ImpShopkeeper are data; the 7 mob java classes,
WndBlacksmith and the Dungeon QUESTS bundle node + Mob.processQuestKills are
deleted. Lua: scripts/npc/{Blacksmith,ScarecrowNPC,CagedKobold,AzuterronNPC,
Shopkeeper}.lua — Shopkeeper.lua is shared by all three shop kinds via
scriptFile (Imp greeting kind-gated in act, seenBefore in restoreData;
AzuterronNPC.lua requires it and falls through to shop.interact on
completion). Quest state keys: "blacksmith", "scarecrow", "cagedKobold",
"azuterron" in quest.lua storage. Java kept: spawn shims (SewerLevel
halloween+d2, IceCavesLevel d18 exit room, CavesLevel room mutation gated
depth>11 with the fit-room scan BEFORE trySpawn so a failed roll doesn't
consume the quest; rollBase depth+1 short-circuits the trySpawn roll for the
unconditional once-per-run quests), painters/TownShopLevel place via
mobByName and stock through a treasury:check+collect helper (the deleted
Shopkeeper.collect applied check() to everything; plain Char.collect does
not — keeps stock parity), WndDontLikeAds face via mobByName. reforge is
fully lua (Mike): effect bundle inlined in Blacksmith.lua (snd_evoke,
UP-specks via emitter:start, ItemUtils:evoke, doUnequip/upgrade/detach,
spendAndNext, badges), order-by-level + verify gates ported verbatim;
verify/reforge exported on the script table for /debug/lua_eval probing.
Shop _buyMode/_sellMode return RPD.BackpackMode constants — Char.sellMode
already script-dispatches those hook names (mob.lua wrappers were already
there). Shopkeeper vanish-on-hit = blockDamage hook (fires before
super.damage; immortal is json-gated separately so the hook stays reachable
on npc mobs — Shopkeeper.json deliberately has NO immortal key). New java
surface: @LuaInterface on ItemUtils.isBag/isFood/evoke (instanceof-family
gates for lua — no luajava.instanceof), Badges.validateItemLevelAcquired/
getNotBroughtBag. defenceVerb is a json key (Char.getClassDef optString) —
the immortal trio + Azuterron keep Ghost_Defense via
"defenceVerb":"Ghost_Defense". Deltas accepted: scarecrow killed float→int
and the java quirk where the quest-START call counted as kill #1 (the lua
port counts real kills only — 25 kills instead of 24); town
fillInventory bags stock unconditionally (the deleted hero-owns-bag gate in
Shopkeeper.collect is gone at paint time; the interact-time bagSold gate
lives in Shopkeeper.lua); effectiveSTR hero-mirror and useBags=false
overrides die (nothing on the stock path reads them). Lua gotcha: method
call then FIELD access is `x:getBelongings().backpack` — `:backpack` without
parens is a parse error ("function arguments expected"). Headless
verification: all 7 spawn via create_mob, full quest flows driven through
/debug/lua_eval (stock counts, verify gates, reforge +1/consume, kill
ladder, exchanges, spirit spawn, shop fallthrough), shopkeeper save/reload
round-trip with stock intact, quest state persists across change_level.
CharUtils.isVisible is always false headless (FOV never builds) — the
ImpShopkeeper greeting needs a desktop run to see; logic is a 1:1 act-hook
port. Scarecrow's halloween spawn shim and painter shop placement are
code-mirror shims of proven patterns (organic/holiday spawns not
headless-drivable).

Design:
- Quest state = scripts/lib/quest.lua quest.state(name) via
  storage.gamePut (SCRIPTS_DATA bundle node, already round-trips). The 7
  java Quest statics + Dungeon bundle nodes die; old-save quest state
  resets silently (accepted; worst case WandMaker placeItem re-runs and
  duplicates the Rotberry/CorpseDust).
- Spawn hooks stay at the java decorate sites (Necromancer/Hedgehog
  precedent), each shrunk to a shim: QuestBridge.trySpawn(name) (new ~40
  line class, java→lua via the LuaEngine.require pattern) consults+rolls+
  records spawned/alternative lua-side, java places via mobByName. Blacksmith
  room scan/type mutation stays java (level painting, not quest logic);
  BlacksmithPainter places via mobByName. Rewards go lazy lua-side at first
  interact (Ghost.Quest.getWeapon lazy precedent). Kill counting = per-NPC
  lua installOnDieCallback (mob.onDie already dispatches); Mob.
  processQuestKills switch deleted. LastShopLevel's Imp.Quest.isCompleted()
  → QuestBridge.isCompleted("imp").
- Windows: WndSadGhost/WndSadGhostNecro/WndWandmaker/WndImp → RPD.
  chooseOption (Bishop pattern). WndBlacksmith recreated lua: sequential
  itemSelector.selectItem(UPGRADEABLE) picks + lua verify checks + confirm;
  the effect bundle (sound, ScrollOfUpgrade.upgrade, evoke, unequip/upgrade/
  detach, badges, spend) moves to ItemUtils.reforge(item1,item2) static.
- Shopkeeper family: Shopkeeper.lua interact = stock food (Remixed,
  difficulty<2), once-per-mob bagSold via restoreData + Badges:getNotBroughtBag,
  CharUtils.generateNewItem fill loop (add @LuaInterface), then
  GameScene:show(WndShopOptions(mob, hero)). TownShopkeeper/ImpShopkeeper =
  json kinds sharing the lua (ImpShopkeeper adds act-greeting). ShopPainter
  picks kind via mobByName. AzuterronNPC completed phase = same shop
  interact, imported from Shopkeeper.lua.
- Rotberry plant+Seed splits out of WandMaker.java into
  com.watabou.pixeldungeon.plants.Rotberry (mirrors all sibling plants,
  Earthroot/Firebloom/etc — inner Seed classes there too) and stays java
  (plants out of scope). FQN-change handling for old saves (researched
  2026-09-15): restore is kind-keyed first — item kind "Rotberry.Seed"
  (ItemFactory registerItemClassByName, explicit because every plant Seed
  has simple name "Seed") and levelObject kind "Rotberry" (LevelObjects
  Factory getSimpleName) are stable strings, so updating the two
  registration lines keeps every post-entity-tags save restoring. Legacy
  pre-tag entries carry bare CLASS_NAME and resolve via Bundle derived
  kind (last . then last $ segment): the plant derives "Rotberry" → hits
  the registry, no alias needed; the Seed derives "Seed" → MISSES
  "Rotberry.Seed" and falls to Class.forName — so register
  Bundle.addAlias(newSeedClass, "…npcs.WandMaker$Rotberry$Seed") next to
  the ItemFactory registration (first caller of the mechanism, map
  applied at Bundle.java alias lookup). Fixtures: cleartext-save grep for
  the Rotberry entry shape pre/post move, reload, both must restore.
  ImmortalNPC stays for ServiceManNPC.

Kind strings stay the java simple names (Ghost, WandMaker, Blacksmith, Imp,
ScarecrowNPC, CagedKobold, AzuterronNPC, Shopkeeper, TownShopkeeper,
ImpShopkeeper) — old saves, town/shop level jsons and mobByName call sites
resolve by string.

Stay java: ServiceManNPC (Mike, for now), MirrorImage/Sheep (engine
mobs), bosses incl. YogsEye and IceGuardianCore (boss-flow story).

## Batch 17 — boss tier groundwork (17a SHIPPED 2026-09-16)

Mike's rulings (2026-09-16): SkeletonKey goes in the boss inventory
(implicit, like the java Boss ctors); isBoss implications are implicit
(no per-mob json re-statement); combat-hook cluster approved; ShadowLord
+ Crystal STAY in lua scope — LevelTools may be exposed or the arena
re-implemented in lua; onNotice hook approved.

17a = java-only groundwork, no migrations. CustomMob now honors the
existing `isBoss` json key with full java-Boss semantics:
die-flow (GameScene.playLevelMusic + bossSlain banner + level().unseal
BEFORE super.die), implicit canBePet=false + Death/ScrollOfPsionicBlast
resistances, SkeletonKey collected at spawn (fresh) and at restore time
only if the bundle lacks one (Boss.restoreFromBundle fixup parity —
verified: reload keeps exactly one key), battleMusic/battleMusicFallback
json keys played from act while Hunting (fallback key used when the
primary path fails ModdingMode.isSoundExists). CustomMob.isBoss() override
added — the flag was a Mob FIELD, the METHOD lived on Char returning
false; without the override lua and the shadowlord-style java checks see
the wrong answer.

New lua surface (mob.lua dispatchers + CustomMob delegations, all
tri-state: nil = fall through to java):
- `notice(self)` — after the base sprite alert (boss intro yells)
- `canAttack(self, enemy)` — replaces the range+LOS check entirely
- `doAttack(self, enemy)` — return true = script took the attack incl.
  its own spend (Goo pump pattern)
- `attackSkill(self, target)` / `damageRoll(self)` — dynamic combat stats
- `CharUtils:beamStrike(attacker, enemy, fromPos, killReportId, burstMax)`
  — Eye/YogsEye gaze: rolls+damages every char on the Ballistica ray
  (attacker spared; recomputes the ray at strike time, no stale trace),
  damage via attacker:damageRoll so the dynamic hook flows through
- `CharUtils:validateBossSlain("<BADGE_ENUM_NAME>")` — per-boss badges
  without inner-enum access; `Char.isBoss()` now @LuaInterface

BossProbe fixture (mobsDesc/BossProbe.json + scripts/mobs/BossProbe.lua,
permanent, isBoss:true + bogus battleMusic key) exercises every new hook
with an observable effect. Headless-verified: implicit pet veto, isBoss,
attackSkill 40 / damageRoll 3 flats, canAttack override, doAttack pump
(no damage) then real attack after ticks, zapProc beamStrike 3+3 per zap
with the Eye_Kill death report when the beam killed the staged hero,
die yell, SkeletonKey heap on the death cell, save/reload round-trip
(one key, isBoss intact). bossSlain banner/level music/unseal are
headless-silent (scene guards) — first real boss migration (17c Goo)
should do one windowed look at the banner + battle track.

Hook convention reminder: user hooks take the JAVA char as arg 1 —
`canAttack = function(self, enemy)`, NOT (self, mob, enemy); a third
param silently reads nil (cost one debug round here).

## Batch 17b — Monk / Senior / King$Undead (SHIPPED 2026-09-16)

Mike's rulings (2026-09-16): Monk's FOOD category loot = roll into the
inventory at CREATION (not the death-roll `loot()` path); Undead = drop
the ToxicGas clearBlob-on-damage outright (no resistance compensation).

Three kinds data-defined: mobsDesc/Monk.json + Senior.json + Undead.json,
scripts/mobs/Monk.lua + Senior.lua + Undead.lua. Kind strings unchanged
(Monk/Senior/Undead) — old saves, CityLevel spawn tables, the Imp quest
token check and King's UNDEAD_CITY_MOBS all resolve by string. java
deleted: Monk.java, Senior.java, King$Undead inner class (~190 lines).
MobFactory: kinds now resolve via the mobsDesc scan → CustomMob; new
`MobFactory.SENIOR` constant (Badges.validateRare was the one
`instanceof Senior` check in a kind-string chain — now SENIOR).

New java surface:
- `CharUtils:disarm(disarmer, victim)` — the Monk-family disarm,
  line-for-line from the old Monk.attackProc body minus the call site:
  1/6 per slot (WEAPON then LEFT_HAND), knuckles and cursed gear stay,
  drop + Monk_Disarm GLog. Static → COLON-call from lua
  (dot-call silently shifts args — victim arrived null, cost one
  debug round; same lesson as 16c-1).
- category loot json form: loot object `{"category": "FOOD"}` (any
  Treasury.Category name) + `lootChance` → rolled once at creation, the
  item is collected into the mob's inventory and drops with the corpse.
  Same net drop rate as the java death-roll for Monk (0.153).

Monk: attackDelay 0.5 json key covers the old `_attackDelay` override;
Amok/Terror immunity via json immunities. The java "kick" actMeleeAttack
override was DEAD CODE (actMeleeAttack is hero-only; mob AI attacks via
doAttack) — the anim never fired in java. Mike wanted the kick playable,
so it is restored ON THE LIVE PATH: lua `doAttack` hook (Monk 50%,
Senior 30%) rolls the kick, then setEnemy + spend + sprite turnTo +
`CharUtils:extraAttack(self, "kick")` — a tiny java bridge that owns the
sprite Callback, because lua closures coerced to java interfaces come
out NULL silently (proven: 20 kicks, 0 damage pre-bridge; 18 kicks,
6 damage events landed post-bridge, rest dodged by the level-11 hero —
codebase convention is lua never passes closures into java, and Mike
vetoed luajava.createProxy: breaks the TeaVM/html build). New
method-level @LuaInterface: Char.setEnemy / attackDelay /
onAttackComplete (natural lua surface for attack scripting; no
createProxy anywhere). Headless drives the hook via
`mob:getScript():run("onDoAttack", hero)` — calling "doAttack" directly
BYPASSES the mob.lua wrapper and shifts args (script table lands in
`self`) — false-positive nil errors, cost one debug round. Senior: own json (dmg 12-20, str 15)
+ attackProc = 1/10 Stun 1.1 then disarm. Undead: `undead: true` json key
(gives setUndead + naturalUndead), exp 0, Wandering aiState, snd_bones on
visible death; 1/5 Stun 1.0 proc. clearBlob dropped per ruling — undead
now just take gas damage like anything else.

King.java stays java (17d): summon spawns by kind string
(`MobFactory.mobByName(MobFactory.UNDEAD)`), tint skip is a kind-string
check. Summoned city mobs keep the runtime-raised profile (undead=true,
naturalUndead=false, exp 0, Hunting) vs the natural Undead kind
(undead+naturalUndead from json) — verified both after save/reload.

Headless-verified: spawn + names/descs for all three; 8/41 Monks carried
creation-rolled Pasty/Ration (expected ~6); disarm live-proven in forced
combat (staff+dagger stripped from hero slots, heap on the floor);
Senior live stun=true after attack batches (probe via direct script
invocation first, then real combat); Undead stun + clean kill; King
zap-summon on CityBossLevel spawns Wandering Undead + HUNTING raised
city mobs; full descend→reload round-trip keeps ids/HP/states/undead
flags; allMobs() smoke clean. android gate compileAndroidFdroidDebug...:
green.

Remaining java-registered after 17b: ~13 bosses + minions (ShadowLord/
Crystal/Deathling/SpiderQueen/Lich/RunicSkull/BurningFist/RottingFist/
YogsEye/IceGuardianCore/Tengu/DM300/King/Eye) + MirrorImage/Sheep +
ServiceManNPC.

## Batch 17c-1 — Goo (SHIPPED 2026-09-16)

First real boss migration. Goo.java (143 lines) deleted; kind resolves
via mobsDesc/Goo.json (battleMusic stub from 17a merged in: primary
`ost_boss_1_fight` = Mike's HiFiDLC pack, vanilla rigs resolve the
`battleMusicFallback` → ost_boss_fight.ogg). isBoss json carries the
whole die-flow (banner/music/unseal/implicit key). Mechanics on hooks:
pumpedUp rides script `data` (mob.restoreData idiom — NOT `self.data`,
the java char has no data field; cost one debug round) and round-trips
the save via serpent (verified: luaData `pumpedUp=true` after
descend→reload); damageRoll 4-11 / 7-21 and attackSkill 11 / 26 via
the 17a dynamic-stat hooks; canAttack returns reach-2 only when pumped
(nil = java check otherwise); water soak heal 1/act in the act hook
(level.water is lua-indexed `pos+1`, WaterElemental precedent);
attackProc = 1/3 Ooze (2-arg Buff:affect is safe for non-Flavour Ooze)
+ black burst + pumped camera shake (RPD.shakeCamera, headless-safe);
doAttack = pump branch (spend 2.2, playExtra "pump", status+GLog:n —
GLog varargs need the trailing `{}` table arg from lua); getCloser and
zap clear pumpedUp — the pumped reach-2 strike at exactly range two is
a BLANK zap that un-pumps (doAttack dist>1 → visual zap → Mob.
onZapComplete → zap() consumed) — faithful java quirk, kept. die hook =
CharUtils:validateBossSlain("BOSS_SLAIN_1") + Goo_Info2 yell; notice
hook = Goo_Info3. Old saves: entityKind Goo → CustomMob; the old
@Packable pumpedUp field is dropped (mid-pump saves un-pump — accepted
delta, Mike's old-save-state-loss precedent).

Verified headless: spawn/name/isBoss/hp68; unpumped + pumped stat
probes via the mob.lua WRAPPER names; zap/getCloser un-pump; ooze
proc; exact +1/act water heal on a staged water cell; SkeletonKey
heap on the death cell; BOSS_SLAIN_1 persisted in badges.dat (debug
saves are gzip — zgrep, plain grep lies); pumpedUp round-trip;
allMobs 126 kinds. WINDOWED (the 17a debt): bossSlain banner renders
(sword art + "Босс повержен!" text), badge log line, death yell in
red, battle music machinery fired with fallback resolution, level
unsealed, 0 exceptions. Debug-harness lessons: Sleeping mobs ignore
test_damage/affect_buff wakes on this level — force `notice()` /
setState via lua; re-spawn the mob after editing its script mid-run
(script cache reload leaves restoreData's knownMobs stale).

17c next: SpiderQueen/DM300/Eye/Deathling; 17d Tengu/Yog organs/
Lich+RunicSkull (pair!)/King/IceGuardianCore; ShadowLord+Crystal
close the series. Existing hooks already cover: kiting (onGetCloser,
Succubus precedent), placeBlob fire/gas trails, Trap:reactivate
(DM300), getNearestLevelObject (King pedestals).

## Batch 17c-2 — light bosses: Eye, SpiderQueen, DM300, Deathling (SHIPPED 2026-09-16)

~400 java lines out (Eye 107, SpiderQueen 93, DM300 134, Deathling 66),
kinds ride mobsDesc/*.json + scripts/mobs/*.lua. New surface, all
method-level: `@LuaInterface Char.collect` (queen carry-gear via spawn
hook), `LevelObject.isTrap()`/Trap override (lua has no instanceof —
DM300 trap-eat filter), `@LuaInterface Char.getId` (self-identity in
the stun check; luajava userdata `==` is unreliable), and two new
tri-state hooks in CustomMob mirroring attackSkill: `defenseSkill`
(Mob.defenseSkill, Am spelling) + `dr` — for Deathling's owner-scaled
stats. mob.lua wrappers onDefenseSkill/onDr (nil = java default).

Eye: canAttack = raw Ballistica ray, NO range cap (script replaces the
range+LOS check; RPD.Ballistica trace/distance statics readable) —
ARRAY GOTCHA BIT AGAIN: luajava arrays are 0-based under 1-based lua
indexing (loop `for i=2,distance`, `trace[i]==enemyPos` — the +1 is on
the INDEX, never on the stored cell VALUE; cost one debug round).
zapProc hook = CharUtils:beamStrike(self, enemy, pos, "Eye_Kill", 2) —
17a groundwork, exact java parity; primary target takes zap + beam
(java double-dip, kept). spawn hook: setViewDistance(level view + 1).
Melee/zap split is positional in Char.doAttack — adjacent = plain
melee, no beam, faithful automatically. CustomMob.canAttack's
friendly() gate short-circuits BEFORE the script — same-fraction
probes of canAttack return false regardless of the script (stage
enemy-fraction targets).

SpiderQueen: 1/21 egg roll in act (math.random(0,20), spawnOnNextCell
w/ 100*difficultyFactor limit — GameLoop bound in commonClasses);
Poison attackProc (7-9)*Poison:durationFactor; below-half canAttack
refusal + getCloser kite (getFurther, HUNTING via
getState():getTag()=="HUNTING" — uppercase); gear = 1/3
ChaosCrystal/SpiderCharm/SpiderArmor collect at spawn, one-shot
gearGranted flag (restore re-runs onSpawn); isBoss json = SkeletonKey
+ die-flow. Her real kill in town registered SPIDER_QUEEN_SLAIN.

DM300: act = seed ToxicGas 30 on own cell (RPD.n); move hook fires
MID-move (before placeTo — self pos still old, `cell` arg = dest, trap
and rock effects key off it like the java post-super code);
object:isTrap() && hp<ht → reactivate("ToxicTrap", difficulty+1) +
heal math.random(1, missing-1) (watabou Random.Int(1,n) is [1,n-1] —
lua uniform differs at both ends) + Elmo burst + repair glog; rock
shower = random NEIGHBOURS8 cell (computed from width — no Level class
binding needed), CellEmitter/Speck.ROCK, shakeCamera(3,0.7),
playSound("snd_rocks"), water ripple or EMPTY→EMPTY_DECO set+updateMap,
findChar → Stun prolong (skip self via getId). Loot 50/50
ChaosCrystal|RingOfThorns:random() at 0.333 via spawn hook
(Mob.loot(item,chance) is @LuaInterface; json loot desc is single-kind,
hence the hook). die = BOSS_SLAIN_3 + yell; notice = yell. Verified
headless: trap-eat (FireTrap→ToxicTrap + heal 140→197 + glog + rat
Stun), gas DoT on a bystander, fresh badge + death yell.

Deathling: owner-scaled (lvl + skillLevel²) via the new hooks +
per-act ht(4+modifier); firstAct full-heal rides script data —
round-trip proven by damage-to-7 → descend → reload → 6 ticks → still
7/14 (no heal replay); owner guard `owner==nil or not owner:valid()`
(java would NPE); setSkillLevel(3) in spawn hook. Accepted delta (Mike
lgtm): ARTIFACT/LEFT_ARTIFACT equipment-slot override dropped — pets
are never equipped by the game. Doctor-owner verified: 1+3² → 14/14.

Windowed smoke: Eye sprite renders (data spriteDesc + DeathRay zap
effect), beam fired on-screen mid-screenshot, rat killed, render loop
healthy, 0 exceptions. Harness notes: get_map passable grid reads
transposed vs level.passable — scan level.passable from lua for
staging runs; findChar takes POS not id; move_hero is one step per
call (loop it).

17d remains: Yog organs (+YogsEye pierce folds in),
Lich+RunicSkull (pair!), King; ShadowLord+Crystal close the series.

## Batch 17d-1 — Tengu, IceGuardianCore (SHIPPED 2026-09-16)

Two more boss kinds data-defined; classes deleted. Zero level-class
edits needed — both spawn via `BossLevel.spawnBoss → Bestiary.mob →
MobFactory.mobByName` kind strings (Bestiary.json entries predate
migration); factory now resolves them through the mobsDesc scan.

New surface (tiny): `@LuaInterface Char.move(int)` (Tengu jump
teleport), explicit `@LuaInterface Level.getWidth()` (lombok `@Getter`
replaced by a hand-written annotated getter) + `@LuaInterface
Level.getLength()` (cell-math in lua: java `Level.adjacent` is a raw
cell-diff check `diff==1||diff==W||diff==W±1` that can wrap rows —
ported verbatim, NOT a chebyshev), class-level `@LuaInterface
ScrollOfMagicMapping` (Tengu re-arm reveals the trap cell; script
binds the class at runtime, not in commonClasses).

Tengu (mobsDesc/Tengu.json + scripts/mobs/Tengu.lua): jump = getCloser
hook (target in FOV → `spend(-1/speed)` refund + jump, Succubus
refund idiom); dodge = doAttack hook decrementing `data.timeToJump`,
every 5th attack adjacent → jump; jump re-arms up to 4 random traps
into PoisonTrap (`getLevelObjects()` + `isTrap()` + `reactivate`,
object list = random-with-replacement like the java getRandomTerrain
rolls) + `ScrollOfMagicMapping:discover(cell)`; no free landing cell →
potion-style fallback `heal(floor(ht*0.1), self)` + detach
Poison/Cripple/Weakness/Bleeding (PotionOfHealing.heal is NOT
lua-bound — clinit trap, batch-15 lesson — so the 4-line body ports
inline); hero-class-gated loot in spawn hook (TomeOfMastery unless
NECROMANCER/GNOLL/DOCTOR, TenguLiver for GNOLL) guarded by
`bag:getItem(kind)` so the restore-time re-run of onSpawn can't
duplicate items; notice yell gender-branched
(`hero:getHeroClass():getGender() == 2` = FEMININE, enum instance
methods work warn-only); die = validateBossSlain("BOSS_SLAIN_2") +
say. SkeletonKey rides isBoss auto-collect.

IceGuardianCore (mobsDesc/IceGuardianCore.json +
scripts/mobs/IceGuardianCore.lua): near-pure json (hp 1000,
baseSpeed 0.5, hasBodyParts false, 7 immunities); spawn hook collects
WandOfIcebolt+1 and IceKey (same getItem guard); die hook sweeps
`level:getMobs()` kind=="IceGuardian" → `m:die(cause)` (same-cause
kill) + validateBossSlain("ICE_GUARDIAN_SLAIN"). Guardian's own die
hook (damage core + respawn pair) behaves identically to the java
loop: core already dead → no-op, fresh guardians outside the loop
snapshot survive, same as before migration.

Verified live headless on the real levels: Tengu via create_mob on
PrisonBossLevel — exact stat parity (120/20/20/8-15/str10),
SkeletonKey+TomeOfMastery (warrior branch) in inventory; jump observed
(666→733, 15+ cells in one AI step, wool-puff path); controlled trap
re-arm: 3 AlarmTrap→PoisonTrap kind flips after a staged jump (the
level's traps are LEVEL OBJECTS — placeTraps makes POISON_TRAP
objects — so the re-arm shows as object kind changes); damaged-hp +
inventory round-trip via go_to_level away/back; kill → badge
"Тенгу побеждён" + both yells in logs (notice yell formatted with the
hero-class title, die say) + TomeOfMastery/SkeletonKey heap at the
death cell + BOSS_SLAIN_2 persisted in gzip badges.dat (ZGREP).
IceGuardianCore via the REAL pressHero→Bestiary arena flow on ice5:
core (Bestiary kind) + guardian spawned, exact parity
(1000/26/10/13-23, SkeletonKey+IceKey+WandOfIcebolt); killing the
core took the guardian with it (die loop). Zero LuaErrors; allMobs
smoke 126 kinds; hasMob ✓ for all nine 17d kinds; android gate green;
blood 6/6, doctor 7/7, all_spells 38/38. Alchemy fails on headless
are the PRE-EXISTING mod-recipe gap (bd bwi): baseline rebuild without
17d-1 changes fails identically ("Potion.handler is null" → lua
recipes half-load, 4 instead of ≥16) — verified by stash-rebuild
A/B, not a regression.

Harness notes added: PrisonBossLevel has NO static arena traps — its
scattered traps are trap LEVEL OBJECTS, and the prison "portal" exit
(room with the portal) is not the spawn room; PrisonBossLevel spawns
Tengu via pressHero when the hero enters the PRISON_BOSS_EXIT room
(getRoomExit is protected — for staging use create_mob instead).
IceCavesBossLevel arena = `outsideEntranceRoom` = hero row above the
arena-door line: walk north through the x15 corridor, one move_hero
step per call WITH position readback between steps (move_to on ice5
queues an action the hero never completes; move_hero to a
non-passable cell reports success but does nothing). reveal_map sets
Dungeon.visible/mapped/visited all-true — avoid before boss-arena
staging that depends on visibility.

## Batch 17d-2 — Yog family: YogsEye, BurningFist, RottingFist (SHIPPED 2026-09-17)

Three guts classes deleted (~330 lines); kind strings unchanged, no
level json edits (HallsBossLevel was the only java construction site:
`new YogsEye()` → `MobFactory.mobByName(MobFactory.YOGS_EYE)` and the
explicit `boss.spawnOrgans()` call DIED — the eye's own lua `spawn`
hook places 2-3 distinct organs of {RottingFist, BurningFist,
YogsBrain, YogsHeart, YogsTeeth}, difficulty>2 → 3).

Engine surface (tiny): `Char.damage` now CONSUMES a numeric script
return from `onDamage` (mob.lua wrapper `onDamage` forwards numbers,
coerces everything else to boolean as before) — the numeric replaces
the incoming damage BEFORE gotDamage/buffs/resist, exactly where a
java damage() override shifted it. All pre-existing `damage` hooks
return nil, so nothing else changes. That was the whole java delta;
organ placement is PURE LUA (a ~10-line nearest-free-cell scan in
YogsEye.lua mirrors Level.getNearestTerrain min-path-distance +
random-pick semantics; java predicate lambdas are not lua-reachable —
lua closures coerce to java interfaces as null).

Mike rulings that shaped the batch: the eye's damage-shift is LIVE
(the java `mob.isBoss()` scan counted nothing — the organs were plain
Mob, never Boss, so `dmg >>= shift` was a no-op since the fork began);
lua counts the 5 organ KINDS present instead, beckons them toward the
eye, halves per organ (verified: 5 organ-kind mobs staged → 32 dmg →
hp -1). BurningFist's ranged attack is LIVE feedback: java flashed the
victim in `attack()`, which only ever runs adjacent (ranged hits go
Char.doAttack → sprite zap → Mob.zap → zapProc), so the flash was dead
code; lua wires it in `zapProc`, which fires exactly on ranged hits.

Key facts: `isBoss:true` json now carries the eye's whole die-flow
(banner/unseal/SkeletonKey/implicit Death+PsionicBlast resists); the
2-key battleMusic stub in mobsDesc/YogsEye.json merged into the full
def. `movable:false` keeps beckon a no-op for the eye (java override
parity for free); fists beckon ALL mobs from their damage hooks
(ported — they were initially missed, caught by the live water-heal
probe). Fists keep displaying Yog_Name/Yog_Desc (no per-fist default
strings exist; only el/uk translations carry fist ids — unused).
`spawnOnNextCell(mob,kind,limit)` third arg is a POPULATION CAP (the
java larva cap 10×difficultyFactor), not a chance. The `spawn` hook
re-runs on restore — `data.organsSpawned` via restoreData/storeData
guards organ duplication across the change_level snapshot round-trip
(verified: exactly eye+2 organs before and after). Accepted deltas:
beam particle count IntRange(1,3) vs java IntRange(2,3) (beamStrike's
fixed shape, one particle of spread); Boss-ctor mobLeveling lvl roll
not replicated (quirk-gated, consistent with Goo/Tengu/Eye).
RingOfElements' fire-immunity whitelist now uses
MobFactory.BURNING_FIST (last java guts reference outside the shim).

Verified live (headless): stat parity ×3 exact (eye 1000/1000 30/30
20-30 SLEEPING SkeletonKey; fists 400/400·26/25·40-62 and 500/500·36/
25·34-46 WANDERING); fire trail blob after 3 ticks; ranged fist hit
at distance 4 with the fist stationary (pet -39 hp through the base
zap flow, canAttack ray cast(flags false,true) == enemyPos); Ooze proc
through real Char.attack chain (1/3 landed on hit 9 of a heal-topped
pet; direct attackProc call also proved attach); water heal +10 via
direct act() on a water tile (Wandering walks the fist off water —
java parity); eye shift math exact twice (32>>5=1, 16>>3=2), larva
spawn + cap, organs beckoned; beam kill at range 4 (base zap + beam
double-dip); organsSpawned guard; YOG_SLAIN badge in gzip badges.dat
after reload; allMobs() smoke — all 126 kinds construct, zero
DummyMob. CI suites all green on the batch jar: alchemy 42/42,
all_spells 38/38, blood 6/6, doctor 7/7, level_navigation 9/9,
pet_transition 5/5 (--start-server), turn_economy 1/1
(--start-server); android gate green. No windowed smoke needed: the
eye sprite json is untouched and beamStrike's visuals were
window-verified in 17c-2.

Harness notes: `pkill -f "[R]emixedDungeon-Headless"` must be a LONE
call even when later arguments of the SAME compound command contain
the jar name — the shell's own cmdline matches and the command dies
before the build (cost one gradle round). change_level takes
`level=` (not `to=`); reload_game restores the last SAVED game (town
after start_game) — for mob round-trips use change_level, which
restores the level snapshot with mobs. test_damage with a srcid that
matches no mob silently no-ops. MobFactory:allMobs() returns
pre-CONSTRUCTED mob instances (java List, `:size()`/`:get(i)`) — its
successful construction of every registered kind is the whole smoke.

## Batch 17d-3 — Lich, RunicSkull (SHIPPED 2026-09-17)

Necropolis boss pair deleted (~400 lines); kind strings unchanged,
Bestiary.json spawns `"Lich"` by kind — zero level edits (NecroBossLevel
keeps building the 4 pedestals in java; level classes stay java).

Architecture: **RunicSkull is pure json, no script** — the Lich drives
everything. Lich.lua scans `level:getMobs()` for kind `"RunicSkull"`
and keys each skull's variant (RED/BLUE/GREEN/PURPLE = spawn order i,
java `makeNewSkull(i)`) in its OWN `data.variantByPos` keyed by the
skull's cell — skulls never move, so position is a free stable id; no
cross-script restoreData sharing. Skull visuals: the lich self-zaps at
useSkull (java parity); the skull's own act-zap loop is dropped (the
zap anim ends on its own; re-trigger per 5-turn switch = same cadence).
Skull json: `aiState Passive` + `pacified` + `movable:false` +
`flying` — movable:false is load-bearing (beckon is the one thing that
both moved a pedestal skull and flipped its state; verified fixed by a
direct `beckon` + ticks probe). The kind int 0-3 was visually inert
(spritesDesc has ONE texture), so variant is data-only.

Engine delta: **CustomMob.canAttack honors `pacified` now** — the
override silently dropped Mob.canAttack's `!pacified` gate (override
predates the batch-12 json key), so pacified data mobs could attack;
RunicSkull hit the hero through it. Fixed for SuspiciousRat and
JarOfSouls too (author intent: `pacified` = never attacks). New
annotations: `WandOfBlink.appear` (skull spawn-in; clinit-safe) and
`CharSprite.idle`. LATENT BUG FIXED in shipped Tengu.lua: its jump
burst called `RPD.CellEmitter`/`RPD.Speck` which don't exist top-level
in commonClasses — they live in `RPD.Sfx` (Tengu jumped fine because
the crash came after `move`); Lich.lua uses `RPD.Sfx.CellEmitter` from
the start.

Lich.lua: act hook cycles `data.timeToSkull` (5); on expiry spawns
once (difficulty 0→2 skulls, >2→4, pedestals from
`getLevelObjects()` kind `"pedestal"`, random sample, variants ride
the random order) then activates a random ALIVE skull — RED heals
floor(ht·0.07·alive) + 4 detachBuffs (PotionOfHealing inline port,
Tengu fallback idiom), BLUE loops `CharUtils:spawnOnNextCell(self,
"Skeleton", 999)` + `setAi Hunting` until DUMMY/invalid (spawnOnNextCell
population-cap arg unused at 999 — java counted set size, level-wide
skeleton count is irrelevant at that bound), GREEN
`RPD.placeBlob(RPD.Blobs.ToxicGas, pos, 30·alive)`, PURPLE = no
use-effect. defenceProc: activated variant PURPLE → return 0 (damage
nullify); else 50% `timeToJump=true`; doAttack hook jumps BEFORE the
base attack when flagged (java order). getCloser = Tengu jump verbatim
(FOV gate, spend refund). spawn hook collects SkeletonKey +
BlackSkull (NECROMANCER → BlackSkullOfMastery) with getItem guards.
die hook: badge + remove() every non-pet mob — **with `m ~= self`**:
the onDie hook runs BEFORE Char.die's destroy(), so the lich is still
in the level list and remove() would quietly eat its own loot (java's
wipe ran after super.die had already dequeued it). Old-save guard:
respawn gated on "no RunicSkull alive", so a half-fight save can't
duplicate skulls while any skull persists.

Accepted deltas: RED heal multiplies ALIVE skull count vs java's stale
set size (java healed less late-fight — arguably a fix); old-save java
`kind` int + `@Packable skullsSpawned`/`timeToJump` bundle fields have
no data twin (old-save skull variants unknown → purple nullify won't
fire until skulls re-spawn); GREEN is not observable headless —
`GameScene.add(Blob)` skips `Actor.add` when no scene (verified the
seed itself lands: manual `Blob:seed` + Actor:add → blobAmountAt 30).

Verified live (headless): stat parity exact (lich 200/200 35/23 12-20
str14 + SkeletonKey/BlackSkull by hero class; skull 70hp PASSIVE);
2 skulls on pedestals at difficulty 0; BLUE summoned 2 Hunting
skeletons; RED heal +14 = floor(200·0.07·1) exactly; PURPLE A/B: hit
155→155 armed vs 155→138 disarmed (WarHammer — note: bare lvl-54 fists
lose to dr 15, first "nullify" observation was dr-eaten 0s); defenceProc
50% jump roll observed (timeToJump true→consumed, jump-before-attack:
lich teleported then base attack proceeded); getCloser jumps
repeatedly; die = badge «Лич побеждён» + full level wipe + BlackSkull
heap; save/reload round-trip kept hp 141, variantByPos, latch, skull
pos; movable:false beckon probe; allMobs() smoke 126 kinds / 0
DummyMob. CI green on batch jar: blood 6/6, level_navigation 9/9,
doctor 7/7, all_spells 38/38, alchemy 42/42.

Harness notes: level_up takes `id=-1` for the hero (the param is
parseInt'd — `id=hero` 500s). A dead hero FREEZES the world (no acts
for anyone) — stage boss fights with a levelled hero (level_up ×4).
`lvl:blobAmountAt(class, cell)` is the direct blob assertion.
setPos-teleporting the hero does not trigger pressHero — real
`move_hero` steps do (NecroBossLevel spawns the boss on arena entry).

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
