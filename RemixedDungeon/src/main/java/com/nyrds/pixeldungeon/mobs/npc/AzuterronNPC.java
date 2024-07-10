package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.guts.TreacherousSpirit;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Stun;
import com.watabou.pixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndQuest;
import com.watabou.utils.Bundle;

import org.jetbrains.annotations.NotNull;

public class AzuterronNPC extends Shopkeeper {

    public AzuterronNPC() {
        movable = false;
        addImmunity(Paralysis.class);
        addImmunity(Stun.class);
        addImmunity(Roots.class);
    }

    @Override
    public int defenseSkill(Char enemy) {
        return 1000;
    }

    @Override
    public String defenseVerb() {
        return StringsManager.getVar(R.string.Ghost_Defense);
    }

    @Override
    public float speed() {
        return 0.5f;
    }

    @Override
    public void damage(int dmg, @NotNull NamedEntityKind src) {
    }

    @Override
    public boolean reset() {
        return super.reset();
    }

    @Override
    public boolean act() {

        return super.act();
    }

    @Override
    public boolean interact(final Char hero) {
        getSprite().turnTo(getPos(), hero.getPos());
        if (Quest.completed) {
            return super.interact(hero);
        }
        if (Quest.given) {

            if (exchangeItem(hero, "HeartOfDarkness", "PotionOfMight")) {
                Quest.complete();
                GameScene.show(new WndQuest(this, StringsManager.getVar(R.string.AzuterronNPC_Quest_End)));
            } else {
                GameScene.show(new WndQuest(this, StringsManager.getVar(R.string.AzuterronNPC_Quest_Reminder)));
            }

        } else {
            GameScene.show(new WndQuest(this, StringsManager.getVar(R.string.AzuterronNPC_Quest_Start)));
            Quest.given = true;
            Quest.process();
            Journal.add(Journal.Feature.AZUTERRON.desc());
        }
        return true;
    }

    public static class Quest {

        private static boolean completed;
        private static boolean given;
        private static boolean processed;

        private static int depth;

        public static void reset() {
            completed = false;
            processed = false;
            given = false;
        }

        private static final String COMPLETED = "completed";
        private static final String NODE = "azuterron";
        private static final String GIVEN = "given";
        private static final String PROCESSED = "processed";
        private static final String DEPTH = "depth";

        public static void storeInBundle(Bundle bundle) {
            Bundle node = new Bundle();

            node.put(GIVEN, given);
            node.put(DEPTH, depth);
            node.put(PROCESSED, processed);
            node.put(COMPLETED, completed);

            bundle.put(NODE, node);
        }

        public static void restoreFromBundle(Bundle bundle) {

            Bundle node = bundle.getBundle(NODE);

            if (!node.isNull()) {
                given = node.getBoolean(GIVEN);
                depth = node.getInt(DEPTH);
                processed = node.getBoolean(PROCESSED);
                completed = node.getBoolean(COMPLETED);
            }
        }

        public static void process() {
            if (given && !processed) {
                final Level level = Dungeon.level;

                int mobPos = level.randomRespawnCell();

                if (level.cellValid(mobPos)) {
                    TreacherousSpirit enemy = new TreacherousSpirit();
                    enemy.setPos(mobPos);
                    level.spawnMob(enemy);
                    processed = true;
                }
            }
        }

        public static void complete() {
            completed = true;
            Journal.remove(Journal.Feature.AZUTERRON.desc());
        }
    }
}


