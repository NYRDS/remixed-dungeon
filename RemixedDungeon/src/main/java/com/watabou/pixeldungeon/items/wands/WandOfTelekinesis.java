
package com.watabou.pixeldungeon.items.wands;

import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Callback;

public class WandOfTelekinesis extends Wand {

	{
		hitChars   = false;
		hitObjects = true;
	}

	@Override
	protected void onZap(int cell, Char victim) {

		boolean mapUpdated = false;

		int maxDistance = effectiveLevel() + 4;
		Ballistica.distance = Math.min(Ballistica.distance, maxDistance);

		Char ch;
		Heap heap = null;

		Level level = Dungeon.level;

		for (int i = 1; i < Ballistica.distance; i++) {

			int c = Ballistica.trace[i];

			int before = level.map[c];

			if ((ch = Actor.findChar(c)) != null) {

				if (i == Ballistica.distance - 1) {

					ch.damage(maxDistance - 1 - i, this);

				} else {
					int next = Ballistica.trace[i + 1];
					if (ch.isMovable() && (level.passable[next] || level.avoid[next]) && Actor.findChar(next) == null) {

                        ch.placeTo(next);
                        ch.getSprite().move(ch.getPos(), next);
                        ch.observe();
					} else {
						ch.damage(maxDistance - 1 - i, this);
					}
				}
			}

			if (heap == null && (heap = level.getHeap(c)) != null) {
				switch (heap.type) {
					case HEAP:
						if (getOwner() instanceof Hero) {
							transport(heap);
						}
						break;
					case CHEST:
					case MIMIC:
						heap.open(getOwner());
						break;
					default:
				}
			}

			level.press(c, new Effect());

			if (before == Terrain.OPEN_DOOR && Actor.findChar(c) == null) {
				level.set(c, Terrain.DOOR);
				GameScene.updateMap(c);
				GameScene.updateMap(c - level.getWidth());
			} else if (level.water[c]) {
				GameScene.ripple(c);
			}

			if (!mapUpdated && level.map[c] != before) {
				mapUpdated = true;
			}

			LevelObject levelObject;
			if((levelObject = level.getTopLevelObject(c) )!=null) {
				levelObject.push(getOwner());
			}
		}

		if (mapUpdated) {
			Dungeon.observe();
		}
	}

	private void transport(Heap heap) {
		Item item = heap.pickUp();
		Char owner = getOwner();
		item = item.pick(owner, heap.pos);
		if (item != null) {
			owner.collectAnimated(item);
		}
	}

	protected void fx(int cell, Callback callback) {
		MagicMissile.force(getOwner().getSprite().getParent(), getOwner().getPos(), cell, callback);
		Sample.INSTANCE.play(Assets.SND_ZAP);
	}

	@Override
	public String desc() {
        return StringsManager.getVar(R.string.WandOfTelekinesis_Info);
    }

	public static class Effect implements Presser {
		@Override
		public boolean affectLevelObjects() {
			return false;
		}
	}
}
