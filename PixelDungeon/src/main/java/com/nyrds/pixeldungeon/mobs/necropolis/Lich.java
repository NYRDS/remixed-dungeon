package com.nyrds.pixeldungeon.mobs.necropolis;

import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
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
    private static final int SKULLS_MAX	= 3;

    private RunicSkull activatedSkull = null;

    public HashSet<RunicSkull> skulls   = new HashSet<>();

    {
        hp(ht(120));
        EXP = 20;
        defenseSkill = 20;

       // baseSpeed = 0f;

        IMMUNITIES.add( Paralysis.class );
        IMMUNITIES.add( ToxicGas.class );
        IMMUNITIES.add( Terror.class );
        IMMUNITIES.add( Death.class );
        IMMUNITIES.add( Amok.class );
        IMMUNITIES.add( Blindness.class );
        IMMUNITIES.add( Sleep.class );

        SpawnSkulls();
    }

    protected void fx( int cell, Callback callback ) {
        MagicMissile.whiteLight( getSprite().getParent(), getPos(), cell, callback );
        Sample.INSTANCE.play( Assets.SND_ZAP );
        getSprite().setVisible(false);
    }

    private void blink(int epos) {

        int cell = getPos();

        Ballistica.cast(epos, cell, true, false);

        for (int i = 1; i < 4; i++) {
            int next = Ballistica.trace[i + 1];
            if (Dungeon.level.cellValid(next) && (Dungeon.level.passable[next] || Dungeon.level.avoid[next]) && Actor.findChar(next) == null) {
                cell = next;
                Dungeon.observe();
            }
        }

        if (cell != getPos()){
            final int tgt = cell;
            final Char ch = this;
            fx(cell, new Callback() {
                @Override
                public void call() {
                    WandOfBlink.appear(ch, tgt);
                }
            });
        }
    }

    protected void activateRandomSkull(){
        if(activatedSkull !=null){
            activatedSkull.Deactivate();
        }
        RunicSkull skull = getRandomSkull();
        skull.Activate();
        activatedSkull = skull;

    }

    public RunicSkull getRandomSkull() {
        return Random.element(skulls);
    }


    @Override
    protected boolean act() {
        return super.act();
    }

    @Override
    protected boolean canAttack( Char enemy ) {
        return !Dungeon.level.adjacent( getPos(), enemy.getPos() ) && Ballistica.cast( getPos(), enemy.getPos(), false, true ) == enemy.getPos();
    }

    @Override
    protected boolean doAttack(Char enemy) {

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

    @Override
    public int damageRoll() {
        return Random.NormalIntRange( 8, 15 );
    }

    @Override
    public int defenseProc(Char enemy, int damage) {
        blink(enemy.getPos());
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
        Mob mob = Dungeon.level.getRandomMob();
        while(mob != null){
            mob.remove();
            mob = Dungeon.level.getRandomMob();
        }
        //Badges.validateBossSlain(Badges.Badge.BOSS_SLAIN_LICH);
    }

    public void SpawnSkulls(){

        int nSkulls = SKULLS_BY_DEFAULT;
        if(Game.getDifficulty() == 0){
            nSkulls = 2;
        }
        else if(Game.getDifficulty() > 2){
            nSkulls = SKULLS_MAX;
        }

        List<Integer> occupiedPedestals = new ArrayList<Integer>();
        int i = 0;
        while (i < nSkulls){
            int skullCell = Dungeon.level.getRandomTerrainCell(Terrain.PEDESTAL);
            if (Dungeon.level.cellValid(skullCell)) {
                if (!occupiedPedestals.contains(skullCell)) {
                    RunicSkull skulls = RunicSkull.makeNewSkull(i);
                    Dungeon.level.spawnMob(skulls);
                    WandOfBlink.appear(skulls, skullCell);
                    occupiedPedestals.add(skullCell);
                    skulls.add(skulls);
                    i++;
                }
            }
        }

    }

}
