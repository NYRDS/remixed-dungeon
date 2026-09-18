
package com.watabou.pixeldungeon.items.armor.glyphs;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.armor.Armor.Glyph;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.GameMath;
import com.watabou.utils.Random;

public class Affection extends Glyph {

	private static final Glowing PINK = new Glowing(0xFF4488);

	@Override
	public int defenceProc(Armor armor, Char attacker, Char defender, int damage) {

		int level = (int) GameMath.gate(0, armor.level(), 6);

		if (attacker.adjacent(defender) && Random.Int(level / 2 + 5) >= 4) {

			int duration = Random.IntRange(2, 5);

			Buff.affect(attacker, BuffFactory.CHARM, CharUtils.charmDurationFactor(attacker) * duration);
			attacker.getSprite().centerEmitter().start(Speck.factory(Speck.HEART), 0.2f, 5);

			Buff.affect(defender, BuffFactory.CHARM, Random.Float(CharUtils.charmDurationFactor(defender) * duration / 2, duration));
			defender.getSprite().centerEmitter().start(Speck.factory(Speck.HEART), 0.2f, 5);
		}

		return damage;
	}

	@Override
	public String name(String weaponName) {
        return Utils.format(R.string.Affection_Txt, weaponName);
	}

	@Override
	public Glowing glowing() {
		return PINK;
	}
}
