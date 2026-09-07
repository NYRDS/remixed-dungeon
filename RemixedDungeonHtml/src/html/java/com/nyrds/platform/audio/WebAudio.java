package com.nyrds.platform.audio;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.nyrds.platform.EventCollector;
import java.util.HashMap;
import java.util.Map;
import org.teavm.jso.JSBody;
import org.teavm.jso.typedarrays.Int8Array;
import org.teavm.jso.webaudio.AudioBuffer;
import org.teavm.jso.webaudio.AudioContext;

/**
 * Gdx.audio for the web: the TeaVM backend leaves Gdx.audio null, so the
 * platform audio managers were silent no-ops. Implements the Audio factory
 * on top of Web Audio and owns the shared AudioContext + decoded-buffer
 * cache. Browsers start a context suspended until a user gesture - a
 * one-time pointerdown/keydown hook resumes it.
 */
public class WebAudio implements Audio {

	private static AudioContext context;
	private static final Map<String, AudioBuffer> decodeCache = new HashMap<>();

	static AudioContext context() {
		if (context == null) {
			try {
				context = new AudioContext();
				installResumeHook(context);
			} catch (Exception e) {
				EventCollector.logException(e, "AudioContext");
			}
		}
		return context;
	}

	static void decode(String path, byte[] bytes, DecodeReady ready) {
		AudioBuffer cached = decodeCache.get(path);
		if (cached != null) {
			ready.ready(cached);
			return;
		}
		AudioContext ctx = context();
		if (ctx == null) {
			return;
		}
		try {
			Int8Array array = Int8Array.fromJavaArray(bytes);
			ctx.decodeAudioData(array.getBuffer(), buffer -> {
				decodeCache.put(path, buffer);
				ready.ready(buffer);
			}, error -> EventCollector.logEvent("audio_decode_failed " + path));
		} catch (Exception e) {
			EventCollector.logException(e, "decode " + path);
		}
	}

	interface DecodeReady {
		void ready(AudioBuffer buffer);
	}

	@Override
	public Sound newSound(FileHandle fileHandle) {
		return new WebSound(fileHandle.path(), fileHandle.readBytes());
	}

	@Override
	public Music newMusic(FileHandle fileHandle) {
		return new WebMusic(fileHandle.path(), fileHandle.readBytes());
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

	@JSBody(params = "ctx", script =
			"window.__audioCtx = ctx;"
			+ "if (window.__rdAudioResume) { return; }"
			+ "window.__rdAudioResume = true;"
			+ "var h = function() { ctx.resume(); };"
			+ "window.addEventListener('pointerdown', h);"
			+ "window.addEventListener('keydown', h);"
			+ "document.addEventListener('visibilitychange', function() {"
			+ "  if (document.visibilityState === 'visible') { ctx.resume(); } });")
	private static native void installResumeHook(AudioContext ctx);
}
