
package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.HealerNPC;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.platform.util.StringsManager;
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


	public WndPriest(final HealerNPC priest, final Char chr) {
		
		super(priest,instructions(chr));

		float y = height + 2*GAP;

		VBox vbox = new VBox();

		RedButton btnHealHero = new RedButton(  Utils.format(R.string.WndPriest_Heal, goldCost) ) {
			@Override
			protected void onClick() {
				Vector<Integer> patients = new Vector<>();
				patients.add(chr.getId());
				doHeal(priest, chr, patients, goldCost);
			}
		};

		btnHealHero.setSize(STD_WIDTH, BUTTON_HEIGHT);
		btnHealHero.enable(!(chr.gold()< goldCost));

		vbox.add( btnHealHero );

		if(chr instanceof Hero) {

			Hero hero = (Hero) chr;

			int minions = hero.countPets();

			if (minions > 0) {
				final int healAllMinionsCost = goldCostPerMinion * minions;
				RedButton btnHealMinions = new RedButton(Utils.format(R.string.WndPriest_Heal_Minions, healAllMinionsCost)) {
					@Override
					protected void onClick() {
						doHeal(priest, hero, hero.getPets(), healAllMinionsCost);
					}
				};

				btnHealMinions.setSize(STD_WIDTH, BUTTON_HEIGHT);
				btnHealMinions.enable(!(hero.gold() < healAllMinionsCost));

				vbox.add(btnHealMinions);
			}
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

	private static String instructions(Char hero) {
		goldCostPerMinion = (int) (GOLD_COST_PER_MINION * GameLoop.getDifficultyFactor());
		goldCost          = (int) (GOLD_COST * GameLoop.getDifficultyFactor());

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

	private void doHeal(HealerNPC priest, Char payer, Collection<Integer> patients, int healingCost) {
		hide();
		payer.spendGold(healingCost);
		for(Integer patientId: patients) {

			Char patient = CharsList.getById(patientId);

			PotionOfHealing.heal(patient, 1.0f);

			patient.hunger().satisfy(Hunger.STARVING);

			if(patient instanceof Mob) {
				if(patient.getEntityKind().equals("Brute")) {
					Badges.validateGnollUnlocked();
				}
			}
		}

        priest.say(StringsManager.getVar(R.string.HealerNPC_Message2));
	}
}
