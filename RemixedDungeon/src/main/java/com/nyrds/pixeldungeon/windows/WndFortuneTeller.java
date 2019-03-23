
package com.nyrds.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.FortuneTellerNPC;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.rings.RingOfHaggler;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfIdentify;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.IconTitle;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndQuest;

public class WndFortuneTeller extends Window {

	private static final int BTN_HEIGHT	= 18;
	private static final int WIDTH		= 120;
	private int GOLD_COST  = 50;

	public WndFortuneTeller(FortuneTellerNPC fortune, final Hero hero) {
		
		super();

		GOLD_COST *= Game.getDifficultyFactor();

		if (hero.hasBuff(RingOfHaggler.Haggling.class ))
		{
			GOLD_COST = (int) (GOLD_COST * 0.9);
		}

		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite(new Gold()) );
		titlebar.label( Utils.capitalize( Game.getVar(R.string.WndFortuneTeller_Title)) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

		Text message = PixelScene.createMultiline( Utils.format(Game.getVar(R.string.WndFortuneTeller_Instruction), GOLD_COST), GuiProperties.regularFontSize() );
		message.maxWidth(WIDTH);
		message.y = titlebar.bottom() + GAP;
		add( message );

		final FortuneTellerNPC npc = fortune;

		RedButton btnYes = new RedButton( Game.getVar(R.string.Wnd_Button_Yes) + " ( "+ GOLD_COST + " )" ) {
			@Override
			protected void onClick() {
				boolean hasTarget = false;

				for (Item item : hero.belongings){
					if (!item.isIdentified()){
						hasTarget = true;
						break;
					}
				}

				if (hasTarget) {
					identify();
					hide();
					hero.spendGold(GOLD_COST);
				} else{
					hide();
					GameScene.show(new WndQuest(npc, Game.getVar(R.string.WndFortuneTeller_No_Item)));
				}
			}
		};

		btnYes.setRect( 0, message.y + message.height() + GAP, WIDTH, BTN_HEIGHT );
		add( btnYes );
		btnYes.enable(!(hero.gold()< GOLD_COST));

		RedButton btnNo = new RedButton( Game.getVar(R.string.Wnd_Button_No) ) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		btnNo.setRect( 0, btnYes.bottom() + GAP, WIDTH, BTN_HEIGHT );
		add( btnNo );
		
		resize( WIDTH, (int)btnNo.bottom() );
	}

	public static WndBag identify() {
		return GameScene.selectItem( itemSelector, WndBag.Mode.UNIDENTIFED, Game.getVar(R.string.ScrollOfIdentify_InvTitle));
	}

	private static WndBag.Listener itemSelector = item -> {
		if (item != null) {
			ScrollOfIdentify.identify(Dungeon.hero,item);
		}
	};
}
