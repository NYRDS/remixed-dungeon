# Mob Item AI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give humanoid mobs utility-scored item usage during combat (potions, scrolls, equipment, wands, SpellBook) via a new `MobItemAi` class wired into the Hunting/Fleeing/Wandering AI states.

**Architecture:** A new `MobItemAi` class holds the scoring loop. Each AI state calls `MobItemAi.tryUseItem(mob, context)` before its normal behavior. If the call returns `true`, the mob's turn was consumed. Scoring is a hybrid: Java default scorer handles known items by entity kind, a Lua hook (`scoreItemAction`) on the mob's script can override per (item, action) pair.

**Tech Stack:** Java (core game), Lua (modder override hooks via `LuaScript`)

**Spec:** `docs/superpowers/specs/2026-07-13-mob-item-ai-design.md`

---

## File Structure

### New files

- `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/MobItemAi.java` — Central scorer. Exposes `tryUseItem(Mob, Context)`. Contains `Context` enum, scoring loop, Java default scorer, Lua hook check.

### Modified files

- `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/Hunting.java` — Call `tryUseItem(Context.COMBAT)` after enemy validation, before attack.
- `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/Fleeing.java` — Call `tryUseItem(Context.FLEEING)` before movement.
- `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/Wandering.java` — Call `tryUseItem(Context.IDLE)` before movement.
- `scripts/lib/mob.lua` — Add `scoreItemAction` hook stub so modders can override.

### Key reference files (do NOT modify)

- `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/items/Item.java:242` — `execute(Char, String)` performs the action.
- `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/mechanics/CommonActions.java` — Action string constants (`AC_DRINK`, `AC_READ`, `AC_EQUIP`).
- `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/items/Item.java:83` — `AC_THROW = "Item_ACThrow"` (protected).
- `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/items/wands/Wand.java:44` — `AC_ZAP = "Wand_ACZap"` (private).
- `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/mechanics/LuaScript.java:113` — `hasMethod(String)`, `runOptional(method, defaultValue, args...)`.
- `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/items/artifacts/SpellBook.java:33` — `spell()` returns the contained `Spell`.
- `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/mechanics/spells/Spell.java:58` — `canCast(Char, boolean)`, `targetingType` field (protected — accessed via `SpellBook.spell()` return).
- `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/mechanics/spells/SpellHelper.java:22` — Targeting type constants (`TARGET_SELF`, `TARGET_CELL`, `TARGET_CHAR`, etc.).
- `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/hero/Belongings.java:54` — `implements Iterable<Item>`, iterates backpack + equipped.
- `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/Char.java:2262` — `getScript()` returns the mob's `LuaScript`.
- `RemixedDungeon/src/android/java/com/nyrds/platform/game/RemixedDungeon.java:223` — `getDifficultyFactor()` returns float.

---

## Task 1: Create MobItemAi skeleton with Context enum and gate

**Files:**
- Create: `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/MobItemAi.java`

- [ ] **Step 1: Write the skeleton class**

```java
package com.nyrds.pixeldungeon.ai;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.ArrayList;

public class MobItemAi {

    public enum Context {
        COMBAT,
        FLEEING,
        IDLE
    }

    // Score threshold — below this, don't use the item.
    private static final float USE_THRESHOLD = 0.1f;

    // Action string constants (match the private/protected fields on Item/Wand).
    private static final String AC_THROW = "Item_ACThrow";
    private static final String AC_ZAP   = "Wand_ACZap";

    /**
     * Attempt to use an item from the mob's belongings.
     * Returns true if an item was used (turn consumed), false otherwise.
     */
    public static boolean tryUseItem(Mob mob, Context context) {
        if (!mob.isHumanoid()) {
            return false;
        }

        Item bestItem = null;
        String bestAction = null;
        float bestScore = 0;

        for (Item item : mob.getBelongings()) {
            ArrayList<String> actions = item.actions(mob);
            for (String action : actions) {
                float score = scoreItemAction(mob, item, action, context);
                if (score > bestScore) {
                    bestScore = score;
                    bestItem = item;
                    bestAction = action;
                }
            }
        }

        if (bestItem != null && bestScore > USE_THRESHOLD) {
            GLog.debug("MobItemAi: %s uses %s (%s) score %.2f",
                    mob.getEntityKind(), bestItem.getEntityKind(), bestAction, bestScore);
            bestItem.execute(mob, bestAction);
            return true;
        }

        return false;
    }

    private static float scoreItemAction(Mob mob, Item item, String action, Context context) {
        // Placeholder — implemented in Task 2.
        return 0;
    }
}
```

- [ ] **Step 2: Build to verify it compiles**

Run: `cd /home/mike/StudioProjects/remixed-dungeon && ./gradlew :RemixedDungeon:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/MobItemAi.java
git commit -m "feat: add MobItemAi skeleton with scoring loop and Context enum"
```

---

## Task 2: Add targetingType() getter to Spell.java

**Files:**
- Modify: `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/mechanics/spells/Spell.java`

`Spell.targetingType` is `protected` and `MobItemAi` is in a different package (`com.nyrds.pixeldungeon.ai`). The scorer needs to read the targeting type to categorize SpellBook spells (self-healing vs enemy-targeting vs utility). Add a one-line public getter — this is the minimal change possible, no behavior change.

- [ ] **Step 1: Add the getter**

In `Spell.java`, after the existing `getMagicAffinity()` getter pattern (around line 37, after the `@Getter` annotation on `magicAffinity`), add:

```java
    @LuaInterface
    public String targetingType() {
        return targetingType;
    }
```

Place it right after the `magicAffinity` field's getter block (after line 37). The `@LuaInterface` annotation also makes it accessible to Lua modders.

- [ ] **Step 2: Build to verify**

Run: `cd /home/mike/StudioProjects/remixed-dungeon && ./gradlew :RemixedDungeon:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/mechanics/spells/Spell.java
git commit -m "feat: expose Spell.targetingType via public getter for AI scoring"
```

---

## Task 3: Implement the Java default scorer

**Files:**
- Modify: `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/MobItemAi.java`

This task replaces the `scoreItemAction` placeholder with the full scoring logic. The scorer dispatches by item entity kind and action, applies context filters, and handles expert-only gating.

- [ ] **Step 1: Replace the placeholder `scoreItemAction` with the full implementation**

Replace the entire `scoreItemAction` method (and remove the `Packable`/unused imports if needed) with:

```java
    private static float scoreItemAction(Mob mob, Item item, String action, Context context) {
        // 1. Lua override — checked first.
        float luaScore = luaScore(mob, item, action);
        if (luaScore > 0) {
            return luaScore;
        }

        // 2. Java default scorer — dispatch by entity kind.
        String kind = item.getEntityKind();
        switch (kind) {
            // --- Survival (COMBAT, FLEEING) ---
            case "PotionOfHealing":
                if (action.equals(CommonActions.AC_DRINK)
                        && (context == Context.COMBAT || context == Context.FLEEING)) {
                    return survivalHealScore(mob);
                }
                return 0;

            case "PotionOfPurity":
                if (action.equals(CommonActions.AC_DRINK)
                        && (context == Context.COMBAT || context == Context.FLEEING)) {
                    return survivalPurityScore(mob);
                }
                return 0;

            case "ScrollOfTeleportation":
                if (action.equals(CommonActions.AC_READ) && context == Context.FLEEING) {
                    return escapeTeleportScore(mob);
                }
                return 0;

            // --- Offensive (COMBAT only) ---
            case "PotionOfLiquidFlame":
                if (action.equals(AC_THROW) && context == Context.COMBAT) {
                    return offensiveThrowScore(mob, 0.6f);
                }
                return 0;

            case "PotionOfToxicGas":
                if (action.equals(AC_THROW) && context == Context.COMBAT) {
                    return offensiveAreaThrowScore(mob, 0.5f);
                }
                return 0;

            case "PotionOfFrost":
                if (action.equals(AC_THROW) && context == Context.COMBAT) {
                    return offensiveAdjacentThrowScore(mob, 0.4f);
                }
                return 0;

            case "ScrollOfTerror":
            case "ScrollOfPsionicBlast":
                if (action.equals(CommonActions.AC_READ) && context == Context.COMBAT) {
                    return offensiveAreaThrowScore(mob, 0.5f);
                }
                return 0;

            // --- Buffs (COMBAT only, expert-gated) ---
            case "PotionOfStrength":
            case "PotionOfMight":
                if (action.equals(CommonActions.AC_DRINK) && context == Context.COMBAT) {
                    return expertGate(0.3f);
                }
                return 0;

            case "ScrollOfUpgrade":
            case "ScrollOfWeaponUpgrade":
                if (action.equals(CommonActions.AC_READ) && context == Context.COMBAT) {
                    return expertGate(0.3f);
                }
                return 0;

            case "ScrollOfRecharging":
                if (action.equals(CommonActions.AC_READ) && context == Context.COMBAT) {
                    return rechargeScore(mob);
                }
                return 0;

            // --- Safe self-use scrolls (low priority, any combat) ---
            case "ScrollOfIdentify":
            case "ScrollOfRemoveCurse":
                if (action.equals(CommonActions.AC_READ) && context == Context.COMBAT) {
                    return 0.1f;
                }
                return 0;

            // --- Equipment (IDLE only) ---
            default:
                if (action.equals(CommonActions.AC_EQUIP) && context == Context.IDLE) {
                    return autoEquipScore(mob, item);
                }
                // SpellBook and Wand scoring.
                if (action.equals(CommonActions.AC_READ) && item instanceof com.nyrds.pixeldungeon.items.artifacts.SpellBook) {
                    return spellBookScore(mob, (com.nyrds.pixeldungeon.items.artifacts.SpellBook) item, context);
                }
                if (action.equals(AC_ZAP) && item instanceof com.watabou.pixeldungeon.items.wands.Wand) {
                    return wandScore(mob, (com.watabou.pixeldungeon.items.wands.Wand) item, context);
                }
                return 0;
        }
    }
```

- [ ] **Step 2: Add the helper scoring methods below `scoreItemAction`**

```java
    // --- Lua hook ---

    private static float luaScore(Mob mob, Item item, String action) {
        try {
            return mob.getScript().runOptional("scoreItemAction", 0f, item, action);
        } catch (Exception e) {
            return 0;
        }
    }

    // --- Survival helpers ---

    private static float survivalHealScore(Mob mob) {
        int deficit = mob.ht() - mob.hp();
        if (deficit <= 0) {
            return 0;
        }
        return (float) deficit / mob.ht();
    }

    private static float survivalPurityScore(Mob mob) {
        if (mob.hasBuff("Burning") || mob.hasBuff("Poison")) {
            return 0.9f;
        }
        return 0;
    }

    private static float escapeTeleportScore(Mob mob) {
        // High score if fleeing and enemy is close.
        Char enemy = mob.getEnemy();
        if (enemy.valid() && mob.level().distance(mob.getPos(), enemy.getPos()) <= 2) {
            return 0.8f;
        }
        return 0;
    }

    // --- Offensive helpers ---

    private static float offensiveThrowScore(Mob mob, float base) {
        Char enemy = mob.getEnemy();
        if (enemy.valid() && mob.isEnemyInFov()) {
            int dist = mob.level().distance(mob.getPos(), enemy.getPos());
            if (dist <= 4) {
                return base;
            }
        }
        return 0;
    }

    private static float offensiveAreaThrowScore(Mob mob, float base) {
        // Require 2+ visible enemies for area effects.
        if (countVisibleEnemies(mob) >= 2) {
            return base;
        }
        return 0;
    }

    private static float offensiveAdjacentThrowScore(Mob mob, float base) {
        Char enemy = mob.getEnemy();
        if (enemy.valid() && mob.level().adjacent(mob.getPos(), enemy.getPos())) {
            return base;
        }
        return 0;
    }

    // --- Buff helpers ---

    private static float expertGate(float score) {
        // getDifficultyFactor(): 1f=easy(0), 1.5f=normal/hard(1-2), 2f=expert(3)
        if (com.nyrds.pixeldungeon.game.GameLoop.getDifficultyFactor() >= 2) {
            return score;
        }
        return 0;
    }

    private static float rechargeScore(Mob mob) {
        Item weapon = mob.getBelongings().getItemFromSlot(com.watabou.pixeldungeon.actors.hero.Belongings.Slot.WEAPON);
        if (weapon instanceof com.watabou.pixeldungeon.items.wands.Wand) {
            if (((com.watabou.pixeldungeon.items.wands.Wand) weapon).curCharges() == 0) {
                return 0.2f;
            }
        }
        return 0;
    }

    // --- Equipment helper ---

    private static float autoEquipScore(Mob mob, Item item) {
        if (!(item instanceof com.watabou.pixeldungeon.items.EquipableItem)) {
            return 0;
        }
        com.watabou.pixeldungeon.items.EquipableItem equip = (com.watabou.pixeldungeon.items.EquipableItem) item;

        // STR check — mandatory.
        if (equip.requiredSTR() > mob.effectiveSTR()) {
            return 0;
        }

        if (equip instanceof com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon) {
            Item current = mob.getBelongings().getItemFromSlot(com.watabou.pixeldungeon.actors.hero.Belongings.Slot.WEAPON);
            if (current instanceof com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon) {
                int newDmg = ((com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon) equip).max();
                int curDmg = ((com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon) current).max();
                if (newDmg > curDmg) {
                    return 0.2f;
                }
                return 0;
            }
            // No weapon equipped — equip this one.
            return 0.2f;
        }

        if (equip instanceof com.watabou.pixeldungeon.items.armor.Armor) {
            Item current = mob.getBelongings().getItemFromSlot(com.watabou.pixeldungeon.actors.hero.Belongings.Slot.ARMOR);
            if (current instanceof com.watabou.pixeldungeon.items.armor.Armor) {
                int newDr = ((com.watabou.pixeldungeon.items.armor.Armor) equip).dr();
                int curDr = ((com.watabou.pixeldungeon.items.armor.Armor) current).dr();
                if (newDr > curDr) {
                    return 0.2f;
                }
                return 0;
            }
            return 0.2f;
        }

        return 0;
    }

    // --- SpellBook helper ---

    private static float spellBookScore(Mob mob, com.nyrds.pixeldungeon.items.artifacts.SpellBook book, Context context) {
        com.nyrds.pixeldungeon.mechanics.spells.Spell spell = book.spell();
        if (!spell.canCast(mob, false)) {
            return 0;
        }

        String targeting = getTargetingType(spell);

        // Self-targeting healing — survival score.
        if (com.nyrds.pixeldungeon.mechanics.spells.SpellHelper.TARGET_SELF.equals(targeting)) {
            if (context == Context.COMBAT || context == Context.FLEEING) {
                return survivalHealScore(mob);
            }
            return 0;
        }

        // Enemy-targeting — offensive score.
        if (com.nyrds.pixeldungeon.mechanics.spells.SpellHelper.TARGET_CHAR.equals(targeting)
                || com.nyrds.pixeldungeon.mechanics.spells.SpellHelper.TARGET_CHAR_NOT_SELF.equals(targeting)
                || com.nyrds.pixeldungeon.mechanics.spells.SpellHelper.TARGET_ENEMY.equals(targeting)) {
            if (context == Context.COMBAT && mob.getEnemy().valid() && mob.isEnemyInFov()) {
                return 0.5f;
            }
            return 0;
        }

        // Cell-targeting — offensive score if enemy visible.
        if (com.nyrds.pixeldungeon.mechanics.spells.SpellHelper.TARGET_CELL.equals(targeting)) {
            if (context == Context.COMBAT && mob.getEnemy().valid() && mob.isEnemyInFov()) {
                return 0.4f;
            }
            return 0;
        }

        // Ally/none — low base score.
        return 0.2f;
    }

    private static String getTargetingType(com.nyrds.pixeldungeon.mechanics.spells.Spell spell) {
        return spell.targetingType();
    }

    // --- Wand helper ---

    private static float wandScore(Mob mob, com.watabou.pixeldungeon.items.wands.Wand wand, Context context) {
        if (context != Context.COMBAT) {
            return 0;
        }
        Char enemy = mob.getEnemy();
        if (!enemy.valid() || !mob.isEnemyInFov()) {
            return 0;
        }
        int charges = wand.curCharges();
        if (charges <= 0) {
            return 0;
        }
        // Scale: 1 charge → 0.4, max charges → 0.7.
        float ratio = Math.min(1f, charges / Math.max(1, wand.maxCharges()));
        return 0.4f + 0.3f * ratio;
    }

    // --- Utility ---

    private static int countVisibleEnemies(Mob mob) {
        int count = 0;
        for (Mob m : mob.level().getCopyOfMobsArray()) {
            if (!mob.friendly(m) && mob.level().fieldOfView[m.getPos()]) {
                count++;
            }
        }
        return count;
    }
```

- [ ] **Step 3: Add the necessary imports at the top of `MobItemAi.java`**

Add this import:

```java
import com.nyrds.pixeldungeon.mechanics.CommonActions;
```

Remove the unused `import com.nyrds.Packable;` line. `GameLoop` is referenced fully-qualified (`com.nyrds.pixeldungeon.game.GameLoop.getDifficultyFactor()`) so no import is needed for it.

- [ ] **Step 4: Build to verify it compiles**

Run: `cd /home/mike/StudioProjects/remixed-dungeon && ./gradlew :RemixedDungeon:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/MobItemAi.java
git commit -m "feat: implement MobItemAi default scorer with survival/offensive/buff/equip categories"
```

---

## Task 4: Wire MobItemAi into Hunting.java

**Files:**
- Modify: `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/Hunting.java:27-30`

The call goes after the `friendly(enemy)` check (line 27-30) and before `me.enemySeen` is set (line 32). At this point we know the mob has a valid, non-friendly enemy.

- [ ] **Step 1: Add the import**

In `Hunting.java`, add after the existing imports (after line 6):

```java
import com.watabou.pixeldungeon.actors.mobs.Mob;
```

- [ ] **Step 2: Insert the tryUseItem call**

After the `if(me.friendly(enemy))` block (line 27-30) and before `me.enemySeen = me.isEnemyInFov();` (line 32), insert:

```java
        if (me instanceof Mob && ((Mob) me).isHumanoid()
                && MobItemAi.tryUseItem((Mob) me, MobItemAi.Context.COMBAT)) {
            return;
        }
```

The full `act` method should now look like:

```java
    @Override
    public void act(@NotNull Char me) {

        if(returnToOwnerIfTooFar(me, 6)) {
            return;
        }

        final Char enemy = me.getEnemy();

        if(enemy.invalid()) {
            me.setEnemy(chooseEnemy(me,1.0f));
        }

        if(me.friendly(enemy)) {
            me.setState(getStateByClass(Wandering.class));
            return;
        }

        if (me instanceof Mob && ((Mob) me).isHumanoid()
                && MobItemAi.tryUseItem((Mob) me, MobItemAi.Context.COMBAT)) {
            return;
        }

        me.enemySeen = me.isEnemyInFov();

        if (me.enemySeen && me.canAttack(enemy)) {
            me.doAttack(enemy);
        } else {
            if (me.enemySeen) {
                me.setTarget(enemy.getPos());
            }

            if(!me.doStepTo(me.getTarget())) {
                me.setTarget(me.level().randomDestination());
                me.setState(getStateByClass(Wandering.class));
            }
        }
    }
```

- [ ] **Step 3: Build**

Run: `cd /home/mike/StudioProjects/remixed-dungeon && ./gradlew :RemixedDungeon:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/Hunting.java
git commit -m "feat: wire MobItemAi into Hunting AI state (COMBAT context)"
```

---

## Task 5: Wire MobItemAi into Fleeing.java

**Files:**
- Modify: `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/Fleeing.java:16-23`

The call goes at the start of `act()`, before the movement logic.

- [ ] **Step 1: Add the import**

In `Fleeing.java`, add:

```java
import com.watabou.pixeldungeon.actors.mobs.Mob;
```

- [ ] **Step 2: Insert the tryUseItem call**

At the start of the `act` method body (after line 16, before `me.enemySeen = me.isEnemyInFov();`), insert:

```java
        if (me instanceof Mob && ((Mob) me).isHumanoid()
                && MobItemAi.tryUseItem((Mob) me, MobItemAi.Context.FLEEING)) {
            return;
        }
```

The full `act` method should now look like:

```java
    @Override
    public void act(@NotNull Char me) {
        if (me instanceof Mob && ((Mob) me).isHumanoid()
                && MobItemAi.tryUseItem((Mob) me, MobItemAi.Context.FLEEING)) {
            return;
        }

        me.enemySeen = me.isEnemyInFov();
        if (me.enemySeen) {
            me.setTarget(me.getEnemy().getPos());
        }

        me.doStepFrom(me.getTarget());
    }
```

- [ ] **Step 3: Build**

Run: `cd /home/mike/StudioProjects/remixed-dungeon && ./gradlew :RemixedDungeon:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/Fleeing.java
git commit -m "feat: wire MobItemAi into Fleeing AI state (FLEEING context)"
```

---

## Task 6: Wire MobItemAi into Wandering.java

**Files:**
- Modify: `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/Wandering.java:14-34`

The call goes after the `returnToOwnerIfTooFar` check and enemy selection, but before the movement. Only the IDLE context is active (auto-equip only).

- [ ] **Step 1: Add the import**

In `Wandering.java`, add:

```java
import com.watabou.pixeldungeon.actors.mobs.Mob;
```

- [ ] **Step 2: Insert the tryUseItem call**

After the enemy selection block (after line 22 `me.setEnemy(enemy);`) and before the `if (me.isEnemyInFov())` check (line 24), insert:

```java
        if (me instanceof Mob && ((Mob) me).isHumanoid()
                && MobItemAi.tryUseItem((Mob) me, MobItemAi.Context.IDLE)) {
            return;
        }
```

The full `act` method should now look like:

```java
    @Override
    public void act(@NotNull Char me) {

        if(returnToOwnerIfTooFar(me, 2)) {
            return;
        }

        Char enemy = chooseEnemy(me, 1f);
        me.setEnemy(enemy);

        if (me instanceof Mob && ((Mob) me).isHumanoid()
                && MobItemAi.tryUseItem((Mob) me, MobItemAi.Context.IDLE)) {
            return;
        }

        if (me.isEnemyInFov()) {
            huntEnemy(me);
        } else {

            me.enemySeen = false;

            if(!me.doStepTo(me.getTarget())) {
                me.setTarget(me.level().randomDestination());
            }
        }
    }
```

- [ ] **Step 3: Build**

Run: `cd /home/mike/StudioProjects/remixed-dungeon && ./gradlew :RemixedDungeon:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/Wandering.java
git commit -m "feat: wire MobItemAi into Wandering AI state (IDLE context)"
```

---

## Task 7: Add scoreItemAction hook stub to mob.lua

**Files:**
- Modify: `scripts/lib/mob.lua` (add after `mob.onAct` near line 83)

The stub lets modders override scoring per (item, action). Java checks via `runOptional("scoreItemAction", 0f, item, action)` — if the function exists and returns a positive number, that score is used. If the script doesn't define `scoreItemAction`, `runOptional` returns the default `0f`.

- [ ] **Step 1: Add the stub function**

In `scripts/lib/mob.lua`, after the `mob.onAct` function (line 83), add:

```lua
mob.onScoreItemAction = function(self, mob, item, action)
    if not self.scoreItemAction then
        return nil
    end
    return self.scoreItemAction(mob, item, action)
end
```

This follows the same pattern as `onAct`, `onDie`, `onMove`, etc. — a wrapper that checks whether the script instance defined the optional callback.

- [ ] **Step 2: Update the Java `luaScore` method in `MobItemAi.java` to call the wrapper**

In `MobItemAi.java`, update `luaScore`:

```java
    private static float luaScore(Mob mob, Item item, String action) {
        try {
            Float result = mob.getScript().runOptional("onScoreItemAction", null, item, action);
            if (result == null) {
                return 0;
            }
            return result;
        } catch (Exception e) {
            return 0;
        }
    }
```

Note: `runOptional` with `null` default runs the method if it exists and coerces the return to the default's class. Passing `null` means "run but don't coerce" — we need a `Float` default to get a float back. Actually, looking at `LuaScript.runOptional`: if `defaultValue` is null, it runs the method and returns null (no coercion). If non-null, it coerces the LuaValue to the default's class.

The correct call: pass `0f` as default (so it returns a Float), and check if the lua function returned a value > 0. But `runOptional` returns the default if the method doesn't exist — we can't distinguish "method returned 0" from "method doesn't exist". That's fine for our purposes: either way, score 0 means "let Java handle it".

So the correct implementation is the original one from Task 2:

```java
    private static float luaScore(Mob mob, Item item, String action) {
        try {
            return mob.getScript().runOptional("onScoreItemAction", 0f, item, action);
        } catch (Exception e) {
            return 0;
        }
    }
```

Do NOT change this method — it's already correct from Task 2. The lua wrapper `onScoreItemAction` returns `nil` when the script doesn't define `scoreItemAction`, which `CoerceLuaToJava.coerce` will convert to 0 when coercing to Float.

Actually, `nil` coercion to `Float` might throw. To be safe, the lua wrapper should return `0` instead of `nil`:

```lua
mob.onScoreItemAction = function(self, mob, item, action)
    if not self.scoreItemAction then
        return 0
    end
    return self.scoreItemAction(mob, item, action) or 0
end
```

- [ ] **Step 3: Use the corrected lua stub**

Final `scripts/lib/mob.lua` addition (after `mob.onAct`):

```lua
mob.onScoreItemAction = function(self, mob, item, action)
    if not self.scoreItemAction then
        return 0
    end
    return self.scoreItemAction(mob, item, action) or 0
end
```

- [ ] **Step 4: Build to verify Java still compiles (no changes needed)**

Run: `cd /home/mike/StudioProjects/remixed-dungeon && ./gradlew :RemixedDungeon:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add scripts/lib/mob.lua RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ai/MobItemAi.java
git commit -m "feat: add scoreItemAction Lua hook stub to mob.lua"
```

---

## Task 8: Full build verification and manual testing

**Files:** None (verification only)

- [ ] **Step 1: Full clean build**

Run: `cd /home/mike/StudioProjects/remixed-dungeon && ./gradlew :RemixedDungeon:compileJava`
Expected: BUILD SUCCESSFUL with no warnings about the new code.

- [ ] **Step 2: Run the desktop game with autoTestAi**

Run: `cd /home/mike/StudioProjects/remixed-dungeon && ./gradlew :RemixedDungeonDesktop:runDesktopGame`

Observe the log output. Look for lines like:
```
MobItemAi: <mobKind> uses <itemKind> (<action>) score <value>
```

- [ ] **Step 3: Verify the behaviors from the spec**

Check these scenarios in the log or by observation:
- Humanoid enemy mobs drink healing potions when low HP (survival score)
- Humanoid enemy mobs throw offensive potions at the hero (PotionOfLiquidFlame, PotionOfFrost)
- Pets use items from their inventory during combat
- Mob with equipped SpellBook casts spells
- Non-humanoid mobs never trigger MobItemAi (no log lines for rats, gnolls non-humanoid, etc.)
- Expert-only items (PotionOfStrength, ScrollOfUpgrade) are not used on normal difficulty

- [ ] **Step 4: Verify the Lua hook works**

Create a test mob script that overrides `scoreItemAction`, e.g. in `scripts/mobs/Skeleton.lua`:

```lua
local mob = require "scripts/lib/mob"

return mob.init{
    scoreItemAction = function(self, mob, item, action)
        if item:getEntityKind() == "PotionOfHealing" and action == "Potion_ACDrink" then
            return 0.99  -- Skeletons really want to heal
        end
        return nil
    end
}
```

Run the game and verify the Skeleton uses healing potions more aggressively than other mobs.

Remove the test script after verification.

- [ ] **Step 5: Final commit if any fixes were needed**

If bugs were found and fixed during testing, commit them.

---

## Verification Checklist

After all tasks are complete, verify:

- [ ] `MobItemAi.java` exists and compiles
- [ ] `Hunting.java` calls `tryUseItem(Context.COMBAT)`
- [ ] `Fleeing.java` calls `tryUseItem(Context.FLEEING)`
- [ ] `Wandering.java` calls `tryUseItem(Context.IDLE)`
- [ ] `mob.lua` has the `onScoreItemAction` wrapper
- [ ] Non-humanoid mobs are unaffected (no item usage, no log spam)
- [ ] Humanoid mobs use healing potions when low HP
- [ ] Humanoid mobs throw offensive potions at enemies
- [ ] SpellBook scoring works (healing when hurt, offensive at enemies)
- [ ] STR-gated equipment isn't auto-equipped when STR too low
- [ ] Expert-only items score 0 on normal difficulty
- [ ] Lua override works when a mob script defines `scoreItemAction`
- [ ] All changes committed
