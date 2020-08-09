package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.ui.QuickSlot;

class SpellButton extends Button {

    private WndHeroSpells wndHeroSpells;
    private final Spell spell;
    private final Char caster;
    private final Image image;


    protected ColorBlock bg;
    protected ColorBlock cooldownMeter;

    public SpellButton(WndHeroSpells wndHeroSpells, Char caster, Spell spell) {
        this.image = spell.image(caster);
        this.wndHeroSpells = wndHeroSpells;
        this.spell = spell;
        this.caster = caster;

        width = image.width + 6;
        height = image.height + 6;

        bg = new ColorBlock(width, height, 0xFF4A4D44);
        add(bg);

        add(image);

        cooldownMeter = new ColorBlock(width, height, 0x7f000000);
        add(cooldownMeter);
    }

    @Override
    protected void layout() {
        super.layout();
        bg.x = x;
        bg.y = y;
        bg.size(width, height);

        cooldownMeter.x = x;
        cooldownMeter.y = y ;
        cooldownMeter.size(width, height * (1-spell.getCooldownFactor(caster)));

        image.x = x + 3;
        image.y = y + 3;
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
