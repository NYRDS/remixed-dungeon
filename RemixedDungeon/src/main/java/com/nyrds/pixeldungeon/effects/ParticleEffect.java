package com.nyrds.pixeldungeon.effects;

import com.nyrds.pixeldungeon.effects.emitters.BloodSink;
import com.nyrds.pixeldungeon.effects.emitters.Candle;
import com.nyrds.pixeldungeon.effects.emitters.IceVein;
import com.nyrds.pixeldungeon.effects.emitters.Smoke;
import com.nyrds.pixeldungeon.effects.emitters.Torch;
import com.nyrds.pixeldungeon.effects.emitters.Vein;
import com.nyrds.pixeldungeon.effects.emitters.WaterSink;
import com.watabou.noosa.Group;

public class ParticleEffect {

    public static Group addToCell(String name, int cell) {
        if(name.equals("BloodSink")) {
            return new BloodSink(cell);
        }

        if(name.equals("Candle")) {
            return new Candle(cell);
        }

        if(name.equals("IceVein")) {
            return new IceVein(cell);
        }

        if(name.equals("Smoke")) {
            return new Smoke(cell);
        }

        if(name.equals("Torch")) {
            return new Torch(cell);
        }

        if(name.equals("Vein")) {
            return new Vein(cell);
        }

        if(name.equals("WaterSink")) {
            return new WaterSink(cell);
        }

        return new Group();
    }
}
