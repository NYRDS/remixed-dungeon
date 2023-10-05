package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Artifact extends EquipableItem {

	@Override
	public Belongings.Slot slot(Belongings belongings) {
		return Belongings.Slot.ARTIFACT;
	}

	public void activate(@NotNull Char ch) {
		super.activate(ch);
		Buff buff = buff();
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
	public ArtifactBuff buff() {
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
        GLog.n(Utils.format(R.string.Ring_Info2, name()));
	}
}
