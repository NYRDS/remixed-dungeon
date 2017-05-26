package com.watabou.pixeldungeon.items.food;

import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.MimicPie;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.ui.AttackIndicator;

public class PseudoPasty extends Food {

	public PseudoPasty() {
		image = ItemSpriteSheet.PASTY;
		energy = Hunger.STARVING;
	}

	@Override
	public Item pick(Char ch, int pos) {

		Level level = Dungeon.level;

		int spawnPos = level.getEmptyCellNextTo(pos);

		if (!level.cellValid(spawnPos)) {
			return this;
		}

		MimicPie mob = new MimicPie();
		mob.setPos(spawnPos);
		mob.setState(mob.WANDERING);
		mob.adjustStats(Dungeon.depth);

		level.spawnMob(mob);

		Dungeon.hero.checkVisibleMobs();
		AttackIndicator.updateState();

		CellEmitter.get(pos).burst(Speck.factory(Speck.STAR), 10);
		Sample.INSTANCE.play(Assets.SND_MIMIC);

		return null;
	}

	@Override
	public void execute(Hero hero, String action) {
		if (action.equals(AC_EAT)) {
			pick(hero, Dungeon.level.getEmptyCellNextTo(hero.getPos()));
			this.removeItemFrom(hero);
			return;
		}

		super.execute(hero, action);
	}
}