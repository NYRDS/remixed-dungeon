
package com.nyrds.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.HealerNPC;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.rings.RingOfHaggler;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.IconTitle;

import java.util.Collection;
import java.util.Vector;

public class WndPriest extends Window {

	private static final int BTN_HEIGHT	= 18;
	private static final int WIDTH		= 120;

	private int GOLD_COST            = 75;
	private int GOLD_COST_PER_MINION = 50;

	public WndPriest(final HealerNPC priest, final Hero hero) {
		
		super();

		float y = 0;

		GOLD_COST_PER_MINION *= Game.instance().getDifficultyFactor();
		GOLD_COST            *= Game.instance().getDifficultyFactor();

		if (hero.buff( RingOfHaggler.Haggling.class )!= null)
		{
			GOLD_COST = (int) (GOLD_COST * 0.9);
			GOLD_COST_PER_MINION = (int) (GOLD_COST_PER_MINION * 0.9);
		}

		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite(new Gold()) );
		titlebar.label( Utils.capitalize(Game.getVar(R.string.WndPriest_Title)) );
		titlebar.setRect( 0, y, WIDTH, 0 );
		add( titlebar );

		y = titlebar.bottom() + GAP;

		int instruction = R.string.WndPriest_Instruction_m;
		if(hero.getGender() == Utils.FEMININE){
			instruction = R.string.WndPriest_Instruction_f;
		}

		Text message = PixelScene.createMultiline( Utils.format(instruction, GOLD_COST), GuiProperties.regularFontSize() );
		message.maxWidth(WIDTH);
		message.measure();
		message.y = y;
		add( message );

		y = message.bottom() + GAP;

		RedButton btnHealHero = new RedButton(  Utils.format(R.string.WndPriest_Heal, GOLD_COST) ) {
			@Override
			protected void onClick() {
				Vector<Char> patients = new Vector<>();
				patients.add(hero);
				doHeal(priest, patients, GOLD_COST);
			}
		};

		btnHealHero.setRect( 0, y, WIDTH, BTN_HEIGHT );
		btnHealHero.enable(!(Dungeon.gold()< GOLD_COST));

		add( btnHealHero );

		y = btnHealHero.bottom() + GAP;

		int minions = hero.getPets().size();

		if (minions > 0) {
			final int healAllMinionsCost = GOLD_COST_PER_MINION * minions;
			RedButton btnHealMinions = new RedButton(Utils.format(R.string.WndPriest_Heal_Minions, healAllMinionsCost)) {
				@Override
				protected void onClick() {
					doHeal(priest,hero.getPets(),healAllMinionsCost);
				}
			};

			btnHealMinions.setRect(0, y, WIDTH, BTN_HEIGHT);
			btnHealMinions.enable(!(Dungeon.gold() < healAllMinionsCost));

			add(btnHealMinions);
			y = btnHealMinions.bottom() + GAP;
		}

		RedButton btnLeave = new RedButton(R.string.WndMovieTheatre_No) {
			@Override
			protected void onClick() {
				doHeal(priest,new Vector<Char>(),0);
			}
		};
		btnLeave.setRect( 0, y, WIDTH, BTN_HEIGHT );
		add( btnLeave );
		y = btnLeave.bottom() + GAP;
		resize( WIDTH, (int) (y));
	}

	private void doHeal(HealerNPC priest, Collection<? extends Char> patients, int healingCost) {
		hide();
		Dungeon.gold(Dungeon.gold() - healingCost);
		for(Char patient: patients) {
			PotionOfHealing.heal(patient, 1.0f);
			if(patient instanceof Hero) {
				patient.buff(Hunger.class).satisfy(Hunger.STARVING);
			}

			if(patient instanceof Mob) {
				if(((Mob) patient).getMobClassName().equals("Brute")) {
					Badges.validateGnollUnlocked();
				}
			}
		}

		priest.say(Game.getVar(R.string.HealerNPC_Message2));
	}
}
