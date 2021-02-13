package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

class TeleportCellListener implements CellSelector.Listener {

    @Override
    public void onSelect(Integer target, @NotNull Char selector) {
        if (target != null) {

            Level level = selector.level();

            if (!level.fieldOfView[target] ||
                !(level.passable[target] || level.avoid[target]) ||
                Actor.findChar( target ) != null || level.getLevelObject(target) != null) {

                GLog.w( Game.getVar(R.string.RogueArmor_Fov) );
                return;
            }

            for (Mob mob : level.getCopyOfMobsArray()) {
                if (level.fieldOfView[mob.getPos()] && !(mob instanceof NPC)) {
                    Buff.prolong( mob, Blindness.class, 2 );
                    mob.setState(MobAi.getStateByClass(Wandering.class));
                    mob.getSprite().emitter().burst( Speck.factory( Speck.LIGHT ), 4 );
                }
            }

            WandOfBlink.appear( selector, target );
            CellEmitter.get( target ).burst( Speck.factory( Speck.WOOL ), 10 );
            Sample.INSTANCE.play( Assets.SND_PUFF );
            level.press( target, selector );
            Dungeon.observe();

            selector.spendAndNext( Actor.TICK );
        }
    }

    @Override
    public String prompt() {
        return Game.getVar(R.string.RogueArmor_Prompt);
    }

    @Override
    public Image icon() {
        return null;
    }
}
