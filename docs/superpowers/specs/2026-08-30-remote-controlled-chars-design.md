# Remote-Controlled Chars — Design

Date: 2026-08-30
Status: approved direction (approach B — direct drive), slice 1 pending implementation plan
Related: LLM control surface (`observe`/`get_map`/`hero_status`/`move_to`), beads roadmap snap-rgt/snap-84s/snap-3kj/snap-bjl

## Purpose

Remotely controlled characters ("puppets"): game chars driven from outside the
process over the debug HTTP API, addressed by id. Four goals, built
incrementally:

1. Debug/test tool (follower/pet transfer repro, scripted scenes)
2. An LLM or script plays a body (possess a mob instead of the hero)
3. Multi-agent: several puppets, several drivers, id-addressed commands
4. Eventually a gameplay feature (play-as-mob) — inherits everything

Deliberately NOT chosen: hero-possession first (approach A) and a dedicated
PuppetChar class (approach C). Direct drive (B) means commands push actions
onto any char without routing through the hero, which is the shape slices 2-3
need; it costs a small new AI state up front.

## What already exists (the design leans on it)

- `Char.nextAction(action)` — generic; sets `curAction` and forwards it to the
  control target. Actions (`Move`, `Attack`, ...) are typed `Char` and work for
  any char.
- `CharUtils.actionForCell(actor, cell, level)` — the game's own tap
  resolution (empty → Move, mob → Attack/Interact, heap → PickUp/OpenChest,
  stairs → Descend, locked door → Unlock, object → InteractObject). Used by
  `Hero.handle` (hero taps) and `OrderCellSelector` (pet orders) — we reuse it
  for puppet taps.
- `Actor` time queue — one shared clock; actors wake in time order and
  `spend()` their action costs. This is what makes puppets turn-based.
- `ControlledAi` / `MoveOrder` / `KillOrder` / `Passive` — prior art for
  commanded chars; none fits direct drive exactly (owner coupling or
  autonomy), hence one new state.
- `Mob.makePet(mob, hero)` — fraction/friendly plumbing and, crucially,
  `followOnLevelChanged` → pets transfer across levels (the machinery the
  spider-nest follower work exercises).

## The puppet

A normal `Mob` of a real mob type (rat, spider — no new Char class), placed in
a new AI state `RemoteAi` (tag `RemoteControlled`).

**Stance toward the dungeon hero is the driver's choice** (`stance=friend|foe`
at spawn/possess), not baked in:

- `friend` → `Mob.makePet(mob, hero)` — HEROES fraction: dungeon mobs hunt it,
  hero's side treats it as friendly, and pet plumbing gives it level-transfer
  following.
- `foe` → untouched dungeon mob — DUNGEON fraction, owns itself: hero and pets
  treat it as an enemy, and it can be commanded to attack the hero.

`RemoteAi` itself is fraction-blind: friend/foe is pure Fraction/ownership
state, orthogonal to who drives the char. `release` drops remote control but
does not change allegiance.

### `RemoteAi` contract

- `act(me)`: if `me.getCurAction() != null` run it (the action spends the
  mob's own time per step); else idle — `spend(TICK)`, no behavior, no
  retaliation, no chasing. Always spends — never trips `Mob.act`'s
  "really confused" retry guard.
- `gotDamage`: no-op. The driver decides everything.
- `onDie`: GLog event; driver sees death via `char_status.alive == false`.
- Watchdog: counts own wake-ups without a new command. After `N` idle world
  turns → clear `curAction`, revert to the state the mob had before going
  remote (remembered per-char; fallback `Wandering`), done — it is a normal
  mob again. `N` settable per puppet at spawn/possess (`revertAfter=`),
  default 10, `0` = never revert. The counter counts world turns, so a frozen
  world never counts down. The revert is surfaced once via `char_status`
  (`"reverted":true`).
- Persistence: AI state is saved by tag, so the puppet reloads as
  `RemoteControlled` with its `N`. The watchdog counter resets on load. The
  remembered pre-remote state is in-memory only; after a reload the watchdog
  falls back to `Wandering` (acceptable — a reloaded mob has no meaningful
  "before").

## Timeline model (command-pump, queue-resident)

- The puppet **stands in the actor queue permanently**. It never takes over
  the clock and never runs ahead of it: it wakes when the shared clock
  reaches it, executes its current action one step per own wake-up at its own
  move cost, and idles between commands.
- **A command is a kick, not a completion.** The command handler (GL thread):
  resolve the tap for that char, set `curAction` via `nextAction`, pump the
  actor queue briefly — bounded (≈20 world ticks, or until the hero parks).
  Multi-cell walks continue on subsequent world advances because `curAction`
  persists. Driver polls `char_status.action == "idle"` for completion —
  same contract as the hero's `hero_status`.
- World fairness: every puppet step pays full game time, so dungeon mobs get
  their own turns interleaved. Between driver commands the world is frozen
  exactly as turn-based games are (hero parked = clock parked); an LLM
  thinking for ten seconds costs zero game time.
- Multi-puppet (slice 3) falls out: puppets with queued actions interleave
  step-by-step in time order. Faster puppets act more often.
- Escape hatch (slice 2): `/debug/realtime?on=1` flips the existing native
  realtime switch (`Dungeon.realtime()` — `Hero.act` already supports it) so
  the world free-runs while a driver thinks.

## Endpoints (slice 1)

| Endpoint | Behavior |
|---|---|
| `/debug/remote/spawn?type=Rat&x=&y=&stance=&revertAfter=` | spawn via `MobFactory`, apply stance (`friend` → `makePet(hero)`, `foe` → untouched), `setState(RemoteAi)` → `{id}` |
| `/debug/remote/possess?id=&stance=&revertAfter=` | flip an existing mob to remote; applies stance change if given; remembers its current state as the revert target |
| `/debug/remote/release?id=` | immediate revert to remembered state (same path as watchdog) |
| `/debug/remote/list` | derive from live mobs whose state tag is `RemoteControlled` — no registry to keep in sync |
| `/debug/char_status?id=` | `hero_status` shape for any char: `alive`, `hp`, `ht`, `pos`, `x`, `y`, `action`, `levelId`, plus `type`, `fraction`, `remote`, `reverted`; works regardless of hero FOV |
| `/debug/move_to?char=<id>&x=&y=` | tap semantics for that char: `updateFieldOfView(puppet)` → `actionForCell(puppet, cell, level)` → `nextAction` → bounded pump. `char` omitted = hero (unchanged, back-compat) |

`observe` mob entries gain `"remote":true/false` (state tag check — free).

## Known sharp edges

- **`level.fieldOfView` is one shared array.** Computing the puppet's FOV
  overwrites the hero's. `ControlledAi` handles the same problem by calling
  `Dungeon.observe()` after stepping; we do the same after a puppet tap.
- **Hero-only actions.** Slice 1 executes Move/Attack/Interact-family only;
  anything else → 400 "action not supported for remote char". No silent
  no-ops.
- **Hero death with live puppets** → normal game-over takes everyone.
  Accepted for slices 1-2; slice 3 revisits (spectator mode).
- **`foe` puppets are level-bound.** Level-transfer following comes from pet
  plumbing (`followOnLevelChanged`), so only `friend` puppets follow the hero
  through stairs. A `foe` puppet stays on its level.
- **All clock and queue manipulation on the GL thread** via
  `GameLoop.pushUiTask` — the queue is not thread-safe (same discipline as
  every existing mutating endpoint).

## Test plan (`tests/http_api/test_remote_chars.py`)

- friend puppet: spawn → `char_status` idle → `move_to` walk → reaches it;
  `friendly(hero)` holds
- foe puppet: commanded to attack the hero (lands hits, hero HP drops); hero
  and pets see it as an enemy
- tap on a dungeon mob resolves Attack and the puppet lands hits
- two puppets, interleaved walks (multi-agent kernel)
- `reload_game` → puppet still alive, still `RemoteControlled`, same id
- descend with friend puppet following → pet-transfer path (follower-bug
  territory)
- watchdog: puppet with no commands for `revertAfter` turns reverts and
  resumes default AI

## Slices

1. `RemoteAi` + spawn/possess/release/list + `char_status` + `char=` on
   `move_to` + observe flag + tests ← this spec
2. More verbs (items, spells through the puppet), realtime toggle,
   observe filtering by char
3. Multi-agent: per-driver command streams, hero-as-spectator mode
4. Gameplay hardening: save consistency, UI, balance
