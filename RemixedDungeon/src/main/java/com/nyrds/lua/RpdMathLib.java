package com.nyrds.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.jse.JseMathLib;

import java.util.Random;

public class RpdMathLib extends JseMathLib {

    public RpdMathLib() {
    }
    public LuaValue call(LuaValue modname, LuaValue env) {
        super.call(modname, env);
        LuaValue math = env.get("math");
        math.set("random", new random());
        return math;
    }

    static class random extends LibFunction {
        final Random random = new Random();
        public LuaValue call() {
            return valueOf( random.nextDouble() );
        }
        public LuaValue call(LuaValue a) {
            int m = a.checkint();
            if (m<1) argerror(1, "interval is empty");
            return valueOf( 1 + random.nextInt(m) );
        }
        public LuaValue call(LuaValue a, LuaValue b) {
            int m = a.checkint();
            int n = b.checkint();

            if(n == m) {
                return a;
            }

            if (n<m) {
                n = n ^ m ^ (m = n);
            }

            return valueOf( m + random.nextInt(n+1-m) );
        }

    }
}
