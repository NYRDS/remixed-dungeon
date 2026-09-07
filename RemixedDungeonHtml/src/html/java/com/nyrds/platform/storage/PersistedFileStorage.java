package com.nyrds.platform.storage;

import com.badlogic.gdx.files.FileHandle;
import com.github.xpenatan.gdx.backends.teavm.TeaFileHandle;
import com.github.xpenatan.gdx.backends.teavm.filesystem.FileData;
import com.github.xpenatan.gdx.backends.teavm.filesystem.MemoryFileStorage;
import com.nyrds.platform.EventCollector;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;
import org.teavm.jso.browser.Storage;

/**
 * MemoryFileStorage mirrored into window.localStorage. The TeaVM backend's
 * local file storage is a plain in-memory map - everything written through
 * Gdx.files.local() (game saves, badges, library, rankings) died with the
 * page. Reads still come from memory; every insert/remove also updates
 * localStorage so files survive reloads. Save files are small gzip JSON -
 * the ~5MB per-origin quota is plenty. Backed by the same hooks the backend
 * itself routes all mutations through (writeInternal/append/delete/
 * deleteDirectory/rename/mkdirs all end in putFile/removeFile with
 * already-normalized paths).
 */
public class PersistedFileStorage extends MemoryFileStorage {

	private static final String KEY_PREFIX = "rdg_file_";
	private static final String DIR_MARKER = "\u0000D";

	private final Storage storage;
	private boolean loading;
	private boolean quotaLogged;

	public PersistedFileStorage() {
		Storage s = null;
		try {
			s = Storage.getLocalStorage();
		} catch (Exception e) {
			EventCollector.logException(e, "localStorage unavailable");
		}
		storage = s;
		if (storage != null) {
			loading = true;
			try {
				restoreAll();
			} finally {
				loading = false;
			}
		}
	}

	private void restoreAll() {
		try {
			for (int i = storage.getLength() - 1; i >= 0; i--) {
				String key = storage.key(i);
				if (key == null || !key.startsWith(KEY_PREFIX)) {
					continue;
				}
				String path = key.substring(KEY_PREFIX.length());
				String value = storage.getItem(key);
				if (value == null) {
					continue;
				}
				if (value.equals(DIR_MARKER)) {
					putFolderInternal(path);
				} else {
					putFileInternal(path, Base64.getDecoder().decode(value));
				}
			}
		} catch (Exception e) {
			EventCollector.logException(e, "restore saved files");
		}
	}

	@Override
	protected void putFile(String path, FileData data) {
		super.putFile(path, data);
		if (storage == null || loading) {
			return;
		}
		try {
			if (data.isDirectory()) {
				storage.setItem(KEY_PREFIX + path, DIR_MARKER);
			} else {
				storage.setItem(KEY_PREFIX + path,
						Base64.getEncoder().encodeToString(data.getBytes()));
			}
		} catch (Exception e) {
			// quota errors repeat on every subsequent write - log once
			if (!quotaLogged) {
				quotaLogged = true;
				EventCollector.logException(e, "persisting " + path);
				EventCollector.logEvent("localStorage_quota_exceeded");
			}
		}
	}

	@Override
	protected void removeFile(String path) {
		super.removeFile(path);
		if (storage != null && !loading) {
			storage.removeItem(KEY_PREFIX + path);
		}
	}

	/**
	 * The backend's own listing walks every entry's TeaFileHandle.parent(),
	 * but with canonical /dir/name/ paths parent("/dir/name/") is the entry
	 * itself - so list() returns nothing and the whole save-slot machinery
	 * (autosave copies, GamesInProgress, slot deletes) sees an empty vault.
	 * List direct children straight from the persisted keys instead.
	 * Results are relative-ish paths ("name" under root, "dir/name" under
	 * "/"), matching what FileDB.list feeds back into new file handles.
	 */
	@Override
	protected String[] paths(TeaFileHandle handle) {
		Set<String> out = new LinkedHashSet<>();
		String base = fixPath(handle.path());
		if (storage != null) {
			for (int i = storage.getLength() - 1; i >= 0; i--) {
				String key = storage.key(i);
				if (key == null || !key.startsWith(KEY_PREFIX)) {
					continue;
				}
				String path = key.substring(KEY_PREFIX.length());
				if (!path.startsWith(base) || path.equals(base)) {
					continue;
				}
				String rel = path.substring(base.length());
				if (rel.endsWith("/")) {
					rel = rel.substring(0, rel.length() - 1);
				}
				if (rel.isEmpty() || rel.contains("/")) {
					continue; // direct children only
				}
				out.add(base.equals("/") ? rel : base.substring(1) + rel);
			}
		}
		// storage keys are the complete picture - every memory-map mutation
		// goes through the putFile/removeFile hooks - so no super.paths()
		// union here (it would only add trailing-slash duplicates)
		return out.toArray(new String[0]);
	}
}
