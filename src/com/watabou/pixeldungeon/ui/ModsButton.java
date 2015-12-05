package com.watabou.pixeldungeon.ui;

import java.io.File;
import java.util.ArrayList;

import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.SystemText;
import com.watabou.noosa.Text;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.windows.WndOptions;


public class ModsButton extends Button {
	
	private Image image;
	private Text  text;
	
	public ModsButton() {
		super();
		
		width = image.width;
		height = image.height;
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
		
		image = Icons.MODDING_MODE.get();
		add( image );
		
		text = new SystemText(8);
		text.text(PixelDungeon.activeMod());
		add(text);
	}
	
	@Override
	protected void layout() {
		super.layout();
		
		image.x = x;
		image.y = y;
		
		text.x = x;
		text.y = image.y + image.height + 2;
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
		
		mods.add(ModdingMode.REMIXED);
		
		for (File file: extList) {
			if(file.isDirectory()) {
				mods.add(file.getName());
			}
		}
		
		getParent().add( new WndOptions(Game.getVar(R.string.ModsButton_SelectMod), "", mods.toArray(new String[0])){
			@Override
			protected void onSelect(int index) {
				super.onSelect(index);
				String mod = mods.get(index);
				
				if(!mod.equals(PixelDungeon.activeMod())) {
					PixelDungeon.activeMod(mod);
					PixelDungeon.instance().doRestart();
				}
			}
		});
	}
}
