# Gameplay Design Decisions

Living record of *how the game is meant to play*. Design rulings made by Mike in feedback rounds, reviews or day-to-day calls get a dated entry here; plain bug fixes stay out (those live in the feedback findings docs, which feed this file: `BETA8_FEEDBACK_FINDINGS.md`, `MINION_FEEDBACK_BETA6_FINDINGS.md`).

Rules:
- New entry per decision: date — ruling — rationale — commit/status. Newest first within a section.
- When shipped, append the commit hash. Open proposals are listed until ruled.
- When a change contradicts an entry here, update the entry (supersede, don't silently drop).

---

## Death & revival
- 2026-09-14 — **Chess duel: checkmate death is final.** Mate kills through the whole revive chain (dew vial auto-drink, LICH resurrect, Ankh are all bypassed). A lost duel is a lost run. (f9fb7f3ce)
- long-standing — The death revive chain (dew vial → LICH → Ankh) is *global* death behavior. Features must not bypass it without an explicit ruling — chess mate above is the only sanctioned bypass.

## Undead & necromancy
- 2026-09-14 — **Moongrace never duplicates undead mobs; moonlight cures necromancy.** An *artificially raised* mob stepping on it is full-healed AND returned to life (undead flag dropped, with the whole immunity set that comes with it). *Natural undead* — species born undead: Skeleton, necropolis mobs (Zombie, knights, skulls, souls, Deathling, JarOfSouls), King.Undead, json `"undead": true` — carry `naturalUndead`, are not curable, and clone like the living. Artificial raise sites (curable): `Carcass.reanimate`, RaiseDead spell, skull-raised pets, the City King's raised servants. Undead state must survive every cloning path — `makeClone` carries `undead`/`naturalUndead`/`expForKill`. (8a81ad382, dbeca4d12, 3e3fe1b1f)
- open (2026-09-14) — VileEssence inputs are Doctor-only (dissect / BoneSaw crits), so necromancy is effectively a Doctor perk. Pending: add a general VileEssence source or keep it Doctor-locked.

## Minions & pets
- 2026-09-14 — **Tap priority: friendly char beats heap.** Tapping a pet (or any friendly) interacts/swaps even with a corpse-heap on the cell; loot under a pet is picked up by walking onto the cell. (8de307077)
- long-standing — Pets always follow their owner across level changes; pets live in the game bundle, never in level saves. Same-level position restore on re-entry is an open proposal.
- open (2026-09-14) — 2H weapon vs occupied offhand: pick auto-unequip semantics (none / AI-only / everyone). Recommend "everyone" + AI stops retrying.
- open (2026-09-14) — Pet equip decisions and pet-UI requirement indicators should use *displayed* (identification-aware) stats, not real ones.

## World & interaction
- long-standing — **Doors + heaps:** heaps wedging doors open is an established player tactic (death drops wedge too). Never remove `Door.leave`'s heap guard.

## Items & economy
- long-standing — Price 0 = not sellable (FOR_SALE gate); the 1g `adjustPrice` floor is deliberate.
- long-standing — **Authored stats over derived heuristics:** game values live as authored data (java/lua/desc json), not computed from formulas where avoidable.

## Mobs & balance
- long-standing — Depth scaling uses the mob's *origin-level depth* (elementals, Wraith, Crystal), not the hero's current depth.
- long-standing — Lua mob `act()` is a pre-act policy hook: return value is discarded, never spend time inside `act`; ranged attacks come from `attackRange` in json; reactive behavior is gated by state set in `act`.

## Heroes & naming
- long-standing — **Hero is just a mob:** no Hero special-casing; behavior belongs in Char/Mob generics.
- long-term — No single-hero / single-level assumptions anywhere (multi-hero, multi-active-level goal).
- open (2026-09-14) — Custom heroes may take DnD-style class names (Elf → Archer, Gnoll → Ranger, Plague Doctor → Physician), matching the original Warrior/Mage/Rogue/Huntress tradition. Final EN+RU names pending. Display-only; enum keys and saves untouched.
