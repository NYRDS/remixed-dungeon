package com.nyrds.pixeldungeon.items.common.armor;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Web;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

public class SpiderArmor extends Armor {

	public SpiderArmor() {
		super( 3 );
		image = 21;
		hasHelmet = true;
	}

	@Override
	public int defenceProc(Char attacker, Char defender, int damage) {
		if (Random.Int(100) < 50) {
			GameScene.add(Blob.seed(defender.getPos(), Random.Int(5, 7), Web.class));
		}
		return super.defenceProc(attacker, defender, damage);
	}
}
