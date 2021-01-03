package com.nyrds.android.util;

import com.nyrds.pixeldungeon.ml.EventCollector;
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

	static public Mods.ModDesc inspectMod(InputStream fin) {
		Mods.ModDesc ret = new Mods.ModDesc();
		ret.name = Utils.EMPTY_STRING;

		try {
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze;

			while ((ze = zin.getNextEntry()) != null) {

				if (ze.isDirectory() && ret.installTo.isEmpty()) {
					ret.installTo = ze.getName().replace("/","");
				} else {
					if(ze.getName().contains("version.json")) {
						var modVersion = JsonHelper.readJsonFromStream(zin);
						ret.version   = modVersion.getInt("version");
						ret.author    = modVersion.optString("author", "Unknown");
						ret.description = modVersion.optString("description", "");
						ret.name      = modVersion.optString("name", ret.installTo);
						ret.url       = modVersion.optString("url", "");
						ret.hrVersion = modVersion.optString("hr_version", String.valueOf(ret.version));
						ret.rpdVersion = modVersion.optInt("rpd_version", 0);
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

	static public boolean unzipStream(InputStream fin, String tgtDir, @Nullable UnzipProgress listener) {
		FileSystem.ensureDir(tgtDir);
		try {
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze;

			byte[] data = new byte[BUFFER_SIZE];

			int entriesProcessed = 0;

			while ((ze = zin.getNextEntry()) != null) {
				entriesProcessed = entriesProcessed + 1;
				if(listener!=null) {
					listener.progress(entriesProcessed);
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
			}
			zin.close();
		} catch (Exception e) {
			EventCollector.logException(e);
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

	static public boolean unzip(String zipFile, String tgtDir, @Nullable UnzipProgress listener) {
		try {
			return unzipStream(new FileInputStream(zipFile), tgtDir, listener);
		} catch (FileNotFoundException e) {
			return false;
		}
	}

	interface UnzipProgress {
		void progress(int unpacked);
	}
}