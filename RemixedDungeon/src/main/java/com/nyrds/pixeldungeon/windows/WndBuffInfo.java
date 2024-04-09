package com.nyrds.pixeldungeon.windows;

import com.nyrds.util.GuiProperties;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.buffs.CharModifier;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.windows.IconTitle;

import org.jetbrains.annotations.NotNull;

public class WndBuffInfo extends Window {

	private final TextureFilm film;

	protected static final int BTN_HEIGHT	= 18;
	protected static final int WIDTH		= 100;
	protected static final int GAP		    = 2;

	public WndBuffInfo(@NotNull final CharModifier buff) {
		super();

		SmartTexture icons = TextureCache.get(Assets.BUFFS_LARGE);
		film = new TextureFilm(icons, 16, 16 );
		int index = buff.icon();
		int bottom = 0;
		int height = 0;

		//Title
		if (index != BuffIndicator.NONE) {
			Image icon = new Image(TextureCache.get(buff.textureLarge()));
			icon.frame(film.get(index));

			IconTitle title = new IconTitle(icon, buff.name());
			title.setRect(0, 0, WIDTH, 0);
			add(title);
			bottom = (int)title.bottom();
			height = (int)title.height();
		} else {
			Text title = PixelScene.createText(buff.name(), GuiProperties.titleFontSize());
			add(title);
			bottom = (int)title.bottom() + GAP;
			height = (int)title.height();
		}

		//Info text
		Text info = PixelScene.createMultiline(buff.desc(), GuiProperties.regularFontSize() );
		info.maxWidth(WIDTH);
		info.setY(bottom + GAP);
		add( info );

		resize( WIDTH, (height + (int)info.height() + GAP * 2));
	}
}
