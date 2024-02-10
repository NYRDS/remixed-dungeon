
package com.watabou.pixeldungeon.ui;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.windows.WndBuffInfo;
import com.nyrds.platform.input.Touchscreen;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.TouchArea;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.scenes.GameScene;

import java.util.HashMap;
import java.util.Map;

import lombok.val;

public class BuffIndicator extends Component {

	public static final int NONE	= -1;
	
	public static final int MIND_VISION	= 0;
	public static final int LEVITATION	= 1;
	public static final int FIRE		= 2;
	public static final int POISON		= 3;
	public static final int PARALYSIS	= 4;
	public static final int HUNGER		= 5;
	public static final int STARVATION	= 6;
	public static final int SLOW		= 7;
	public static final int OOZE		= 8;
	public static final int AMOK		= 9;
	public static final int TERROR		= 10;
	public static final int ROOTS		= 11;
	public static final int INVISIBLE	= 12;
	public static final int SHADOWS		= 13;
	public static final int WEAKNESS	= 14;
	public static final int FROST		= 15;
	public static final int BLINDNESS	= 16;
	public static final int COMBO		= 17;
	public static final int FURY		= 18;
	public static final int HEALING		= 19;
	public static final int ARMOR		= 20;
	public static final int HEART		= 21;
	public static final int LIGHT		= 22;
	public static final int CRIPPLE		= 23;
	public static final int BARKSKIN	= 24;
	public static final int BLEEDING	= 26;
	public static final int MARK		= 27;
	public static final int DEFERRED	= 28;
	public static final int VERTIGO		= 29;
	public static final int ROSE        = 30;
	public static final int CURSED_ROSE = 31;
	public static final int BLOODLUST   = 32;
	public static final int RAT_SKULL   = 33;
	public static final int RATTNESS    = 34;
	public static final int DARKVEIL    = 35;
	public static final int FROSTAURA   = 36;
	public static final int STONEBLOOD  = 37;
	public static final int NECROTISM	= 39;

	public static final int BLEESSED    = 42;
	public static final int MOONGRACE   = 51;

	
	//public static final int   SIZE	= 16;
	public static final int   SIZE	= 7;
	public static final float ICON_SIZE = 7;

    private static BuffIndicator heroInstance;

	private TextureFilm film;
	
	private Map<Integer, Image> icons = new HashMap<>();
	
	private final Char ch;
	
	public BuffIndicator( Char ch ) {
		super();
		
		this.ch = ch;
		if (ch == Dungeon.hero) {
			heroInstance = this;
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		if (this == heroInstance) {
			heroInstance = null;
		}
	}
	
	@Override
	protected void createChildren() {
		film = new TextureFilm(TextureCache.get(Assets.BUFFS_SMALL), SIZE, SIZE );
	}
	
	@Override
	public void update() {
		clear();
		
		val newIcons = new HashMap<Integer, Image>();
		final int[] iconCounter = {0};

		ch.forEachBuff(b->
		{
			int icon = b.icon();
			if (icon != NONE) {
				Image img = new Image(TextureCache.get(b.textureSmall()));
				img.frame(film.get(icon));
				img.setX(x + iconCounter[0] * (ICON_SIZE+1));
				img.setY(y);
				iconCounter[0] +=1;
				img.setScaleXY(ICON_SIZE/SIZE,ICON_SIZE/SIZE);
				val imgTouch = new TouchArea(img) {
					@Override
					protected void onClick(Touchscreen.Touch touch) {
						GameScene.show( new WndBuffInfo(b));
					}
				};
				add(imgTouch);
				add(img);

				newIcons.put( icon, img );
			}
		});

		for (Integer key : icons.keySet()) {
			if (newIcons.get( key ) == null) {
				Image icon = icons.get( key );
				icon.setOrigin( ICON_SIZE / 2 );
				add( icon );
				add( new AlphaTweener( icon, 0, 0.6f ) {
					@Override
					protected void updateValues( float progress ) {
						super.updateValues( progress );
						image.setScale( ICON_SIZE/SIZE*(1 + 5 * progress) );
					}
				} );
			}
		}
		
		icons = newIcons;
	}

	@LuaInterface
	public static void refreshHero() {
		if (heroInstance != null) {
			heroInstance.layout();
		}
	}
}
