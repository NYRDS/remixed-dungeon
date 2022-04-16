package com.nyrds.pixeldungeon.windows;

import com.watabou.noosa.Gizmo;
import com.watabou.pixeldungeon.utils.GLog;

/**
 * Created by mike on 01.05.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class VBox extends BasicBox {

    public enum Align {
      Top,Bottom,Center
    }

    private Align align = Align.Top;
    private int gap = 0;

    public void setAlign(Align align) {
        this.align = align;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

    private void alignTop() {
        float pos = top();

        for(Gizmo g :members) {
            if (g instanceof IPlaceable) {
                ((IPlaceable) g).setPos(x, pos);
                pos += ((IPlaceable) g).height() + gap;
            }
        }
    }

    private void alignBottom() {
        float pos = bottom();

        for(Gizmo g :members) {
            if (g instanceof IPlaceable) {
                ((IPlaceable) g).setPos(x,pos - ((IPlaceable) g).height() - gap);
                pos -= ((IPlaceable) g).height() + gap;
            }
        }
    }

    public float childsHeight() {
        float childsHeight = 0;

        for(Gizmo g :members) {
            if (g instanceof IPlaceable) {
                childsHeight += ((IPlaceable) g).height() + gap;
            }
        }
        return childsHeight;
    }

    private void alignCenter() {
        float pos = top() + (height() - childsHeight()) / 2;

        for(Gizmo g :members) {
            if (g instanceof IPlaceable) {
                ((IPlaceable) g).setPos(x, pos);
                pos += ((IPlaceable) g).height() + gap;
            }
        }
    }

    @Override
    public float top() {
        if(align == Align.Bottom) {
            return bottom() - childsHeight();
        }
        return super.top();
    }

    @Override
    protected void _measure() {
        width = 0;
        height = 0;

        for(Gizmo g :members) {
            if (g instanceof IPlaceable) {
                height += ((IPlaceable) g).height() + gap;
                //GLog.debug("vbox item: %s, %3.0f", g.getClass().getSimpleName(), ((IPlaceable) g).width());
                width = Math.max(width,((IPlaceable) g).width());
            }
        }
    }

    @Override
    protected void layout() {
        switch (align) {

            case Top:
                alignTop();
                break;
            case Bottom:
                alignBottom();
                break;
            case Center:
                alignCenter();
                break;
        }
        super.layout();
    }
}
