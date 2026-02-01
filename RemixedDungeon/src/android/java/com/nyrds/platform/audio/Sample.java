

package com.nyrds.platform.audio;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.nyrds.util.ModdingMode;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum Sample implements SoundPool.OnLoadCompleteListener {

	INSTANCE;

	public static final int MAX_STREAMS = 8;
	String playOnComplete;

	private SoundPool pool =
			new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);

	@NotNull
	private final Set<String> missingAssets = new HashSet<>();

	@NotNull
	private final Map<String, Integer> ids =
			new HashMap<>();

	private AssetManager manager;
	private boolean enabled = true;

	public void reset() {

		pool.release();

		pool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
		pool.setOnLoadCompleteListener(this);

		ids.clear();
	}

	public void pause() {
		if (pool != null) {
			pool.autoPause();
		}
	}

	public void resume() {
		if (pool != null) {
			pool.setOnLoadCompleteListener(this);
			if (manager == null) {
				manager = Game.instance().getAssets();
			}
			pool.autoResume();
		}
	}

	private void load(String asset) {

		if (!ids.containsKey(asset) && !missingAssets.contains(asset)) {
			try {
				String assetFile = ModdingMode.getSoundById("sound/" + asset);
				int streamID;

				File file = ModdingMode.getFile(assetFile);
				if (file != null && file.exists()) {
					streamID = pool.load(file.getAbsolutePath(), 1);
				} else {
					streamID = fromAsset(manager, assetFile);
				}

				ids.put(asset, streamID);

			} catch (IOException e) {
				missingAssets.add(asset);
				playOnComplete = null;
				EventCollector.logException(e,asset);
			}
		}
	}

	private int fromAsset(AssetManager manager, String asset)
			throws IOException {
		AssetFileDescriptor fd = manager.openFd(asset);
		int streamID = pool.load(fd, 1);
		fd.close();
		return streamID;
	}

	public void play(String id) {
		play(id, 1, 1, 1);
	}

	public void play(String id, float volume) {
		play(id, volume, volume, 1);
	}

	public void play(String id, float leftVolume, float rightVolume, float rate) {
		if(!enabled) {
			return;
		}
		GameLoop.instance().soundExecutor.execute(() -> {
			Integer sampleId = ids.get(id);
			if (sampleId != null) {
				float g_volume = GamePreferences.soundFxVolume() / 10f;

				pool.play(sampleId, leftVolume * g_volume, rightVolume * g_volume, 0, 0, rate);
			} else {
				playOnComplete = id;
				GameLoop.execute(() -> load(id));
			}
		});
	}

	public void enable(boolean value) {
		enabled = value;
	}

	@Override
	public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
		if (status == 0 && playOnComplete != null) {
			play(playOnComplete);
			playOnComplete = null;
		}
	}
}
