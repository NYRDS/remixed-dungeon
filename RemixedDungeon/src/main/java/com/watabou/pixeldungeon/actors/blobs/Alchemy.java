
package com.watabou.pixeldungeon.actors.blobs;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.Splash;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.plants.Seed;
import com.watabou.utils.Random;

public class Alchemy extends Blob {

	private static final int SEEDS_TO_POTION = 3;

	@Override
	protected void evolve() {
		for (int i = 0;i<getLength();i++) {
			off[i] = cur[i];
			if(cur[i]>0) {
				setVolume(getVolume() + cur[i]);
				if (Dungeon.isCellVisible(i)) {
					Journal.add(Journal.Feature.ALCHEMY.desc());
				}
			}
		}
	}

	@LuaInterface
	public static void transmute( int cell ) {
		final Level level = Dungeon.level;

		Heap heap = level.getHeap( cell );
		if (heap != null) {

			Item result = ItemsList.DUMMY;

			CellEmitter.get(heap.pos).burst(Speck.factory(Speck.BUBBLE), 3);
			Splash.at(heap.pos, 0xFFFFFF, 3);

			float[] chances = new float[heap.items.size()];
			int count = 0;

			int index = 0;
			for (Item item : heap.items) {
				if (item instanceof Seed) {
					count += item.quantity();
					chances[index++] = item.quantity();
				} else {
					count = 0;
					break;
				}
			}

			if (count >= SEEDS_TO_POTION) {

				CellEmitter.get(heap.pos).burst(Speck.factory(Speck.WOOL), 6);
				Sample.INSTANCE.play(Assets.SND_PUFF);

				if (Random.Int(count) == 0) {

					CellEmitter.center(heap.pos).burst(Speck.factory(Speck.EVOKE), 3);

					heap.destroy();

					Statistics.potionsCooked++;
					Badges.validatePotionsCooked();

					result = Treasury.getLevelTreasury().random(Treasury.Category.POTION);

				} else {

					Seed proto = (Seed) heap.items.get(Random.chances(chances));
					Class<? extends Item> itemClass = proto.alchemyClass;

					heap.destroy();

					Statistics.potionsCooked++;
					Badges.validatePotionsCooked();

					if (itemClass == null) {
						result = Treasury.getLevelTreasury().random(Treasury.Category.POTION);
					} else {
						try {
							result = itemClass.newInstance();
						} catch (Exception e) {

						}
					}
				}
			}

			if (result.valid()) {
				level.animatedDrop( result, cell );
			}
		}
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );	
		emitter.start( Speck.factory( Speck.BUBBLE ), 0.4f, 0 );
	}
}
