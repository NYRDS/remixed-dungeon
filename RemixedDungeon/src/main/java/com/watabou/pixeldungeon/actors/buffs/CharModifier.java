package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.CharSprite;

import java.util.Set;

public interface CharModifier {
    int drBonus(Char chr);
    int stealthBonus(Char chr);
    float speedMultiplier(Char chr);

    float hasteLevel(Char chr);

    int defenceSkillBonus(Char chr);
    int attackSkillBonus(Char chr);

    int defenceProc(Char defender, Char enemy, int damage);
    int attackProc(Char attacker, Char defender, int damage );

    int charGotDamage(int damage, NamedEntityKind src, Char target);

    int regenerationBonus(Char chr);
    int manaRegenerationBonus(Char chr);
    void charAct(Char chr);
    void spellCasted(Char caster, Spell spell);

    int dewBonus(Char chr);

    Set<String> resistances(Char chr);
    Set<String> immunities(Char chr);

    CharSprite.State charSpriteStatus();

    int icon();
    String name();
    String desc();
    String textureSmall();
    String textureLarge();

    Image smallIcon();
}
