package com.watabou.pixeldungeon.actors;

import androidx.annotation.NonNull;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Sleeping;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.game.ModQuirks;
import com.nyrds.pixeldungeon.items.Carcass;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.levels.cellCondition;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.ml.actions.Ascend;
import com.nyrds.pixeldungeon.ml.actions.Attack;
import com.nyrds.pixeldungeon.ml.actions.CharAction;
import com.nyrds.pixeldungeon.ml.actions.Descend;
import com.nyrds.pixeldungeon.ml.actions.Examine;
import com.nyrds.pixeldungeon.ml.actions.Expel;
import com.nyrds.pixeldungeon.ml.actions.Interact;
import com.nyrds.pixeldungeon.ml.actions.InteractObject;
import com.nyrds.pixeldungeon.ml.actions.MapItemAction;
import com.nyrds.pixeldungeon.ml.actions.Move;
import com.nyrds.pixeldungeon.ml.actions.OpenChest;
import com.nyrds.pixeldungeon.ml.actions.Order;
import com.nyrds.pixeldungeon.ml.actions.PickUp;
import com.nyrds.pixeldungeon.ml.actions.Push;
import com.nyrds.pixeldungeon.ml.actions.Steal;
import com.nyrds.pixeldungeon.ml.actions.Taunt;
import com.nyrds.pixeldungeon.ml.actions.Unlock;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.VHBox;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mimic;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Lightning;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.particles.SparkParticle;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.traps.LightningTrap;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class CharUtils {

    static public boolean isVisible(@Nullable Char ch) {

        if (Dungeon.isLoading()) {
            return false;
        }

        if (ch == null) {
            return false;
        }

        if (!ch.level().cellValid(ch.getPos())) {
            EventCollector.logException("Checking visibility on invalid cell");
            return false;
        }

        return Dungeon.isCellVisible(ch.getPos());
    }

    public static void checkDeathReport(Char attacker, @NotNull Char victim, String desc) {
        if (!victim.isAlive() && victim == Dungeon.hero) {
            Dungeon.fail(Utils.format(ResultDescriptions.getDescription(ResultDescriptions.Reason.MOB),
                    Utils.indefinite(attacker.getName()), Dungeon.depth));
            GLog.n(desc, attacker.getName());
        }
    }

    public static void lightningProc(@NotNull Char caster, int targetCell, int damage) {

        int[] points = {caster.getPos(), targetCell};
        GameScene.addToMobLayer(new Lightning(points));

        Char enemy = Actor.findChar(targetCell);

        if (enemy == null) {
            return;
        }

        if (enemy.level().water[enemy.getPos()] && !enemy.isFlying()) {
            damage *= 2f;
        }

        enemy.damage(damage, LightningTrap.LIGHTNING);

        enemy.getSprite().centerEmitter().burst(SparkParticle.FACTORY, 3);
        enemy.getSprite().flash();

        if (enemy == Dungeon.hero) {
            Camera.main.shake(2, 0.3f);
        }
    }

    public static boolean canDoOnlyRangedAttack(@NotNull Char attacker, @NotNull Char enemy) {
        return !attacker.adjacent(enemy)
                && Ballistica.cast(attacker.getPos(), enemy.getPos(), false, true) == enemy.getPos();
    }

    public static boolean steal(@NotNull Char thief, @NotNull Char victim) {

        if (!thief.adjacent(victim)) {
            return false;
        }

        if (victim.getBelongings().isBackpackEmpty()) {
            return false;
        }

        Item item = victim.getBelongings().randomUnequipped();
        GLog.w(StringsManager.getVars(R.array.Char_Stole)[thief.getGender()], thief.getName(), item.name(), victim.getName_objective());
        item.detachAll(victim.getBelongings().backpack);
        thief.collect(item);

        victim.onActionTarget(CommonActions.MAC_STEAL, thief);

        return true;
    }

    public static void teleportRandom(@NotNull Char ch) {
        Level level = ch.level();
        if (level.isBossLevel() || !ch.isMovable()) {
            GLog.w(Utils.format(R.string.ScrollOfTeleportation_NoTeleport2, ch.getName_objective()));
            return;
        }

        int pos = level.randomRespawnCell();

        if (!level.cellValid(pos)) {
            GLog.w(Utils.format(R.string.ScrollOfTeleportation_NoTeleport2, ch.getName_objective()));
        } else {
            WandOfBlink.appear(ch, pos);
            level.press(pos, ch);
            ch.observe();
            GLog.i(Utils.format(R.string.ScrollOfTeleportation_Teleport2, ch.getName_objective()));
        }
    }

    public static boolean hit(@NotNull Char attacker, Char defender, boolean magic) {
        if (attacker.invisible > 0) {
            return true;
        }

        float acuRoll = Random.Float(attacker.attackSkill(defender));
        float defRoll = Random.Float(defender.defenseSkill(attacker));
        boolean hit = (magic ? acuRoll * 2 : acuRoll) >= defRoll;

        if (ModQuirks.mobLeveling) {
            if (hit && attacker instanceof Mob) {
                attacker.earnExp(1);
            }

            if (!hit && defender instanceof Mob) {
                defender.earnExp(1);
            }
        }

        return hit;
    }

    public static void challengeAllMobs(Char ch, String sound) {

        if (!ch.isOnStage()) {
            return;
        }

        for (Mob mob : ch.level().mobs) {
            mob.beckon(ch.getPos());
        }

        for (Heap heap : ch.level().allHeaps()) {
            if (heap.type == Heap.Type.MIMIC) {
                Mimic m = Mimic.spawnAt(heap.pos, heap.items);
                if (m != null) {
                    m.beckon(ch.getPos());
                    heap.destroy();
                }
            }
        }

        ch.getSprite().centerEmitter().start(Speck.factory(Speck.SCREAM), 0.3f, 3);

        Sample.INSTANCE.play(sound);
        Invisibility.dispel(ch);
    }

    @NotNull
    public static CharAction actionForCell(@NonNull Char actor, int cell, @NotNull Level level) {
        final Char target = Actor.findChar(cell);
        final Char controlTarget = actor.getControlTarget();


        if (controlTarget instanceof Hero && (target == null || target.friendly(controlTarget)) ) {
            CharAction charAction = handleObjectOrHeap(actor, cell, level);
            if (charAction != null) return charAction;

            if (level.map[cell] == Terrain.LOCKED_DOOR || level.map[cell] == Terrain.LOCKED_EXIT) {
                return new Unlock(cell);
            }

            if (level.isExit(cell)) {
                return new Descend(cell);
            }

            if (cell == level.entrance) {
                return new Ascend(cell);
            }
        }

        if (level.fieldOfView[cell] && target != null && target != controlTarget) {
            if (target.friendly(controlTarget)) {
                return new Interact(target);
            } else {
                if (!(target.state instanceof Sleeping)) {
                    return new Attack(target);
                } else {

                    Set<String> actions = new HashSet<>(target.actions(actor));

                    actions.remove(CommonActions.MAC_HIT);
                    actions.remove(CommonActions.MAC_TAUNT);

                    if (actions.isEmpty() || actor instanceof Mob) {
                        return new Attack(target);
                    }

                    return new Examine(target);
                }
            }
        }

        CharAction charAction = handleObjectOrHeap(actor, cell, level);
        if (charAction != null) return charAction;


        if (actor.getPos() != cell) {
            return new Move(cell);
        }

        return new NoAction();
    }

    private static @androidx.annotation.Nullable CharAction handleObjectOrHeap(@NonNull Char actor, int cell, @NonNull Level level) {
        final LevelObject topLevelObject = level.getTopLevelObject(cell);

        if (cell != actor.getPos() && topLevelObject != null) {
            if (topLevelObject.interactive()) {
                return new InteractObject(topLevelObject);
            }
        }

        Heap heap;
        if ((heap = level.getHeap(cell)) != null) {
            if (heap.type == Heap.Type.HEAP) {
                if (heap.peek() instanceof Carcass) {
                    return new MapItemAction(cell);
                } else {
                    return new PickUp(cell);
                }
            } else {
                return new OpenChest(cell);
            }
        }
        return null;
    }

    public static void execute(Char target, Char hero, @NotNull String action) {
        if (action.equals(CommonActions.MAC_STEAL)) {
            hero.nextAction(new Steal(target));
            return;
        }

        if (action.equals(CommonActions.MAC_TAUNT)) {
            hero.nextAction(new Taunt(target));
            return;
        }

        if (action.equals(CommonActions.MAC_PUSH)) {
            hero.nextAction(new Push(target));
            return;
        }

        if (action.equals(CommonActions.MAC_HIT)) {
            hero.nextAction(new Attack(target));
            return;
        }

        if (action.equals(CommonActions.MAC_ORDER)) {
            hero.nextAction(new Order(target));
            return;
        }

        if(action.equals(CommonActions.MAC_EXPEL)){
            hero.nextAction(new Expel(target));
            return;
        }

        target.getScript().run("executeAction", target, action);
    }

    public static @NotNull ArrayList<String> actions(@NotNull Char target, Char hero) {
        ArrayList<String> actions = new ArrayList<>();

        if (target instanceof NPC) {
            return actions;
        }

        if (!target.friendly(hero) && target.movable) {
            actions.add(CommonActions.MAC_TAUNT);
        }

        if (target.adjacent(hero) && hero.stealth() > 2 && hero.friendly(target)) {
            actions.add(CommonActions.MAC_STEAL);
        }

        if (hero.canAttack(target)) {
            actions.add(CommonActions.MAC_HIT);
        }

        if (target.adjacent(hero) && target.movable) {
            actions.add(CommonActions.MAC_PUSH);
        }

        if (target.getOwnerId() == hero.getId()) {
            actions.add(CommonActions.MAC_ORDER);
            actions.add(CommonActions.MAC_EXPEL);
        }

        actions.removeAll(hero.getHeroClass().getForbiddenActions());

        return actions;
    }

    @LuaInterface //for auto tests
    public static String randomAction(@NotNull Char target, Char actor) {
        return Random.element(target.actions(actor));
    }

    public static void blinkAway(@NotNull Char chr, cellCondition condition) {
        final Level level = chr.level();

        int tgt = level.getNearestTerrain(chr.getPos(), condition);

        if (level.cellValid(tgt)) {
            final Char ch = chr;
            chr.fx(chr.getPos(), () -> WandOfBlink.appear(ch, tgt));
        }
        chr.observe();
    }

    public static void blinkTo(@NotNull Char chr, int target) {
        int cell = Ballistica.cast(chr.getPos(), target, true, true);

        if (Actor.findChar(cell) != null && Ballistica.distance > 1) {
            cell = Ballistica.trace[Ballistica.distance - 2];
        }

        WandOfBlink.appear(chr, cell);
    }

    public static void generateNewItem(Char shopkeeper) {
        Item newItem = Treasury.getLevelTreasury().random();

        if (newItem instanceof Gold) {
            return;
        }

        if (newItem.isCursed()) {
            return;
        }

        var supply = shopkeeper.getItem(newItem.getEntityKind());

        if (!newItem.stackable && supply.valid()) {
            return;
        }

        if (newItem.stackable && supply.valid() && supply.price() > 100) {
            return;
        }

        shopkeeper.collect(newItem);
    }

    @NotNull
    public static Char spawnOnNextCell(@NotNull Char src, String mobClass, int limit) {
        final Level level = src.level();
        int pos = src.emptyCellNextTo();

        if (level.cellValid(pos) && level.countMobsOfKind(mobClass) < limit) {
            Mob mob = MobFactory.mobByName(mobClass);
            mob.setPos(pos);
            level.spawnMob(mob, 0, src.getPos());
            return mob;
        }

        return CharsList.DUMMY;
    }

    @NonNull
    public static VHBox makeActionsBlock(int maxWidth, Char mob, @NonNull Char selector) {

        VHBox actions = new VHBox(maxWidth - 2 * Window.GAP);
        actions.setAlign(HBox.Align.Width);
        actions.setHGap(Window.GAP);
        actions.setGap(Window.GAP);

        if (selector.isAlive()) {

            for (final String action : mob.actions(selector)) {

                RedButton btn = new RedButton(StringsManager.maybeId(action)) {
                    @Override
                    protected void onClick() {
                        execute(mob, selector, action);
                        Window.hideParentWindow(this);
                    }
                };
                btn.setSize(Math.max(btn.reqWidth(), 24), Window.BUTTON_HEIGHT);

                actions.add(btn);
            }
        }
        return actions;
    }

    public static void tryPickUp(Char hero, @NonNull Item item) {
        Heap oldHeap = item.getHeap();

        int heapPos = hero.getPos();
        if (oldHeap!= null) {
            heapPos  = oldHeap.pos;
        }

        item = item.pick(hero, hero.getPos());

        if (item.doPickUp(hero)) {

            hero.itemPickedUp(item);

            if (oldHeap != null) {
                oldHeap.pickUp();
                if (!oldHeap.isEmpty()) {
                    GLog.i(StringsManager.getVar(R.string.Hero_SomethingElse));
                }
            }

        } else {
            Heap newHeap = hero.level().drop(item, heapPos);

            newHeap.sprite.drop();
            newHeap.pickUpFailed();
        }
        hero.readyAndIdle();
    }

    static public final Set<Image> markers = new HashSet<>();
    static public void mark(Char chr) {
        var marker = Icons.TARGET.get();
        chr.getSprite().getParent().add(marker);

        marker.point(DungeonTilemap.tileToWorld(chr.getPos()));
        marker.y+=chr.getSprite().visualOffsetY();
        markers.add(marker);
    }

    static public void markTarget(Char chr) {
        var marker = Icons.TARGET.get();
        chr.getSprite().getParent().add(marker);
        marker.point(DungeonTilemap.tileToWorld(chr.getTarget()));
        markers.add(marker);
    }

    static public void clearMarkers() {
        for(var marker: markers) {
            marker.killAndErase();
        }
        markers.clear();
    }

    public static @NonNull Item tryToSpawnMimic(Item item, Char ch, int pos, String mimicKind) {
        Level level = ch.level();

        int spawnPos = pos;

        if(ch.getPos() == pos) {
            spawnPos = level.getEmptyCellNextTo(ch.getPos());

            if (!level.cellValid(spawnPos)) {
                return item;
            }
        }

        Mob mimic = MobFactory.mobByName(mimicKind);
        mimic.setPos(spawnPos);
        mimic.setState(MobAi.getStateByClass(Wandering.class));
        mimic.adjustStats(Dungeon.depth);

        level.spawnMob( mimic );
        ch.checkVisibleEnemies();

        CellEmitter.get(pos).burst( Speck.factory( Speck.STAR ), 10 );
        Sample.INSTANCE.play( Assets.SND_MIMIC );

        return ItemsList.DUMMY;
    }
}
