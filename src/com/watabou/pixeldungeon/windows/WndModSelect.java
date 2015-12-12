package com.watabou.pixeldungeon.windows;

import java.io.File;
import java.util.ArrayList;

import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.PixelDungeon;

public class WndModSelect extends WndOptions {

	private static ArrayList<String> mMods;
	
	public WndModSelect() {
		super(Game.getVar(R.string.ModsButton_SelectMod), "", buildModsList().toArray(new String[0]));
	}
	
	private static ArrayList<String> buildModsList() {
		File[] extList = FileSystem.listExternalStorage();
		final ArrayList<String> mods = new ArrayList<String>();
		
		mods.add(ModdingMode.REMIXED);
		
		for (File file: extList) {
			if(file.isDirectory()) {
				mods.add(file.getName());
			}
		}
		mMods = mods;
		return mods;
	}
	
	protected void onSelect(int index) {
		super.onSelect(index);
		String mod = mMods.get(index);
		
		if(!mod.equals(PixelDungeon.activeMod())) {
			PixelDungeon.activeMod(mod);
			PixelDungeon.instance().doRestart();
		}
	}
}
