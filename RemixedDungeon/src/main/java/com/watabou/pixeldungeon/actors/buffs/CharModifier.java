package com.watabou.pixeldungeon.actors.buffs;

import com.watabou.pixeldungeon.actors.Char;

import java.util.Set;

public interface CharModifier {
    int drBonus();
    int stealthBonus();
    float speedMultiplier();
    int defenceProc(Char defender, Char enemy, int damage);
    int regenerationBonus();
    void charAct();
    Set<String> resistances();
    Set<String> immunities();

}
