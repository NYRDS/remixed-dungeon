package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ScrollOfDomination extends Scroll {

	@Override
	protected void doRead(@NotNull Char reader) {
		SpellSprite.show( reader, SpellSprite.DOMINATION );
		Sample.INSTANCE.play( Assets.SND_DOMINANCE );
		Invisibility.dispel(reader);
		
		ArrayList<Mob> mobsInSight = new ArrayList<>();

		Level level = Dungeon.level;
		for (Mob mob : level.getCopyOfMobsArray()) {
			if (level.fieldOfView[mob.getPos()] && !(mob.isBoss()) && !mob.isPet() && !(mob instanceof NPC)) {
				mobsInSight.add(mob);
			}
		}
		
		while(!mobsInSight.isEmpty()) {
			Mob pet = Random.element(mobsInSight);

			if(pet.canBePet()) {
				Mob.makePet(pet, reader.getId());
				new Flare(3, 32).show(pet.getSprite(), 2f);
				break;
			}
			mobsInSight.remove(pet);
		}
		
		reader.observe();
		
		setKnown();

		reader.spend( TIME_TO_READ );
	}

	@Override
	public int price() {
		return isKnown() ? 80 * quantity() : super.price();
	}
}
