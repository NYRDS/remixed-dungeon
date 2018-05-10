package com.nyrds.pixeldungeon.windows;

import com.watabou.noosa.Gizmo;
import com.watabou.noosa.ui.Component;

/**
 * Created by mike on 10.05.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
abstract class BasicBox extends Component{
    protected boolean dirty;

    @Override
    public void measure() {
        if (dirty) {
            _measure();
            dirty = false;
        }
    }

    @Override
    public Gizmo add(Gizmo g) {
        dirty = true;

        return super.add(g);
    }

    @Override
    public float width() {
        measure();
        return super.width();
    }

    @Override
    public float height() {
        measure();
        return super.height();
    }

    @Override
    public void remove(Gizmo g) {
        dirty = true;
        super.remove(g);
    }

    protected abstract void _measure();
}
