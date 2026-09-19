# Beta.10 feedback round — triage findings (2026-09-19)

Raw player feedback relayed 2026-09-19 (two beta.9 items + six beta.10 items + one suggestion), triaged against `beta.8` @ d61b790de = 32.4.beta.10.
Verdicts: **BUG** / **AS-CODED** (works as designed, explanation attached) / **DESIGN** (needs a call) / **SUGGESTION** (feature) / **NEEDS-INFO**. Nothing changed yet — decision sheet. Note: **none of the beta.9 items were touched by beta.10** — both are alive in beta.10 builds.

---

## Beta.9 items

### B9-1. "Amulet-mimic attacked, chaos shield effect should have fired, but an error appeared"
**Findings:** the pasted crash log is **not a chaos-shield crash** — it is the PlagueDoctorMask unequip crash, fatal on every take-off of the mask:

```
LuaError: scripts/items/PlagueDoctorMask.lua:35 coercion error:
Belongings.getItemFromSlot argument 1 has type Belongings$Slot, got java.lang.String
```

`RPD.Slots.*` are plain strings by design (`commonClasses.lua:212-219`, `armor = "ARMOR"`), but `Belongings.getItemFromSlot` only takes the `Slot` enum (`Belongings.java:69`). The stack is a plain inventory click: `WndItem` → `UseItem` → `EquipableItem.doUnequip` → `Belongings.removeItem` → `CustomItem.deactivate` → mask script. Line 35 was added in the beta.8 doctor-armor round (`04ab87c01`, armor-carries-mask GasesImmunity fix) — the deactivate path shipped untested; **beta.8-round regression, present in beta.8/9/10**. `Char.getItemFromSlot(String)` exists and works (`Char.java:2115`, used by `scripts/actors/town/Compass.lua:23`) — the script just called it one hop too deep.
The chaos shield itself never touches equipment: its block effects are heal / damage / clone / buff / curse / sheep (`scripts/buffs/ChaosShieldLeft.lua:149-155`). If the player saw a *different* error at mimic-attack time, we need that text; the pasted one is fully explained by a mask unequip.

**Verdict: BUG (crash, highest priority).**
**Fix (one line, lua):** `scripts/items/PlagueDoctorMask.lua:35` → `hero:getItemFromSlot(RPD.Slots.armor)` (goes through the Char string overload). Optional hardening: add a `getItemFromSlot(String)` overload to `Belongings` so the deep form can't blow up again.

### B9-2. Chaos shield duplicated the boss (Shadow Lord); killing the copy counted as boss victory and unlocked stairs
**Findings:** real. `ChaosShieldLeft.defenceProc` rolls one of six block effects; `cloneEnemy` = `enemy:split(cell, 0)` has **no boss guard** (`scripts/buffs/ChaosShieldLeft.lua:91-96`), while `turnToSheep` right below does check `enemy:isBoss()` (:98-101). `Mob.split` (`Mob.java:450-468`) clones the same class, so the copy is a full `ShadowLord` — `Boss` ctor sets `isBoss=true`, and it keeps the twist-level/crystal AI. Killing the clone runs `ShadowLord.die` (`ShadowLord.java:222-229`): maze cleanup + `level().unseal()` + `Badges.validateBossSlain(SHADOW_LORD_SLAIN)` → stairs unlock + badge. Exactly the report.

**Verdict: BUG.**
**Fix (small, lua):** add the same `if enemy:isBoss() then return end` guard to `cloneEnemy`. Decision: also guard `Mob.split` engine-side for `isBoss` (would cover Moongrace/glyph paths) or lua-only? Recommend lua-only for now — split-on-boss could be a wanted chaos flavor later, and engine-level touches the pet/split family (mod-sweep territory).

## Beta.10 items

### B10-1. Missile thrown at a trap flies through an obstacle (mob or closed door) next to the trap
**Findings:** `Item.cast` (`Item.java:617-625`) casts a Ballistica to the target, then snaps: `if (level.distance(cell, dst) == 1 && topLevelObject(dst).affectItems()) cell = dst`. Traps are LevelObjects with `affectItems()==true`, and Ballistica's blocked stop sits exactly one cell short of the target: a mob → returns the mob's cell (`hitChars`), a closed door → DOOR is `PASSABLE|LOS_BLOCKING|SOLID` so the `losBlocking` branch returns the door cell (`TerrainFlags.java:31`). The snap then teleports the projectile onto the trap through the obstacle. The snap is old code (8fdb863e3, 2024-06-03) — it just became visible now that players throw at traps deliberately (beta.8 potions-vs-traps round made trap-throwing mainstream).
**Verdict: BUG.**
**Fix (small):** only snap when the path wasn't actually blocked: add `Actor.findChar(cell) == null && !level.losBlocking[cell]` to the snap condition (a natural Bresenham stop lands on plain passable floor; a blocked stop lands on a char or a losBlocking cell). Keeps the 2024 intent (rounding-miss throws still land on the trap) and the beta.8 potion behavior untouched.

### B10-2. Warning dialog dismissed by clicking outside → inventory stays dead until reopened
**Findings:** `MissileWeapon.doEquip` (`MissileWeapon.java:86-110`): `wndBag.setItemsActive(false)` disables the item list, then a `WndOptions` confirm is shown **inside** the bag window; the re-enable (`wndBag.updateItems()`) lives **only in `onSelect`**. `Window`'s outside-click blocker closes a dialog via `onBackPressed → hide()` (`ui/Window.java:56-66, 118-124, 150-151`) — `onSelect` never fires → list stays disabled. Exactly the report (any ranged-weapon equip warning; reproducible every time).
**Verdict: BUG.**
**Fix (tiny):** in that anonymous dialog override `hide()` to `wndBag.setItemsActive(true)` before `super.hide()` (keep the `updateItems()` in `onSelect`; hide() covers both paths). Worth a quick grep for other `setItemsActive(false)` callers with the same shape.

### B10-3. Killed a snail → world stopped reacting to clicks (UI alive); after reenter, several DummyItem errors. Screenshot 2 is a 14×32 px thumbnail — unreadable
**Findings:** no log text available, but the suspect area is strong: the beta.8 Doctor-round **carcass machinery**. Default `carcassChance = 0.5` (`Mob.java:81`) → the snail death very likely dropped a "Тушка улитки" item. `Carcass` is both an item and a registered Actor (`Actor.add(this)` in the ctor, `Carcass.java:55-57`) holding `@Packable Char src` — a reference to the mob that is destroyed the moment it dies (`Carcass.java:44`). A per-tick exception from that actor/heap pair would freeze the turn loop while HUD keeps working; on save/reload the stale carcass in a heap fails to restore into DummyItem-shaped errors. Supporting coincidence: ItemFactory's broken-carcass fallback literally builds `new Carcass(new CustomMob("Snail"))` (`ItemFactory.java:466-477`) — the Snail name is baked into this failure path.
**Update 2026-09-19:** the reattached "screenshot 2" turned out to be the B10-6 kobold scene, not the snail error — still no readable evidence for this item. Need the error dialog text (or the save) from the player.
**Verdict: BUG (high confidence in the area), NEEDS-INFO for the exact throw.**
**Next:** ask the player for the error dialog text (or the save). Headless repro planned: spawn snail → kill → confirm carcass drop → save/load → read log.

### B10-4. Thrown weapons don't reveal mimics and get absorbed (boomerang case)
**Findings:** a hidden mimic is a `Heap.Type.MIMIC`. `Item.cast` resolves `enemy = Actor.findChar(cell)` → null (a heap is not a char) → the missile "misses", `onThrow → dropTo → Heap.drop` inserts the weapon into the mimic's item list (`Heap.java:232-259`) — eaten until the mimic dies (its belongings drop on death, hence the boomerang coming back). Reveal paths exist only for open/burn/freeze/poison (`Heap.java:145-153, 293-302, 338-345, 359-365`); nothing reveals on being hit.
**Verdict: BUG (design gap).**
**Options:** (a) minimal — any item landing on a MIMIC heap reveals it (spawn-at like `open()` does); the thrown item still lands in the revealed mimic's loot; (b) fuller — MissileWeapon hits reveal AND strike the spawned mimic (normal damage roll), boomerang returns on reveal. Recommend (a) now; (b) if you want the juice.

### B10-5. Potion thrown into Well of Knowledge shatters instead of being identified; well effect fired as if the hero drank
**Findings:** the well is a `WaterOfAwareness` blob + a `well.lua` LevelObject with `affectItems = true`. For **normal** items the flow works: land → heap → `Level.press` → `WellWater.affect()` heap branch → `affectItem` identifies + water consumed (`WellWater.java:72-91`), leftovers tossed out of the well by the lua. **Potions override `onThrow`** (`Potion.java:212-227`, the beta.8 potions-vs-traps fix): it calls `lo.bump(potion)` **before any heap exists** — the well finds no heap, `affect()` returns false, water stays — then unconditionally `shatter(cell)` → potion wasted. The "as if the hero drank" impression is the bump animation, not an actual consume.
**Verdict: BUG (ordering), entangled with the beta.8 trap fix — traps must keep "react but shatter".**
**Fix options:** (a) in `Potion.onThrow`, for well-type objects land the potion as a heap first, then bump (identify consumes water, lua throws the identified potion out of the well); traps keep the current path via their own branch. (b) lua-only: `well.lua bump` already receives the potion as `presser` — identify it there and flag the shatter off, needs a small flag plumbed back to `onThrow`. Recommend (a). Decision: should the same landing apply to WaterOfHealth / WaterOfTransmutation (potion healed/transmuted instead of broken)?

### B10-6. Kobold icemancer drank a healing potion and hit the hero the same turn, though it "had no way to attack" (no shot either)
**Findings:** two parts.
*The drink is legal and not free:* `MobItemAi` scores PotionOfHealing in COMBAT by hp deficit (`MobItemAi.java:98-103`) — the potion was in its belongings — and `Potion.drink` spends a full turn (`doOperate(TIME_TO_DRINK)` = 1.0, `Potion.java:40, 195-205`). No double action in code. The reattached screenshot (kobold scene, log: «использует Зелье Исцеления» → «ударил тебя» → «Ты ударила кобольда») is fully explained by normal actor-time interleaving: after the drink (+1.0) the kobold's next activation came up no later than the hero's next swing (weapon `attackDelay()` ≥ 1.0, tie at equal timers processed toward the mob) — so the player saw drink→hit with no own action in between and read it as "same turn". The hero hit back only after.
*The impossible hit is real:* `KoboldIcemancer.canAttack` is `Ballistica.cast(...) == enemyPos` with **no distance cap** (`KoboldIcemancer.java:41-43`), and `Level.distance` is Chebyshev (`Level.java:1612-1618`). At diagonal distance 1 the single Bresenham step lands directly on the hero — the two flanking corner walls are never sampled → `canAttack` true **through a diagonal wall corner**. The screenshot geometry (kobold in the left arm of a T-junction, hero at the junction head, corner post between) matches. `doAttack` with distance ≤ 1 plays the *melee* animation (`Char.java:1234-1239`) — hence "hit me, didn't shoot". Same quirk exists for every mob (`Char.canAttack`: adjacent → true, `Char.java:1977-1985`) but is glaring for a caster that otherwise zaps.
**Verdict: BUG (corner-bleed), long-standing engine quirk.**
**Decision:** (a) keep as classic-PD diagonal quirk (vanilla has diagonal melee too) and only document; (b) make `canAttack` corner-safe (block diagonal adjacency when both flanking cells are blocked) — affects all melee mobs, needs a balance pass + mod sweep. Lean (b) but it's a behavior change across the board — your call.

### S1. SUGGESTION (player): spiders in the spider nest under Солнечник should heal allies instead of attacking the hero
"Солнечник" = **Sungrass** (`Sungrass_Name`, values-ru strings_all.xml:2241). Idea: spiders standing on Sungrass foliage in the SpiderLevel heal nearby nest allies instead of pressing the attack. Feature shape: foliage-presence check in the spider AI / Sungrass script branch ("ally heal" behavior for nest spiders only?). Recorded, not triaged further — needs a design note (trigger, duration, balance) if you want it.

---

## Summary

| # | Item | Verdict | Fix size |
|---|------|---------|----------|
| B9-1 | PlagueDoctorMask unequip crash | BUG (crash) | one line lua |
| B9-2 | Boss cloned by chaos shield, clone-kill = victory | BUG | small lua guard |
| B10-1 | Throw-at-trap flies through obstacle | BUG | small java guard |
| B10-2 | Outside-click leaves inventory disabled | BUG | tiny java |
| B10-3 | Snail-kill freeze + DummyItem on reload | BUG (NEEDS-INFO) | carcass area, repro pending |
| B10-4 | Mimic absorbs thrown weapons, no reveal | BUG (design gap) | small, option (a)/(b) |
| B10-5 | Potion into Well of Knowledge breaks | BUG (ordering) | medium, option (a)/(b) |
| B10-6 | Kobold melee through diagonal corner | DESIGN (quirk) | decision |
| S1 | Sungrass spiders heal allies | SUGGESTION | design note |

**Decisions needed from Mike:** B10-4 (a/b), B10-5 (a/b + other wells?), B10-6 (keep quirk vs corner-safe), B9-2 (lua-only vs engine split guard), S1 (adopt?).
**Needs from player:** B10-3 error text/save.
