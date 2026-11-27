package com.watabou.pixeldungeon.actors.blobs;

import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.particles.DarknessParticle;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.effects.particles.LeafParticle;
import com.watabou.pixeldungeon.effects.particles.ShaftParticle;
import com.watabou.pixeldungeon.effects.particles.WebParticle;

/**
 * The blob rendering layer: one emitter pour per kind, verbatim from the
 * per-kind java classes the ScriptBlob executors replace. Rendering stays
 * java per the end-state classification - the kind scripts never touch
 * particles.
 */
final class BlobEmitters {

    private BlobEmitters() {
    }

    static void configure(String kind, BlobEmitter emitter) {
        switch (kind) {
            case "ToxicGas":
                emitter.pour(Speck.factory(Speck.TOXIC), 0.6f);
                break;
            case "ParalyticGas":
                emitter.pour(Speck.factory(Speck.PARALYSIS), 0.6f);
                break;
            case "ConfusionGas":
                emitter.pour(Speck.factory(Speck.CONFUSION, true), 0.6f);
                break;
            case "MiasmaGas":
                emitter.pour(Speck.factory(Speck.MIASMA), 0.6f);
                break;
            case "Fire":
                emitter.start(FlameParticle.FACTORY, 0.03f, 0);
                break;
            case "LiquidFlame":
                emitter.start(FlameParticle.FACTORY, 0.03f, 0);
                break;
            case "Web":
                emitter.pour(WebParticle.FACTORY, 0.4f);
                break;
            case "Darkness":
                emitter.start(DarknessParticle.FACTORY, 0.9f, 0);
                break;
            case "Regrowth":
                emitter.start(LeafParticle.LEVEL_SPECIFIC, 0.2f, 0);
                break;
            case "Foliage":
                emitter.start(ShaftParticle.FACTORY, 0.9f, 0);
                break;
            case "WaterOfHealth":
                emitter.start(Speck.factory(Speck.HEALING), 0.5f, 0);
                break;
            case "WaterOfAwareness":
                emitter.pour(Speck.factory(Speck.QUESTION), 0.3f);
                break;
            case "WaterOfTransmutation":
                emitter.start(Speck.factory(Speck.CHANGE), 0.2f, 0);
                break;
            case "Alchemy":
                emitter.start(Speck.factory(Speck.BUBBLE), 0.4f, 0);
                break;
            default:
                // a mod kind without a registered pour: faint mist so the
                // volume is still visible
                emitter.pour(Speck.factory(Speck.MIST), 0.4f);
                break;
        }
    }
}
