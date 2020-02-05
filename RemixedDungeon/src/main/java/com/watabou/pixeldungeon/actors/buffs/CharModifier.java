package com.watabou.pixeldungeon.actors.buffs;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.sprites.CharSprite;

import java.util.Set;

public interface CharModifier {
    int drBonus();
    int stealthBonus();
    float speedMultiplier();
    int defenceProc(Char defender, Char enemy, int damage);
    int attackProc(Char attacker, Char defender, int damage );

    int regenerationBonus();
    void charAct();

    int dewBonus();

    Set<String> resistances();
    Set<String> immunities();

    CharSprite.State charSpriteStatus();

    int icon();

    String name();

    String textureSmall();

    Object textureLarge();
}
