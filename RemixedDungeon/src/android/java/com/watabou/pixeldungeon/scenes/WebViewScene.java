package com.watabou.pixeldungeon.scenes;

import android.webkit.WebView;
import android.webkit.WebViewClient;

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
	private WebView webView;
	
	public static void show(String url) {
		// For now, just open in external browser until we can fix the UI issues
		Game.openUrl("WebServer", url);
	}
	
	@Override
	public void create() {
		super.create();
		
		// For now, just show a message and open browser
		int w = Camera.main.width;
		int h = Camera.main.height;
		
		Archs archs = new Archs();
		archs.setSize(w, h);
		add(archs);
		
		// Show message that WebView is available
		String message = "Opening WebServer in your browser..." + "\n\n" +
						"If your browser didn't open automatically, please navigate to:" + "\n" +
						(url != null ? url : "http://localhost:8080");
		
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