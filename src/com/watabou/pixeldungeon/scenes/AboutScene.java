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

import com.watabou.input.Touchscreen.Touch;
import com.watabou.noosa.BitmapTextMultiline;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TouchArea;
import com.watabou.pixeldungeon.PixelDungeon;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.ui.Archs;
import com.watabou.pixeldungeon.ui.ExitButton;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.Window;

public class AboutScene extends PixelScene {

	private static final String TXT           = Game.getVar(R.string.AboutScene_Txt);
	private static final String LNK           = Game.getVar(R.string.AboutScene_Lnk);
	private static final String TRANSLATE     = Game.getVar(R.string.AboutScene_Translate);
	private static final String TRANSLATE_LNK = Game.getVar(R.string.AboutScene_TranslateLnk);
	private static final String TRANSLATE_SND = Game.getVar(R.string.AboutScene_TranslateSnd);
	
	private static final String TRANSLATE_RUS_SUP     = Game.getVar(R.string.AboutScene_TranslateRusSup);
	private static final String TRANSLATE_RUS_SUP_LNK = Game.getVar(R.string.AboutScene_TranslateRusSupLnk);
	
	private static final String TRANSLATE_RUS         = Game.getVar(R.string.AboutScene_TranslateRus);
	private static final String TRANSLATE_RUS_LNK     = Game.getVar(R.string.AboutScene_TranslateRusLnk);
	
	private BitmapTextMultiline createTouchEmail(final String address, BitmapTextMultiline upper)
	{
		BitmapTextMultiline text = createText(address);
		text.hardlight( Window.TITLE_COLOR );
		placeBellow(text, upper);
		
		TouchArea area = new TouchArea( text ) {
			@Override
			protected void onClick( Touch touch ) {
				Intent intent = new Intent( Intent.ACTION_SEND);
				intent.setType("message/rfc822");
				intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address} );
				intent.putExtra(Intent.EXTRA_SUBJECT, Game.getVar(R.string.app_name) );

				Game.instance.startActivity( Intent.createChooser(intent, TRANSLATE_SND) );
			}
		};
		add(area);
		return text;
	}
	
	private void placeBellow(BitmapTextMultiline elem, BitmapTextMultiline upper)
	{
		elem.x = upper.x;
		elem.y = upper.y + upper.height();
	}

	private BitmapTextMultiline createText(String text)
	{
		BitmapTextMultiline multiline = createMultiline( text, 8 );
		multiline.maxWidth = Math.min( Camera.main.width, 120 );
		multiline.measure();
		add( multiline );
		
		return multiline;
	}
	
	@Override
	public void create() {
		super.create();
		
		BitmapTextMultiline text = createText( TXT );
		
		text.x = align( (Camera.main.width - text.width()) / 2 );
		text.y = align( (Camera.main.height - text.height()) / 3 );
		
		BitmapTextMultiline link = createText( LNK );
		add( link );
		link.hardlight( Window.TITLE_COLOR );
		
		placeBellow(link,text);
		
		TouchArea hotArea = new TouchArea( link ) {
			@Override
			protected void onClick( Touch touch ) {
				Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( "http://" + LNK ) );
				Game.instance.startActivity( intent );
			}
		};
		add( hotArea );


		BitmapTextMultiline txtTra = createText(TRANSLATE);		
		placeBellow(txtTra,link);
		
		BitmapTextMultiline lnkTra = createTouchEmail(TRANSLATE_LNK, txtTra);
		
		BitmapTextMultiline txtTraRusSup = createText( TRANSLATE_RUS_SUP);		
		placeBellow(txtTraRusSup,lnkTra);

		BitmapTextMultiline traRusSupLnk = createTouchEmail(TRANSLATE_RUS_SUP_LNK, txtTraRusSup);
		
		BitmapTextMultiline txtTraRus = createText( TRANSLATE_RUS);	
		placeBellow(txtTraRus, traRusSupLnk);
		BitmapTextMultiline traRusLnk = createTouchEmail(TRANSLATE_RUS_LNK, txtTraRus);
		
			
		Image wata = Icons.WATA.get();
		wata.x = align( text.x + (text.width() - wata.width) / 2 );
		wata.y = text.y - wata.height - 8;
		add( wata );
		
		new Flare( 7, 64 ).color( 0x112233, true ).show( wata, 0 ).angularSpeed = +20;
		
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
