package com.nyrds.platform.audio;

import com.badlogic.gdx.audio.Music;
import com.nyrds.platform.EventCollector;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLAudioElement;

/**
 * gdx Music over HTMLAudioElement: the browser streams and decodes off the
 * main thread; loop, pause/resume and position tracking are native.
 */
class WebMusic implements Music {

	private final String path;
	private HTMLAudioElement element;
	private boolean started;

	private boolean looping;
	private float volume = 1;
	private OnCompletionListener completionListener;

	WebMusic(String path) {
		this.path = path;
		element = WebAudio.createElement();
		element.setSrc(path);
		element.setPreload("auto");
		element.setLoop(false);
		element.addEventListener("ended", new EventListener<Event>() {
			@Override
			public void handleEvent(Event event) {
				if (!looping) {
					if (completionListener != null) {
						completionListener.onCompletion(this0());
					}
				}
			}
		});
	}

	private WebMusic this0() {
		return this;
	}

	@Override
	public void play() {
		if (element == null) {
			return;
		}
		if (element.isPaused()) {
			WebAudio.play(element);
			WebAudio.requeueIfBlocked(element);
			started = true;
		}
	}

	@Override
	public void pause() {
		if (element != null && !element.isPaused()) {
			element.pause();
		}
	}

	@Override
	public void stop() {
		if (element != null) {
			element.pause();
			try {
				element.setCurrentTime(0);
			} catch (Exception e) {
				EventCollector.logException(e, "music stop " + path);
			}
		}
		started = false;
	}

	@Override
	public boolean isPlaying() {
		return element != null && started && !element.isPaused() && !element.isEnded();
	}

	@Override
	public void setLooping(boolean isLooping) {
		looping = isLooping;
		if (element != null) {
			element.setLoop(isLooping);
		}
	}

	@Override
	public boolean isLooping() {
		return looping;
	}

	@Override
	public void setVolume(float vol) {
		volume = vol;
		if (element != null) {
			try {
				element.setVolume(Math.max(0, Math.min(1, vol)));
			} catch (Exception e) {
				EventCollector.logException(e, "music volume " + path);
			}
		}
	}

	@Override
	public float getVolume() {
		return volume;
	}

	@Override
	public void setPan(float leftVolume, float rightVolume) {
		setVolume(Math.max(leftVolume, rightVolume));
	}

	@Override
	public void setPosition(float position) {
		if (element != null) {
			try {
				element.setCurrentTime(position);
			} catch (Exception e) {
				EventCollector.logException(e, "music seek " + path);
			}
		}
	}

	@Override
	public float getPosition() {
		return element != null ? (float) element.getCurrentTime() : 0;
	}

	@Override
	public void dispose() {
		if (element != null) {
			element.pause();
			element = null;
		}
		completionListener = null;
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		completionListener = listener;
	}
}
