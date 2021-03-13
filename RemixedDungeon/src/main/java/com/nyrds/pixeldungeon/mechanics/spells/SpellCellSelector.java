package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.scenes.CellSelector;

import org.jetbrains.annotations.NotNull;

class SpellCellSelector implements CellSelector.Listener {
    private final Spell spell;
    private @NotNull
    final Char caster;

    public SpellCellSelector(Spell spell, @NotNull Char caster) {
        this.spell = spell;
        this.caster = caster;
    }

    @Override
    public void onSelect(Integer cell, @NotNull Char selector) {
        if (cell != null) {
            spell.cast(caster, cell);

            caster.spend(spell.castTime);
            caster.busy();
            caster.getSprite().zap(cell);
        }
    }

    @Override
    public String prompt() {
        return Game.getVar(R.string.Spell_SelectACell);
    }

    @Override
    public Image icon() {
        return spell.image(caster);
    }
}
