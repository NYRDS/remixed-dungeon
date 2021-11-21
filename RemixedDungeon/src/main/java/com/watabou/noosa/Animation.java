package com.watabou.noosa;

import com.nyrds.platform.compatibility.RectF;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.util.List;

/**
 * Created by mike on 29.03.2016.
 * This file is part of Remixed Pixel Dungeon.
 */
public class Animation {

	public float   delay;
	public RectF[] frames;
	public int[] framesIndexes;
	public boolean looped;

	public Animation(int fps, boolean looped) {
		this.delay = 1f / fps;
		this.looped = looped;
	}

	public Animation frames(RectF... frames) {
		this.frames = frames;
		return this;
	}

	public Animation frames(int shift, TextureFilm film, int... frames) {
		this.frames = new RectF[frames.length];
		for (int i = 0; i < frames.length; i++) {
			this.frames[i] = film.get(frames[i] + shift);
		}
		return this;
	}

	public void frames(TextureFilm film, int... frames) {
		this.frames = new RectF[frames.length];
		for (int i = 0; i < frames.length; i++) {
			this.frames[i] = film.get(frames[i]);
		}
	}

	@NotNull
	public Animation clone() {
		Animation ret = new Animation(Math.round(1 / delay), looped).frames(frames);
		ret.framesIndexes = framesIndexes;
		return ret;
	}


	public void frames(TextureFilm film, List<Integer> frames, int shift) throws JSONException {
		this.frames = new RectF[frames.size()];
		framesIndexes = new int[frames.size()];

		for (int i = 0; i < frames.size(); i++) {
			framesIndexes[i] = frames.get(i);
			this.frames[i] = film.get(frames.get(i) + shift);
			if(this.frames[i]==null) {
				throw new JSONException("no frame "+ (i + shift) +" in film");
			}
		}
	}


	public static class AnimationSeq {
		public int     fps;
		public int[]   frames;
		public boolean looped;
	}
}
