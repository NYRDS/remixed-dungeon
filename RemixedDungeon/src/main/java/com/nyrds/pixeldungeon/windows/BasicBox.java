package com.nyrds.pixeldungeon.windows;

import com.watabou.noosa.Gizmo;
import com.watabou.noosa.ui.Component;

/**
 * Created by mike on 10.05.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public abstract class BasicBox extends Component{
    protected boolean dirty = true;

    @Override
    public void measure() {
        if (dirty) {
            _measure();
            dirty = false;

            //GLog.i("%s: %dx%d",getClass().getSimpleName(), (int)width(),(int)height());
        }
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
    public Gizmo add(Gizmo g) {
        dirty = true;

        if(g instanceof Component) {
            ((Component)g).measure();
        }

        return super.add(g);
    }

    @Override
    public void remove(Gizmo g) {
        dirty = true;
        super.remove(g);
        if(g instanceof Component) {
            ((Component)g).measure();
        }
    }

    protected abstract void _measure();

    @Override
    protected void layout() {
        super.layout();
        measure();
        //GLog.i("layout : %s",getClass().getSimpleName());
    }
}
