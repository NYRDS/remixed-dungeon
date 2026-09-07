package com.nyrds.platform.audio;

import com.badlogic.gdx.audio.Music;
import com.nyrds.platform.EventCollector;
import org.teavm.jso.webaudio.AudioBuffer;
import org.teavm.jso.webaudio.AudioBufferSourceNode;
import org.teavm.jso.webaudio.AudioContext;
import org.teavm.jso.webaudio.GainNode;

/**
 * gdx Music over Web Audio: a single looping-capable BufferSource through a
 * gain node. pause/resume track the playback offset; the game never starts
 * audio before the first user gesture, so the suspended-context autoplay
 * policy doesn't bite.
 */
class WebMusic implements Music {

	private final String path;
	private AudioBuffer buffer;
	private boolean wantPlay;

	private AudioBufferSourceNode node;
	private GainNode gain;

	private boolean playing;
	private boolean looping;
	private float volume = 1;
	private float pan;
	private double playbackPos;
	private double startedAt;
	private OnCompletionListener completionListener;

	WebMusic(String path, byte[] bytes) {
		this.path = path;
		WebAudio.decode(path, bytes, buf -> {
			buffer = buf;
			if (wantPlay) {
				wantPlay = false;
				startNode(playbackPos);
			}
		});
	}

	private void startNode(double offset) {
		AudioContext ctx = WebAudio.context();
		if (ctx == null || buffer == null) {
			wantPlay = true;
			return;
		}

		stopNode();

		node = ctx.createBufferSource();
		node.setBuffer(buffer);
		node.setLoop(looping);

		gain = ctx.createGain();
		gain.getGain().setValue(volume);
		node.connect(gain);
		gain.connect(ctx.getDestination());

		node.onEnded(event -> {
			if (!looping) {
				playing = false;
				playbackPos = 0;
				if (completionListener != null) {
					completionListener.onCompletion(this);
				}
			}
		});

		playing = true;
		startedAt = ctx.getCurrentTime();
		node.start(0, Math.max(0, Math.min(offset, buffer.getDuration())));
	}

	private void stopNode() {
		if (node != null) {
			try {
				node.stop();
			} catch (Exception e) {
				EventCollector.logException(e, "music stop " + path);
			}
			node.disconnect();
			gain.disconnect();
			node = null;
			gain = null;
		}
	}

	@Override
	public void play() {
		if (playing) {
			return;
		}
		if (buffer == null) {
			wantPlay = true;
			return;
		}
		startNode(playbackPos);
	}

	@Override
	public void pause() {
		if (!playing) {
			return;
		}
		playbackPos = getPosition();
		playing = false;
		stopNode();
	}

	@Override
	public void stop() {
		playing = false;
		playbackPos = 0;
		wantPlay = false;
		stopNode();
	}

	@Override
	public boolean isPlaying() {
		return playing;
	}

	@Override
	public void setLooping(boolean isLooping) {
		looping = isLooping;
		if (node != null) {
			node.setLoop(isLooping);
		}
	}

	@Override
	public boolean isLooping() {
		return looping;
	}

	@Override
	public void setVolume(float vol) {
		volume = vol;
		if (gain != null) {
			gain.getGain().setValue(vol);
		}
	}

	@Override
	public float getVolume() {
		return volume;
	}

	@Override
	public void setPan(float leftVolume, float rightVolume) {
		pan = rightVolume - leftVolume;
		setVolume(Math.max(leftVolume, rightVolume));
	}

	@Override
	public void setPosition(float position) {
		playbackPos = position;
		if (playing) {
			startNode(position);
		}
	}

	@Override
	public float getPosition() {
		if (!playing) {
			return (float) playbackPos;
		}
		AudioContext ctx = WebAudio.context();
		double elapsed = ctx != null ? ctx.getCurrentTime() - startedAt : 0;
		return (float) (playbackPos + elapsed);
	}

	@Override
	public void dispose() {
		stop();
		buffer = null;
		completionListener = null;
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		completionListener = listener;
	}
}
