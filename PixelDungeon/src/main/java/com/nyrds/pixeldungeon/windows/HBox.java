package com.nyrds.pixeldungeon.windows;

import com.watabou.noosa.Gizmo;
import com.watabou.noosa.ui.Component;

/**
 * Created by mike on 01.05.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class HBox extends BasicBox {

    private float maxWidth;

    public enum Align {
      Left,Right,Center,Width
    }

    private Align align = Align.Left;
    private float gap = 0;

    public HBox(float maxWidth) {
        this.maxWidth = maxWidth;
    }

    public void wrapContent() {
        maxWidth = childsWidth();
    }

    public void setMaxWidth(float maxWidth) {
        this.maxWidth = maxWidth;
    }

    public void setAlign(Align align) {
        this.align = align;
    }

    public void setGap(int gap) {
        this.gap = gap;
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
        float pos = x + maxWidth;

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

    private void alignCenter() {

        float pos = x + (maxWidth - childsWidth()) / 2;

        for(Gizmo g :members) {
            if (g instanceof Component) {
                ((Component) g).setPos(pos, y);
                pos += ((Component) g).width() + gap;
            }
        }
    }

    private void alignWidth() {
        if(members.size() > 1) {
            gap = 0;
            float totalGap = maxWidth - childsWidth();
            gap = totalGap / (members.size() - 1);
        }
        alignLeft();
    }

    @Override
    protected void _measure() {
        width = 0;
        height = 0;

        for(Gizmo g :members) {
            if (g instanceof Component) {
                width += ((Component) g).width() + gap;
                height = Math.max(height,((Component) g).height());
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
            case Width:
                alignWidth();
                break;
        }
    }
}
