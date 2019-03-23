package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.watabou.noosa.ColorBlock;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.ui.ImageButton;
import com.watabou.pixeldungeon.ui.QuickSlot;

class SpellButton extends ImageButton {

    private WndHeroSpells wndHeroSpells;
    private final Spell spell;
    private final Char caster;

    protected ColorBlock bg;
    protected ColorBlock cooldownMeter;

    public SpellButton(WndHeroSpells wndHeroSpells, Char caster, Spell spell) {
        super(spell.image());
        this.wndHeroSpells = wndHeroSpells;
        this.spell = spell;
        this.caster = caster;
    }

    @Override
    protected void createChildren() {
        super.createChildren();
        bg = new ColorBlock(width + 6, height + 6, 0xFF4A4D44);
        add(bg);

        cooldownMeter = new ColorBlock(width + 6, height + 6, 0xff000000);
        add(cooldownMeter);
    }

    @Override
    protected void layout() {
        super.layout();
        bg.x = x - 3;
        bg.y = y - 3;
        bg.size(width + 6, height + 6);

        cooldownMeter.x = x - 3;
        cooldownMeter.y = y - 3;
        cooldownMeter.size(width + 6, (height + 6) * (1-spell.getCooldownFactor(caster)));

        image.x = x;
        image.y = y;
    }

    @Override
    protected void onClick() {
        super.onClick();
        wndHeroSpells.onSpellClick(spell);

    }

    @Override
    protected boolean onLongClick() {
        wndHeroSpells.hide();
        QuickSlot.selectSlotFor(spell.itemForSlot());
        return true;
    }

}
