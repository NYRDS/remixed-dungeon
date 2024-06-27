
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.npcs.Ghost;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.quest.DriedRose;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class WndSadGhost extends Window {
	
	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 18;
	
	public WndSadGhost( final Ghost ghost, final Item item ) {
		
		super();
		
		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( item ) );
		titlebar.label( Utils.capitalize( item.name() ) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

        Text message = PixelScene.createMultiline(
				item instanceof DriedRose ? R.string.WndSadGhost_Rose : R.string.WndSadGhost_Rat,
				GuiProperties.regularFontSize()
		);
		message.maxWidth(WIDTH);
		message.setY(titlebar.bottom() + GAP);
		add( message );

        RedButton btnWeapon = new RedButton(R.string.WndSadGhost_Wepon) {
			@Override
			protected void onClick() {
				selectReward( ghost, item, Ghost.Quest.getWeapon());
			}
		};
		btnWeapon.setRect( 0, message.getY() + message.height() + GAP, WIDTH, BTN_HEIGHT );
		add( btnWeapon );

        RedButton btnArmor = new RedButton(R.string.WndSadGhost_Armor) {
			@Override
			protected void onClick() {
				selectReward( ghost, item, Ghost.Quest.getArmor());
			}
		};
		btnArmor.setRect( 0, btnWeapon.bottom() + GAP, WIDTH, BTN_HEIGHT );
		add( btnArmor );
		
		resize( WIDTH, (int)btnArmor.bottom() );
	}

	private void selectReward(Ghost ghost, @NotNull Item item, @NotNull Item reward ) {
		hide();

		item.removeItemFrom(Dungeon.hero);

		Dungeon.hero.collectAnimated(reward);

        ghost.say(StringsManager.getVar(R.string.WndSadGhost_Farewell));
		ghost.die( CharsList.DUMMY );
		
		Ghost.Quest.complete();
	}
}
