package com.nyrds.platform.storage;

import com.badlogic.gdx.Gdx;
import com.nyrds.util.ModError;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.SneakyThrows;

public class FileSystem {

	static public @NotNull File getInternalStorageFile(String fileName) {
		return Gdx.files.internal(fileName).file();
	}

	static public String[] listInternalStorage() {
		return new File(Gdx.files.getLocalStoragePath()).list();
	}

	@NotNull
	static public File[] listExternalStorage() {
		File storageDir = new File(Gdx.files.getExternalStoragePath());

		File[] ret = storageDir.listFiles();
		if(ret != null) {
			return ret;
		}

		return new File[0];
	}

	static public OutputStream getOutputStream(String filename) throws FileNotFoundException {
		File dir = new File(filename).getParentFile();
		if (dir != null && !dir.exists()) {
			dir.mkdirs();
		}

		return new FileOutputStream(FileSystem.getInternalStorageFile(filename));
	}

	static public InputStream getInputStream(String filename) throws FileNotFoundException {
		return new FileInputStream(FileSystem.getInternalStorageFile(filename));
	}

	static public File getExternalStorageFile(String fileName) {
		return Gdx.files.external(fileName).file();
	}

	static public String getExternalStorageFileName(String fname) {
		return getExternalStorageFile(fname).getAbsolutePath();
	}

	static public File getFile(String fname) {
		return getInternalStorageFile(fname);
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
				zip.putNextEntry(new ZipEntry(getRelativePath(file,rootFolder)));
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

	public static boolean deleteFile(String file) {
		return new File(file).delete();
	}
}
