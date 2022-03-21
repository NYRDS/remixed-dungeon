package com.nyrds.util;

import com.nyrds.pixeldungeon.utils.ModDesc;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.storage.FileSystem;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import lombok.var;

public class Unzip {

	private static final int    BUFFER_SIZE = 16384;

	static public ModDesc inspectMod(InputStream fin) {
		ModDesc ret = new ModDesc();
		ret.name = Utils.EMPTY_STRING;

		try {
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze;

			while ((ze = zin.getNextEntry()) != null) {

				if (ze.isDirectory() && ret.installDir.isEmpty()) {
					ret.installDir = ze.getName().replace("/","");
				} else {
					if(ze.getName().contains("version.json")) {
						var modVersion = JsonHelper.readJsonFromStream(zin, ze.getName());
						ModDesc.fromJson(ret, modVersion);
					}
					zin.closeEntry();
				}

			}
			zin.close();
		} catch (Exception e) {
			EventCollector.logException(e);
			return ret;
		}

		return ret;
	}

	static public boolean unzipStream(InputStream fin, String tgtDir, @Nullable UnzipStateListener listener) {
		FileSystem.ensureDir(tgtDir);
		try {
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze;

			byte[] data = new byte[BUFFER_SIZE];

			int entriesProcessed = 0;

			while ((ze = zin.getNextEntry()) != null) {
				entriesProcessed = entriesProcessed + 1;
				if(listener!=null) {
					listener.UnzipProgress(entriesProcessed);
				}

				GLog.debug( "Unzipping " + ze.getName());

				String exPath = sanitizeExtractPath(tgtDir, ze);

				if (ze.isDirectory()) {
					FileSystem.ensureDir(exPath);
				} else {
					FileOutputStream fout = new FileOutputStream(exPath);

					int bytesRead;
					while ((bytesRead = zin.read(data)) != -1) {
						fout.write(data, 0, bytesRead);
					}

					zin.closeEntry();
					fout.close();
				}
				Thread.yield();
			}
			zin.close();

			if(listener!=null) {
				listener.UnzipComplete(true);
			}

		} catch (Exception e) {
			EventCollector.logException(e);
			if(listener!=null) {
				listener.UnzipComplete(false);
			}
			return false;
		}

		return true;
	}

	@NotNull
	public static String sanitizeExtractPath(String tgtDir, ZipEntry ze) {
		String exPath = new File(tgtDir + "/" + ze.getName()).getAbsolutePath();

		if(!exPath.startsWith(tgtDir)) {
			throw new ModError("Zip traversal attack attempt: " + exPath);
		}
		return exPath;
	}

	static public boolean unzip(String zipFile, String tgtDir, @Nullable UnzipStateListener listener) {
		try {
			return unzipStream(new FileInputStream(zipFile), tgtDir, listener);
		} catch (FileNotFoundException e) {
			return false;
		}
	}
}