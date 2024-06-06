
package com.watabou.pixeldungeon.ui;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.windows.WndBuffInfo;
import com.nyrds.platform.input.Touchscreen;
import com.watabou.noosa.Image;
import com.watabou.noosa.TouchArea;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.CharModifier;
import com.watabou.pixeldungeon.scenes.GameScene;

import org.apache.commons.collections4.map.HashedMap;
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

	public static final float ICON_SCALE = ICON_SIZE / SIZE;

    private static BuffIndicator heroInstance;

	private final Map<Integer, Image> icons = new HashedMap<>();
	private final Map<Integer, Image> newIcons = new HashedMap<>();
	private final Char ch;

	private int updateCount;
	
	public BuffIndicator( Char ch ) {
		super();

		updateCount = 0;

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
	public void update() {
		super.update();

		if(ch.getBuffsUpdatedCount() > updateCount) {
			if(findByClass(Tweener.class, 0) >= 0) { //Tweener is still running
				return;
			}

			updateCount = ch.getBuffsUpdatedCount();

			clear();

			val buffs = ch.buffs();
			int iconCounter = 0;
			for (CharModifier b : buffs)
			{
				int icon = b.icon();
				if (icon != NONE ) {
					Image img = b.smallIcon();
					if (img != null) {
						img.setX(x + iconCounter * (ICON_SIZE + 1));
						img.setY(y);
						iconCounter += 1;
						img.setScaleXY(ICON_SCALE , ICON_SCALE );
						val imgTouch = new TouchArea(img) {
							@Override
							protected void onClick(Touchscreen.Touch touch) {
								GameScene.show(new WndBuffInfo(b));
							}
						};
						add(imgTouch);
						add(img);

						if(!icons.containsKey(icon)) {
							img.setScaleXY(ICON_SCALE * 6, ICON_SCALE * 6);
							img.alpha(0);
							var fadeInTweener = new AlphaTweener(img, 1, 0.6f) {
								@Override
								protected void updateValues(float progress) {
									super.updateValues(progress);
									image.setScale(ICON_SCALE * (6 - 5 * progress));
								}
							};
							add(fadeInTweener);
						}
						newIcons.put(icon, img);
					}
				}
			}

			for (Integer key : icons.keySet()) {
				if (newIcons.get(key) == null) {
					Image icon = icons.get(key);
					icon.setOrigin(ICON_SIZE / 2);
					add(icon);

					var fadeOutTweener = new AlphaTweener(icon, 0, 0.6f) {
						@Override
						protected void updateValues(float progress) {
							super.updateValues(progress);
							image.setScale(ICON_SCALE * (1 + 5 * progress));
						}
					};
					fadeOutTweener.listener = (t) -> icon.killAndErase();
					add(fadeOutTweener);
				}
			}

			icons.clear();
			icons.putAll(newIcons);
			newIcons.clear();
		}
	}

	@LuaInterface
	public static void refreshHero() { //used by mods, can't remove it
		if (heroInstance != null) {
			heroInstance.ch.buffsUpdated();
		}
	}
}
