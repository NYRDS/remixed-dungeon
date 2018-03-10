package com.nyrds.pixeldungeon.items.artifacts;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.mechanics.spells.SpellFactory;
import com.nyrds.pixeldungeon.mechanics.spells.SpellHelper;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.CommonActions;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SpellBook extends Artifact {

	@Packable(defaultValue = "")
	private String spell;

	public SpellBook() {
		imageFile = "items/books.png";
		image = 3;
		defaultAction = CommonActions.AC_READ;
	}

	public void spell(String spellName){
		spell = spellName;
	}

	public Spell spell(){
		if(spell == null || spell.equals("")){
			ArrayList<String> spells = SpellFactory.getSpellsByAffinity(SpellHelper.AFFINITY_COMMON);
			spell(Random.element(spells));
		}
		return SpellFactory.getSpellByName(spell);
	}

	@Override
	public ArrayList<String> actions(Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped(hero)){
			actions.add(CommonActions.AC_READ );
		}
		return actions;
	}

	@Override
	public void execute(final Hero ch, String action) {
		setCurUser(ch);
		if (action.equals(CommonActions.AC_READ)) {
			spell().cast(ch);
		} else {
			super.execute(ch, action);
		}
	}

	@Override
	public String desc(){
		if(this.isIdentified()){
			return  Utils.format(R.string.SpellBook_Info_Identified, spell().name(), spell().desc());
		}
		return super.desc();
	}

	@Override
	public Item burn(int cell){
		return null;
	}

	@Override
	public int price() {
		return 500;
	}
}
