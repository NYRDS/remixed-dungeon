package com.nyrds.platform.storage;

import static com.nyrds.pixeldungeon.ml.BuildConfig.SAVES_PATH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.nyrds.platform.util.PUtil;
import com.nyrds.util.ModError;
import com.nyrds.util.ModdingBase;

import org.jetbrains.annotations.NotNull;

import com.nyrds.pixeldungeon.ml.BuildConfig;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.SneakyThrows;

public class FileSystem {
	static CaseInsensitiveFileCache fileCache = null;
	static CaseInsensitiveFileCache modCache = null;

	private static CaseInsensitiveFileCache getFileCache() {
		if (fileCache == null) {
			fileCache = new CaseInsensitiveFileCache(getAllResPaths());
		}
		return fileCache;
	}
	private static CaseInsensitiveFileCache getModCache() {
		if (modCache == null) {
			modCache = new CaseInsensitiveFileCache(getModResPaths());
		}
		return modCache;
	}

	static public FileHandle getInternalStorageFileHandle(String fileName) {
		FileHandle fileHandle;
		for(String path : getAllResPaths()) {
			fileHandle = Gdx.files.absolute(path+fileName);
			if(fileHandle.exists()) {
				return fileHandle;
			}
		}

		fileHandle = getFileCache().getFile(fileName);
		return fileHandle;
	}

	private static String[] getAllResPaths() {
		return new String[]{
				"data/mods/" + ModdingBase.activeMod() + "/",
				"data/mods/Remixed/",
				FileSystem.getUserDataPath("mods/" + ModdingBase.activeMod() + "/"),
				"mods/Remixed/",
				"../assets/",
				"../d_assets/",
				"../l10ns/",
				"./",
		};
	}

	private static String[] getModResPaths() {
		return new String[]{
				FileSystem.getUserDataPath("mods/" + ModdingBase.activeMod() + "/")
		};
	}


	static public String[] listResources(String resName) {
		Set<String> resList = new HashSet<>();
		for (String path : getAllResPaths()) {
			FileHandle fileHandle = Gdx.files.absolute(path + resName);
			FileHandle[] fileHandles = fileHandle.list(file -> true);
			for (FileHandle file : fileHandles) {
				resList.add(file.name());
			}
		}
		return resList.toArray(new String[0]);
	}

	static public boolean exists(String fileName) { return getFileCache().exists(fileName); } // exists
	static public boolean existsInMod(String fileName) { return getModCache().exists(fileName); } // exists

	static public @NotNull FileHandle getInternalStorageFileHandleBase(String fileName) {
		FileHandle fileHandle = null;
		for(String path : new String[] {
				"data/mods/Remixed/",
				"mods/Remixed/",
				"../assets/",
				"../d_assets/",
				"../l10ns/",
				"./",
		}) {
			fileHandle = Gdx.files.absolute(path+fileName);
			if(fileHandle.exists()) {
				return fileHandle;
			}
		}
		PUtil.slog("file", "Internal file not found: " + fileName);
		return fileHandle;
	}


	static public @NotNull File getInternalStorageFile(String fileName) {
		FileHandle fileHandle = getInternalStorageFileHandle(fileName);
		if(fileHandle == null) {
			return new File("file_not_found") {
				@Override
				public boolean exists() {
					return false;
				}
			};
		}
		return fileHandle.file();
	}

	static public @NotNull File getInternalStorageFileBase(String fileName) {
		return getInternalStorageFileHandleBase(fileName).file();
	}

	@NotNull
	static public File[] listExternalStorage() {
		File storageDir = new File(getUserDataPath("mods"));

		if(!storageDir.exists()) {
			storageDir.mkdirs();
		}

		File[] ret = storageDir.listFiles();
		if(ret != null) {
			return ret;
		}

		return new File[0];
	}


	//Will be used for saves only
	static public OutputStream getOutputStream(String filename) throws FileNotFoundException {
		filename = getUserDataPath(SAVES_PATH) + filename;

		FileHandle fileHandle = Gdx.files.absolute(filename);
		if(!fileHandle.parent().exists()) {
			fileHandle.parent().mkdirs();
		}

		return new FileOutputStream(Gdx.files.absolute(filename).file());
	}

	static public InputStream getInputStream(String filename) throws FileNotFoundException {
		filename = getUserDataPath(SAVES_PATH) + filename;
		return new FileInputStream(Gdx.files.absolute(filename).file());
	}

	static public File getExternalStorageFile(String fileName) {
		File modsDir = new File(getUserDataPath("mods"));
		if(!modsDir.exists()) {
			modsDir.mkdirs();
		}
		return new File(modsDir, fileName);
	}

	static public String getExternalStorageFileName(String fname) {
		return getExternalStorageFile(fname).getAbsolutePath();
	}

	static public void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				deleteRecursive(child);

		fileOrDirectory.delete();
	}

	@SneakyThrows
	static public void copyStream(InputStream in, OutputStream out) {
			byte[] buffer = new byte[4096];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();

			out.flush();
			out.close();
	}

	@SneakyThrows
	static public void copyFile(String inputFile, OutputStream out) {
		InputStream in = new FileInputStream(inputFile);
		copyStream(in, out);
	}

	@SneakyThrows
	static public void copyFile(String inputFile, String outputFile) {
		File dir = new File(outputFile).getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}

		copyFile(inputFile, new FileOutputStream(outputFile));
	}

	public static void zipFolderTo(OutputStream out, File srcFolder, int depth, FileFilter filter) throws IOException {
		ZipOutputStream zip = new ZipOutputStream(out);
		addFolderToZip(srcFolder,srcFolder,depth, zip, filter);

		zip.flush();
		zip.close();
	}

	private static void addFolderToZip(File rootFolder, File srcFolder, int depth,
	                                   ZipOutputStream zip, FileFilter filter) throws IOException {

		for (File file : srcFolder.listFiles(filter)) {

			if (file.isFile()) {
				addFileToZip(rootFolder, file, zip);
				continue;
			}

			if(depth > 0 && file.isDirectory()) {
				zip.putNextEntry(new ZipEntry(getRelativePath(file, rootFolder)));
				addFolderToZip(rootFolder, srcFolder, depth-1, zip, filter);
				zip.closeEntry();
			}
		}
	}

	private static void addFileToZip(File rootFolder, File file, ZipOutputStream zip) throws IOException {
			byte[] buf = new byte[4096];
			int len;
			try(FileInputStream in = new FileInputStream(file)) {
				zip.putNextEntry(new ZipEntry(getRelativePath(file, rootFolder)));
				while ((len = in.read(buf)) > 0) {
					zip.write(buf, 0, len);
				}
				zip.closeEntry();
			}
		}

	public static String getRelativePath(File file, File folder) {
		String filePath = file.getAbsolutePath();
		String folderPath = folder.getAbsolutePath();
		if (filePath.startsWith(folderPath)) {
			return filePath.substring(folderPath.length() + 1);
		} else {
			return null;
		}
	}

	public static void ensureDir(String dir) {
		File f = new File(dir);

		if(f.exists() && f.isDirectory()){
			return;
		}

		if(f.exists() && !f.delete()) {
			throw new ModError("Can't cleanup:"+dir);
		}

		if (!f.mkdirs()) {
			throw new ModError("Can't create directory:"+dir);
		}
	}

	public static void reinitFileCache() {
		fileCache = null;
	}

	public static void reinitModCache() {
		modCache = null;
	}

	public static void reinitAllCaches() {
		reinitFileCache();
		reinitModCache();
	}

	public static String getUserDataPath(String subPath) {
		// For snap and appimage, route user data to XDG-style directory
		if (BuildConfig.FLAVOR_market.equals("snap") || BuildConfig.FLAVOR_market.equals("appimage")) {
			String home = System.getProperty("user.home");
			if (home != null && !home.isEmpty()) {
				return home + File.separator + ".local" + File.separator + "share" +
					   File.separator + "remixed-dungeon" + File.separator + subPath;
			}
		}

		// Fallback to the original relative path behavior
		return subPath;
	}
}
