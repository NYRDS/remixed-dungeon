
package com.watabou.pixeldungeon.scenes;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.input.Touchscreen.Touch;
import com.nyrds.platform.storage.AndroidSAF;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.Scene;
import com.watabou.noosa.Text;
import com.watabou.noosa.TouchArea;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.items.ArmorKit;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.Archs;
import com.watabou.pixeldungeon.ui.ExitButton;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

public class AboutScene extends PixelScene {

	private static String getTXT() {
        return StringsManager.getVar(R.string.AboutScene_Txt_Intro) + "\n\n"
				+ StringsManager.getVar(R.string.AboutScene_Code) + " " + StringsManager.getVar(R.string.AboutScene_Code_Names) + "\n\n"
				+ StringsManager.getVar(R.string.AboutScene_Graphics) + " " + StringsManager.getVar(R.string.AboutScene_Graphics_Names) + "\n\n"
				+ StringsManager.getVar(R.string.AboutScene_Music) + " " + StringsManager.getVar(R.string.AboutScene_Music_Names) + "\n\n"
				+ StringsManager.getVar(R.string.AboutScene_Sound) + " " + StringsManager.getVar(R.string.AboutScene_Sound_Names) + "\n\n"
				+ StringsManager.getVar(R.string.AboutScene_Thanks) + " " + StringsManager.getVar(R.string.AboutScene_Thanks_Names) + "\n\n"
				+ StringsManager.getVar(R.string.AboutScene_Email_Us);
	}

	private static String getTRN() {
        return StringsManager.getVar(R.string.AboutScene_Translation) + "\n" + StringsManager.getVar(R.string.AboutScene_Translation_Names);
	}

	private Text createTouchEmail(final String address, Text text2)
	{
		Text text = createText(address, text2);
		text.hardlight( Window.TITLE_COLOR );
		
		TouchArea area = new TouchArea( text ) {
			@Override
			protected void onClick( Touch touch ) {
				Game.sendEmail(address, StringsManager.getVar(R.string.app_name));
			}
		};
		add(area);
		return text;
	}
	
	private Text createTouchLink(final int desc_res, final String address, Text visit)
	{
		Text text = createText(address, visit);
		text.hardlight( Window.TITLE_COLOR );
		
		TouchArea area = new TouchArea( text ) {
			@Override
			protected void onClick( Touch touch ) {
                Game.openUrl(StringsManager.getVar(desc_res), address);
			}
		};
		add(area);
		return text;
	}
	
	private void placeBellow(Text elem, Text upper)
	{
		elem.setX(upper.getX());
		elem.setY(upper.getY() + upper.height() + 1);
	}

	private Text createText(String text, Text upper)
	{
		Text multiline = createMultiline( text, GuiProperties.regularFontSize() );
		multiline.maxWidth(Camera.main.width * 5 / 6);
		add( multiline );
		if(upper!=null){
			placeBellow(multiline, upper);
		}
		return multiline;
	}
	
	@Override
	public void create() {
		super.create();

		Text text = createText(getTXT(), null );
		
		text.setX(align( (Camera.main.width - text.width()) / 2 ));
		text.setY(align( (Camera.main.height - text.height()) / 3 ));


        Text email = createTouchEmail(StringsManager.getVar(R.string.AboutScene_Mail), text);

        Text visit = createText(StringsManager.getVar(R.string.AboutScene_OurSite), email);
        Text site  = createTouchLink(R.string.AboutScene_OurSite, StringsManager.getVar(R.string.AboutScene_Lnk), visit);
		
		Text trn = createText(getTRN(), site);
		Text getCode = createText(StringsManager.getVar(R.string.AboutScene_SourceCode), trn);
		Text code = createTouchLink(R.string.AboutScene_SourceCode, "https://github.com/NYRDS/remixed-dungeon", getCode);
		
		Image nyrdie = Icons.NYRDIE.get();
		nyrdie.setX(align( text.getX() + (text.width() - nyrdie.width) / 2 ));
		nyrdie.setY(text.getY() - nyrdie.height - 8);
		add( nyrdie );

		TouchArea area = new TouchArea( nyrdie ) {
			private int clickCounter = 0;

			@Override
			protected void onClick( Touch touch ) {
				clickCounter++;

				if(clickCounter > 11) {
					return;
				}

				if(clickCounter>10) {
					Game.toast("Levels test mode enabled");
					Scene.setMode(LEVELS_TEST);
					return;
				}

				if(clickCounter>7) {
					Game.toast("Are you sure?");
					return;
				}

				if(clickCounter>3) {
					Game.toast("%d", clickCounter);
				}
			}
		};
		add(area);

		new Flare( 7, 64 ).color( 0x332211, true ).show( nyrdie, 0 ).angularSpeed = -20;

		if(Utils.isAndroid()) {
			ItemSprite sprite = new ItemSprite(new ArmorKit());
			sprite.alpha(0.1f);
			sprite.setX(align(text.getX() + (text.width() - sprite.width()) / 2));
			sprite.setY(nyrdie.getY() - sprite.height() - 8);
			add(sprite);

			TouchArea area2 = new TouchArea(sprite) {
				@Override
				protected void onClick(Touch touch) {
					Game.toast("Entering dev mode, pick directory");
					AndroidSAF.pickDirectoryForModInstall();
				}
			};
			add(area2);
			
			// Secret button for enabling WebServer (only on Android)
			ItemSprite webSprite = new ItemSprite();
			webSprite.view(Assets.ITEMS, 11, null);
			webSprite.alpha(0.1f);
			webSprite.setX(align(sprite.getX() + sprite.width() + 10));
			webSprite.setY(sprite.getY());
			add(webSprite);
			
			TouchArea webArea = new TouchArea(webSprite) {
				private int clickCounter = 0;
				
				@Override
				protected void onClick(Touch touch) {
					clickCounter++;
					
					if(clickCounter > 3) {
						Game.toast("dev mode enabled");
						// Start WebServer here
						com.nyrds.platform.app.WebServer server = new com.nyrds.platform.app.WebServer(8080);
						try {
							server.start();
							Game.toast("WebServer started on port 8080");
						} catch (java.io.IOException e) {
							com.nyrds.platform.EventCollector.logException(e, "WebServer");
							Game.toast("Failed to start WebServer");
						}
						return;
					}
					
					if(clickCounter > 1) {
						Game.toast("dev mode?");
					}
				}
			};
			add(webArea);
		}
		Archs archs = new Archs();
		archs.setSize( Camera.main.width, Camera.main.height );
        sendToBack(archs);

        ExitButton btnExit = new ExitButton();
		btnExit.setPos( Camera.main.width - btnExit.width(), 0 );
		add( btnExit );
		
		fadeIn();
	}
	
	@Override
	protected void onBackPressed() {
		RemixedDungeon.switchNoFade( TitleScene.class );
	}
}
