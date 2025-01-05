/*
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.nyrds.platform.audio;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public enum MusicManager implements MediaPlayer.OnPreparedListener,
		MediaPlayer.OnErrorListener {

	INSTANCE;

	private MediaPlayer player;

	@Nullable
	private String lastPlayed;
	private boolean lastLooping;

	private boolean enabled = true;

	public void play(@NotNull String assetName, boolean looping) {
		if (!enabled) {
			return;
		}

		if (isPlaying() && assetName.equals(lastPlayed)) {
			return;
		}

		String assetFilename = ModdingMode.getSoundById("sound/"+assetName);

		String filename = null;
		AssetFileDescriptor afd = null;

		File file = ModdingMode.getFile(assetFilename);
		if (file!=null && file.exists()) {
			filename = file.getAbsolutePath();
		} else {
			try {
				afd = Game.instance().getAssets().openFd(assetFilename);
			} catch (IOException e) {
				EventCollector.logException(e,assetName);
			}
		}

		if(filename == null && afd == null) {
			return;
		}

		stop();

		lastPlayed = assetName;
		lastLooping = looping;

		try {
			player = new MediaPlayer();
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);

			if (filename != null) {
				player.setDataSource(filename);
			} else {
				player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
				afd.close();
			}
			
			player.setOnPreparedListener(this);
			player.setOnErrorListener(this);
			player.setLooping(looping);
			volume(1);
			player.prepareAsync();
		}

		catch (Exception e) {
			if(player!=null) {
				player.release();
				player = null;
			}
			EventCollector.logException(e,assetName);
		}
	}

	public void mute() {
		lastPlayed = null;
		stop();
	}

	@Override
	public void onPrepared(MediaPlayer player) {
		try {
			player.start();
		} catch (Exception e) {
			EventCollector.logException(e);
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		EventCollector.logException("Music:" + Utils.format("%d %d",what, extra));
		if (player != null) {
			player.release();
			player = null;
		}
		return true;
	}

	public void pause() {
		if (isPlaying()) {
			player.pause();
		}
	}

	public void resume() {
		if (isPlaying()) {
			player.start();
		}
	}

	public void stop() {

		if(player!=null) {
			player.release();
			player = null;
		}
	}

	public void volume(float value) {
		try {
			if (player != null) {
				value *= GamePreferences.musicVolume() / 10f;
				player.setVolume(value, value);
			}
		} catch (Exception e) {
			EventCollector.logException(e);
		}
	}

	public boolean isPlaying() {
		try {
			return player != null && player.isPlaying();
		} catch (Exception e) {
			EventCollector.logException(e);
		}
		return false;
	}

	public void enable(boolean value) {
		enabled = value;
		if (isPlaying() && !value) {
			stop();
		} else if (!isPlaying() && value) {
			if(lastPlayed!=null) {
				play(lastPlayed, lastLooping);
			}
		}
	}
}
