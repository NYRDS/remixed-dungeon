package com.watabou.pixeldungeon.items;

import com.nyrds.pixeldungeon.mobs.spiders.SpiderServant;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.effects.Wound;
import com.watabou.pixeldungeon.items.rings.UsableArtifact;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.plants.Sungrass.Health;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

import org.jetbrains.annotations.NotNull;

public class SpiderCharm extends UsableArtifact {

	public SpiderCharm() {
		image = ItemSpriteSheet.SPIDER_CHARM;
	}

	private static final Glowing WHITE = new Glowing( 0xFFFFFF );
	
	@Override
	public Glowing glowing() {
		return WHITE;
	}
	
	@Override
	public void _execute(@NotNull final Char ch, @NotNull String action ) {

		if (action.equals( AC_USE )) {
			Wound.hit(ch);
			ch.damage(ch.ht()/4, this);
			Buff.detach(ch, Health.class);
			Level level = ch.level();

			int spawnPos = level.getEmptyCellNextTo(ch.getPos());
			
			if (level.cellValid(spawnPos)) {
				Mob pet = Mob.makePet(new SpiderServant(), ch.getId());
				pet.setPos(spawnPos);
				
				level.spawnMob(pet );
				Actor.addDelayed( new Pushing( pet, ch.getPos(), pet.getPos() ), -1 );
			}
			return;
		}
		super._execute( ch, action );
	}
}
