package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.Nullable;

public class Artifact extends EquipableItem {

	public static final float TIME_TO_EQUIP = 1f;

	@Nullable
	protected ArtifactBuff buff;

	@Override
	protected Belongings.Slot slot() {
		return Belongings.Slot.ARTIFACT;
	}

	@Override
	public void deactivate(Char ch) {
		if(buff!=null) {
			ch.remove(buff);
			buff = null;
		}
	}

	public void activate(Char ch) {
		buff = buff();
		if (buff != null) {
			buff.setSource(this);
			buff.attachTo(ch);
		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Nullable
	protected ArtifactBuff buff() {
		return null;
	}

	public String getText() {
		return null;
	}

	public int getColor() {
		return 0;
	}

	@Override
	public void equippedCursed() {
		GLog.n(Utils.format(Game.getVar(R.string.Ring_Info2), name()));
	}
}
