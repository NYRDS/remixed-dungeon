package com.nyrds.pixeldungeon.mobs.necropolis;

import com.nyrds.pixeldungeon.items.necropolis.BlackSkull;
import com.watabou.noosa.Animation;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Skeleton;
import com.watabou.pixeldungeon.actors.mobs.Yog;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by DeadDie on 12.02.2016
 */
public class Lich extends Boss {

    private static final int SKULLS_BY_DEFAULT	= 3;
    private static final int SKULLS_MAX	= 4;
    private static final int HEALTH	= 150;
    private int skullTimer = 5;
    private static final int JUMP_DELAY = 5;

    private RunicSkull activatedSkull;

    public HashSet<RunicSkull> skulls   = new HashSet<>();

    {
        hp(ht(HEALTH));
        EXP = 20;
        defenseSkill = 20;

        loot = new BlackSkull();
        lootChance = 1f;

        IMMUNITIES.add( Paralysis.class );
        IMMUNITIES.add( ToxicGas.class );
        IMMUNITIES.add( Terror.class );
        IMMUNITIES.add( Death.class );
        IMMUNITIES.add( Amok.class );
        IMMUNITIES.add( Blindness.class );
        IMMUNITIES.add( Sleep.class );

        SpawnSkulls();
    }

    private int timeToJump = JUMP_DELAY;

    @Override
    public void onMotionComplete() {
        super.onMotionComplete();
        postpone(1);
        getSprite().idle();
    }

    @Override
    protected boolean getCloser( int target ) {
        if (Dungeon.level.fieldOfView[target]) {
            jump();
            return true;
        } else {
            return super.getCloser( target );
        }
    }

    @Override
    protected boolean canAttack( Char enemy ) {
        return Ballistica.cast( getPos(), enemy.getPos(), false, true ) == enemy.getPos();
    }

    @Override
    protected boolean doAttack( Char enemy ) {
        timeToJump--;
        if (timeToJump <= 0 && Dungeon.level.adjacent( getPos(), enemy.getPos() )) {
            jump();
            return true;
        } else {
            getSprite().zap(enemy.getPos());
            return super.doAttack( enemy );
        }
    }

    private void jump() {
        timeToJump = JUMP_DELAY;

        for (int i=0; i < 4; i++) {
            int trapPos;
            do {
                trapPos = Random.Int( Dungeon.level.getLength() );
            } while (!Dungeon.level.fieldOfView[trapPos] || !Dungeon.level.passable[trapPos]);

            if (Dungeon.level.map[trapPos] == Terrain.INACTIVE_TRAP) {
                Dungeon.level.set( trapPos, Terrain.POISON_TRAP );
                GameScene.updateMap( trapPos );
                ScrollOfMagicMapping.discover( trapPos );
            }
        }

        int newPos;
        do {
            newPos = Random.Int( Dungeon.level.getLength() );
        } while (
                !Dungeon.level.fieldOfView[newPos] ||
                        !Dungeon.level.passable[newPos] ||
                        Dungeon.level.adjacent( newPos, getEnemy().getPos() ) ||
                        Actor.findChar( newPos ) != null);

        getSprite().move( getPos(), newPos );
        move( newPos );

        if (Dungeon.visible[newPos]) {
            CellEmitter.get( newPos ).burst( Speck.factory( Speck.WOOL ), 6 );
            Sample.INSTANCE.play( Assets.SND_PUFF );
        }

        spend( 1 / speed() );
    }

    //Runic skulls handling
    //***

    protected void activateRandomSkull(){
        if (!skulls.isEmpty()){
            if (activatedSkull != null){
                activatedSkull.Deactivate();
            }

            RunicSkull skull = getRandomSkull();
            if(skull == null){
                activatedSkull = null;
            } else{
                skull.Activate();
                activatedSkull = skull;
            }
        }
    }

    public RunicSkull getRandomSkull() {
        while(!skulls.isEmpty()){
            RunicSkull skull = Random.element(skulls);
            if(skull.isAlive()){
                return skull;
            }
            else{
                skulls.remove(skull);
            }
        }
        return null;
    }

    public void useSkull(){
        getSprite().zap(getPos());
        switch (activatedSkull.getKind()) {
            case RunicSkull.RED_SKULL:
                PotionOfHealing.heal(this,0.07f * skulls.size());
                break;

            case RunicSkull.BLUE_SKULL:
                List<Integer> occupiedCells = new ArrayList<>();
                int i = 0;
                while (i < skulls.size()){
                    int pos = Dungeon.level.getEmptyCellNextTo(getPos());
                    if (Dungeon.level.cellValid(pos)) {
                        if (!occupiedCells.contains(pos)) {
                            Skeleton skeleton = new Skeleton();
                            skeleton.setPos(pos);
                            Dungeon.level.spawnMob(skeleton, 0);
                            Actor.addDelayed(new Pushing(skeleton, getPos(), skeleton.getPos()), -1);
                            i++;
                        }
                    }
                }
                occupiedCells.clear();
                break;

            case RunicSkull.GREEN_SKULL:
                GameScene.add( Blob.seed( getPos(), 30 * skulls.size(), ToxicGas.class ) );
                break;
        }
    }

    //***

    @Override
    protected boolean act() {
        if (this.hp() < HEALTH) {
           activateRandomSkull();
        }
        if (activatedSkull != null) {
            useSkull();
        }
        postpone(skullTimer);
        return super.act();
    }


    @Override
    public int damageRoll() {
        return Random.NormalIntRange( 8, 15 );
    }

    @Override
    public int defenseProc(Char enemy, int damage) {
        if (activatedSkull != null)
        {
            if(activatedSkull.getKind() == 3){
                return 0;
            }
        }
        jump();
        return damage;
    }

    @Override
    public int attackSkill( Char target ) {
        return 20;
    }

    @Override
    public int dr() {
        return 5;
    }

    @Override
    public void die( Object cause ) {
        GameScene.bossSlain();
        Dungeon.level.drop( new SkeletonKey(), getPos() ).sprite.drop();
        super.die( cause );

        //Kill everthing
        skulls.clear();
        Mob mob = Dungeon.level.getRandomMob();
        while(mob != null){
            mob.remove();
            mob = Dungeon.level.getRandomMob();
        }
        Badges.validateBossSlain(Badges.Badge.LICH_SLAIN);
    }

    public void SpawnSkulls(){

        int nSkulls = SKULLS_BY_DEFAULT;
        if(Game.getDifficulty() == 0){
            nSkulls = 2;
        }
        else if(Game.getDifficulty() > 2){
            nSkulls = SKULLS_MAX;
        }

        List<Integer> occupiedPedestals = new ArrayList<>();
        int i = 0;
        while (i < nSkulls){
            int skullCell = Dungeon.level.getRandomTerrainCell(Terrain.PEDESTAL);
            if (Dungeon.level.cellValid(skullCell)) {
                if (!occupiedPedestals.contains(skullCell)) {
                    RunicSkull skull = RunicSkull.makeNewSkull(i);
                    Dungeon.level.spawnMob(skull);
                    WandOfBlink.appear(skull, skullCell);
                    occupiedPedestals.add(skullCell);
                    skulls.add(skull);
                    i++;
                }
            }
        }
        occupiedPedestals.clear();
    }

}
