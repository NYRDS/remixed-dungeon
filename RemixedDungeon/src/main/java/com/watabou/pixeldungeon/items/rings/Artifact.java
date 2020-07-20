package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.modding.Hook;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Artifact extends EquipableItem {
	@Nullable
	protected ArtifactBuff buff;

	@Override
	public Belongings.Slot slot(Belongings belongings) {
		return Belongings.Slot.ARTIFACT;
	}

	@Override
	public void deactivate(Char ch) {
		super.deactivate(ch);

		if(buff!=null) {
			ch.remove(buff);
			buff = null;
		}

		new Hook().Call("onArtifactDeactivate", this, ch);
	}

	public void activate(@NotNull Char ch) {
		super.activate(ch);
		buff = buff();
		if (buff != null) {
			buff.setSource(this);
			buff.attachTo(ch);
		}

		new Hook().Call("onArtifactActivate", this, ch);
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
