package com.nyrds.teavm.reflection;

import org.teavm.classlib.ResourceSupplier;
import org.teavm.classlib.ResourceSupplierContext;

/**
 * Embeds the lua sandbox map into the TeaVM build so
 * LuaSandbox's runtime ClassLoader.getResourceAsStream finds it.
 * Without this the html build has no classpath resources at all.
 *
 * Loaded via META-INF/services from the TeaVM tool's classpath.
 */
public class LuaResourceSupplier implements ResourceSupplier {
    @Override
    public String[] supplyResources(ResourceSupplierContext context) {
        return new String[] {"lua-interface-map.json"};
    }
}
