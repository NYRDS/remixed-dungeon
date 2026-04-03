package com.watabou.pixeldungeon.scenes;

import com.nyrds.platform.game.Game;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.ui.Archs;
import com.watabou.pixeldungeon.ui.ExitButton;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.utils.Bundle;

public class WebViewScene extends PixelScene {

	private String url;
	
	public static void show(String url) {
		Game.openUrl("WebServer", url);
	}
	
	@Override
	public void create() {
		super.create();
		
		int w = Camera.main.width;
		int h = Camera.main.height;
		
		Archs archs = new Archs();
		archs.setSize(w, h);
		add(archs);
		
		String message = "Opening WebServer in your browser..." + "\n\n" +
						"If your browser didn't open automatically, please navigate to:" + "\n" +
						(url != null ? url : "http://localhost:8080");
		
		Text text = createMultiline(message, 6);
		text.maxWidth(w - 20);
		text.setX((w - text.width()) / 2);
		text.setY((h - text.height()) / 2);
		add(text);
		
		if (url != null) {
			Game.openUrl("WebServer", url);
		}
		
		ExitButton btnExit = new ExitButton();
		btnExit.setPos(w - btnExit.width(), 0);
		add(btnExit);
		
		fadeIn();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}
