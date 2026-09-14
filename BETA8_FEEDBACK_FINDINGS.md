# Beta.8 feedback round — triage findings (2026-09-14)

Raw player feedback relayed 2026-09-14, triaged against `beta.8` @ 24451b1cf.
Verdicts: **BUG** (code defect) / **DOC** (string/doc defect) / **AS-CODED** (works as designed, explanation attached) / **DESIGN** (needs a call) / **SUGGESTION** (feature request). Nothing changed yet — this is the decision sheet. All refs checked out on beta.8.

---

## A. General reports

### A1. Chess duel: checkmate "did nothing", hero survived on dew vial
**Report:** got mated, hero drank the dew vial and kept playing; expected death. All pieces incl. both kings killable.

**Findings:** checkmate → hero death **is implemented**. The duel is a lua actor over the sunfish engine (`scripts/actors/Chess.lua`, `scripts/stuff/chess/sunfish.lua`, board carved by `CityBossLevel.pressHero`, CityBossLevel.java:410-427). After the AI's move there is an explicit frame check (`scripts/actors/Chess.lua:556-561`): `is_checkmate` → `processLose()` → `RPD.Dungeon.hero:die(hero)` (Chess.lua:219-231). The hero survived via the standard death chain: `Hero.die` → `DewVial.autoDrink` silently revives (Hero.java:586-589; Ankh :601-604, Lich :591-595).
"Kill both kings" is not a coded win condition: pieces are PASSIVE mobs and `repairPieces()` respawns dead ones while the match is in progress (Chess.lua:434-467). Walking away mid-game is impossible by design — `cellClicked` consumes every click while `gameInProgress` (Chess.lua:748-766).

**Verdict: AS-CODED.** Mate = death; the dew vial auto-revive is global death behavior, not a chess hole. If the reporter saw "nothing happened at all" (no death, no game-over), that matches pre-`49a11e673` builds (beta.4 era soft-lock, fixed 2026-08-30) — worth asking which build they played.
**Update 2026-09-14: decision applied** — mate death is now permanent: `Hero.setPermanentDeath` (@LuaInterface) gates the dew vial / LICH / Ankh revives in `Hero.die`; `Chess.lua processLose` sets it around the `pcall(die)` and clears it even if `die` throws.

### A2. Shadow Lord reads Scroll of Weapon Upgrade: UI opens on the player's screen, scroll lost
**Findings:** real bug chain. `ShadowLord` spawns with a ScrollOfWeaponUpgrade (ShadowLord.java:61) and `isHumanoid()=true`. On expert difficulty the mob item AI explicitly whitelists upgrade scrolls as readable in combat (`MobItemAi.scoreItemAction`, MobItemAi.java:151-156, `expertGate(0.3f)` ≥ difficulty 2) and calls `Item.execute(mob, "read")`. `Scroll._execute` **detaches (consumes) the scroll first** (Scroll.java:130), then `InventoryScroll.doRead` opens the real `GameScene.selectItem` UI with the **mob** as selector (InventoryScroll.java:21-33) → modal inventory window over the boss's empty backpack in `UPGRADABLE_WEAPON` mode. Scroll is gone, `onItemSelected` can never run.

**Verdict: BUG.**
**Fix (small):** remove upgrade scrolls from the MobItemAi whitelist (they need interactive selection and are useless to AI); optionally also guard `InventoryScroll.doRead` to hero-only as belt-and-braces.

### A3 / B6. Thrown potions sometimes don't shatter (trap tile; doorway throw at warlock)
**Findings:** two systems read one flag in opposite ways. `Potion.onThrow` (Potion.java:212-225): if the landing cell holds a `LevelObject` with `affectItems()==true` or is a pit → the potion **lands as a heap, unbroken**; otherwise it shatters. Modern traps are LevelObjects and `Trap.affectItems()` returns **true unconditionally** (Trap.java:271-274) — the flag exists so items pressing the cell trigger the trap via `Trap.bump` (Trap.java:121-147, which does fire). Plants, Barrel, script CustomObjects do the same. Terrain traps (old TOXIC_TRAP etc.) shatter potions fine.
Doorway repro (a): the Ballistica diagonal reaches the warlock directly (the mob one cell below is never sampled, Ballistica.java:78-89), so the most likely explanation is the warlock stood on a (possibly hidden) trap → heap branch, trap set off, potion intact. No dodge/miss roll exists for potions at all.

**Verdict: BUG** (one root cause covers both repros).
**Fix (small):** potions always shatter unless the cell is a pit; keep press → `Trap.bump` so traps still trigger. Touch point: Potion.java:218-221.

### A4. Rotberry → Potion of Strength "stopped working" — did we fix it?
**Findings:** no rotberry→strength recipe exists today.
- Legacy alchemy pot is a 3-seed **lottery**, same as 2014 PD: heap must be all-seeds, count ≥ 3 (`Alchemy.transmute`, Alchemy.java:56-100, `SEEDS_TO_POTION=3`), random weighted pick; Rotberry.Seed still carries `alchemyClass = PotionOfStrength` (WandMaker.java:296) but you only get 1 seed per run → effectively never brews.
- New recipe system (`scripts/alchemy_recipes.json`): the only rotberry recipe is `Rotberry.Seed ×1 + VileEssence ×10 → PotionOfMight` (:85-93), and `VileEssence` is obtainable only via the Doctor's harvest loop → for any other hero the recipe is unbuildable. No version of the new data ever produced PotionOfStrength from rotberry.

**Verdict: DESIGN.**
Options: (a) add an explicit recipe `Rotberry ×1 → PotionOfStrength` (restores the remembered mechanic; decide if bare or with a cheap filler); (b) keep PotionOfMight as the only path and open a non-Doctor VileEssence source (see A5); (c) leave as is. Recommend (a).

### A5. "Презренная субстанция" (VileEssence) — "nothing can be brewed from it"
**Findings:** not a dead end in data — it is the hub ingredient: 12 fixed recipes (rat/spider/gnoll armors, gas potions, SacrificialSword, Goo, Zombie×10, PotionOfMight…) **plus every auto-generated necromancy resurrection recipe** (`5×Carcass + N×VileEssence`, AlchemyRecipes.java:118-144). The UI only shows the alchemy action when the player already holds **all** other ingredients of some recipe (Item.java:158-206), so with no carcasses in pack it looks useless. Practical catch: its own inputs (BoneShard + RottenOrgan + ToxicGland) come only from Doctor dissect / BoneSaw crits → for non-Doctor heroes it **is** a dead end in practice.

**Verdict: AS-CODED (explain) + DESIGN** — do we want necromancy reachable for all heroes? If yes, add a general VileEssence source (e.g. dissection as a generic action or a craftable path); if Doctor-only is intended, just leave.

### A6. Plague Doctor starts knowing Healing potion, description doesn't say
**Findings:** `initHeroes.json` DOCTOR `knownItems` = PotionOfHealing + 2 gas potions; the class perks string mentions only the gas potions (`HeroClass_PlagueDoctorPerks_3`, values-ru/strings_all.xml:985-989, EN mirrored). Necromancer documents his identical Healing start (NecromancerPerks_4). Note: "knows the recipe" is actually potion **identification** — no per-hero recipe-knowledge system exists anywhere.
**Verdict: DOC. Fix: one-line perk string, EN+RU.**

### A7. DnD-style class names for custom heroes
**Proposal (player's):** keep the original tradition — original heroes are DnD classes; name ours the same way: Elf → Лучник/Archer, Gnoll → Следопыт/Ranger, Plague Doctor → Лекарь/Physician.
**Verdict: DESIGN** (naming, Mike's call). Mechanically string-only: hero enum keys (`ELF`, `GNOLL`, `DOCTOR`) and saves are untouched; it's display names + maybe perk text. Also affects wiki/announces.

### A8. Doctor epic armor already includes a mask — why also an accessory mask?
**Verdict: DESIGN, not yet investigated** (epic Doctor armor/mask set mechanics need a code pass before options can be drawn). Player suggestion: fold gas resistance into the epic armor, free the accessory slot.

### A9. Surgical saw: visible damage + upgradeable
**Verdict: SUGGESTION.** BoneSaw is a lua CustomItem (`scripts/items/BoneSaw.lua`); harvest-crit loop at :81-91. Easy mechanically (authored stats). Needs: damage number, tier, upgrade behavior.

### A10. Remains should match the enemy (snails without bones, skeletons bones-only, phantoms nothing)
**Findings:** `Carcass` is one generic class for all mobs (Carcass.java:253-259), dropped at flat `carcassChance = 0.5` unless a mob opts out (Mob.java:418-428, 77). ~20 mobs already opt out (Mimics, Shadow, Wraith, elementals, Piranha, Statue, Succubus, skulls/souls, SpiderExploding…) — but Snail and Skeleton are not among them. Dissect/BoneSaw harvest is mob-agnostic: fixed random trio {ToxicGland, RottenOrgan, BoneShard} (Carcass.java:165, BoneSaw.lua:84-90).
**Verdict: SUGGESTION** (data work). Sketch: authored per-mob carcass content/chance table (json, per prefer-authored-stats), dissect picks from it; phantoms/wraiths already covered by the opt-out list.

**Bonus incidental bug found while probing:** `AlchemyRecipes.loadRecipesFromJson` validates ingredients/outputs with `break` but then `getRecipes().add(...)` runs unconditionally (AlchemyRecipes.java:99-107) — invalid recipes would still register. Tiny fix, current data unaffected.

---

## B. beta.8 minion reports

### B1. Moongrace copy of a necromanced mob becomes "normal" (gains exp)
**Findings:** bug confirmed. Moongrace copies via `Mob.split` → `makeClone` (Moongrace.java:36-52, Mob.java:438-456, 652-668). `makeClone` serializes through a bare `storeInBundle` into a fresh Bundle — which **skips all `@Packable` fields**, so the clone loses `undead` and `expForKill`. Consequences: `Char.earnExp` undead gate no longer fires (Char.java:2233-2236) → clone levels up and regenerates like a normal mob; `expForKill` resets to 1 → killing the clone even grants hero exp. Fraction/pos/ownerId are copied manually, so the copy stays friendly. Necromancy itself is unaffected (`Carcass.reanimate` re-applies `setUndead(true)` after cloning, Carcass.java:226).
**Verdict: BUG** (player "not sure it's a bug" — it is).
**Fix (small):** copy `undead` + `expForKill` explicitly in `makeClone`, next to the existing manual `pos`/`ownerId` copies — targeted, does not change clone semantics for other callers.
**Update 2026-09-14: shipped + follow-up ruling by Mike** — Moongrace no longer duplicates undead mobs at all: moonlight full-heals a necromanced mob instead of splitting it; living mobs still clone as before.

### B2. Two-handed weapon vs occupied offhand: "can't wear both" spam every turn
**Findings:** minion idle AI tries to equip every Wandering turn (`Wandering.java:23-26` → `MobItemAi.tryUseItem`, IDLE). `autoEquipScore` for weapons compares only MAX vs current weapon and **never checks `blockSlot()`/offhand occupancy** (MobItemAi.java:173-175, 280-289). `Belongings.equip` rejects the conflict with `GLog.w(Belongings_CantWearBoth)` (Belongings.java:716-727), spends no time, AI retries forever: two log lines per turn, pet burns turns. No unequip-first path exists anywhere (hero, AI, pet UI).
**Verdict: BUG + DESIGN** (player proposes: equipping a 2H weapon auto-removes both hand items).
Options: (a) auto-unequip conflicting slot in `Belongings.equip` for **everyone** (matches the request, changes hero UX too); (b) AI-only: `autoEquipScore` returns 0 when the blocked slot is occupied → no spam, no auto-unequip; (c) both — auto-unequip semantics + AI stops retrying pointlessly. Recommend (c); the message then only appears when something actually gets swapped.

### B3. Minion equips an unidentified claymore (20 STR) while having 18 STR
**Findings:** layered. (1) `Belongings.equip` enforces **no STR at all** — for anyone (equipping over STR = penalties, PD design). (2) The gates that do exist use the **real** requirement regardless of identification: AI `autoEquipScore` (MobItemAi.java:276) and pet-UI `statsRequirementsSatisfied` (EquipableItem.java:203-205) — that's the "minions know item properties" feel. Displayed stats for unidentified are `typicalSTR()` ("20?"). (3) Pet `effectiveSTR = baseStr + lvl()/5` (Char.java:1876-1878) — the stat window shows it, but the player can't tell an "18 STR" pet actually passes a 20 gate at high level.
**Verdict: BUG (information leak), not an equip exploit.**
**Fix:** gate AI equip decisions and pet-UI requirement indicators on the **displayed** stat (`isLevelKnown ? requiredSTR : typicalSTR`). Keep over-STR equip-with-penalty as is.

### B4. All minions teleport to the hero on re-entering the game
**Findings:** explicit design, not corruption. Hero pets always follow level changes (`Mob.followOnLevelChanged`, Mob.java:165-168); on load/return `Dungeon.spawnPet` plants them at the nearest spawnable cell to the hero, saved position never consulted (Dungeon.java:414-430); the stale in-level copy is deleted first (Dungeon.java:361-375). Pets live in the game bundle, never in level saves (Dungeon.java:489-509, Level.java:815-825).
**Verdict: DESIGN.** Options: keep follow-everywhere, or restore pets to their saved cell when re-entering the **same** level (needs per-level pet position persistence — medium work). Recommend same-level restore, cross-level follow stays.

### B5. Shield upgrades: no STR-req change; blocking?
**Findings:** shields are lua items with per-tier constants (`scripts/lib/shields.lua`). Upgrade level **does** multiply blocked damage ×1.3 (shields.lua:27-29, ShieldLeft.lua:66-72), but block chance and STR requirement are frozen per tier (shields.lua:33-37, 74-79) — unlike armor (`requiredSTR = max(typical − level, 2)`, Armor.java:236-237) and melee weapons (−1 STR per upgrade, MeleeWeapon.java:47-49).
**Verdict: DESIGN + small fix.** Recommend: `requiredSTR = max(2, base − level)` like armor, and show block numbers in the item desc so the ×1.3 is visible.

### B7. Swap with a minion standing on a corpse cell silently fails
**Findings:** heap under the pet shadows the swap. Tap dispatch consults `handleObjectOrHeap` **before** the friendly-Interact branch (`CharUtils.actionForCell`, CharUtils.java:239-241, 289-313): a fresh kill leaves a heap (Carcass via Mob.die:418-428 + dropAll). Carcass on top → `MapItemAction` opens an item info window and idles — the "nothing happened". Loot on top → `PickUp` path (accidental swap only as pathing side effect).
**Verdict: BUG.**
**Fix (small):** when the cell holds a friendly char, `Interact` wins over heap actions. Tradeoff: tapping the pet's cell no longer picks up its loot (walk onto it instead) — acceptable, mention in changelog.

### B8. Gnoll + signposts: "Ты не можешь рассмотреть текст отсюда"
**Findings:** two defects. (1) `Level_TileDescSign` (values-ru/strings_all.xml:1256) is the generic SIGN tile description shown to **every** hero examining the sign at any distance — a nonsense string (Level.java:1748, Sign.java:80-82). (2) For an illiterate hero `Sign.interact` silently no-ops (`forbidden(AC_READ)`, Sign.java:46-55); no "you can't read" string exists anywhere in the tables.
**Verdict: DOC ×2 + small fix.** Proper sign tile description + explicit illiteracy message on interact. Wording to pick (RU proposal: «Ты не умеешь читать.»).

---

## Status (updated 2026-09-14)

**SHIPPED on beta.8:** A1 permanent death (f9fb7f3ce), A2 (9a8c95b1e), A3/B6 (fb5de3c9a), B1 (8a81ad382), B7 (8de307077), B8+A6 (8c914de94), AlchemyRecipes validation nit (cb996a6a5).

**Decision-gated (open):**
- B2 — auto-unequip semantics (a/b/c above; recommend c)
- B3 — display-stats gating direction (recommend yes)
- B4 — same-level pet position restore (recommend yes)
- B5 — shield STR req per level (recommend yes)
- A4 — rotberry recipe shape (recommend Rotberry×1 → PotionOfStrength)
- A5 — general VileEssence source or Doctor-only necromancy
- A7 — final class names EN+RU
- A8 — epic armor mask merge (needs code pass first)
- A9 — BoneSaw stats authoring
- A10 — per-mob carcass table (data work)
