
package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.HealerNPC;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.rings.RingOfHaggler;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndQuest;

import java.util.Collection;
import java.util.Vector;

public class WndPriest extends WndQuest {

	static final private int GOLD_COST             = 75;
	static final private int GOLD_COST_PER_MINION  = 50;

	private static int goldCost;
	private static int goldCostPerMinion;


	public WndPriest(final HealerNPC priest, final Hero hero) {
		
		super(priest,instructions(hero));

		float y = height + 2*GAP;

		VBox vbox = new VBox();

		RedButton btnHealHero = new RedButton(  Utils.format(R.string.WndPriest_Heal, goldCost) ) {
			@Override
			protected void onClick() {
				Vector<Char> patients = new Vector<>();
				patients.add(hero);
				doHeal(priest, hero, patients, goldCost);
			}
		};

		btnHealHero.setSize(STD_WIDTH, BUTTON_HEIGHT);
		btnHealHero.enable(!(hero.gold()< goldCost));

		vbox.add( btnHealHero );

		int minions = hero.getPets().size();

		if (minions > 0) {
			final int healAllMinionsCost = goldCostPerMinion * minions;
			RedButton btnHealMinions = new RedButton(Utils.format(R.string.WndPriest_Heal_Minions, healAllMinionsCost)) {
				@Override
				protected void onClick() {
					doHeal(priest,hero,hero.getPets(),healAllMinionsCost);
				}
			};

			btnHealMinions.setSize(STD_WIDTH, BUTTON_HEIGHT);
			btnHealMinions.enable(!(hero.gold() < healAllMinionsCost));

			vbox.add(btnHealMinions);
		}

		RedButton btnLeave = new RedButton(R.string.WndMovieTheatre_No) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		btnLeave.setRect( 0, y, STD_WIDTH, BUTTON_HEIGHT);
		vbox.add( btnLeave );

		add(vbox);
		vbox.setRect(0,y,STD_WIDTH,vbox.childsHeight());

		resize( STD_WIDTH, (int) (vbox.bottom()));
	}

	private static String instructions(Hero hero) {
		goldCostPerMinion = (int) (GOLD_COST_PER_MINION * Game.getDifficultyFactor());
		goldCost          = (int) (GOLD_COST * Game.getDifficultyFactor());

		if (hero.hasBuff(RingOfHaggler.Haggling.class ))
		{
			goldCost *= 0.9;
			goldCostPerMinion *= 0.9;
		}

		int instruction = R.string.WndPriest_Instruction2_m;
		if(hero.getGender() == Utils.FEMININE){
			instruction = R.string.WndPriest_Instruction2_f;
		}

		return Utils.format(instruction) +"\n"+ Utils.format(R.string.WndPriest_Instruction2, goldCost);
	}

	private void doHeal(HealerNPC priest, Hero payer, Collection<? extends Char> patients, int healingCost) {
		hide();
		payer.spendGold(healingCost);
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
