package com.nyrds.platform.audio;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import org.teavm.jso.JSBody;
import org.teavm.jso.dom.html.HTMLAudioElement;

/**
 * Gdx.audio for the web: the TeaVM backend leaves Gdx.audio null, so the
 * platform audio managers were silent no-ops. Playback is HTMLAudioElement
 * based - the browser fetches and decodes off the main thread. (Web Audio's
 * decodeAudioData was measured blocking the game thread for hundreds of ms
 * per file, which made the first attack on every level feel laggy; it is
 * deliberately not used here.)
 * <p>
 * Autoplay policy: play() before any user gesture is rejected by the
 * browser. Elements whose play was blocked are retried by the one-time
 * pointerdown/keydown hook.
 */
public class WebAudio implements Audio {

	@JSBody(params = {}, script = "return document.createElement('audio');")
	static native HTMLAudioElement createElement();

	@JSBody(params = "e", script = "e.load();")
	static native void load(HTMLAudioElement e);

	@JSBody(params = "e", script =
			"try { var p = e.play(); if (p && p['catch']) { p['catch'](function() {"
			+ " var a = window.__blockedAudio || (window.__blockedAudio = []);"
			+ " a.push(e); }); } } catch (ignored) {}")
	static native void play(HTMLAudioElement e);

	@JSBody(params = "e", script =
			"if (e.paused && !e.ended) {"
			+ " var a = window.__blockedAudio || (window.__blockedAudio = []);"
			+ " a.push(e); }")
	static native void requeueIfBlocked(HTMLAudioElement e);

	@JSBody(params = {}, script =
			"var a = window.__blockedAudio;"
			+ "if (a && a.length) {"
			+ " for (var i = 0; i < a.length; i++) { try { a[i].play(); } catch (ignored) {} }"
			+ " a.length = 0; }")
	static native void retryBlocked();

	@JSBody(params = {}, script =
			"if (window.__rdAudioHook) { return; }"
			+ "window.__rdAudioHook = true;"
			+ "var h = function() {"
			+ " var a = window.__blockedAudio;"
			+ " if (a && a.length) {"
			+ "  for (var i = 0; i < a.length; i++) { try { a[i].play(); } catch (ignored) {} }"
			+ "  a.length = 0; }"
			+ " window.removeEventListener('pointerdown', h);"
			+ " window.removeEventListener('keydown', h); };"
			+ "window.addEventListener('pointerdown', h);"
			+ "window.addEventListener('keydown', h);")
	private static native void installGestureHook();

	@Override
	public Sound newSound(FileHandle fileHandle) {
		installGestureHook();
		return new WebSound(fileHandle.path());
	}

	@Override
	public Music newMusic(FileHandle fileHandle) {
		installGestureHook();
		return new WebMusic(fileHandle.path());
	}

	@Override
	public AudioDevice newAudioDevice(int sampleRate, boolean isMono) {
		// nothing in the game needs raw PCM output; swallow writes
		return new AudioDevice() {
			@Override
			public void writeSamples(short[] samples, int offset, int numSamples) {
			}

			@Override
			public void writeSamples(float[] samples, int offset, int numSamples) {
			}

			@Override
			public void setVolume(float volume) {
			}

			@Override
			public void pause() {
			}

			@Override
			public void resume() {
			}

			@Override
			public void dispose() {
			}

			@Override
			public boolean isMono() {
				return isMono;
			}

			@Override
			public int getLatency() {
				return 0;
			}
		};
	}

	@Override
	public AudioRecorder newAudioRecorder(int samplerate, boolean isMono) {
		throw new UnsupportedOperationException("audio recording is not supported on web");
	}

	@Override
	public boolean switchOutputDevice(String outputDevice) {
		return false;
	}

	@Override
	public String[] getAvailableOutputDevices() {
		return new String[0];
	}
}
