
package com.nyrds.retrodungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.retrodungeon.ml.R;
import com.nyrds.retrodungeon.mobs.npc.HealerNPC;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.rings.RingOfHaggler;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.IconTitle;

public class WndPriest extends Window {

	private static final String BTN_HEAL       = Game.getVar(R.string.WndPriest_Heal);
	private static final String BTN_NO          = Game.getVar(R.string.WndMovieTheatre_No);
	private static final String TXT_BYE         = Game.getVar(R.string.HealerNPC_Message2);
	private static final String TXT_INSTRUCTION_M = Game.getVar(R.string.WndPriest_Instruction_m);
	private static final String TXT_INSTRUCTION_F = Game.getVar(R.string.WndPriest_Instruction_f);
	private static final String TXT_TITLE       = Game.getVar(R.string.WndPriest_Title);

	private static final int BTN_HEIGHT	= 18;
	private static final int WIDTH		= 120;
	private int GOLD_COST  = 75;

	public WndPriest(final HealerNPC npc, final Hero hero) {
		
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

		String instruction = TXT_INSTRUCTION_M;
		if(Dungeon.hero.getGender() == Utils.FEMININE){
			instruction = TXT_INSTRUCTION_F;
		}

		Text message = PixelScene.createMultiline( Utils.format(instruction, GOLD_COST), GuiProperties.regularFontSize() );
		message.maxWidth(WIDTH);
		message.measure();
		message.y = titlebar.bottom() + GAP;
		add( message );
		
		RedButton btnYes = new RedButton(  Utils.format(BTN_HEAL, GOLD_COST) ) {
			@Override
			protected void onClick() {
				npc.say( TXT_BYE );
				doHeal( hero );
				Dungeon.gold(Dungeon.gold() - GOLD_COST);
			}
		};

		btnYes.setRect( 0, message.y + message.height() + GAP, WIDTH, BTN_HEIGHT );
		add( btnYes );
		btnYes.enable(!(Dungeon.gold()< GOLD_COST));


		RedButton btnNo = new RedButton( BTN_NO ) {
			@Override
			protected void onClick() {
				npc.say( TXT_BYE );
				hide();
			}
		};
		btnNo.setRect( 0, btnYes.bottom() + GAP, WIDTH, BTN_HEIGHT );
		add( btnNo );
		
		resize( WIDTH, (int)btnNo.bottom() );
	}

	private void doHeal(final Hero hero) {
		hide();
		if(hero!=null && hero.isAlive()) {
			PotionOfHealing.heal(hero,1.0f);
			hero.buff( Hunger.class ).satisfy(Hunger.STARVING);
		}
	}
}
