package com.nyrds.platform.audio;

import com.badlogic.gdx.audio.Sound;
import com.nyrds.platform.EventCollector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLAudioElement;

/**
 * gdx Sound over HTMLAudioElement: one fresh element per play() (they share
 * the browser's HTTP/memory cache, so clones start instantly and the decode
 * happens off the main thread - unlike decodeAudioData, which stalls the
 * game). Pitch maps to playbackRate; pan is not supported by media
 * elements and is folded into volume (the game never pans SFX).
 */
class WebSound implements Sound {

	private final String path;
	private final Map<Long, HTMLAudioElement> voices = new HashMap<>();
	private static long nextId = 1;

	WebSound(String path) {
		this.path = path;
		// warm the browser cache so the first play starts without a fetch
		HTMLAudioElement warm = WebAudio.createElement();
		warm.setSrc(path);
		warm.setPreload("auto");
		WebAudio.load(warm);
	}

	@Override
	public long play() {
		return play(1);
	}

	@Override
	public long play(float volume) {
		return play(volume, 1, 0);
	}

	@Override
	public long play(float volume, float pitch, float pan) {
		HTMLAudioElement voice = WebAudio.createElement();
		voice.setSrc(path);
		voice.setPreload("auto");
		apply(voice, volume, pitch);
		WebAudio.load(voice);
		WebAudio.play(voice);

		long id = nextId++;
		voices.put(id, voice);
		voice.addEventListener("ended", new EventListener<Event>() {
			@Override
			public void handleEvent(Event event) {
				voices.remove(id);
			}
		});
		return id;
	}

	private void apply(HTMLAudioElement voice, float volume, float pitch) {
		try {
			voice.setVolume(clamp(volume));
			voice.setPlaybackRate(Math.max(0.25, Math.min(4, pitch)));
		} catch (Exception e) {
			EventCollector.logException(e, "sound apply " + path);
		}
	}

	private static float clamp(float v) {
		return Math.max(0, Math.min(1, v));
	}

	private HTMLAudioElement find(long soundId) {
		return voices.get(soundId);
	}

	@Override
	public long loop() {
		return loop(1);
	}

	@Override
	public long loop(float volume) {
		return loop(volume, 1, 0);
	}

	@Override
	public long loop(float volume, float pitch, float pan) {
		long id = play(volume, pitch, pan);
		HTMLAudioElement voice = find(id);
		if (voice != null) {
			voice.setLoop(true);
		}
		return id;
	}

	@Override
	public void stop() {
		for (HTMLAudioElement voice : all()) {
			halt(voice);
		}
		voices.clear();
	}

	@Override
	public void pause() {
		for (HTMLAudioElement voice : all()) {
			voice.pause();
		}
	}

	@Override
	public void resume() {
		for (HTMLAudioElement voice : all()) {
			if (voice.isPaused()) {
				WebAudio.play(voice);
				WebAudio.requeueIfBlocked(voice);
			}
		}
	}

	@Override
	public void dispose() {
		stop();
	}

	@Override
	public void stop(long soundId) {
		HTMLAudioElement voice = find(soundId);
		if (voice != null) {
			halt(voice);
			voices.remove(soundId);
		}
	}

	@Override
	public void pause(long soundId) {
		HTMLAudioElement voice = find(soundId);
		if (voice != null) {
			voice.pause();
		}
	}

	@Override
	public void resume(long soundId) {
		HTMLAudioElement voice = find(soundId);
		if (voice != null && voice.isPaused()) {
			WebAudio.play(voice);
			WebAudio.requeueIfBlocked(voice);
		}
	}

	@Override
	public void setLooping(long soundId, boolean looping) {
		HTMLAudioElement voice = find(soundId);
		if (voice != null) {
			voice.setLoop(looping);
		}
	}

	@Override
	public void setPitch(long soundId, float pitch) {
		HTMLAudioElement voice = find(soundId);
		if (voice != null) {
			try {
				voice.setPlaybackRate(Math.max(0.25, Math.min(4, pitch)));
			} catch (Exception e) {
				EventCollector.logException(e, "sound pitch " + path);
			}
		}
	}

	@Override
	public void setVolume(long soundId, float volume) {
		HTMLAudioElement voice = find(soundId);
		if (voice != null) {
			try {
				voice.setVolume(clamp(volume));
			} catch (Exception e) {
				EventCollector.logException(e, "sound volume " + path);
			}
		}
	}

	@Override
	public void setPan(long soundId, float pan, float volume) {
		setVolume(soundId, volume);
	}

	private void halt(HTMLAudioElement voice) {
		try {
			voice.pause();
			voice.setCurrentTime(0);
		} catch (Exception e) {
			EventCollector.logException(e, "sound stop " + path);
		}
	}

	private List<HTMLAudioElement> all() {
		return new ArrayList<>(voices.values());
	}
}
