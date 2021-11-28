package com.nyrds.pixeldungeon.effects;

import com.nyrds.LuaInterface;
import com.nyrds.util.JsonHelper;
import com.watabou.noosa.Animation;
import com.watabou.noosa.Image;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomClipEffect extends MovieClip implements MovieClip.Listener, ICustomEffect {

	private Callback    onAnimComplete;
	private PointF      centerShift;
	private TextureFilm film;

	public CustomClipEffect(){
	}

	public CustomClipEffect(Object texture, int xs, int ys) {
		init(texture, xs, ys);
	}

	private void init(Object texture, int xs, int ys) {
		texture(texture);

		film = new TextureFilm(texture,xs,ys);
		centerShift = new PointF(-(xs - DungeonTilemap.SIZE) / 2.f,
				-(ys-DungeonTilemap.SIZE) / 2.f);
		origin.set(xs / 2.f, ys / 2.f);
	}

	public void place(int cell) {
		PointF p = DungeonTilemap.tileToWorld(cell);
		x = p.x + centerShift.x;
		y = p.y + centerShift.y + Image.isometricShift();
	}

	@LuaInterface
	public void playWithOffset(int offset) {
		Animation anim = curAnim.frames(offset, film, curAnim.framesIndexes);
		play(anim, true);
	}

	public void playAnimOnce() {
		if(!curAnim.looped) {
			onAnimComplete = this::killAndErase;
		}
		listener = this;
		play(curAnim,true);
	}

	@Override
	public void setupFromJson(JSONObject json) throws JSONException {
		init(json.getString("texture"),
				json.getInt("width"),
				json.getInt("height"));

		curAnim = JsonHelper.readAnimation(json,"anim",film,0);
	}

	public void playAnim(Animation anim,Callback animComplete) {
		onAnimComplete = animComplete;
		listener = this;
		play(anim, true);
	}

	@Override
	public void onComplete(Animation anim) {
		if (onAnimComplete != null) {
			onAnimComplete.call();
			onAnimComplete = null;
		}
	}
}
