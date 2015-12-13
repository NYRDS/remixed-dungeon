package com.nyrds.android.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Unzip {
	
	private static final String TAG = "Unzip";
	private static final int BUFFER_SIZE = 16384;

	static public void ensureDir(String dir) {
		File f = new File(dir);

		if (!f.isDirectory()) {
			f.mkdirs();
		}
	}
	
	static public boolean unzip(String zipFile, String tgtDir) {
		ensureDir(tgtDir);
		
		try {
			FileInputStream fin = new FileInputStream(zipFile);
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze;
			
			byte data[] = new byte[BUFFER_SIZE];
			
			while ((ze = zin.getNextEntry()) != null) {
				Log.v(TAG, "Unzipping " + ze.getName());

				if (ze.isDirectory()) {
					ensureDir(tgtDir+"/"+ze.getName());
				} else {
					
					FileOutputStream fout = new FileOutputStream(tgtDir + "/"+ ze.getName());
					
					int bytesRead;
					while((bytesRead= zin.read(data))!=-1) {
						fout.write(data,0, bytesRead);
					}
					
					zin.closeEntry();
					fout.close();
				}

			}
			zin.close();
		} catch (Exception e) {
			Log.e(TAG, "unzip", e);
			return false;
		}
		
		return true;
	}
}