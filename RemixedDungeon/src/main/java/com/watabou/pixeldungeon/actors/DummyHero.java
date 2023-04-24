package com.watabou.pixeldungeon.actors;

import com.watabou.pixeldungeon.actors.hero.Hero;

public class DummyHero extends Hero {
    @Override
    public boolean valid() {
        return false;
    }
}
