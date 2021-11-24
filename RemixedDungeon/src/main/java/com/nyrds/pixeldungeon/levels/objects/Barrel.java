package com.nyrds.pixeldungeon.levels.objects;

import androidx.annotation.Keep;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.LiquidFlame;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;

import org.json.JSONObject;

/**
 * Created by mike on 01.07.2016.
 */
public class Barrel extends LevelObject {

	private boolean burned = false;

	@Keep
	public Barrel() {
		this(-1);
	}

	public Barrel(int pos) {
		super(pos);
		textureFile = "levelObjects/barrels.png";
	}

	@Override
	void setupFromJson(Level level, JSONObject obj) {
	}

	@Override
	public boolean pushable(Char hero) {
		return true;
	}

	@Override
	public void burn() {

		if (burned) {
			return;
		}

		burned = true;

		sprite.playAnim(10, false, () -> remove(), image() + 0, image() + 1, image() + 2, image() + 3, image() + 4);


		Sample.INSTANCE.play(Assets.SND_EXPLOSION);

		LiquidFlame fire = Blob.seed(getPos(), 10, LiquidFlame.class);
		GameScene.add(fire);
	}

	@Override
	public void bump(Presser presser) {
		burn();
	}

	@Override
	public String desc() {

		if (ModdingMode.isHalloweenEvent()) {
            return StringsManager.getVar(R.string.Barrel_Pumpkin_Desc);
        } else {
            return StringsManager.getVar(R.string.Barrel_Desc);
        }

	}

	@Override
	public String name() {
		if (ModdingMode.isHalloweenEvent()) {
            return StringsManager.getVar(R.string.Barrel_Pumpkin_Name);
        } else {
            return StringsManager.getVar(R.string.Barrel_Name);
        }
	}

	@Override
	public int image() {
		if (ModdingMode.isHalloweenEvent()) {
			return 0;
		} else {
			return 8;
		}
	}

	@Override
	public boolean affectLevelObjects() {
		return true;
	}

	@Override
	public boolean nonPassable(Char ch) {
		if(ch.valid()) {
			return true;
		}
		return false;
	}
}
