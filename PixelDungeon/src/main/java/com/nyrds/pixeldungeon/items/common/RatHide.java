package com.nyrds.pixeldungeon.items.common;

import com.watabou.pixeldungeon.items.rings.Artifact;

public class RatHide extends Artifact {


	public RatHide() {
		imageFile = "items/artifacts.png";
		image = 23;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

}
