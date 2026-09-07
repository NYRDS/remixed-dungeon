package com.nyrds.platform.audio;

import com.badlogic.gdx.audio.Sound;
import com.nyrds.platform.EventCollector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.teavm.jso.webaudio.AudioBuffer;
import org.teavm.jso.webaudio.AudioBufferSourceNode;
import org.teavm.jso.webaudio.AudioContext;
import org.teavm.jso.webaudio.GainNode;
import org.teavm.jso.webaudio.StereoPannerNode;

/**
 * gdx Sound over Web Audio: one BufferSource per play() through a gain
 * (and a stereo panner when panned). decodeAudioData is async - plays
 * requested before the buffer is ready replay once on decode.
 */
class WebSound implements Sound {

	private final String path;
	private AudioBuffer buffer;
	private boolean pendingPlay;
	private float pendingVolume = 1;
	private float pendingPitch = 1;
	private float pendingPan = 0;

	private final Map<Long, Voice> voices = new HashMap<>();
	private static long nextId = 1;

	WebSound(String path, byte[] bytes) {
		this.path = path;
		WebAudio.decode(path, bytes, buf -> {
			buffer = buf;
			if (pendingPlay) {
				pendingPlay = false;
				startVoice(pendingVolume, pendingPitch, pendingPan, 0);
			}
		});
	}

	private static final class Voice {
		long id;
		AudioBufferSourceNode node;
		GainNode gain;
		StereoPannerNode panner;
		float volume = 1;
		float pitch = 1;
		float pan;
		double startOffset;
		double startedAt;
		boolean playing;
		boolean looping;
	}

	private Voice startVoice(float volume, float pitch, float pan, double offset) {
		AudioContext ctx = WebAudio.context();
		if (ctx == null || buffer == null) {
			return null;
		}
		Voice voice = new Voice();
		voice.id = nextId++;
		voice.volume = volume;
		voice.pitch = pitch;
		voice.pan = pan;
		voice.startOffset = offset;

		AudioBufferSourceNode node = ctx.createBufferSource();
		node.setBuffer(buffer);
		GainNode gain = ctx.createGain();
		gain.getGain().setValue(volume);

		voice.node = node;
		voice.gain = gain;

		if (pan != 0) {
			StereoPannerNode panner = ctx.createStereoPanner();
			panner.getPan().setValue(Math.max(-1, Math.min(1, pan)));
			voice.panner = panner;
			node.connect(gain);
			gain.connect(panner);
			panner.connect(ctx.getDestination());
		} else {
			node.connect(gain);
			gain.connect(ctx.getDestination());
		}

		node.getPlaybackRate().setValue(Math.max(0.1f, pitch));
		node.onEnded(event -> {
			voice.playing = false;
			node.disconnect();
			gain.disconnect();
			voices.remove(voice.id);
		});

		voice.playing = true;
		voice.startedAt = ctx.getCurrentTime();
		node.start(0, offset);

		voices.put(voice.id, voice);
		return voice;
	}

	private Voice find(long soundId) {
		return voices.get(soundId);
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
		if (buffer == null) {
			pendingPlay = true;
			pendingVolume = volume;
			pendingPitch = pitch;
			pendingPan = pan;
			return 0;
		}
		Voice voice = startVoice(volume, pitch, pan, 0);
		return voice != null ? voice.id : 0;
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
		Voice voice = startVoice(volume, pitch, pan, 0);
		if (voice != null) {
			voice.looping = true;
			voice.node.setLoop(true);
			return voice.id;
		}
		return 0;
	}

	@Override
	public void stop() {
		for (Voice voice : all()) {
			halt(voice);
		}
		voices.clear();
	}

	@Override
	public void pause() {
		for (Voice voice : all()) {
			pauseVoice(voice);
		}
	}

	@Override
	public void resume() {
		for (Voice voice : all()) {
			if (voice.playing) {
				continue;
			}
			if (buffer != null && !voice.looping
					&& voice.startOffset >= buffer.getDuration()) {
				continue;
			}
			restart(voice, voice.startOffset);
		}
	}

	@Override
	public void dispose() {
		stop();
		buffer = null;
	}

	@Override
	public void stop(long soundId) {
		Voice voice = find(soundId);
		if (voice != null) {
			halt(voice);
			voices.remove(soundId);
		}
	}

	@Override
	public void pause(long soundId) {
		Voice voice = find(soundId);
		if (voice != null) {
			pauseVoice(voice);
		}
	}

	@Override
	public void resume(long soundId) {
		Voice voice = find(soundId);
		if (voice != null && !voice.playing) {
			restart(voice, voice.startOffset);
		}
	}

	@Override
	public void setLooping(long soundId, boolean looping) {
		Voice voice = find(soundId);
		if (voice != null) {
			voice.looping = looping;
			voice.node.setLoop(looping);
		}
	}

	@Override
	public void setPitch(long soundId, float pitch) {
		Voice voice = find(soundId);
		if (voice != null) {
			voice.pitch = pitch;
			voice.node.getPlaybackRate().setValue(Math.max(0.1f, pitch));
		}
	}

	@Override
	public void setVolume(long soundId, float volume) {
		Voice voice = find(soundId);
		if (voice != null) {
			voice.volume = volume;
			voice.gain.getGain().setValue(volume);
		}
	}

	@Override
	public void setPan(long soundId, float pan, float volume) {
		Voice voice = find(soundId);
		if (voice == null) {
			return;
		}
		voice.volume = volume;
		voice.gain.getGain().setValue(volume);
		if (voice.panner != null) {
			voice.pan = pan;
			voice.panner.getPan().setValue(Math.max(-1, Math.min(1, pan)));
		}
	}

	private void restart(Voice voice, double offset) {
		AudioContext ctx = WebAudio.context();
		if (ctx == null || buffer == null) {
			return;
		}
		AudioBufferSourceNode node = ctx.createBufferSource();
		node.setBuffer(buffer);
		node.connect(voice.gain);
		if (voice.panner != null) {
			voice.gain.connect(voice.panner);
			voice.panner.connect(ctx.getDestination());
		} else {
			voice.gain.connect(ctx.getDestination());
		}
		node.getPlaybackRate().setValue(Math.max(0.1f, voice.pitch));
		node.onEnded(event -> {
			voice.playing = false;
			node.disconnect();
			voices.remove(voice.id);
		});
		voice.node = node;
		voice.playing = true;
		voice.startedAt = ctx.getCurrentTime();
		node.start(0, offset);
	}

	private void pauseVoice(Voice voice) {
		if (!voice.playing) {
			return;
		}
		voice.startOffset += contextTime() - voice.startedAt;
		voice.playing = false;
		try {
			voice.node.stop();
		} catch (Exception e) {
			EventCollector.logException(e, "sound pause " + path);
		}
		voice.node.disconnect();
		voice.gain.disconnect();
	}

	private void halt(Voice voice) {
		try {
			voice.node.stop();
		} catch (Exception e) {
			EventCollector.logException(e, "sound stop " + path);
		}
		voice.playing = false;
		voice.node.disconnect();
		voice.gain.disconnect();
	}

	private List<Voice> all() {
		return new ArrayList<>(voices.values());
	}

	private static double contextTime() {
		AudioContext ctx = WebAudio.context();
		return ctx != null ? ctx.getCurrentTime() : 0;
	}
}
