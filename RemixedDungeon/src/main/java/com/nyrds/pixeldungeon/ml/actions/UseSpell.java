package com.nyrds.pixeldungeon.ml.actions;

import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.scenes.GameScene;

public class UseSpell extends CharAction{

    public final Spell spell;

    public UseSpell(Spell spell) {
        this.spell = spell;
    }

    @Override
    public boolean act(Char hero) {
        spell.cast(hero);

        if(hero.getCurAction() == this) {
            hero.setCurAction(null);
        }

        if(GameScene.defaultCellSelector() && !hero.getSprite().doingSomething()) {
            hero.next();
        }

        return false;
    }
}
