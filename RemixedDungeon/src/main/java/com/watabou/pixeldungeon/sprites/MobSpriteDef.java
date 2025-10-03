package com.watabou.pixeldungeon.sprites;

import com.nyrds.pixeldungeon.effects.ISpriteEffect;
import com.nyrds.pixeldungeon.effects.ZapEffect;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.gl.Gl;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.JsonHelper;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.Splash;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.noosa.Camera;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import lombok.val;

public class MobSpriteDef extends MobSprite {

    private static final String DEATH_EFFECT = "deathEffect";
    private static final String ZAP_EFFECT = "zapEffect";
    private static final String SPRITE_EFFECT = "spriteEffects";
    private static final String ZAP = "zap";
    private static final String ATTACK = "attack";
    private static final String LAYERS = "layers";
    private static final String EVENT_HANDLERS = "eventHandlers";
    private static final String PARTICLE_EMITTERS = "particleEmitters";

    private int bloodColor;
    private JSONArray onCompleteEffects;
    private JSONObject particleEmitters;
    private final Map<String, Emitter> emitterMap = new HashMap<>();

    private float alpha = 1.0f; // Default to fully opaque
    private String blendMode; // For OpenGL blending modes

    private int framesInRow;
    private int kind;
    private String zapEffect;

    private final Set<State> initialState = new HashSet<>();

    private float visualWidth;
    private float visualHeight;

    private float visualOffsetX;
    private float visualOffsetY;


    static private final Map<String, JSONObject> defMap = new HashMap<>();

    private final String name;
    private String deathEffect;
    private final Set<String> spriteEffects = new HashSet<>();

    private final Set<ISpriteEffect> effects = new HashSet<>();

    public MobSpriteDef(String defName, int kind) {
        super();

        name = defName;

        if (!defMap.containsKey(name)) {
            defMap.put(name, JsonHelper.readJsonFromAsset(name));
        }

        selectKind(kind);
    }

    @Override
    public void selectKind(int kind) {

        this.kind = kind;
        JSONObject json = defMap.get(name);

        try {
            texture(json.getString("texture"));

            if (json.has(LAYERS)) {
                JSONArray layers = json.getJSONArray(LAYERS);

                for (int i = 0; i < layers.length(); ++i) {
                    JSONObject layer = layers.getJSONObject(i);
                    addLayer(layer.getString("id"), TextureCache.get(layer.get("texture")));
                }
            }

            int width = json.getInt("width");
            visualWidth = (float) json.optDouble("visualWidth", width);

            int height = json.getInt("height");
            visualHeight = (float) json.optDouble("visualHeight", height);

            visualOffsetX = (float) json.optDouble("visualOffsetX", 0);
            visualOffsetY = (float) json.optDouble("visualOffsetY", 0);

            charScale = (float) json.optDouble("scale", 1.0);
            setScale(charScale);

            TextureFilm film = TextureCache.getFilm(texture, width, height);

            bloodColor = 0xFFBB0000;
            Object _bloodColor = json.opt("bloodColor");
            if (_bloodColor instanceof Number) {
                bloodColor = (int) _bloodColor;
            }

            if (_bloodColor instanceof String) {
                bloodColor = Long.decode((String) _bloodColor).intValue();
            }

            // Parse alpha
            if (json.has("alpha")) {
                alpha = (float) json.optDouble("alpha", 1.0f);
            }

            // Parse blendMode
            if (json.has("blendMode")) {
                blendMode = json.getString("blendMode");
            }

            // Parse particle emitters
            if (json.has(PARTICLE_EMITTERS)) {
                particleEmitters = json.getJSONObject(PARTICLE_EMITTERS);
            }

            // Parse death effects from event handlers
            if (json.has(EVENT_HANDLERS)) {
                JSONObject eventHandlers = json.getJSONObject(EVENT_HANDLERS);
                if (eventHandlers.has("onComplete")) {
                    onCompleteEffects = eventHandlers.getJSONArray("onComplete");
                }
            }

            Set<String> states = new HashSet<>();
            JsonHelper.readStringSet(json, "states", states);

            for (String state : states) {
                initialState.add(CharSprite.State.valueOf(state.toUpperCase(Locale.ROOT)));
            }

            framesInRow = texture.width / width;

            idle = readAnimation(json, "idle", film);
            run = readAnimation(json, "run", film);
            die = readAnimation(json, "die", film);

            if (json.has(ATTACK)) { //attack was not defined for some peaceful NPC's
                attack = readAnimation(json, ATTACK, film);
            } else {
                attack = run.clone();
            }

            if (json.has(ZAP)) {
                zap = readAnimation(json, ZAP, film);
            } else {
                zap = attack.clone();
            }

            if (json.has(ZAP_EFFECT)) {
                zapEffect = json.getString(ZAP_EFFECT);
            }

            if (json.has(DEATH_EFFECT)) {
                deathEffect = json.getString(DEATH_EFFECT);
            }

            JsonHelper.readStringSet(json, SPRITE_EFFECT, spriteEffects);


            extras.put("std_idle", idle.clone());
            extras.put("std_run", run.clone());
            extras.put("std_attack", attack.clone());
            extras.put("std_die", die.clone());
            extras.put("std_zap", zap.clone());

            if (json.has("extras")) {
                val extrasJson = json.getJSONObject("extras");
                for (Iterator<String> it = extrasJson.keys(); it.hasNext(); ) {
                    String key = it.next();
                    extras.put(key, readAnimation(extrasJson, key, film));
                }
            }

            loadAdditionalData(json, film, kind);

        } catch (Exception e) {
            throw new TrackedRuntimeException(Utils.format("Something bad happens when loading %s", name), e);
        }

        play(idle);
    }

    protected void loadAdditionalData(JSONObject json, TextureFilm film, int kind) throws JSONException {
    }

    protected Animation readAnimation(JSONObject root, String animKind, TextureFilm film) throws JSONException {
        return JsonHelper.readAnimation(root, animKind, film, kind * framesInRow);
    }

    @Override
    public void link(Char ch) {
        super.link(ch);
        for (CharSprite.State state : initialState) {
            add(state);
        }

        for (String effect : spriteEffects) {
            if (effect.equals("ManaShield")) {
                var eff = new ManaShield(this);
                effects.add(eff);
                eff.setIsometricShift(true);
                GameScene.addToMobLayer(eff);
            }
        }

        // Set alpha if defined
        if (alpha != 1.0f) {
            alpha(alpha);
        }

        // Create particle emitters defined in JSON
        createParticleEmitters();
    }

    @Override
    public void die() {
        ch.ifPresent(chr -> {
            if (deathEffect != null) {
                ZapEffect.play(null, chr.getPos(), deathEffect);
            }
        });

        removeAllStates();

        for (ISpriteEffect eff : effects) {
            eff.die();
        }

        super.die();
    }

    @Override
    public void zap(int cell) {
        ch.ifPresent(chr -> {
            val parent = getParent();
            if (parent != null) {
                super.zap(cell);

                ZapEffect.zap(getParent(), chr.getPos(), cell, zapEffect);
            }
        });
    }

    @Override
    public int blood() {
        return bloodColor;
    }

    @Override
    public float visualHeight() {
        return visualHeight * charScale;
    }

    @Override
    public float visualWidth() {
        return visualWidth * charScale;
    }

    @Override
    public float visualOffsetX() {
        return visualOffsetX + super.visualOffsetX();
    }

    @Override
    public float visualOffsetY() {
        return visualOffsetY + super.visualOffsetY();
    }

    @Override
    public void onComplete(Animation anim) {
        if (isVisible() && onCompleteEffects != null) {
            handleEventActions(anim, onCompleteEffects);
        }
        super.onComplete(anim);
    }

    private void handleEventActions(Animation anim, JSONArray actions) {
        try {
            for (int i = 0; i < actions.length(); i++) {
                JSONObject effect = actions.getJSONObject(i);
                val aName = effect.getString("animation");
                if (aName.equals(anim.name)) {
                    if (effect.has("actions")) {
                        JSONArray actionArray = effect.getJSONArray("actions");
                        executeActions(actionArray);
                    }
                }
            }
        } catch (Exception e) {
            EventCollector.logException(e);
        }
    }

    private void executeActions(JSONArray actions) throws JSONException {
        for (int j = 0; j < actions.length(); j++) {
            JSONObject action = actions.getJSONObject(j);
            String actionType = action.getString("action");

            switch (actionType) {
                case "emitParticles":
                    String particleType = action.getString("particleType");
                    int particleTypeId = getParticleTypeId(particleType);
                    int count = action.getInt("count");
                    emitter().burst(Speck.factory(particleTypeId), count);
                    break;

                case "splash":
                    String colorStr = action.getString("color");
                    int color = Long.decode(colorStr).intValue();
                    int splashCount = action.getInt("count");
                    Splash.at(center(), color, splashCount);
                    break;

                case "playSound":
                    String soundName = action.getString("sound");
                    float volume = action.optFloat("volume", 1);
                    Sample.INSTANCE.play(soundName, volume);
                    break;

                case "ripple":
                    ch.ifPresent(chr -> {
                        GameScene.ripple(chr.getPos());
                    });
                    break;

                case "cameraShake":
                    int intensity = action.optInt("intensity", 4);
                    float duration = (float) action.optDouble("duration", 0.2f);
                    Camera.main.shake(intensity, duration);
                    break;

                case "killAndErase":
                    killAndErase();
                    break;
            }
        }
    }

    private int getParticleTypeId(String particleType) {
        switch (particleType) {
            case "Speck.HEALING":
                return Speck.HEALING;
            case "Speck.STAR":
                return Speck.STAR;
            case "Speck.LIGHT":
                return Speck.LIGHT;
            case "Speck.QUESTION":
                return Speck.QUESTION;
            case "Speck.UP":
                return Speck.UP;
            case "Speck.SCREAM":
                return Speck.SCREAM;
            case "Speck.BONE":
                return Speck.BONE;
            case "Speck.WOOL":
                return Speck.WOOL;
            case "Speck.ROCK":
                return Speck.ROCK;
            case "Speck.NOTE":
                return Speck.NOTE;
            case "Speck.CHANGE":
                return Speck.CHANGE;
            case "Speck.HEART":
                return Speck.HEART;
            case "Speck.BUBBLE":
                return Speck.BUBBLE;
            case "Speck.STEAM":
                return Speck.STEAM;
            case "Speck.COIN":
                return Speck.COIN;
            case "Speck.MIST":
                return Speck.MIST;
            case "Speck.SPELL_STAR":
                return Speck.SPELL_STAR;
            case "Speck.DISCOVER":
                return Speck.DISCOVER;
            case "Speck.EVOKE":
                return Speck.EVOKE;
            case "Speck.MASTERY":
                return Speck.MASTERY;
            case "Speck.KIT":
                return Speck.KIT;
            case "Speck.RATTLE":
                return Speck.RATTLE;
            case "Speck.JET":
                return Speck.JET;
            case "Speck.TOXIC":
                return Speck.TOXIC;
            case "Speck.PARALYSIS":
                return Speck.PARALYSIS;
            case "Speck.DUST":
                return Speck.DUST;
            case "Speck.FORGE":
                return Speck.FORGE;
            case "Speck.CONFUSION":
                return Speck.CONFUSION;
            case "Speck.MAGIC":
                return Speck.MAGIC;
            default:
                return Speck.WOOL; // Default fallback
        }
    }

    private void createParticleEmitters() {
        if (particleEmitters != null) {
            try {
                Iterator<String> keys = particleEmitters.keys();
                while (keys.hasNext()) {
                    String emitterId = keys.next();
                    JSONObject emitterConfig = particleEmitters.getJSONObject(emitterId);

                    String type = emitterConfig.getString("type");
                    if ("Emitter".equals(type)) {
                        Emitter emitter = new Emitter();

                        // Set emitter properties
                        emitter.autoKill = emitterConfig.optBoolean("autoKill", true);

                        // Set position if specified
                        if (emitterConfig.has("position")) {
                            JSONObject position = emitterConfig.getJSONObject("position");
                            float x = (float) position.optDouble("x", 0);
                            float y = (float) position.optDouble("y", 0);
                            emitter.pos(getX() + x, getY() + y);
                        } else {
                            emitter.pos(this);
                        }

                        // Add to game scene
                        GameScene.addToMobLayer(emitter);

                        // Store reference
                        emitterMap.put(emitterId, emitter);
                    }
                }
            } catch (Exception e) {
                // Log error using EventCollector but don't crash
                EventCollector.logException(e);
            }
        }
    }

    @Override
    public void draw() {
        if (blendMode != null) {
            applyBlendMode();
            super.draw();
            resetBlendMode();
        } else {
            super.draw();
        }
    }

    @Override
    public void update() {
        super.update();

        // Update emitter visibility
        boolean visible = getVisible();
        for (Emitter emitter : emitterMap.values()) {
            emitter.setVisible(visible);
        }
    }

    private void applyBlendMode() {
        if ("srcAlphaOne".equals(blendMode)) {
            Gl.blendSrcAlphaOne();
        } else if ("srcAlphaOneMinusAlpha".equals(blendMode)) {
            Gl.blendSrcAlphaOneMinusAlpha();
        }
    }

    private void resetBlendMode() {
        Gl.blendSrcAlphaOneMinusAlpha();
    }
}
