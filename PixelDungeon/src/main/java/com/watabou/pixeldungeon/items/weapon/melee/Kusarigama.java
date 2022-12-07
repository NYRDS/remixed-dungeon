package com.watabou.pixeldungeon.items.weapon.melee;

import com.nyrds.retrodungeon.ml.R;
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

import java.util.ArrayList;

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
				getCurUser().spendAndNext(TIME_TO_IMPALE);
				int hitCell = Ballistica.cast(getCurUser().getPos(), target, false, true);
				
				if(hitCell == getCurUser().getPos()) {
					return;
				}
				
				if (Dungeon.level.distance(getCurUser().getPos(), hitCell) < 4) {
					Char chr = Actor.findChar(hitCell);
					
					if (chr != null && chr.isMovable()) {
						chr.move(Ballistica.trace[1]);
						chr.getSprite().move(chr.getPos(), Ballistica.trace[1]);

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
		getCurUser().getSprite()
				.getParent()
				.add(new KusarigamaChain(getCurUser().getSprite().center(),
						DungeonTilemap.tileCenterToWorld(tgt)));
	}

	@Override
	public void execute(Hero hero, String action) {
		setCurUser(hero);
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
		setCurUser(hero);

		if (Dungeon.level.distance(getCurUser().getPos(), tgt.getPos()) > 1) {
			drawChain(tgt.getPos());
		}

		if (Random.Float(1) < 0.1f) {
			Buff.prolong(tgt, Vertigo.class, 3);
		}
	}
}
