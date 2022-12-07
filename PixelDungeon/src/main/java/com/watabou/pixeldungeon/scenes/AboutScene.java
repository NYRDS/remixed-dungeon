/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.scenes;

import android.content.Intent;
import android.net.Uri;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.input.Touchscreen.Touch;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.TouchArea;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.ui.Archs;
import com.watabou.pixeldungeon.ui.ExitButton;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.Window;

public class AboutScene extends PixelScene {

	private static final String TXT          = Game.getVar(R.string.AboutScene_Txt_Intro);
	private static final String TXT_CODE     = Game.getVar(R.string.AboutScene_Code) + " " + Game.getVar(R.string.AboutScene_Code_Names);
	private static final String TXT_GRAPHICS = Game.getVar(R.string.AboutScene_Graphics) + " " + Game.getVar(R.string.AboutScene_Graphics_Names);
	private static final String TXT_MUSIC    = Game.getVar(R.string.AboutScene_Music) + " " + Game.getVar(R.string.AboutScene_Music_Names);
	private static final String TXT_SOUND    = Game.getVar(R.string.AboutScene_Sound) + " " + Game.getVar(R.string.AboutScene_Sound_Names);
	private static final String TXT_THANKS   = Game.getVar(R.string.AboutScene_Thanks) + " " + Game.getVar(R.string.AboutScene_Thanks_Names);
	private static final String TXT_EMAIL_US = Game.getVar(R.string.AboutScene_Email_Us);

	private static final String OUR_SITE = Game.getVar(R.string.AboutScene_OurSite);
	private static final String LNK = Game.getVar(R.string.AboutScene_Lnk);
	private static final String SND = Game.getVar(R.string.AboutScene_Snd);
	private static final String TRN = Game.getVar(R.string.AboutScene_Translation) + "\n\t" + Game.getVar(R.string.AboutScene_Translation_Names);

	private static String getTXT() {
		return TXT + "\n\n" + TXT_CODE + "\n\n" + TXT_GRAPHICS + "\n\n" + TXT_MUSIC + "\n\n" + TXT_SOUND + "\n\n" + TXT_THANKS + "\n\n" + TXT_EMAIL_US;
	}

	private static String getTRN() {
		return TRN;
	}

	private Text createTouchEmail(final String address, Text text2)
	{
		Text text = createText(address, text2);
		text.hardlight( Window.TITLE_COLOR );
		
		TouchArea area = new TouchArea( text ) {
			@Override
			protected void onClick( Touch touch ) {
				Intent intent = new Intent( Intent.ACTION_SEND);
				intent.setType("message/rfc822");
				intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address} );
				intent.putExtra(Intent.EXTRA_SUBJECT, Game.getVar(R.string.app_name) );

				Game.instance().startActivity( Intent.createChooser(intent, SND) );
			}
		};
		add(area);
		return text;
	}
	
	private Text createTouchLink(final String address, Text visit)
	{
		Text text = createText(address, visit);
		text.hardlight( Window.TITLE_COLOR );
		
		TouchArea area = new TouchArea( text ) {
			@Override
			protected void onClick( Touch touch ) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));

				Game.instance().startActivity( Intent.createChooser(intent, OUR_SITE) );
			}
		};
		add(area);
		return text;
	}
	
	private void placeBellow(Text elem, Text upper)
	{
		elem.x = upper.x;
		elem.y = upper.y + upper.height();
	}

	private Text createText(String text, Text upper)
	{
		Text multiline = createMultiline( text, GuiProperties.regularFontSize() );
		multiline.maxWidth(Camera.main.width * 5 / 6);
		multiline.measure();
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
		
		text.camera = uiCamera;
		
		text.x = align( (Camera.main.width - text.width()) / 2 );
		text.y = align( (Camera.main.height - text.height()) / 3 );
		

		Text email = createTouchEmail(Game.getVar(R.string.AboutScene_Mail), text);

		Text visit = createText(Game.getVar(R.string.AboutScene_OurSite), email);
		Text site  = createTouchLink(LNK, visit);		
		
		createText("\n"+ getTRN(), site);
		
		Image nyrdie = Icons.NYRDIE.get();
		nyrdie.x = align( text.x + (text.width() - nyrdie.width) / 2 );
		nyrdie.y = text.y - nyrdie.height - 8;
		add( nyrdie );
		
		new Flare( 7, 64 ).color( 0x332211, true ).show( nyrdie, 0 ).angularSpeed = -20;
		
		Archs archs = new Archs();
		archs.setSize( Camera.main.width, Camera.main.height );
		addToBack( archs );
		
		ExitButton btnExit = new ExitButton();
		btnExit.setPos( Camera.main.width - btnExit.width(), 0 );
		add( btnExit );
		
		fadeIn();
	}
	
	@Override
	protected void onBackPressed() {
		PixelDungeon.switchNoFade( TitleScene.class );
	}
}
