
package com.watabou.pixeldungeon.actors;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.val;


public abstract class Actor implements Bundlable, NamedEntityKind {

    public static final float TICK = 1f;
    private static float realTimeMultiplier = 1f;

    private static ArrayList<Actor> npcActors;

    @Packable
    float time;
    @Packable
    float prevTime = -1;

    private boolean added = false;

    public static void setRealTimeMultiplier(float realTimeMultiplier) {
        Actor.realTimeMultiplier = realTimeMultiplier;
    }

    protected abstract boolean act();

    private static final float SPEND_EMA_ALPHA = 0.1f;
    private float spendEma = 1f;

    public void spend(float d_t) {
        GLog.debug("%s spend %4.1f", getEntityKind(), d_t);
        if(this instanceof Char) {
            if (d_t < 0.01) {
                GLog.debug("sus!");
            }
            if (d_t > 5) {
                GLog.debug("sus!");
            }
        }
        time += d_t;
        spendEma = (1 - SPEND_EMA_ALPHA) * spendEma + SPEND_EMA_ALPHA * d_t;
        if (spendEma < 0.01) {
            GLog.debug("spendEma = %4.2f", spendEma);
        }
    }

    public void postpone(float d_t) {
        if (time < now + d_t) {
            time = now + d_t;
        }
    }

    @LuaInterface
    public float cooldown() {
        return time - now;
    }

    @LuaInterface
    public void deactivateActor() {
        time = Util.BIG_FLOAT;
        Actor.remove(this);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
    }

    @Override
    public boolean dontPack() {
        return false;
    }

    // **********************
    // *** Static members ***

    private static final HashSet<Actor> all = new HashSet<>();
    private static Actor current;

    private static float now = 0;

    {
        time = now;
    }

    public static final Multimap<Integer, Char> chars = MultimapBuilder.hashKeys().hashSetValues().build();

    public static void clearActors() {
        now = 0;
        chars.clear();

        for (Actor a : all) {
            a.added = false;
        }

        all.clear();

        CharsList.reset();
        current = null;
    }

    public static void fixTime() {
        Hero hero = Dungeon.hero;

        if (hero.valid() && all.contains(hero)) {
            Statistics.duration = hero.actorTime();
        }

        float heroTime = hero.actorTime();

        now = heroTime;

        for (Actor a : all) {
            if (a.time < now) {
                a.time = now;
            }
        }
    }

    public static void init(@NotNull Level level) {
        clearActors();

        addDelayed(Dungeon.hero, -Float.MIN_VALUE);

        for (Mob mob : level.mobs) {
            mob.regenSprite();
            add(mob);
        }

        for (Blob blob : level.blobs.values()) {
            add(blob);
        }
    }

    public static void occupyCell(@NotNull Char ch) {
        if (ch.getPos() == Level.INVALID_CELL && !(ch instanceof DummyChar)) {
            throw new TrackedRuntimeException("trying to spawn mob in void");
        }
        chars.put(ch.getPos(), ch);
    }

    public static void freeCell(Char actor) {
        chars.remove(actor.getPos(), actor);
    }

    final public void next() {
        //Log.i("Main loop", String.format("next:\nNext: %s Current: %s", this, current));
        if (current == this) {
            //Log.i("Main loop", "next == current");

            current = null;
        }
    }

    private static void processReaTime(float elapsed) {

        now += elapsed * realTimeMultiplier;

        Actor next;
        while ((next = getNextActor(now)) != null) {

            //Log.i("Main loop", String.format("%s %4.2f %4.2f",next.getClass().getSimpleName(),now,next.time));

            //float timeBefore = next.time;

            current = next;
            EventCollector.setSessionData("actor", next.getEntityKind());
            next.act();
/*
			if(!(next.time>timeBefore)) {
				//Log.i("Main loop", String.format("%s %4.2f, time not increased!",next.getClass().getSimpleName(),next.time));
			}
*/
        }

    }


    public static void processTurnBased(float elapsed) {
        Hero hero = Dungeon.hero;
        if(!hero.isAlive()) {
            if(Util.isDebug()) {
                return;
            }
            if (heroMotionInProgress() || motionInProgress()) {
                return;
            }
        }
        // action still in progress
        if (current == hero && !current.timeout()) {
            return;
        }

        GLog.debug("Main loop start - %d actors", all.size());
        while ((current = nextActor()) != null) {
            now = current.time;

            if (hero.actorTime() > now + TICK * 2) {
                if (motionInProgress()) {
                    return;
                }
            }

            GLog.debug("actor %s %4.1f hero: %4.1f now: %4.1f", current.getEntityKind(), current.time, hero.actorTime(), now);

            if (current != hero) {
                current.act();
                current = null;
                if(!hero.isAlive()) {
                    break;
                }
            } else {
                current.act();
                break;
            }
        }
    }

    public static void process(float elapsed) {
        if (Dungeon.realtime()) {
            processReaTime(elapsed);
        } else {
            processTurnBased(elapsed);
        }
    }

    public static Actor getNextActor(float upTo) {

        var copyOfAll = all.toArray(new Actor[0]);

        Set<Char> toRemove = new HashSet<>();
        Actor next = null;

        chars.clear();

        //Log.i("Main loop","getNextActor");

        for (Actor actor : copyOfAll) {
            if (actor instanceof Char) {
                Char ch = (Char) actor;

                if (!ch.level().cellValid(ch.getPos())) {
                    actor.next();
                    toRemove.add(ch);
                }
            }
        }

        for (Char ch : toRemove) {
            ch.regenSprite();
        }

        all.removeAll(toRemove);
        copyOfAll = all.toArray(new Actor[0]);


        for (Actor actor : copyOfAll) {

            boolean busy = false;

            //fill chars
            if (actor instanceof Char) {
                Char ch = (Char) actor;

                if (ch.hasSprite()) {
                    final CharSprite chSprite = ch.getSprite();

                    if (chSprite.doingSomething()) {
                        busy = true;
                    }
                }

                chars.put(ch.getPos(), ch);
            }

            //select actor to act
            if (actor.time < upTo) {
                upTo = actor.time;
                next = actor;
            }

            if (actor.time == upTo && !busy) {
                next = actor;
                //GLog.debug("not busy");
            }
        }

        Actor.now = upTo;

        return next;
    }

    static ArrayList<Actor> toActBeforeHero = new ArrayList<>();

    static Actor nextActor() {
        toActBeforeHero.clear();
        chars.clear();

        for (Actor actor : all) {
            actor.useCell();
            toActBeforeHero.add(actor);
        }

        Collections.sort(toActBeforeHero, (a1, a2) -> Float.compare(a1.time, a2.time));

        return toActBeforeHero.get(0);
    }

    protected void useCell() {
    }

    protected boolean timeout() {
        return false;
    }

    public static void add(Actor actor) {
        add(actor, now);
    }

    public static void addDelayed(Actor actor, float delay) {
        add(actor, now + delay);
    }

    private static void add(Actor actor, float new_time) {
        actor.added = true;

        if (all.contains(actor)) {
            return;
        }

        all.add(actor);

        if(actor.time < new_time) {
            actor.time = new_time;
        }

        if (actor instanceof Char) {
            Char ch = (Char) actor;

            CharsList.add(ch, ch.getId());

            chars.put(ch.getPos(), ch);
            all.addAll(ch.buffs());
            for (var item : ch.getBelongings()) {
                all.add(item);
            }
        }


    }

    public static void remove(Actor actor) {

        if (actor != null) {
            actor.next();
            actor.added = false;
            if (actor instanceof Char) {
                freeCell((Char) actor);
            }
            all.remove(actor);
        }
    }

    @LuaInterface
    public static boolean heroMotionInProgress() {
        if(!Dungeon.hero.valid()) {
            return false;
        }

        return Dungeon.hero.getSprite().doingSomething();
    }

    @LuaInterface
    public static boolean motionInProgress() {
        for (Actor ch : all.toArray(new Actor[0])) {
            if (ch instanceof Char) {
                if (((Char) ch).getSprite().doingSomething()) {
                    return true;
                }
            }
        }

        return false;
    }

    @LuaInterface
    public static Char getRandomChar() {
        var ret = Random.element(chars.values());
        GLog.debug("selected %s at %d", ret.getEntityKind(), ret.getPos());
        return ret;
    }

    @LuaInterface
    public static Char findChar(int pos) {
        val ret = chars.get(pos);
        if (ret.isEmpty()) {
            return null;
        }

        for (val retChar:  ret) {
            if (retChar.isOnStage() && retChar.isAlive()) {
                return retChar;
            }
        }

        return null;
    }

    @LuaInterface
    public boolean myMove() {
        return current == this;
    }

    public static HashSet<Actor> all() {
        return all;
    }

    @LuaInterface
    public float actorTime() {
        return time;
    }

    @LuaInterface
    static public float localTime() {
        return now;
    }

    @Override
    public String name() {
        return getEntityKind();
    }

    @Override
    public String getEntityKind() {
        return getClass().getSimpleName();
    }

    public boolean testAct() {
        return act();
    }

    public boolean isOnStage() {
        return added && GameScene.isSceneReady();
    }
}
