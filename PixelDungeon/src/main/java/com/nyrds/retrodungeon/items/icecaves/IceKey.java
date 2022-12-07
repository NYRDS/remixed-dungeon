package com.nyrds.retrodungeon.items.icecaves;

import com.watabou.pixeldungeon.items.rings.Artifact;

public class IceKey extends Artifact {


	public IceKey() {
		imageFile = "items/artifacts.png";
		image = 22;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

}
