
package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.FortuneTellerNPC;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.rings.RingOfHaggler;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfIdentify;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndQuest;

public class WndFortuneTeller extends WndQuest {

	private static final int GOLD_COST  = 50;
	private static int goldCost;

	private Char hero;

	static private String instructions(final Char hero) {
		goldCost = (int) (GOLD_COST * Game.getDifficultyFactor());

		if (hero.hasBuff(RingOfHaggler.Haggling.class ))
		{
			goldCost = (int) (goldCost * 0.9);
		}
		return Utils.format(Game.getVar(R.string.WndFortuneTeller_Instruction), goldCost);
	}

	public WndFortuneTeller(FortuneTellerNPC fortuneTellerNPC, final Char hero) {

		super(fortuneTellerNPC, instructions(hero));

		this.hero = hero;

		float y = height + 2*GAP;

		RedButton btnYes = new RedButton( Game.getVar(R.string.Wnd_Button_Yes) + " ( "+ goldCost + " )" ) {
			@Override
			protected void onClick() {
				boolean hasTarget = false;

				for (Item item : hero.getBelongings()){
					if (!item.isIdentified()){
						hasTarget = true;
						break;
					}
				}

				if (hasTarget) {
					identify();
					hide();
					hero.spendGold(goldCost);
				} else{
					hide();
					GameScene.show(new WndQuest(fortuneTellerNPC, Game.getVar(R.string.WndFortuneTeller_No_Item)));
				}
			}
		};

		btnYes.setRect( 0, y, STD_WIDTH, BUTTON_HEIGHT );
		add( btnYes );
		btnYes.enable(!(hero.gold()< goldCost));

		RedButton btnNo = new RedButton( Game.getVar(R.string.Wnd_Button_No) ) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		btnNo.setRect( 0, btnYes.bottom(), STD_WIDTH, BUTTON_HEIGHT );
		add( btnNo );
		
		resize( STD_WIDTH, (int)btnNo.bottom() );
	}

	public WndBag identify() {
		return GameScene.selectItem(hero, itemSelector, WndBag.Mode.UNIDENTIFED, Game.getVar(R.string.ScrollOfIdentify_InvTitle));
	}

	private WndBag.Listener itemSelector = (item, selector) -> {
		if (item != null) {
			ScrollOfIdentify.identify(hero,item);
		}
	};
}
