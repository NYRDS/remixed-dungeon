package com.nyrds.teavm.reflection;

import java.util.function.Predicate;
import org.teavm.extension.spi.substitution.SimpleSubstitutionPolicy;
import org.teavm.extension.spi.substitution.SubstitutionSink;

/**
 * Maps gdx classes onto the TeaVM-safe re-implementations that the
 * gdx-teavm backend ships under an `emu` package prefix (BufferUtils,
 * SharedLibraryLoader, the freetype.js bindings, ...).
 *
 * <p>Linked code keeps referring to the real com.badlogic/net.mgsx/org.jbox2d
 * names while the parsed bodies come from the emulated classes; the mapping
 * only applies where an emu class actually exists, everything else falls
 * back to the original.
 *
 * <p>Loaded from META-INF/services by TeaVM's class-source name mapping.
 */
public class GdxEmuSubstitutionPolicy extends SimpleSubstitutionPolicy {
    @Override
    public void contribute(SubstitutionSink sink) {
        Predicate<String> emulated = name ->
                name.startsWith("com.badlogic.gdx.")
                        || name.startsWith("net.mgsx.gltf.")
                        || name.startsWith("org.jbox2d.");
        sink.selectClasses(emulated).packagePrefix("emu");
    }
}
