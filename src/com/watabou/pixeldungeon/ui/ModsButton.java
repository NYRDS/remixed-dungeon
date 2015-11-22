package com.watabou.pixeldungeon.ui;

import java.io.File;
import java.util.ArrayList;

import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.ModdingMode;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.windows.WndOptions;

public class ModsButton extends Button {
	
	private Image image;
	
	public ModsButton() {
		super();
		
		width = image.width;
		height = image.height;
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
		
		image = Icons.PREFS.get();
		add( image );
	}
	
	@Override
	protected void layout() {
		super.layout();
		
		image.x = x;
		image.y = y;
	}
	
	@Override
	protected void onTouchDown() {
		image.brightness( 1.5f );
		Sample.INSTANCE.play( Assets.SND_CLICK );
	}
	
	@Override
	protected void onTouchUp() {
		image.resetColor();
	}
	
	@Override
	protected void onClick() {
		
		File[] extList = FileSystem.listExternalStorage();
		final ArrayList<String> mods = new ArrayList<String>();
		
		for (File file: extList) {
			if(file.isDirectory()) {
				mods.add(file.getName());
			}
		}
		
		getParent().add( new WndOptions("Select Mod", "", mods.toArray(new String[0])){
			@Override
			protected void onSelect(int index) {
				super.onSelect(index);
				ModdingMode.selectMod(mods.get(index));
			}
		});
	}
}
