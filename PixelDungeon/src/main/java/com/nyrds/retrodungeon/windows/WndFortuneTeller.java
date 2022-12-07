
package com.nyrds.retrodungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.retrodungeon.ml.R;
import com.nyrds.retrodungeon.mobs.npc.FortuneTellerNPC;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
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

	private static final String BTN_IDENTIFY     = Game.getVar(R.string.Wnd_Button_Yes);
	private static final String BTN_NO          = Game.getVar(R.string.Wnd_Button_No);
	private static final String TXT_INSTRUCTION = Game.getVar(R.string.WndFortuneTeller_Instruction);
	private static final String TXT_TITLE       = Game.getVar(R.string.WndFortuneTeller_Title);
	private static final String TXT_NO_ITEM     = Game.getVar(R.string.WndFortuneTeller_No_Item);


	private static final int BTN_HEIGHT	= 18;
	private static final int WIDTH		= 120;
	private int GOLD_COST  = 50;

	public WndFortuneTeller(FortuneTellerNPC fortune) {
		
		super();

		if (Dungeon.hero.belongings.ring1 instanceof RingOfHaggler || Dungeon.hero.belongings.ring2 instanceof RingOfHaggler )
		{
			GOLD_COST = (int) (GOLD_COST * 0.9);
		}

		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite(new Gold()) );
		titlebar.label( Utils.capitalize( TXT_TITLE ) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

		Text message = PixelScene.createMultiline( Utils.format(TXT_INSTRUCTION, GOLD_COST), GuiProperties.regularFontSize() );
		message.maxWidth(WIDTH);
		message.measure();
		message.y = titlebar.bottom() + GAP;
		add( message );

		final FortuneTellerNPC npc = fortune;

		RedButton btnYes = new RedButton( BTN_IDENTIFY + " ( "+ GOLD_COST + " )" ) {
			@Override
			protected void onClick() {
				boolean hasTarget = false;

				for (Item item : Dungeon.hero.belongings){
					if (!item.isIdentified()){
						hasTarget = true;
						break;
					}
				}

				if (hasTarget) {
					identify();
					hide();
					Dungeon.gold(Dungeon.gold() - GOLD_COST);
				} else{
					hide();
					GameScene.show(new WndQuest(npc, TXT_NO_ITEM));
				}
			}
		};

		btnYes.setRect( 0, message.y + message.height() + GAP, WIDTH, BTN_HEIGHT );
		add( btnYes );
		btnYes.enable(!(Dungeon.gold()< GOLD_COST));

		RedButton btnNo = new RedButton( BTN_NO ) {
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

	private static WndBag.Listener itemSelector = new WndBag.Listener() {
		@Override
		public void onSelect( Item item ) {
			if (item != null) {
				ScrollOfIdentify.identify(Dungeon.hero,item);
			}
		}
	};
}
