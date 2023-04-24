/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.items.common.UnknownItem;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.ItemStatusHandler;
import com.watabou.pixeldungeon.items.bags.Keyring;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Ring extends Artifact implements UnknownItem {

    private static final Class<?>[] rings = {
            RingOfMending.class,
            RingOfDetection.class,
            RingOfShadows.class,
            RingOfPower.class,
            RingOfHerbalism.class,
            RingOfAccuracy.class,
            RingOfEvasion.class,
            RingOfSatiety.class,
            RingOfHaste.class,
            RingOfHaggler.class,
            RingOfElements.class,
            RingOfThorns.class
    };

    private static final Integer[] images = {
            ItemSpriteSheet.RING_DIAMOND,
            ItemSpriteSheet.RING_OPAL,
            ItemSpriteSheet.RING_GARNET,
            ItemSpriteSheet.RING_RUBY,
            ItemSpriteSheet.RING_AMETHYST,
            ItemSpriteSheet.RING_TOPAZ,
            ItemSpriteSheet.RING_ONYX,
            ItemSpriteSheet.RING_TOURMALINE,
            ItemSpriteSheet.RING_EMERALD,
            ItemSpriteSheet.RING_SAPPHIRE,
            ItemSpriteSheet.RING_QUARTZ,
            ItemSpriteSheet.RING_AGATE};

    private static ItemStatusHandler<Ring> handler;

    private String gem;

    private int ticksToKnow = 200;

    @SuppressWarnings("unchecked")
    public static void initGems() {
        handler = new ItemStatusHandler<>((Class<? extends Ring>[]) rings, images);
    }

    public static void save(Bundle bundle) {
        handler.save(bundle);
    }

    @SuppressWarnings("unchecked")
    public static void restore(Bundle bundle) {
        handler = new ItemStatusHandler<>((Class<? extends Ring>[]) rings, images, bundle);
    }

    public Ring() {
        super();
        syncGem();
    }

    public void syncGem() {
        image = handler.index(this);
        gem = StringsManager.getVars(R.array.Ring_Gems)[ItemStatusHandler.indexByImage(image, images)];
    }

    @Override
    public Item upgrade() {
        super.upgrade();

        Buff.detachAllBySource(getOwner(), this);

        Buff buff = buff();
        if (buff != null) {
            buff.setSource(this);
            buff.attachTo(getOwner());
        }

        return this;
    }

    public boolean isKnown() {
        return handler.isKnown(this);
    }

    public void setKnown() {
        if (!isKnown()) {
            handler.know(this);
        }

        Badges.validateAllRingsIdentified();
    }

    @Override
    public String name() {
        return isKnown() ? name : Utils.format(R.string.Ring_Name, gem);
    }

    @Override
    public String desc() {
        return Utils.format(R.string.Ring_Info, gem);
    }

    @Override
    public String info() {
        if (isEquipped(Dungeon.hero)) {
            return Utils.format(R.string.Ring_Info3a, desc(), name(), (isCursed() ? StringsManager.getVar(R.string.Ring_Info3b) : "."));
        } else if (isCursed() && isCursedKnown()) {
            return Utils.format(R.string.Ring_Info4, desc(), name());
        } else {
            return desc();
        }
    }

    @Override
    public boolean isIdentified() {
        return super.isIdentified() && isKnown();
    }

    @Override
    public boolean isUpgradable() {
        return true;
    }

    @Override
    public Item identify() {
        setKnown();
        return super.identify();
    }

    @Override
    public Item random() {
        level(Random.Int(1, 3));
        if (Random.Float() < 0.3f) {
            level(-level());
            setCursed(true);
        }
        return this;
    }

    public static boolean allKnown() {
        return handler.known().size() == rings.length - 2;
    }

    @Override
    public int price() {
        return adjustPrice(80);
    }

    public class RingBuff extends ArtifactBuff {

        public RingBuff() {
            level(Ring.this.level());
        }

        @Override
        public boolean attachTo(@NotNull Char target) {

            if (target.getHeroClass() == HeroClass.ROGUE && !isKnown()) {
                setKnown();
                GLog.i(StringsManager.getVar(R.string.Ring_BuffKnown), Ring.this.trueName());
                Badges.validateItemLevelAcquired(Ring.this);
            }

            return super.attachTo(target);
        }

        @Override
        public boolean act() {

            if (!isIdentified() && --ticksToKnow <= 0) {
                String gemName = Ring.this.name();
                identify();
                GLog.w(StringsManager.getVar(R.string.Ring_Identify), gemName, Ring.this.trueName());
                Badges.validateItemLevelAcquired(Ring.this);
            }

            spend(TICK);

            return true;
        }
    }

    @Override
    public String bag() {
        return Keyring.class.getSimpleName();
    }
}
