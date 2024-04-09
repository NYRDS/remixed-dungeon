
package com.watabou.pixeldungeon.plants;

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.watabou.noosa.Gizmo;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Barkskin;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.LeafParticle;
import com.watabou.pixeldungeon.items.Dewdrop;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

public class Plant extends LevelObject {

	public Plant(int pos) {
		super(pos);
		textureFile = Assets.PLANTS;
	}

	public Plant(){
		this(Level.INVALID_CELL);
	}


	@Override
	public boolean stepOn(Char chr) {
		interact(chr);

		if (chr instanceof Hero) {
			Hero hero = (Hero) chr;
			hero.interrupt();
		}
		return true;
	}

	@Override
	public boolean interact(Char ch) {
		if (ch.getSubClass() == HeroSubClass.WARDEN) {
			Buff.affect(ch, Barkskin.class).level(ch.ht() / 3);

			if (Random.Int(5) == 0) {
				Treasury.getLevelTreasury().random(Treasury.Category.SEED).doDrop(ch);
            }
			if (Random.Int(5) == 0) {
				new Dewdrop().doDrop(ch);
			}
		}

		return true;
	}

	@Override
	public void bump(Presser presser) {
		if(presser instanceof Char) {
			interact((Char)presser);
		}

		wither();
		effect(getPos(),presser);
	}

	private void wither() {
		Dungeon.level.remove(this);
		lo_sprite.ifPresent(
				Gizmo::kill);
		if (Dungeon.isCellVisible(pos)) {
			CellEmitter.get(pos).burst(LeafParticle.GENERAL, 6);
		}
	}

	public String desc() {
		return Utils.getClassParam(this.getClass().getSimpleName(), "Desc", Utils.EMPTY_STRING, true);
	}

	@Override
	public String name() {
		return Utils.getClassParam(this.getClass().getSimpleName(), "Name", Utils.EMPTY_STRING, true);
	}

	public void effect(int pos, Presser ch) {
		
	}

	@Override
	public boolean nonPassable(Char ch) {
        return super.nonPassable(ch);
    }

	@Override
	public boolean affectItems() {
		return true;
	}
}
