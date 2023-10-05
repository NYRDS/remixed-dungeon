
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.npcs.WandMaker;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

public class WndWandmaker extends Window {
	
	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 18;
	
	public WndWandmaker( final WandMaker wandmaker, final Item item ) {
		
		super();
		
		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( item ) );
		titlebar.label( Utils.capitalize( item.name() ) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

        Text message = PixelScene.createMultiline(R.string.WndWandmaker_Message, GuiProperties.regularFontSize() );
		message.maxWidth(WIDTH);
		message.setY(titlebar.bottom() + GAP);
		add( message );

        RedButton btnBattle = new RedButton(R.string.WndWandmaker_Battle) {
			@Override
			protected void onClick() {
				selectReward( wandmaker, item, WandMaker.makeBattleWand() );
			}
		};
		btnBattle.setRect( 0, message.getY() + message.height() + GAP, WIDTH, BTN_HEIGHT );
		add( btnBattle );

        RedButton btnNonBattle = new RedButton(R.string.WndWandmaker_NonBattle) {
			@Override
			protected void onClick() {
				selectReward( wandmaker, item, WandMaker.makeNonBattleWand() );
			}
		};
		btnNonBattle.setRect( 0, btnBattle.bottom() + GAP, WIDTH, BTN_HEIGHT );
		add( btnNonBattle );
		
		resize( WIDTH, (int)btnNonBattle.bottom() );
	}
	
	private void selectReward( WandMaker wandmaker, Item item, Wand reward ) {
		hide();

		item.removeItemFrom(Dungeon.hero);

		reward.identify();
		if (reward.doPickUp( Dungeon.hero )) {
			GLog.i( Hero.getHeroYouNowHave(), reward.name() );
		} else {
			reward.doDrop(wandmaker);
		}

        wandmaker.say(Utils.format(R.string.WndWandmaker_Farawell, Dungeon.hero.className() ) );
		wandmaker.destroy();
		
		wandmaker.getSprite().die();
		
		WandMaker.Quest.complete();
	}
}
