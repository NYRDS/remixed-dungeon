package com.watabou.pixeldungeon.items.weapon.melee;

import java.util.ArrayList;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.KusarigamaChain;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

public class Kusarigama extends SpecialWeapon {

	private static final String AC_PULL = Game.getVar(R.string.Kusarigama_Pull);
	private static final float TIME_TO_IMPALE = 1.5f;

	public Kusarigama() {
		super(3, 2f, 1f);

		image = 0;
		imageFile = "items/kusarigama.png";

		range = 2;
	}

	protected static CellSelector.Listener impaler = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {

			if (target != null) {
				curUser.spendAndNext(TIME_TO_IMPALE);
				int hitCell = Ballistica.cast(curUser.pos, target, false, true);
				
				if(hitCell == curUser.pos) {
					return;
				}
				
				if (Dungeon.level.distance(curUser.pos, hitCell) < 4) {
					Char chr = Actor.findChar(hitCell);
					
					if (chr != null) {
						target = chr.pos;

						chr.move(Ballistica.trace[1]);
						chr.getSprite().move(chr.pos, Ballistica.trace[1]);

						Dungeon.observe();
					}

					drawChain(hitCell);
				} else {
					drawChain(Ballistica.trace[4]);
				}
			}

		}

		@Override
		public String prompt() {
			return TXT_DIR_THROW;
		}
	};

	private static void drawChain(int tgt) {
		curUser.getSprite()
				.getParent()
				.add(new KusarigamaChain(curUser.getSprite().center(),
						DungeonTilemap.tileCenterToWorld(tgt)));
	}

	@Override
	public void execute(Hero hero, String action) {
		curUser = hero;
		if (action.equals(AC_PULL)) {
			GameScene.selectCell(impaler);
		} else {
			super.execute(hero, action);
		}
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (isEquipped(hero)) {
			actions.add(AC_PULL);
		}
		return actions;
	}

	public void applySpecial(Hero hero, Char tgt) {
		curUser = hero;

		if (Dungeon.level.distance(curUser.pos, tgt.pos) > 1) {
			drawChain(tgt.pos);
		}

		if (Random.Float(1) < 0.1f) {
			Buff.prolong(tgt, Vertigo.class, 3);
		}
	}
}
