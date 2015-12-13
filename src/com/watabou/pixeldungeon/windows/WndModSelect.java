package com.watabou.pixeldungeon.windows;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import com.nyrds.android.util.DownloadStateListener;
import com.nyrds.android.util.DownloadTask;
import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.Unzip;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.utils.GLog;

public class WndModSelect extends WndOptions implements DownloadStateListener {

	private static ArrayList<String> mMods = new ArrayList<>();

	static private class ModDesc {
		public String name;
		public String link;
	}
	
	private String selectedMod;
	private String downloadTo;
	
	private static Map<String, ModDesc> mModsMap = new HashMap<>();

	public WndModSelect() {
		super(Game.getVar(R.string.ModsButton_SelectMod), "", buildModsList().toArray(new String[0]));
	}

	private static ArrayList<String> buildModsList() {
		mModsMap.clear();
		mMods.clear();

		File[] extList = FileSystem.listExternalStorage();
		final ArrayList<String> mods = new ArrayList<String>();

		String[] knownMods = Game.getVars(R.array.known_mods);

		for (int i = 0; i < knownMods.length; i++) {
			
			try {
				JSONArray modDesc = new JSONArray(knownMods[i]);
				ModDesc desc = new ModDesc();
				desc.name = modDesc.getString(0);
				
				//TODO check versions...
				if(FileSystem.getExternalStorageFile(desc.name).exists()) {
					continue;
				}
				
				desc.link = modDesc.getString(1);
				
				String option = "Download "+desc.name;
				mModsMap.put(option, desc);
				mods.add(option);

			} catch (JSONException e) {
				GLog.w(e.getMessage());
				continue;
			}
		}

		mods.add(ModdingMode.REMIXED);

		for (File file : extList) {
			if (file.isDirectory()) {
				mods.add(file.getName());
			}
		}
		mMods = mods;
		return mods;
	}

	protected void onSelect(int index) {
		super.onSelect(index);
		String option = mMods.get(index);

		File modDir = FileSystem.getExternalStorageFile(option);

		if (!modDir.exists() && !option.equals(ModdingMode.REMIXED)) {
			selectedMod = mModsMap.get(option).name;
			downloadTo = FileSystem.getExternalStorageFile(selectedMod + ".zip").getAbsolutePath();
			
			new DownloadTask(this).execute(mModsMap.get(option).link,downloadTo);
			return;
		}

		if (!option.equals(PixelDungeon.activeMod())) {
			PixelDungeon.activeMod(option);
			PixelDungeon.instance().doRestart();
		}
	}

	@Override
	public void DownloadProgress(String file, Integer percent) {

	}

	@Override
	public void DownloadComplete(String url, Boolean result) {
		if (result) {
			
			String tmpDirName = "tmp";
			
			File tmpDirFile = FileSystem.getExternalStorageFile(tmpDirName);
			if(tmpDirFile.exists()) {
				tmpDirFile.delete();
			}
			
			if(Unzip.unzip(downloadTo,
							FileSystem.getExternalStorageFile(tmpDirName).getAbsolutePath())) {
				
				File[] unpackedList = tmpDirFile.listFiles();
				
				for(File file:unpackedList) {
					if(file.isDirectory()) {
						
						String modDir = downloadTo.substring(0,downloadTo.length()-4);
						file.renameTo(new File(modDir));
						
						FileSystem.deleteRecursive(tmpDirFile);
						FileSystem.deleteRecursive(new File(downloadTo));
						continue;
					}
				}
				
				Game.toast("Download %s ok", url);
			} else {
				Game.toast("unzipping %s failed", downloadTo);
			}
		} else {
			Game.toast("Download %s failed", url);
		}
	}
}