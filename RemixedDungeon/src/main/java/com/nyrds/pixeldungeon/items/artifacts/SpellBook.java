package com.nyrds.pixeldungeon.items.artifacts;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.mechanics.spells.SpellFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.ml.actions.UseSpell;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SpellBook extends Artifact {

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
		Spell retSpell = SpellFactory.getSpellByName(spell);
		spell = retSpell.getEntityKind();
		return retSpell;
	}


	@Override
	public void activate(@NotNull Char ch) {
		setDefaultAction(CommonActions.AC_READ);
		super.activate(ch);
	}

	@Override
	public void deactivate(@NotNull Char ch) {
		setDefaultAction(AC_THROW);
		super.deactivate(ch);
	}

	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped(hero)){
			actions.add(CommonActions.AC_READ );
		}
		return actions;
	}

	@Override
	public void _execute(@NotNull final Char ch, @NotNull String action) {
		if (action.equals(CommonActions.AC_READ)) {
			ch.nextAction(new UseSpell(spell()));
		} else {
			super._execute(ch, action);
		}
	}

	@Override
	public String desc(){
		if(isIdentified()){
			return  Utils.format(R.string.SpellBook_Info_Identified, spell().name(), spell().desc());
		}
		return super.desc();
	}

	@Override
	public String name(){
		if(isIdentified()){
			return  String.format("%s (%s)", super.name(), spell().name());
		}
		return super.name();
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
