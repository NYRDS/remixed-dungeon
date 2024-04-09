package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.ui.QuickSlot;

class SpellButton extends Button {

    private final WndHeroSpells wndHeroSpells;
    private final Spell spell;
    private final Char caster;
    private final Image image;


    protected final ColorBlock bg;
    protected final ColorBlock cooldownMeter;

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
        bg.setX(x);
        bg.setY(y);
        bg.size(width, height);

        cooldownMeter.setX(x);
        cooldownMeter.setY(y);
        cooldownMeter.size(width, height * (1-spell.getCooldownFactor(caster)));

        image.setX(x + 3);
        image.setY(y + 3);
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
