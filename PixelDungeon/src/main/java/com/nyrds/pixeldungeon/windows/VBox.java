package com.nyrds.pixeldungeon.windows;

import com.watabou.noosa.Gizmo;
import com.watabou.noosa.ui.Component;

/**
 * Created by mike on 01.05.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class VBox extends Component {

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
            if (g instanceof Component) {
                ((Component) g).setPos(x, pos);
                pos += ((Component) g).height() + gap;
            }
        }
    }

    private void alignBottom() {
        float pos = bottom();

        for(Gizmo g :members) {
            if (g instanceof Component) {
                ((Component) g).setPos(x,pos - ((Component) g).height() - gap);
                pos -= ((Component) g).height() + gap;
            }
        }
    }

    public float childsHeight() {
        float childsHeight = 0;

        for(Gizmo g :members) {
            if (g instanceof Component) {
                childsHeight += ((Component) g).height() + gap;
            }
        }
        return childsHeight;
    }

    public boolean willFit() {
        return childsHeight() <= height();
    }

    private void alignCenter() {
        float pos = top() + (height() - childsHeight()) / 2;

        for(Gizmo g :members) {
            if (g instanceof Component) {
                ((Component) g).setPos(x, pos);
                pos += ((Component) g).height() + gap;
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
    protected void layout() {
        super.layout();

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
    }
}
