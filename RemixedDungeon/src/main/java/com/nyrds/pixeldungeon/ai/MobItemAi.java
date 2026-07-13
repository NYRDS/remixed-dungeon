package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.items.artifacts.SpellBook;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.mechanics.spells.SpellHelper;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.ArrayList;

public class MobItemAi {

    public enum Context {
        COMBAT,
        FLEEING,
        IDLE
    }

    private static final float USE_THRESHOLD = 0.1f;

    private static final String AC_THROW = "Item_ACThrow";
    private static final String AC_ZAP   = "Wand_ACZap";

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
            GLog.toFile("MobItemAi: %s uses %s (%s) score %.2f",
                    mob.getEntityKind(), bestItem.getEntityKind(), bestAction, bestScore);

            if (Dungeon.isCellVisible(mob.getPos())) {
                GLog.i(StringsManager.getVar(R.string.Mob_ItemUse),
                        mob.getName(), bestItem.name());
            }

            if (bestAction.equals(AC_ZAP) && bestItem instanceof Wand) {
                // Wands go through selectCell() in execute() which is a no-op for mobs.
                // Use mobWandUseCharged which fires the effect AND consumes charge + time.
                Wand wand = (Wand) bestItem;
                Char enemy = mob.getEnemy();
                int target = enemy.valid() ? enemy.getPos() : mob.getPos();
                wand.mobWandUseCharged(mob, target);
            } else {
                bestItem.execute(mob, bestAction);
            }
            return true;
        }

        return false;
    }

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

            default:
                if (action.equals(CommonActions.AC_EQUIP) && context == Context.IDLE) {
                    return autoEquipScore(mob, item);
                }
                if (action.equals(CommonActions.AC_READ) && item instanceof SpellBook) {
                    return spellBookScore(mob, (SpellBook) item, context);
                }
                if (action.equals(AC_ZAP) && item instanceof Wand) {
                    return wandScore(mob, (Wand) item, context);
                }
                return 0;
        }
    }

    // --- Lua hook ---

    private static float luaScore(Mob mob, Item item, String action) {
        try {
            return mob.getScript().runOptional("onScoreItemAction", 0f, item, action);
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
        if (GameLoop.getDifficultyFactor() >= 2) {
            return score;
        }
        return 0;
    }

    private static float rechargeScore(Mob mob) {
        Item weapon = mob.getBelongings().getItemFromSlot(Belongings.Slot.WEAPON);
        if (weapon instanceof Wand && ((Wand) weapon).curCharges() == 0) {
            return 0.2f;
        }
        return 0;
    }

    // --- Equipment helper ---

    private static float autoEquipScore(Mob mob, Item item) {
        if (!(item instanceof EquipableItem)) {
            return 0;
        }
        EquipableItem equip = (EquipableItem) item;

        // STR check — mandatory.
        if (equip.requiredSTR() > mob.effectiveSTR()) {
            return 0;
        }

        if (equip instanceof MeleeWeapon) {
            Item current = mob.getBelongings().getItemFromSlot(Belongings.Slot.WEAPON);
            if (!(current instanceof MeleeWeapon)) {
                return 0.2f;
            }
            if (((MeleeWeapon) equip).MAX > ((MeleeWeapon) current).MAX) {
                return 0.2f;
            }
            return 0;
        }

        if (equip instanceof Armor) {
            Item current = mob.getBelongings().getItemFromSlot(Belongings.Slot.ARMOR);
            if (!(current instanceof Armor)) {
                return 0.2f;
            }
            if (((Armor) equip).effectiveDr() > ((Armor) current).effectiveDr()) {
                return 0.2f;
            }
            return 0;
        }

        return 0;
    }

    // --- SpellBook helper ---

    private static float spellBookScore(Mob mob, SpellBook book, Context context) {
        Spell spell = book.spell();
        if (!spell.canCast(mob, false)) {
            return 0;
        }

        String targeting = spell.getTargetingType();

        if (SpellHelper.TARGET_SELF.equals(targeting)) {
            if (context == Context.COMBAT || context == Context.FLEEING) {
                return survivalHealScore(mob);
            }
            return 0;
        }

        if (SpellHelper.TARGET_CHAR.equals(targeting)
                || SpellHelper.TARGET_CHAR_NOT_SELF.equals(targeting)
                || SpellHelper.TARGET_ENEMY.equals(targeting)) {
            if (context == Context.COMBAT && mob.getEnemy().valid() && mob.isEnemyInFov()) {
                return 0.5f;
            }
            return 0;
        }

        if (SpellHelper.TARGET_CELL.equals(targeting)) {
            if (context == Context.COMBAT && mob.getEnemy().valid() && mob.isEnemyInFov()) {
                return 0.4f;
            }
            return 0;
        }

        return 0.2f;
    }

    // --- Wand helper ---

    private static float wandScore(Mob mob, Wand wand, Context context) {
        // WandOfBlink is a utility wand — deals no damage, only repositions.
        // Score it only in FLEEING (blink away from enemy), never in COMBAT.
        if (wand instanceof WandOfBlink) {
            if (context == Context.FLEEING && wand.curCharges() > 0) {
                return 0.3f;
            }
            return 0;
        }

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
        float ratio = Math.min(1f, (float) charges / Math.max(1, wand.maxCharges()));
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
}

