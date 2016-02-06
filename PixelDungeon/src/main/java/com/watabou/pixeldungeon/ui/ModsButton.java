package com.watabou.pixeldungeon.ui;

import android.Manifest;

import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.noosa.SystemText;
import com.watabou.noosa.Text;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.windows.WndModSelect;
import com.watabou.pixeldungeon.windows.WndTitledMessage;

public class ModsButton extends Button implements InterstitialPoint {
	
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
		String [] requiredPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET};
		Game.instance().doPermissionsRequest(this, requiredPermissions);
	}

	@Override
	public void returnToWork(final boolean result) {
		final Group parent = getParent();
		Game.executeInGlThread(new Runnable() {
			@Override
			public void run() {
				if (result) {
					parent.add(new WndModSelect());
				} else {
					parent.add(new WndTitledMessage(Icons.get(Icons.SKULL), "No permissions granted", "No permissions granted"));
				}
			}
		});

	}
}
