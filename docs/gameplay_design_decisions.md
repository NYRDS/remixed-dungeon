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
- 2026-09-14 — **VileEssence stays Doctor-only.** Its inputs (dissect / BoneSaw crits) are Doctor loops, so necromancy remains a Doctor perk for now. No general source, ruled by Mike.

## Minions & pets
- 2026-09-14 — **Tap priority: friendly char beats heap.** Tapping a pet (or any friendly) interacts/swaps even with a corpse-heap on the cell; loot under a pet is picked up by walking onto the cell. (8de307077)
- 2026-09-14 — **Two-hander vs offhand: the equipped item yields.** Equipping a two-hander auto-removes the conflicting hand item (and back), for heroes and pets alike; cursed blockers still refuse. The pet AI never auto-strips gear to equip something — that path is manual-only. (0f302d35f, b257007be)
- 2026-09-14 — **Pet equip decisions use displayed stats.** AI equip scoring and pet-UI requirement indicators judge unidentified items by their typical values, not the real ones — minions no longer "know" an unidentified claymore needs 20 STR. (b257007be)
- 2026-09-14 — **Pets keep their spot on game reload.** Re-entering (loading) the level a pet was left on restores it to its own cell; it still follows the owner onto every *new* level. Implemented via a per-mob level-id stamp. (ad8f5a7fd)
- long-standing — Pets always follow their owner across level changes; pets live in the game bundle, never in level saves.

## World & interaction
- long-standing — **Doors + heaps:** heaps wedging doors open is an established player tactic (death drops wedge too). Never remove `Door.leave`'s heap guard.

## Items & economy
- 2026-09-14 — **BoneSaw is not upgradable by design** — it scales with its wielder's skill level instead. Its 9 STR requirement is mechanically real: a deficit applies the standard weapon encumbrance penalties (accuracy ÷1.5ⁿ, delay ×1.2ⁿ), and the item info shows effective average damage.
- 2026-09-14 — **Doctor class armor's built-in mask is the real one.** Wearing the epic DoctorArmor grants GasesImmunity; the accessory mask is an optional pre-armor backup. Either source alone keeps the immunity. (04ab87c01)
- 2026-09-14 — **Shield upgrades lower STR requirement** like armor does: `max(2, base − level)`; blocked damage already scales ×1.3/level and the desc shows effective values. (4e73c6b05)
- 2026-09-14 — **Rotberry → Potion of Strength belongs to the legacy pot lottery, not recipes.** The `Rotberry×1 → PotionOfStrength` recipe was reverted (66ac065f6, was 65db2ae68): the player's "30%" is the alchemy pot's 3-seed lottery — all-seed heap of ≥3 brews a potion, each seed's `alchemyClass` gets its weighted share, and rotberry's is PotionOfStrength (≈1/3 of a 3-seed pot). Dropping the seed alone does nothing (`SEEDS_TO_POTION = 3`). No authored strength-potion recipe; `Rotberry + 10 VileEssence → PotionOfMight` stays.
- long-standing — Price 0 = not sellable (FOR_SALE gate); the 1g `adjustPrice` floor is deliberate.
- long-standing — **Authored stats over derived heuristics:** game values live as authored data (java/lua/desc json), not computed from formulas where avoidable.

## Mobs & balance
- long-standing — Depth scaling uses the mob's *origin-level depth* (elementals, Wraith, Crystal), not the hero's current depth.
- long-standing — Lua mob `act()` is a pre-act policy hook: return value is discarded, never spend time inside `act`; ranged attacks come from `attackRange` in json; reactive behavior is gated by state set in `act`.

## Heroes & naming
- long-standing — **Hero is just a mob:** no Hero special-casing; behavior belongs in Char/Mob generics.
- long-term — No single-hero / single-level assumptions anywhere (multi-hero, multi-active-level goal).
- 2026-09-14 — **No hero renames.** The hero class names are a deliberate hidden joke referencing the Stick of Truth class system; renaming custom heroes to DnD-style classes (Archer/Ranger/Physician) would kill the reference. Player suggestion declined.
