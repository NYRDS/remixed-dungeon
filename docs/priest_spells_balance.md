# Priest spells — balance review vs other classes — 2026-11-09

Follow-up to [priest_spells.md](priest_spells.md). Sources: spell scripts, `HeroClass.java`
perk procs, `SpellFactory`/`Spell` mechanics, `initHeroes.json`. No gameplay was changed.

## Frame: who can actually cast

- SP pools (`initHeroes.json`, merged via `initCommon` then the class block): **all classes
  10/10** from the `common` entry; **MAGE 15/15**, **PRIEST 12/12**. Every affinity set is live
  for its hero.
- SP refill: +1 maxSp and +maxSp/3 SP per hero level; PotionOfMana/ManaPotion restore maxSp/3.
  No passive regen. Sanctify also draws from this pool (maxSp/3 = 4 SP at start).
- Availability gate: `WndHeroSpells` hides spells with `level > skillLevel`; skillLevel starts
  1 and grows only via TomeOfKnowledge (+1, town shop). Priest spans lv1–4 → 3 tomes for the
  full kit, same curve as everyone else's top spells (SummonBeast lv4/20 SP, Possess lv4/15 SP,
  RaiseDead lv4/15 SP — those exceed the 10 SP pool at start, priest's Last Rites 10/12 fits
  the "top spell eats most of the pool" pattern).

## Kit comparison (damage per SP at skill k=1, non-undead target)

| Set (hero) | Universal damage | Conditional damage | Control/utility | Heal | Mobility |
|---|---|---|---|---|---|
| **Priest 12** | **none** | Holy Water 0 (!), Exorcism 1.4→2.9 undead, Last Rites 0 | Blessing (+1/+1 30t, 4SP) | Heal | none |
| Elemental (mage 15) | WindGust 2/SP + push, Ignite DOT 2SP | — | Roots 10t (2SP), Frost+Slow 10t (4SP) | — | — |
| Combat (warrior 10) | Smash weapon-roll AoE (10SP) | — | Vertigo (Smash, Dash) | — | Dash 10SP |
| Rogue (10) | KunaiThrow (5SP) | Backstab | Cloak invis k×5 turns (5SP) | — | Cloak, Haste (refund k/2 turns) |
| Huntress (10) | ShootInEye √k×weapon (5SP) | — | Calm sleep 2SP, Charm 3SP | — | — |
| Elf (10) | **MagicArrow random(1, caster.ht/2), 5SP, cd1** — avg ~20 at ht 80 | — | Sprout regrowth, NatureArmor | — | HideInGrass |
| PlagueDoctor (10) | — | CorpseExplosion corpse-scaled gas (9SP) | Anesthesia sleep 10k (6SP) | BloodTransfusion self-drain | — |
| Necromancy (10) | — | DarkSacrifice corpse→fire 3SP | Possess 15SP, RaiseDead 15SP | — | — |
| Common (books) | — | — | TownPortal 3SP | Healing java 10SP | TownPortal |

Priest is the **only hero set with no universal offensive spell**: Holy Water and Last Rites
deal literally nothing to non-undead (the cast still succeeds, steam/light particles play —
it looks like a bug to a player fighting gnolls). Against undead Exorcism is the best nuke in
the game per SP (2.9→6.3 dmg/SP), so the class is elite in undead-heavy places and
buff-and-stick elsewhere.

Verified facts the suggestions rely on:

- The melee perk (+25% undead / +12.5% demon damage, −25%/−12.5% taken) is implemented in
  `HeroClass.attackProc`/`charGotDamage` — melee only; spells carry their own multipliers.
- Holy Water's splash closure explicitly skips every victim that is not undead
  (`scripts/spells/HolyWater.lua`); Last Rites likewise.
- Heal is in the Priest window list, but its script declares `magicAffinity = "Witchcraft"`
  (metadata only — grouping is driven by `CustomSpellsList.lua`) and its icon sits on
  `witchcraft.png`. The perk text promises exactly 4 divine spells, without Heal.
- Blessed buff scaling is inconsistent between sources: Blessing sets level `1 + skill/3`,
  Sanctify sets `1 + hero lvl/5`.

## Suggestions (smallest change first; all are lua/content-side, no engine edits)

1. **Holy Water — universal splash component.** Add `1 + skill` damage for every hostile in
   the 3×3, keep `4 + 3×skill` + Slow 3 as the undead bonus. Cost stays 5. Removes the
   dead-card problem without touching the undead ceiling.
2. **Exorcism — demon clause.** `elseif target:isDemon() then damage = damage + damage/4`
   (mirrors the melee perk's +12.5% demon line; no Slow). Brings the spell layer in line with
   the perk text, which already promises demon interaction.
3. **Last Rites — universal ring.** Add `2 + skill` damage to *all* hostiles in radius 2
   (undead keep `5 + 5×skill` + Cripple). Keep 10 SP — costing most of the pool is the
   established top-tier pattern; the problem is conditionality, not the price.
4. **Heal identity — pick one:**
   - (a) make it divine: new frame on `spellsIcons/priest.png` (16×16, format documented in
     priest_spells.md; frames 0–3 taken, sheet has room), set `magicAffinity = "Priest"` in
     Heal.lua (cosmetic), perk text gains Heal;
   - (b) drop Heal from the Priest list to match the perk text.
   Lean (a): healing is the priest's core identity (the Bishop sells heals; legacy WndPriest),
   and the shared Heal would then read correctly in every list it appears in.
5. **One new utility spell to close the universal gap** (optional; slot f4/f5 on priest.png is
   free):
   - *Sanctuary* — cell, lv3, 6 SP, cd 20: hostiles within r2 get Vertigo 3 and are pushed a
     cell away. Universal (works on beasts/constructs/demons), defensive, orthodox bell icon.
   - *Purify* — self, lv2, 4 SP, cd 30: strip harmful buffs, short Blessed. Weaker — overlaps
     potions/scrolls.
   Lean Sanctuary: the gap is offensive/defensive tools vs non-undead, not cleansing.
6. **Blessing curve** (optional): level `1 + skill/2` instead of `1 + skill/3` (+1/+1 at k1
   stays, k4 goes +2→+3). Its peers scale flat or faster (BodyArmor sets buff level = k
   outright).
7. **Consistency nit:** make Sanctify's Blessed use `skillLevel` like Blessing does, so one
   stat drives the whole kit (currently hero level drives the armor's version).

## Observed outlier outside priest (flag for a separate pass)

`MagicArrow` (Elf, 5 SP, cd 1, castTime 0.1) deals `random(1, caster.ht/2)` — it scales off the
caster's max HP, not skill, and lands ~4 dmg/SP unconditional with a per-turn cooldown. At
lv-30 HP pools that is several times anything else in the table. Suggest capping by skill
(e.g., `random(1, 2 + 2×skill)`) whenever the next cross-class balance pass happens.
