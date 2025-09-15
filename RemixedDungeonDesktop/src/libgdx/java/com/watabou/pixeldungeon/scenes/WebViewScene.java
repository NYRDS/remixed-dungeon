package com.watabou.pixeldungeon.scenes;

import com.nyrds.platform.game.Game;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.ui.Archs;
import com.watabou.pixeldungeon.ui.ExitButton;
import com.watabou.utils.Bundle;

public class WebViewScene extends PixelScene {

	private String url;
	
	public static void show(String url) {
		// On desktop platforms, open in external browser
		Game.openUrl("Open WebServer", url);
	}
	
	@Override
	public void create() {
		super.create();
		
		// On desktop platforms, just show a message
		int w = Camera.main.width;
		int h = Camera.main.height;
		
		Archs archs = new Archs();
		archs.setSize(w, h);
		add(archs);
		
		// Show message that WebView is only available on Android
		String message = "WebServer interface is only available within the app on Android devices." + "\n\n" +
						"On desktop platforms, your browser has been opened to:" + "\n" +
						(url != null ? url : "http://localhost:8080") +
						"\n\nIf your browser didn't open automatically, please navigate to this address manually.";
		
		Text text = createMultiline(message, 6);
		text.maxWidth(w - 20);
		text.setX((w - text.width()) / 2);
		text.setY((h - text.height()) / 2);
		add(text);
		
		// Open the URL in browser
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