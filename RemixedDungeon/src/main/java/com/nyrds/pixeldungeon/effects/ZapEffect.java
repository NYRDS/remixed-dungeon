package com.nyrds.pixeldungeon.effects;

import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.platform.audio.Sample;
import com.nyrds.util.Util;
import com.watabou.noosa.Group;
import com.watabou.noosa.Visual;
import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.DeathRay;
import com.watabou.pixeldungeon.effects.Lightning;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.particles.ElmoParticle;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.MissileSprite;
import com.watabou.utils.PointF;

import lombok.val;

public class ZapEffect {
    public static final float SPEED	= 240f;

    static public void zap(Group parent, int from, int to, String zapEffect)
    {
        Level level = Dungeon.level;
        if (zapEffect != null && level.cellValid(from) && level.cellValid(to)) {
            
            if (!Dungeon.isCellVisible(from) && !Dungeon.isCellVisible(to)){
                return;
            }

            if(EffectsFactory.isValidEffectName(zapEffect)) {
                attachMissileTeenier(GameScene.clipEffect(from, 1,zapEffect),from,to);
                return;
            }

            if(ItemFactory.isValidItemClass(zapEffect)) {
                ((MissileSprite)parent.recycle( MissileSprite.class )).
                        reset(from, to, ItemFactory.itemByName(zapEffect), Util.nullCallback);
                return;
            }

            if(zapEffect.equals("Lightning")) {
                parent.add(new Lightning(from, to, Util.nullCallback));
                return;
            }

            if(zapEffect.equals("DeathRay")) {
                parent.add(new DeathRay(from, to));
                return;
            }

            if(zapEffect.equals("Shadow")) {
                MagicMissile.shadow(parent, from, to, Util.nullCallback);
                Sample.INSTANCE.play(Assets.SND_ZAP);
                return;
            }

            if(zapEffect.equals("Fire")) {
                MagicMissile.fire(parent, from, to, Util.nullCallback);
                return;
            }

            if(zapEffect.equals("Ice")) {
                MagicMissile.ice(parent, from, to, Util.nullCallback);
                return;
            }

            if(zapEffect.equals("Telekinesis")) {
                MagicMissile.force(parent, from, to, Util.nullCallback);
                return;
            }
        }
    }

    static public void play(Group parent, int pos, String effect) {
        Level level = Dungeon.level;
        if (effect != null && level.cellValid(pos)) {

            if (!Dungeon.isCellVisible(pos)) {
                return;
            }

            if (EffectsFactory.isValidEffectName(effect)) {
                GameScene.clipEffect(pos, 1, effect);
                return;
            }

            val emitter = CellEmitter.get(pos);

            if (effect.equals("Bones")) {
                emitter.burst( Speck.factory( Speck.BONE ), 6 );
            }

            if(effect.equals("Succubus")) {
                emitter.burst( Speck.factory( Speck.HEART ), 6 );
                emitter.burst( ShadowParticle.UP, 8 );
            }

            if(effect.equals("Golem")) {
                emitter.burst( ElmoParticle.FACTORY, 4 );
            }
        }
    }

    private static void attachMissileTeenier(Visual target, int from, int to) {

        target.point( DungeonTilemap.tileToWorld( from ) );
        PointF dest = DungeonTilemap.tileToWorld( to );

        PointF d = PointF.diff( dest, target.point() );
        target.speed.set( d ).normalize().scale(SPEED );

        target.angularSpeed = 0;
        target.angle = (float) (135 - Math.toDegrees(Math.atan2( d.x, d.y )));

        PosTweener tweener = new PosTweener( target, dest, d.length() / SPEED );
        tweener.listener = tweener1 -> target.killAndErase();
        target.getParent().add( tweener );
    }
}
