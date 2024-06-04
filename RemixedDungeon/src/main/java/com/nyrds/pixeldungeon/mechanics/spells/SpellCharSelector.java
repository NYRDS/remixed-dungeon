package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;


class SpellCharSelector implements CellSelector.Listener {
    private final Spell spell;
    private @NotNull
    final Char caster;



    public SpellCharSelector(Spell spell, @NotNull Char caster, String mode) {
        this.spell = spell;
        this.caster = caster;

        Level level = caster.level();

        for(Char chr: Actor.chars.values()) {

            if(mode.equals(SpellHelper.TARGET_CHAR_NOT_SELF) && chr == caster) {
                continue;
            }

            int pos = chr.getPos();

            if(level.fieldOfView[pos]) {
                GLog.debug("%s: visible: %s", spell.getEntityKind(), chr.getEntityKind());
                CharUtils.mark(chr);
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
        CharUtils.clearMarkers();
    }

    @Override
    public String prompt() {
        return StringsManager.getVar(R.string.Spell_SelectAChar);
    }

    @Override
    public Image icon() {
        return spell.image(caster);
    }
}
