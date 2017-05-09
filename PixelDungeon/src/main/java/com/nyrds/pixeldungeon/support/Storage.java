package com.nyrds.pixeldungeon.support;

import com.nyrds.android.util.FileSystem;

import org.apache.commons.io.output.TeeOutputStream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by mike on 09.05.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class Storage {
	public OutputStream getOutputStream(String id) throws FileNotFoundException {
		if(PlayGames.isConnected()) {
			return new TeeOutputStream(
						new FileOutputStream(FileSystem.getInternalStorageFile(id)),
						PlayGames.streamToSnapshot(id)
					);
		} else {
			return new FileOutputStream(FileSystem.getInternalStorageFile(id));
		}
	}

	public InputStream getInputStream(String id) throws FileNotFoundException {
		if(PlayGames.isConnected()) {
			if(PlayGames.haveSnapshot(id)) {
				return PlayGames.streamFromSnapshot(id);
			}
		}

		return new FileInputStream(FileSystem.getInternalStorageFile(id));
	}
}
