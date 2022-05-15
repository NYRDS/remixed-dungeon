
package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.FortuneTellerNPC;
import com.nyrds.platform.util.StringsManager;
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

	private final Char hero;

	static private String instructions(final Char hero) {
		goldCost = (int) (GOLD_COST * GameLoop.getDifficultyFactor());

		if (hero.hasBuff(RingOfHaggler.Haggling.class ))
		{
			goldCost = (int) (goldCost * 0.9);
		}
        return Utils.format(R.string.WndFortuneTeller_Instruction, goldCost);
	}

	public WndFortuneTeller(FortuneTellerNPC fortuneTellerNPC, final Char hero) {

		super(fortuneTellerNPC, instructions(hero));

		this.hero = hero;

		float y = height + 2 * GAP;

		int unknownItemsCount = 0;

		for (Item item : hero.getBelongings()){
			if (!item.isIdentified()){
				unknownItemsCount++;
			}
		}

		int finalUnknownItemsCount = unknownItemsCount;

        RedButton btnYes = new RedButton( StringsManager.getVar(R.string.Wnd_Button_Yes) + " ( " + goldCost + " )" ) {
			@Override
			protected void onClick() {
				if (finalUnknownItemsCount > 0) {
                    GameScene.selectItem(WndFortuneTeller.this.hero, (item, selector) -> {
						if (item != null) {
							ScrollOfIdentify.identify(WndFortuneTeller.this.hero, item);
						}
					}, WndBag.Mode.UNIDENTIFED, StringsManager.getVar(R.string.ScrollOfIdentify_InvTitle));
					hide();
					hero.spendGold(goldCost);
				} else {
					hide();
                    GameScene.show(new WndQuest(fortuneTellerNPC, StringsManager.getVar(R.string.WndFortuneTeller_No_Item)));
				}
			}
		};

		btnYes.setRect( 0, y, STD_WIDTH, BUTTON_HEIGHT );
		add( btnYes );
		btnYes.enable(!(hero.gold()< goldCost));

		y += BUTTON_HEIGHT;

		if(finalUnknownItemsCount > 0) {
			final int identifyAllCost = goldCost * finalUnknownItemsCount;
            RedButton btnAll = new RedButton(StringsManager.getVar(R.string.WndFortuneTeller_IdentifyAll) + " ( " + identifyAllCost + " )") {
				@Override
				protected void onClick() {
					hero.getBelongings().identify();
					hide();
					hero.spendGold(identifyAllCost);
				}
			};

			btnAll.setRect( 0, y, STD_WIDTH, BUTTON_HEIGHT );
			add( btnAll );
			btnAll.enable(!(hero.gold() < identifyAllCost));
			y += BUTTON_HEIGHT ;
		}


        RedButton btnNo = new RedButton(R.string.Wnd_Button_No) {
			@Override
			protected void onClick() {
				hide();
			}
		};

		btnNo.setRect( 0, y, STD_WIDTH, BUTTON_HEIGHT );
		add( btnNo );
		
		resize( STD_WIDTH, (int)btnNo.bottom() );
	}

}
