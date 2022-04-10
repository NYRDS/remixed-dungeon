package com.nyrds.pixeldungeon.windows;

import com.watabou.noosa.Gizmo;

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
    private VBox.Align vAlign = VBox.Align.Top;
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

    public float getMaxWidth() {
        return maxWidth;
    }

    public void setAlign(Align align) {
        this.align = align;
    }

    public void setAlign(VBox.Align vAlgin) {
        this.vAlign = vAlgin;
    }

    private float yAlign(IPlaceable c) {
        switch (vAlign) {
            case Top:
                return top();
            case Bottom:
                return bottom() - c.height();
            case Center:
                return top() + (height() - c.height())/2;
        }
        return y;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

    private void alignLeft() {
        float pos = x;

        for(Gizmo g :members) {
            if (g instanceof IPlaceable) {
                ((IPlaceable) g).setPos(pos, yAlign((IPlaceable) g));
                pos += ((IPlaceable) g).width() + gap;
            }
        }
    }

    private void alignRight() {
        float pos = x + maxWidth;

        for(Gizmo g :members) {
            if (g instanceof IPlaceable) {
                ((IPlaceable) g).setPos(pos - ((IPlaceable) g).width() - gap, yAlign((IPlaceable) g));
                pos -= ((IPlaceable) g).width() + gap;
            }
        }
    }

    private float childsWidth() {
        float childsWidth = 0;

        for(Gizmo g :members) {
            if (g instanceof IPlaceable) {
                childsWidth += ((IPlaceable) g).width() + gap;
            }
        }
        return childsWidth;
    }

    private void alignCenter() {

        float pos = x + (maxWidth - childsWidth()) / 2;

        for(Gizmo g :members) {
            if (g instanceof IPlaceable) {
                ((IPlaceable) g).setPos(pos, yAlign((IPlaceable) g));
                pos += ((IPlaceable) g).width() + gap;
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
            if (g instanceof IPlaceable) {
                width += ((IPlaceable) g).width() + gap;
                height = Math.max(height,((IPlaceable) g).height());
            }
        }
    }

    @Override
    protected void layout() {
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
        super.layout();
    }
}
