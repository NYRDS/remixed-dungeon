# Mob Item AI Design

## Purpose

Give humanoid mobs (enemies + pets) the ability to use items from their inventory during combat. Mobs evaluate their belongings each turn using utility scoring, pick the highest-value (item, action) pair, and execute it — spending their turn just as a hero would.

## Scope

- **Who:** All humanoid mobs (`isHumanoid() == true`) — enemies and pets
- **What:** Potions (drink/throw), scrolls (read), equipment (auto-equip), wands (zap), SpellBook (cast spell)
- **Intelligence:** Full utility scoring — evaluate all items × actions, pick highest score
- **Time cost:** Using an item consumes the mob's full turn (standard PD rules)
- **Architecture:** Hybrid — Java core scorer with Lua override hooks per (item, action)

## Architecture

```
AI State (Hunting/Fleeing/Wandering)
  │
  │  MobItemAi.tryUseItem(mob, context)
  │
  ▼
┌──────────────────────────────────────────┐
│              MobItemAi                    │
│                                          │
│  1. Gate: mob.isHumanoid()?              │
│  2. For each item in mob.getBelongings():│
│     For each action in item.actions(mob):│
│       a. Lua hook: scoreItemAction()     │
│       b. Java default scorer             │
│       c. Apply context filter            │
│  3. Pick highest-scoring (item, action)  │
│  4. If score > 0.1 threshold:            │
│       item.execute(mob, action)          │
│       return true (turn consumed)        │
│  5. Else return false (normal AI)        │
└──────────────────────────────────────────┘
```

### New file

- `com/nyrds/pixeldungeon/ai/MobItemAi.java` — Central scorer class

### Modified files

- `com/nyrds/pixeldungeon/ai/Hunting.java` — Call `tryUseItem(Context.COMBAT)` before attack
- `com/nyrds/pixeldungeon/ai/Fleeing.java` — Call `tryUseItem(Context.FLEEING)` before movement
- `com/nyrds/pixeldungeon/ai/Wandering.java` — Call `tryUseItem(Context.IDLE)` before movement
- `scripts/lib/mob.lua` — Add `scoreItemAction` hook stub

## Scoring System

Each (item, action) pair receives a float score. The scorer evaluates every item in the mob's belongings, and for each item, every available action (`item.actions(mob)`). The highest-scoring pair above the 0.1 threshold wins.

### Context filters

Three contexts control which scoring categories are active:

| Context | AI State | Categories active |
|---------|----------|-------------------|
| `COMBAT` | Hunting | Survival, Offensive, Buffs |
| `FLEEING` | Fleeing | Survival, Escape |
| `IDLE` | Wandering | Auto-equip only |

Categories not active in the current context score 0.

### Category: Survival (scores 0.0–1.5)

Evaluated in: COMBAT, FLEEING

| Item | Action | Score logic |
|------|--------|-------------|
| PotionOfHealing | AC_DRINK | `hpDeficit / maxHp` (at 20% HP → ~0.8; at 50% HP → ~0.3) |
| PotionOfPurity | AC_DRINK | 0.9 if burning or poisoned, else 0 |
| ScrollOfTeleportation | AC_READ | 0.8 if FLEEING and surrounded, else 0 |

### Category: Offensive (scores 0.0–0.8)

Evaluated in: COMBAT only

| Item | Action | Score logic |
|------|--------|-------------|
| PotionOfLiquidFlame | AC_THROW | 0.6 if enemy within throw range |
| PotionOfToxicGas | AC_THROW | 0.5 if 2+ enemies clustered |
| PotionOfFrost | AC_THROW | 0.4 if enemy adjacent |
| Equipped wand with charges | AC_ZAP | 0.4–0.7 based on charge count |
| SpellBook (enemy-targeting spell) | AC_READ | 0.4–0.6 if enemy visible (Ignite, FreezeGlobe), gated by `canCast()` |
| SpellBook (self-targeting spell) | AC_READ | Survival score if healing (see Survival category), gated by `canCast()` |

### Category: Buffs (scores 0.0–0.5)

Evaluated in: COMBAT only

| Item | Action | Score logic |
|------|--------|-------------|
| PotionOfStrength | AC_DRINK | 0.3, **expert-only** (0 otherwise) |
| PotionOfMight | AC_DRINK | 0.3, **expert-only** (0 otherwise) |
| ScrollOfUpgrade | AC_READ | 0.3, **expert-only** (0 otherwise) |
| ScrollOfWeaponUpgrade | AC_READ | 0.3, **expert-only** (0 otherwise) |
| ScrollOfRecharging | AC_READ | 0.2 if wand equipped with 0 charges, else 0 |
| SpellBook (summoning spell) | AC_READ | 0.3 if no active summon (SummonDeathling), gated by `canCast()` |
| SpellBook (utility spell) | AC_READ | 0.2 base (WindGust, MagicTorch), gated by `canCast()` |

### Category: Auto-equip (scores 0.0–0.3)

Evaluated in: IDLE only (Wandering — never swap gear mid-combat)

| Item | Action | Score logic |
|------|--------|-------------|
| Weapon | AC_EQUIP | 0.2 if `requiredSTR() <= effectiveSTR()` AND dmg > current |
| Armor | AC_EQUIP | 0.2 if `requiredSTR() <= effectiveSTR()` AND dr > current |

STR check is mandatory — items exceeding the mob's effective STR score 0.

### Expert-only items

These permanent power-up items score 0 on non-expert difficulty:

- PotionOfStrength
- PotionOfMight
- ScrollOfUpgrade
- ScrollOfWeaponUpgrade

Expert difficulty check: `RemixedDungeon.getDifficultyFactor()` meets the expert threshold.

### Scrolls — safety categorization

| Category | Scrolls | Behavior |
|----------|---------|----------|
| Safe self-use | Identify, RemoveCurse, Recharging, Upgrade*, WeaponUpgrade* | Scored normally |
| Escape | Teleportation | Only in FLEEING context |
| Offensive area | Terror, PsionicBlast | Scored as offensive (require 2+ enemies visible) |
| Never use | Challenge, MagicMapping, Curse | Score always 0 — Challenge draws aggro (suicidal), MagicMapping has no strategic value, Curse harms self |

(* = expert-only)

### SpellBook — scoring by spell targeting type

SpellBook is an equipped artifact that casts a contained spell via `AC_READ`. The scorer evaluates it by checking the spell's `targetingType` and `canCast()`:

1. **Gate:** `spell.canCast(mob, false)` — if on cooldown or insufficient resources, score 0.
2. **Score by category:**
   - **Self-targeting healing** (e.g., Healing): Scored as survival (same formula as PotionOfHealing — `hpDeficit / maxHp`).
   - **Enemy-targeting** (e.g., Ignite, FreezeGlobe): Offensive score 0.4–0.6 if enemy visible.
   - **Summoning** (e.g., SummonDeathling): 0.3 if no active summon pet.
   - **Utility** (e.g., WindGust, MagicTorch): 0.2 base.
3. **Lua override** applies first — modders can score any spell customly via `scoreItemAction`.

This approach requires no modifications to `Spell.java` — scoring is purely based on `targetingType` and the spell's entity kind.

## Lua Hook

Mob Lua scripts can override scoring per (item, action) pair. Java checks Lua first; if Lua returns a positive number, that score is used. If Lua returns nil, Java's default scorer applies.

### mob.lua stub

```lua
-- Optional override. Return a number to override Java scoring.
-- Return nil to let Java default scorer handle it.
mob.scoreItemAction = function(self, mob, item, action)
    return nil
end
```

### Example: custom mob script

```lua
return mob.init{
    scoreItemAction = function(self, mob, item, action)
        -- This mob prefers throwing flame potions
        if item:getEntityKind() == "PotionOfLiquidFlame"
           and action == "AC_THROW" then
            return 0.9
        end
        -- Defer everything else to Java
        return nil
    end
}
```

### Java-side Lua check

```java
// In MobItemAi.scoreItemAction():
LuaScript script = mob.getScript();
if (script.hasFunction("scoreItemAction")) {
    float luaScore = script.run("scoreItemAction", mob, item, action).optfloat(0);
    if (luaScore > 0) return luaScore;
}
return defaultScore(mob, item, action);
```

## AI Integration

### Gate

All calls are gated by `mob.isHumanoid()`. Non-humanoid mobs skip item logic entirely — zero overhead.

### Hunting.java

Insert after enemy validation, before attack decision:

```java
if (me instanceof Mob && me.isHumanoid()
        && MobItemAi.tryUseItem((Mob) me, Context.COMBAT)) {
    return;  // turn consumed
}
```

### Fleeing.java

Insert before movement logic:

```java
if (me instanceof Mob && me.isHumanoid()
        && MobItemAi.tryUseItem((Mob) me, Context.FLEEING)) {
    return;
}
```

### Wandering.java

Insert before random movement:

```java
if (me instanceof Mob && me.isHumanoid()
        && MobItemAi.tryUseItem((Mob) me, Context.IDLE)) {
    return;
}
```

### Not called from

Sleeping, Passive, RunningAmok, ControlledAi, ThiefFleeing, Horrified — mobs in these states do not use items.

## What does NOT change

- Item classes — no modifications needed. `item.execute(char, action)` already works for any Char.
- Spell.java — no modifications. SpellBook scoring uses `targetingType` and `canCast()`.
- Belongings — no changes. Mobs already have functional backpacks.
- Non-humanoid mobs — completely unaffected.
- Hero item usage — unchanged.

## Verification

1. Build: `./gradlew :RemixedDungeon:compileJava`
2. Run with autoTestAi, verify:
   - Humanoid enemy mobs drink healing potions when low HP
   - Humanoid enemy mobs throw offensive potions at the hero
   - Pets use items from their inventory during combat
   - Mob with equipped SpellBook casts spells (healing when hurt, offensive at enemies)
   - Non-humanoid mobs never use items (no regression)
   - STR-gated equipment isn't auto-equipped when STR too low
   - Expert-only items never used on normal difficulty
   - Lua override works when a mob script defines `scoreItemAction`
