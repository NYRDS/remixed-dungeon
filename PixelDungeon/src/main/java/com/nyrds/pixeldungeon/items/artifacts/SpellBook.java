package com.nyrds.pixeldungeon.items.artifacts;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.mechanics.spells.SpellFactory;
import com.nyrds.pixeldungeon.mechanics.spells.SpellHelper;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.ArrayList;
import java.util.Random;

public class SpellBook extends Artifact {

	public static final String AC_USE = Game.getVar(R.string.SpiderCharm_Use);
	public static final String IDENTIFIED = Game.getVar(R.string.SpellBook_Info_Identified);

	@Packable
	private String spell;

	public SpellBook() {
		imageFile = "items/books.png";
		image = 3;
	}

	public void spell(String spellName){
		spell = spellName;
	}

	public Spell spell(){
		if(spell == null || spell.equals("")){
			ArrayList<String> spells = SpellFactory.getSpellsByAffinity(SpellHelper.AFFINITY_COMMON);
			int index = new Random().nextInt(spells.size());
			spell(spells.get(index));
		}
		return SpellFactory.getSpellByName(spell);
	}

	@Override
	public ArrayList<String> actions(Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped(hero)){
			actions.add( AC_USE );
		}
		return actions;
	}

	@Override
	public void execute(final Hero ch, String action) {
		setCurUser(ch);
		if (action.equals(AC_USE)) {
			spell().cast(ch);
		} else {
			super.execute(ch, action);
		}
	}

	@Override
	public String desc(){
		if(this.isIdentified()){
			return  Utils.format(IDENTIFIED, spell().name(), spell().desc());
		}
		return super.desc();
	}

	@Override
	public Item burn(int cell){
		return null;
	}
}
