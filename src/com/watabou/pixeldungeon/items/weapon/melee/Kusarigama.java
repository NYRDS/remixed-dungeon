package com.watabou.pixeldungeon.items.weapon.melee;

import java.util.ArrayList;

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

	public static final String AC_IMPALE = "IMPALE";

	public Kusarigama() {
		super(3, 2f, 1f);

		image = 0;
		imageFile = "items/kusarigama.png";

		range = 2;
	}

	protected static CellSelector.Listener impaler = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			
			if (target != null
					&& Dungeon.level.distance(curUser.pos, target) <= 4) {
				Char chr = Actor.findChar(target);
				if (chr != null) {
					curUser.getSprite()
							.getParent()
							.add(new KusarigamaChain(curUser.getSprite()
									.center(), DungeonTilemap
									.tileCenterToWorld(target)));

					Ballistica.cast(curUser.pos, target, true, false);

					chr.move(Ballistica.trace[1]);
					chr.getSprite().move(chr.pos, Ballistica.trace[1]);
					
					curUser.spendAndNext(TIME_TO_THROW);
					
					Dungeon.observe();
				}
			}
		}

		@Override
		public String prompt() {
			return TXT_DIR_THROW;
		}
	};

	@Override
	public void execute(Hero hero, String action) {
		curUser = hero;
		if (action.equals(AC_IMPALE)) {
			GameScene.selectCell(impaler);
		} else {
			super.execute(hero, action);
		}
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (isEquipped(hero)) {
			actions.add(AC_IMPALE);
		}
		return actions;
	}

	public void applySpecial(Hero hero, Char tgt) {
		curUser = hero;
		
		if(Dungeon.level.distance(curUser.pos, tgt.pos) > 1) {
			curUser.getSprite()
			.getParent()
			.add(new KusarigamaChain(curUser.getSprite()
					.center(), DungeonTilemap
					.tileCenterToWorld(tgt.pos)));
		}
		
		if (Random.Float(1) < 0.1f) {
			Buff.prolong(tgt, Vertigo.class, 3);
		}
	}
}
