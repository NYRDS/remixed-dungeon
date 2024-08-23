package com.watabou.pixeldungeon.items.food;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

import org.jetbrains.annotations.NotNull;

public class PseudoPasty extends Food {

	public PseudoPasty() {
		image = ItemSpriteSheet.PASTY;
		energy = Hunger.STARVING;
	}

	@Override
	public Item pick(Char ch, int pos) {
		return CharUtils.tryToSpawnMimic(this,ch, pos, "MimicPie");
	}

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action) {
		if (action.equals(CommonActions.AC_EAT)) {
			pick(chr, chr.level().getEmptyCellNextTo(chr.getPos()));
			this.removeItemFrom(chr);
			return;
		}

		super._execute(chr, action);
	}
}