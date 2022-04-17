package com.watabou.pixeldungeon.ui;

import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.watabou.noosa.ColorBlock;
import com.watabou.pixeldungeon.Chrome;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.windows.elements.Tool;

class QuickslotTool extends Tool {

    private final QuickSlot slot;

    protected ColorBlock cooldownMeter;

    QuickslotTool() {
        super(-1, Chrome.Type.QUICKSLOT);

        slot = new QuickSlot();
        add(slot);

        cooldownMeter = new ColorBlock(width, height, 0x7f000000);
        add(cooldownMeter);
    }

    @Override
    protected void layout() {
        super.layout();
        slot.setRect(base.getX(), base.getY(), base.width(), base.height());
    }

    @Override
    public void update() {
        super.update();

        if(slot.getQuickslotItem() instanceof Spell.SpellItem) {
            Spell.SpellItem spell = (Spell.SpellItem)slot.getQuickslotItem();
            cooldownMeter.setX(base.getX());
            cooldownMeter.setY(base.getY());
            cooldownMeter.size(base.width(), base.height() * (1 - spell.spell().getCooldownFactor(Dungeon.hero)));
            cooldownMeter.setVisible(true);
        } else {
            cooldownMeter.setVisible(false);
        }
    }

    public void show(boolean value) {
        setVisible(value);
        enable(value);
    }

    @Override
    public void enable(boolean value) {
        slot.enable(value);
        setActive(value);
    }
}
