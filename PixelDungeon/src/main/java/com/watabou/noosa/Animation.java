package com.watabou.noosa;

import android.graphics.RectF;

import java.util.List;

/**
 * Created by mike on 29.03.2016.
 * This file is part of Remixed Pixel Dungeon.
 */
public class Animation {

	public float   delay;
	public RectF[] frames;
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

	public Animation frames(TextureFilm film, int... frames) {
		this.frames = new RectF[frames.length];
		for (int i = 0; i < frames.length; i++) {
			this.frames[i] = film.get(frames[i]);
		}
		return this;
	}

	public Animation clone() {
		return new Animation(Math.round(1 / delay), looped).frames(frames);
	}

	public Animation frames(TextureFilm film, List<Integer> frames, int shift) {
		this.frames = new RectF[frames.size()];
		for (int i = 0; i < frames.size(); i++) {
			this.frames[i] = film.get(frames.get(i) + shift);
		}
		return this;
	}
}
