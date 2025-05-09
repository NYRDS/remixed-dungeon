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
package com.watabou.pixeldungeon.effects;

import android.annotation.SuppressLint;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Assets;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class Speck extends Image {

    public static final int HEALING = 0;
    public static final int STAR = 1;
    public static final int LIGHT = 2;
    public static final int QUESTION = 3;
    public static final int UP = 4;
    public static final int SCREAM = 5;
    public static final int BONE = 6;
    public static final int WOOL = 7;
    public static final int ROCK = 8;
    public static final int NOTE = 9;
    public static final int CHANGE = 10;
    public static final int HEART = 11;
    public static final int BUBBLE = 12;
    public static final int STEAM = 13;
    public static final int COIN = 14;
    public static final int MIST = 15;
    public static final int SPELL_STAR = 16;

    public static final int DISCOVER = 101;
    public static final int EVOKE = 102;
    public static final int MASTERY = 103;
    public static final int KIT = 104;
    public static final int RATTLE = 105;
    public static final int JET = 106;
    public static final int TOXIC = 107;
    public static final int PARALYSIS = 108;
    public static final int DUST = 109;
    public static final int FORGE = 110;
    public static final int CONFUSION = 111;
    public static final int MAGIC = 112;


    private static final int SIZE = 7;

    private int evolutionType;

    private float lifespan;
    private float left;

    private static TextureFilm film;

    private static final Map<AbstractMap.SimpleEntry<Integer, Integer>, Emitter.Factory> factories = new HashMap<>(); // Because Pair is android-specific for Java 8

    public Speck() {
        texture(Assets.SPECKS);
        if (film == null) {
            film = TextureCache.getFilm(texture, SIZE, SIZE);
        }

        origin.set(SIZE / 2f);
    }

    public void reset(int index, float x, float y, int oldCombinedType) {
        reset(index, x, y, oldCombinedType, oldCombinedType);
    }

    public void reset(int index, float x, float y, int particleType, int evolutionType) {
        revive();

        this.evolutionType = evolutionType;

        selectFrameByType(particleType);

        this.x = x - origin.x;
        this.y = y - origin.y;

        resetColor();
        scale.set(1);
        speed.set(0);
        acc.set(0);
        angle = 0;
        angularSpeed = 0;

        setEvolutionByType(index, evolutionType);

        left = lifespan;
    }

    private void setEvolutionByType(int index, int type) {
        switch (type) {

            case HEALING:

            case UP:
                speed.set(0, -20);
                lifespan = 1f;
                break;

            case STAR:
                speed.polar(Random.Float(2 * 3.1415926f), Random.Float(128));
                acc.set(0, 128);
                angle = Random.Float(360);
                angularSpeed = Random.Float(-360, +360);
                lifespan = 1f;
                break;

            case MIST:
                speed.polar(Random.Float(2 * 3.1415926f), Random.Float(3));
                angle = Random.Float(360);
                angularSpeed = Random.Float(-3.6f, +3.6f);
                lifespan = 2f;
                break;

            case FORGE:
                speed.polar(Random.Float(-3.1415926f, 0), Random.Float(64));
                acc.set(0, 128);
                angle = Random.Float(360);
                angularSpeed = Random.Float(-360, +360);
                lifespan = 0.51f;
                break;

            case EVOKE:
                speed.polar(Random.Float(-3.1415926f, 0), 50);
                acc.set(0, 50);
                angle = Random.Float(360);
                angularSpeed = Random.Float(-180, +180);
                lifespan = 1f;
                break;

            case KIT:
                speed.polar(index * 3.1415926f / 5, 50);
                acc.set(-speed.x, -speed.y);
                angle = index * 36;
                angularSpeed = 360;
                lifespan = 1f;
                break;

            case MASTERY:
                speed.set(Random.Int(2) == 0 ? Random.Float(-128, -64) : Random.Float(+64, +128), 0);
                angularSpeed = speed.x < 0 ? -180 : +180;
                acc.set(-speed.x, 0);
                lifespan = 0.5f;
                break;

            case LIGHT:
                angle = Random.Float(360);
                angularSpeed = 90;
                lifespan = 1f;
                break;

            case DISCOVER:
                angle = Random.Float(360);
                angularSpeed = 90;
                lifespan = 0.5f;
                am = 0;
                break;

            case QUESTION:
                lifespan = 0.8f;
                break;

            case SCREAM:
                lifespan = 0.9f;
                break;

            case BONE:
                lifespan = 0.2f;
                speed.polar(Random.Float(2 * 3.1415926f), 24 / lifespan);
                acc.set(0, 128);
                angle = Random.Float(360);
                angularSpeed = 360;
                break;

            case RATTLE:
                lifespan = 0.5f;
                speed.set(0, -200);
                acc.set(0, -2 * speed.y / lifespan);
                angle = Random.Float(360);
                angularSpeed = 360;
                break;

            case WOOL:
                lifespan = 0.5f;
                speed.set(0, -50);
                angle = Random.Float(360);
                angularSpeed = Random.Float(-360, +360);
                break;

            case ROCK:
                angle = Random.Float(360);
                angularSpeed = Random.Float(-360, +360);
                scale.set(Random.Float(1, 2));
                speed.set(0, 64);
                lifespan = 0.2f;
                break;

            case NOTE:
                angularSpeed = Random.Float(-30, +30);
                speed.polar((angularSpeed - 90) * PointF.G2R, 30);
                lifespan = 1f;
                break;

            case CHANGE:
                angle = Random.Float(360);
                speed.polar((angle - 90) * PointF.G2R, Random.Float(4, 12));
                lifespan = 1.5f;
                break;

            case HEART:
                speed.set(Random.Int(-10, +10), -40);
                angularSpeed = Random.Float(-45, +45);
                lifespan = 1f;
                break;

            case BUBBLE:
                speed.set(0, -15);
                scale.set(Random.Float(0.8f, 1));
                lifespan = Random.Float(0.8f, 1.5f);
                break;

            case STEAM:
                speed.y = -Random.Float(20, 30);
                angularSpeed = Random.Float(+180);
                angle = Random.Float(360);
                lifespan = 1f;
                break;

            case JET:
                speed.y = +32;
                acc.y = -64;
                angularSpeed = Random.Float(180, 360);
                angle = Random.Float(360);
                lifespan = 0.5f;
                break;

            case TOXIC:
                hardlight(0x50FF60);
                angularSpeed = 30;
                angle = Random.Float(360);
                lifespan = Random.Float(1f, 3f);
                break;

            case PARALYSIS:
                hardlight(0xFFFF66);
                angularSpeed = -30;
                angle = Random.Float(360);
                lifespan = Random.Float(1f, 3f);
                break;

            case CONFUSION:
                hardlight(Random.Int(0x1000000) | 0x000080);
                angularSpeed = Random.Float(-20, +20);
                angle = Random.Float(360);
                lifespan = Random.Float(1f, 3f);
                break;

            case DUST:
                hardlight(0xFFFF66);
                angle = Random.Float(360);
                speed.polar(Random.Float(2 * 3.1415926f), Random.Float(16, 48));
                lifespan = 0.5f;
                break;

            case COIN:

            case MAGIC:
                speed.polar(-PointF.PI * Random.Float(0.3f, 0.7f), Random.Float(48, 96));
                acc.y = 256;
                lifespan = -speed.y / acc.y * 2;
                break;
        }
    }

    private void selectFrameByType(int type) {
        switch (type) {
            case DISCOVER:
                frame(film.get(LIGHT));
                break;
            case EVOKE:
            case MASTERY:
            case KIT:
            case FORGE:
                frame(film.get(STAR));
                break;
            case RATTLE:
                frame(film.get(BONE));
                break;
            case JET:
            case TOXIC:
            case PARALYSIS:
            case CONFUSION:
            case DUST:
            case MIST:
                frame(film.get(STEAM));
                break;
            case MAGIC:
                frame(film.get(SPELL_STAR));
                break;
            default:
                frame(film.get(type));
        }
    }

    @SuppressLint("FloatMath")
    @Override
    public void update() {
        super.update();

        left -= GameLoop.elapsed;
        if (left <= 0) {

            kill();

        } else {

            float p = 1 - left / lifespan;    // 0 -> 1

            updateByEvolutionType(p);
        }
    }

    private void updateByEvolutionType(float p) {
        switch (evolutionType) {

            case STAR:
            case FORGE:
                scale.set(1 - p);
                am = p < 0.2f ? p * 5f : (1 - p) * 1.25f;
                break;

            case KIT:
            case MASTERY:

            case NOTE:
                am = 1 - p * p;
                break;

            case EVOKE:

            case HEALING:
                am = p < 0.5f ? 1 : 2 - p * 2;
                break;

            case LIGHT:
                am = scale.set(p < 0.2f ? p * 5f : (1 - p) * 1.25f).x;
                break;

            case DISCOVER:
                am = 1 - p;
                scale.set((p < 0.5f ? p : 1 - p) * 2);
                break;

            case QUESTION:
                scale.set((float) (Math.sqrt(p < 0.5f ? p : 1 - p) * 3));
                break;

            case UP:
                scale.set((float) (Math.sqrt(p < 0.5f ? p : 1 - p) * 2));
                break;

            case SCREAM:
                am = (float) Math.sqrt((p < 0.5f ? p : 1 - p) * 2f);
                scale.set(p * 7);
                break;

            case BONE:
            case RATTLE:
                am = p < 0.9f ? 1 : (1 - p) * 10;
                break;

            case ROCK:

            case BUBBLE:
                am = p < 0.2f ? p * 5 : 1;
                break;

            case WOOL:
                scale.set(1 - p);
                break;

            case CHANGE:
                am = (float) Math.sqrt((p < 0.5f ? p : 1 - p) * 2);
                scale.y = (1 + p) * 0.5f;
                scale.x = (float) (scale.y * Math.cos(left * 15));
                break;

            case HEART:
                scale.set(1 - p);
                am = 1 - p * p;
                break;

            case MIST:
            case STEAM:
            case TOXIC:
            case PARALYSIS:
            case CONFUSION:
            case DUST:
                am = p < 0.5f ? p : 1 - p;
                scale.set(1 + p * 2);
                break;

            case JET:
                am = (p < 0.5f ? p : 1 - p) * 2;
                scale.set(p * 1.5f);
                break;

            case COIN:
                scale.x = (float) Math.cos(left * 5);
                rm = gm = bm = (Math.abs(scale.x) + 1) * 0.5f;
                am = p < 0.9f ? 1 : (1 - p) * 10;
                break;
            case MAGIC:
                am = 2 - p * p / 2;
                break;
        }
    }

    public static Emitter.Factory factory(final int type) {
        return factory(type, type, false);
    }

    public static Emitter.Factory factory(final int type, boolean lightMode) {
        return factory(type, type, lightMode);
    }

    @LuaInterface
    public static Emitter.Factory factory(final int particleType, final int evolutionType) {
        return factory(particleType, evolutionType, false);
    }

    public static Emitter.Factory factory(final int animationType, final int evolutionType, final boolean lightMode) {

        var key = new AbstractMap.SimpleEntry<>(animationType, evolutionType);

        Emitter.Factory factory = factories.get(key);

        if (factory == null) {
            factory = new Emitter.Factory() {
                @Override
                public void emit(Emitter emitter, int index, float x, float y) {
                    Speck p = (Speck) emitter.recycle(Speck.class);
                    p.reset(index, x, y, animationType, evolutionType);
                }

                @Override
                public boolean lightMode() {
                    return lightMode;
                }
            };
            factories.put(key, factory);
        }

        return factory;
    }
}
