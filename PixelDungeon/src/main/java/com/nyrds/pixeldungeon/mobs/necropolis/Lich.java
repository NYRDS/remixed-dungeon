package com.nyrds.pixeldungeon.mobs.necropolis;

import com.nyrds.pixeldungeon.items.necropolis.BlackSkull;
import com.nyrds.pixeldungeon.items.necropolis.BlackSkullOfMastery;
import com.watabou.noosa.Game;
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
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Skeleton;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
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
    private static final int HEALTH	= 200;
    private static final int SKULL_DELAY = 5;
    private static final int JUMP_DELAY = 6;

    private RunicSkull activatedSkull;

    public HashSet<RunicSkull> skulls   = new HashSet<>();

    public Lich() {
        hp(ht(HEALTH));
        EXP = 25;
        defenseSkill = 23;

        IMMUNITIES.add( Paralysis.class );
        IMMUNITIES.add( ToxicGas.class );
        IMMUNITIES.add( Terror.class );
        IMMUNITIES.add( Death.class );
        IMMUNITIES.add( Amok.class );
        IMMUNITIES.add( Blindness.class );
        IMMUNITIES.add( Sleep.class );
    }

    private int timeToSkull = SKULL_DELAY;
    private int timeToJump = JUMP_DELAY;

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
        return Dungeon.level.distance(getPos(), enemy.getPos()) < 4 && Ballistica.cast(getPos(), enemy.getPos(), false, true) == enemy.getPos();
    }

    @Override
    protected boolean doAttack( Char enemy ) {
        timeToJump--;
        if (timeToJump <= 0 && Dungeon.level.adjacent( getPos(), enemy.getPos() )) {
            jump();
            return true;
        }

        if (Dungeon.level.distance(getPos(), enemy.getPos()) <= 1) {
            return super.doAttack(enemy);
        } else {

            getSprite().zap(enemy.getPos());

            spend(1);

            if (hit(this, enemy, true)) {
                enemy.damage(damageRoll(), this);
            }
            return true;
        }
    }

    private void jump() {
        timeToJump = JUMP_DELAY;
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
        PlayZap();

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
        timeToSkull--;
        if (timeToSkull < 0){
            timeToSkull = SKULL_DELAY;
            activateRandomSkull();
            if (activatedSkull != null) {
                useSkull();
            }
        }
        return super.act();
    }


    @Override
    public int damageRoll() {
        return Random.NormalIntRange( 12, 20 );
    }

    @Override
    public int defenseProc(Char enemy, int damage) {
        if (activatedSkull != null)
        {
            if(activatedSkull.getKind() == RunicSkull.PURPLE_SKULL){
                return 0;
            }
        }

        if (Random.Int(2) == 1 && this.isAlive()){
            jump();
        }

        return damage;
    }

    @Override
    public int attackSkill( Char target ) {
        return 30;
    }

    @Override
    public int dr() {
        return 15;
    }

    @Override
    public void die( Object cause ) {
        GameScene.bossSlain();
        if ( Dungeon.hero.heroClass == HeroClass.NECROMANCER){
            Dungeon.level.drop( new BlackSkullOfMastery(), getPos() ).sprite.drop();
        }
        else {
            Dungeon.level.drop( new BlackSkull(), getPos() ).sprite.drop();
        }
        super.die( cause );

        //Kill everything
        skulls.clear();
        Mob mob = Dungeon.level.getRandomMob();
        while(mob != null){
            mob.remove();
            mob = Dungeon.level.getRandomMob();
        }
        Badges.validateBossSlain(Badges.Badge.LICH_SLAIN);
    }


    public void spawnSkulls(){

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

    public void PlayZap() {
        getSprite().zap(getPos(), null);
    }
}
