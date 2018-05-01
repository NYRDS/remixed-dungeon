package com.nyrds.pixeldungeon.windows;

import com.watabou.noosa.Gizmo;
import com.watabou.noosa.ui.Component;

/**
 * Created by mike on 01.05.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class HBox extends Component {

    public enum Align {
      Left,Right,Center
    };

    private Align align = Align.Left;
    private int gap = 0;

    public void setAlign(Align align) {
        this.align = align;
    }

    private void alignLeft() {
        float pos = x;

        for(Gizmo g :members) {
            if (g instanceof Component) {
                ((Component) g).setPos(pos, y);
                pos += ((Component) g).width() + gap;
            }
        }
    }

    private void alignRight() {
        float pos = width();

        for(Gizmo g :members) {
            if (g instanceof Component) {
                ((Component) g).setPos(pos - ((Component) g).width() - gap, y);
                pos -= ((Component) g).width() + gap;
            }
        }
    }

    private float childsWidth() {
        float childsWidth = 0;

        for(Gizmo g :members) {
            if (g instanceof Component) {
                childsWidth += ((Component) g).width() + gap;
            }
        }
        return childsWidth;
    }

    public boolean willFit() {
        return childsWidth() <= width();
    }

    private void alignCenter() {
        float childsWidth = childsWidth();

        for(Gizmo g :members) {
            if (g instanceof Component) {
                childsWidth += ((Component) g).width() + gap;
            }
        }

        float pos = x - (width() - childsWidth) / 2;

        for(Gizmo g :members) {
            if (g instanceof Component) {
                ((Component) g).setPos(pos, y);
                pos += ((Component) g).width() + gap;
            }
        }


    }


    @Override
    protected void layout() {
        super.layout();

        switch (align) {

            case Left:
                alignLeft();
                break;
            case Right:
                alignRight();
                break;
            case Center:
                alignCenter();
                break;
        }
    }
}
