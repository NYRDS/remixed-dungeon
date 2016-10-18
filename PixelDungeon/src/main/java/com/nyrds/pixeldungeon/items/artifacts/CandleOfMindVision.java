package com.nyrds.pixeldungeon.items.artifacts;

import com.watabou.pixeldungeon.items.rings.Artifact;

public class CandleOfMindVision extends Artifact {

	public CandleOfMindVision() {
		imageFile = "items/artifacts.png";
		image = 21;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
}
