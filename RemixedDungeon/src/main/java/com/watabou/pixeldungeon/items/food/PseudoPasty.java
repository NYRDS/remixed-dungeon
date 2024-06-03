package com.watabou.pixeldungeon.items.food;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.mobs.MimicPie;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

import org.jetbrains.annotations.NotNull;

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
		mob.setState(MobAi.getStateByClass(Wandering.class));

		level.spawnMob(mob);

		ch.checkVisibleEnemies();

		CellEmitter.get(pos).burst(Speck.factory(Speck.STAR), 10);
		Sample.INSTANCE.play(Assets.SND_MIMIC);

		return ItemsList.DUMMY;
	}

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action) {
		if (action.equals(CommonActions.AC_EAT)) {
			pick(chr, Dungeon.level.getEmptyCellNextTo(chr.getPos()));
			this.removeItemFrom(chr);
			return;
		}

		super._execute(chr, action);
	}
}