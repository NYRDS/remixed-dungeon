package com.nyrds.pixeldungeon.effects;

import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.effects.DeathRay;
import com.watabou.pixeldungeon.effects.Lightning;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.sprites.MissileSprite;

public class ZapEffect {
    static public void zap(Group parent, int from, int to, String zapEffect)
    {
        Level level = Dungeon.level;
        if (zapEffect != null && level.cellValid(from) && level.cellValid(to)) {
            
            if (!Dungeon.visible[from] && !Dungeon.visible[to]){
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
        }
    }
}
