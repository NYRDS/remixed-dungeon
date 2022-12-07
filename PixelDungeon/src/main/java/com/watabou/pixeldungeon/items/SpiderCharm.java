package com.watabou.pixeldungeon.items;

import com.nyrds.retrodungeon.mobs.spiders.SpiderServant;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.effects.Wound;
import com.watabou.pixeldungeon.items.rings.UsableArtifact;
import com.watabou.pixeldungeon.plants.Sungrass.Health;
import com.watabou.pixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

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
	public void execute( final Hero ch, String action ) {
		setCurUser(ch);
		
		if (action.equals( AC_USE )) {
			Wound.hit(ch);
			ch.damage(ch.ht()/4, this);
			Buff.detach(ch, Health.class);
			
			int spawnPos = Dungeon.level.getEmptyCellNextTo(ch.getPos());
			
			if (Dungeon.level.cellValid(spawnPos)) {
				Mob pet = Mob.makePet(new SpiderServant(), getCurUser());
				pet.setPos(spawnPos);
				
				Dungeon.level.spawnMob(pet );
				Actor.addDelayed( new Pushing( pet, ch.getPos(), pet.getPos() ), -1 );
			}
			return;
		}
		super.execute( ch, action );
	}
}
