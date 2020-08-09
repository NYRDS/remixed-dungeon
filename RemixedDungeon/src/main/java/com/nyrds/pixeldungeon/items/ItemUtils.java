package com.nyrds.pixeldungeon.items;

import com.nyrds.pixeldungeon.utils.CharsList;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class ItemUtils {
    public static void throwItemAway(int pos) {
		Heap heap = Dungeon.level.getHeap( pos );
		if(heap!=null) {
			Item item = heap.pickUp();
			int cell = Dungeon.level.getEmptyCellNextTo(pos);
			if (Dungeon.level.cellValid(cell)) {
				Dungeon.level.animatedDrop(item,cell);
			}
		}
	}

    public static void evoke(@NotNull Char hero) {
        hero.getSprite().emitter().burst(Speck.factory(Speck.EVOKE), 5);
    }

	public static void equipCursed(@NotNull Char chr) {
		chr.getSprite().emitter().burst( ShadowParticle.CURSE, 6 );
		Sample.INSTANCE.play( Assets.SND_CURSED );
	}

	public static boolean usableAsArmor(@NotNull EquipableItem item) {
    	return item.slot(new Belongings(CharsList.DUMMY)) == Belongings.Slot.ARMOR;
	}

	public static boolean usableAsWeapon(@NotNull EquipableItem item) {
		return item.slot(new Belongings(CharsList.DUMMY)) == Belongings.Slot.WEAPON;
	}

    public static void tintBackground(@NotNull Item item, @NotNull ColorBlock bg) {
        if (item.isCursed() && item.isCursedKnown()) {
            bg.ra = +0.2f;
            bg.ga = -0.1f;
        } else if (!item.isIdentified()) {
            bg.ra = 0.1f;
            bg.ba = 0.1f;
        }
    }

    public static Item random(Item item) {
        if (Random.Float() < 0.4) {
            int n = 1;
            if (Random.Int( 3 ) == 0) {
                n++;
                if (Random.Int( 3 ) == 0) {
                    n++;
                }
            }
            if (Random.Int( 2 ) == 0) {
                item.upgrade( n );
            } else {
                item.degrade( n );
                item.setCursed(true);
            }
        }
        return item;
    }
}
