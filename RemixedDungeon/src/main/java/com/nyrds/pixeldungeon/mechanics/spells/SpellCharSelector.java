package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

class SpellCharSelector implements CellSelector.Listener {
    private final Spell spell;
    private @NotNull
    final Char caster;

    private final Set<Flare> flares = new HashSet<>();

    public SpellCharSelector(Spell spell, @NotNull Char caster) {
        this.spell = spell;
        this.caster = caster;

        Level level = caster.level();

        for(Char chr: Actor.chars.values()) {
            int pos = chr.getPos();

            if(level.fieldOfView[pos]) {
                GLog.debug("%s: visible: %s", spell.getEntityKind(), chr.getEntityKind());
                flares.add(new Flare(5, 24).color(0x7777aa, true).show(chr.getSprite(), 4).permanent());
            }
        }
    }

    @Override
    public void onSelect(Integer cell, @NotNull Char selector) {
        if (cell != null) {
            Char target = Actor.findChar(cell);

            if(target!=null) {

                spell.cast(caster, target);

                caster.spend(spell.castTime);
                caster.busy();
                caster.getSprite().zap(target.getPos());
            }
        }

        for(Flare flare:flares) {
            flare.killAndErase();
        }
    }

    @Override
    public String prompt() {
        return Game.getVar(R.string.Spell_SelectAChar);
    }
}
