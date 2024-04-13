package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.sprites.CharSprite;

import java.util.Set;

public interface CharModifier {
    int drBonus();
    int stealthBonus();
    float speedMultiplier();

    float hasteLevel();

    int defenceSkillBonus();
    int attackSkillBonus();

    int defenceProc(Char defender, Char enemy, int damage);
    int attackProc(Char attacker, Char defender, int damage );

    int charGotDamage(int damage, NamedEntityKind src, Char target);

    int regenerationBonus();
    int manaRegenerationBonus();
    void charAct();
    void spellCasted(Char caster, Spell spell);

    int dewBonus();

    Set<String> resistances();
    Set<String> immunities();

    CharSprite.State charSpriteStatus();

    int icon();
    String name();
    String desc();
    String textureSmall();
    String textureLarge();

    Image smallIcon();
}
