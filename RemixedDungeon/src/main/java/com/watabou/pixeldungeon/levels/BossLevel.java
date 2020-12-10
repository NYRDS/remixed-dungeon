package com.watabou.pixeldungeon.levels;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ai.AiState;
import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.levels.objects.ConcreteBlock;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Bestiary;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Bundle;

import lombok.var;

public abstract class BossLevel extends RegularLevel {

    protected static final String DOOR	    = "door";
    protected static final String ENTERED	= "entered";

    protected int arenaDoor = INVALID_CELL;

    protected boolean enteredArena = false;

    @Packable
    protected int stairs = INVALID_CELL;

    protected boolean[] water() {
        return Patch.generate(this, 0.45f, 5 );
    }
    protected boolean[] grass() {
        return Patch.generate(this, 0.30f, 4 );
    }

    @Override
    public void seal() {
        if(cellValid(arenaDoor)) {
            set(arenaDoor, Terrain.LOCKED_DOOR);
            GameScene.updateMap(arenaDoor);
        }

        Dungeon.observe();
    }

    private void sealEntrance() {
        if(cellValid(entrance)) {
            CellEmitter.get(entrance).start(Speck.factory(Speck.ROCK), 0.07f, 10);
            addLevelObject(new ConcreteBlock(entrance, 50));
        }

        for(var cell: exitMap.values()) {
            if (!cellValid(cell) || map[cell]==Terrain.LOCKED_EXIT) {
                continue;
            }
            CellEmitter.get(cell).start(Speck.factory(Speck.ROCK), 0.07f, 10);
            addLevelObject(new ConcreteBlock(cell, 50));
        }

    }

    public void unseal() {
        if (cellValid(stairs)) { //for old saves compatibility

            entrance = stairs;
            stairs = INVALID_CELL;

            set( entrance, Terrain.ENTRANCE );
            GameScene.updateMap( entrance );
        }

        if(cellValid(arenaDoor)) {
            CellEmitter.get(arenaDoor).start(Speck.factory(Speck.ROCK), 0.07f, 10);

            set(arenaDoor, Terrain.EMPTY_DECO);
            GameScene.updateMap(arenaDoor);
        }

        LevelObject obj;
        if(cellValid(entrance)) {
            while ((obj = getLevelObject(entrance))!=null) {
                CellEmitter.get(entrance).start(Speck.factory(Speck.ROCK), 0.07f, 10);
                obj.remove();
            }
        }

        for(var cell: exitMap.values()) {
            while ((obj = getLevelObject(cell))!=null) {
                CellEmitter.get(cell).start(Speck.factory(Speck.ROCK), 0.07f, 10);
                obj.remove();
            }
        }

        Dungeon.observe();
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( DOOR, arenaDoor );
        bundle.put( ENTERED, enteredArena );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        arenaDoor = bundle.getInt( DOOR );
        enteredArena = bundle.getBoolean( ENTERED );
    }

    @Override
    public boolean isBossLevel() {
        return true;
    }

    @Override
    protected void pressHero(int cell, Hero hero) {
        super.pressHero(cell, hero);
        if (cell != entrance && (!enteredArena) && getLevelObject(entrance) == null) {
            sealEntrance();
        }
    }

    protected void spawnBoss(int pos) {
        spawnBoss(pos, MobAi.getStateByClass(Hunting.class));
    }

    protected void spawnBoss(int pos, AiState state) {
        Mob boss = Bestiary.mob(this);

        boss.setPos(pos);
        spawnMob(boss);

        boss.setState(state);
        boss.notice();

        press( boss.getPos(), boss );
        seal();
    }

    @Override
    protected void createMobs() {
    }

    @Override
    protected void createItems() {
        dropBones();
    }
}
