
package com.nyrds.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.HealerNPC;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.Identification;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.IconTitle;
import com.watabou.pixeldungeon.windows.WndBag;

public class WndFortuneTeller extends Window {

	private static final String BTN_IDENTIFY       = Game.getVar(R.string.Wnd_Button_Yes);
	private static final String BTN_NO          = Game.getVar(R.string.Wnd_Button_No);
	private static final String TXT_INSTRUCTION = Game.getVar(R.string.WndFortuneTeller_Instruction);
	private static final String TXT_TITLE = Game.getVar(R.string.WndFortuneTeller_Title);
	private static final String TXT_NO_ITEM = Game.getVar(R.string.WndFortuneTeller_No_Item);


	private static final int BTN_HEIGHT	= 18;
	private static final int WIDTH		= 120;
	private static final int GOLD_COST  = 75;

	public WndFortuneTeller() {
		
		super();
		
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
		
		RedButton btnYes = new RedButton( BTN_IDENTIFY + " ( "+ GOLD_COST + " )" ) {
			@Override
			protected void onClick() {
				identify();
				Dungeon.gold(Dungeon.gold() - GOLD_COST);
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
				Dungeon.hero.getSprite().getParent().add( new Identification( Dungeon.hero.getSprite().center().offset( 0, -16 ) ) );

				item.identify();
				GLog.i(Utils.format(Game.getVar(R.string.ScrollOfIdentify_Info1), item));

				Badges.validateItemLevelAquired( item );
			}
		}
	};
}
