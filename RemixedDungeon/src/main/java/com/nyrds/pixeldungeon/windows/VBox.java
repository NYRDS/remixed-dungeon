package com.nyrds.pixeldungeon.windows;

import androidx.annotation.NonNull;

import com.watabou.noosa.Gizmo;

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
                IPlaceable gip = (IPlaceable) g;

                IPlaceable shadowOf = gip.shadowOf();
                if(shadowOf != null) {
                    gip.setPos(x, shadowOf.getY());
                    continue;
                }

                gip.setPos(x, pos);
                pos += gip.height() + gap;
            }
        }
    }

    private void alignBottom() {
        float pos = bottom();

        for(Gizmo g :members) {
            if (g instanceof IPlaceable) {
                IPlaceable gip = (IPlaceable) g;

                IPlaceable shadowOf = gip.shadowOf();
                if(shadowOf != null) {
                    gip.setPos(x, shadowOf.getY());
                    continue;
                }

                gip.setPos(x,pos - gip.height() - gap);
                pos -= gip.height() + gap;
            }
        }
    }

    public float childsHeight() {
        float childsHeight = 0;

        for(Gizmo g :members) {
            if (g instanceof IPlaceable) {
                IPlaceable gip = (IPlaceable) g;

                IPlaceable shadowOf = gip.shadowOf();
                if(shadowOf != null) {
                    continue;
                }

                childsHeight += ((IPlaceable) g).height() + gap;
            }
        }
        return childsHeight;
    }

    private void alignCenter() {
        float pos = top() + (height() - childsHeight()) / 2;

        for(Gizmo g :members) {
            if (g instanceof IPlaceable) {
                IPlaceable gip = (IPlaceable) g;

                IPlaceable shadowOf = gip.shadowOf();
                if(shadowOf != null) {
                    gip.setPos(x, shadowOf.getY());
                    continue;
                }

                gip.setPos(x, pos);
                pos += gip.height() + gap;
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
                IPlaceable gip = (IPlaceable) g;

                IPlaceable shadowOf = gip.shadowOf();
                if(shadowOf != null) {
                    continue;
                }
                height += gip.height() + gap;
                //GLog.debug("vbox item: %s, %3.0f", g.getClass().getSimpleName(), gip.width());
                width = Math.max(width,gip.width());
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

    public void addRow(int maxWidth, HBox.Align align, @NonNull Gizmo ...elements) {
        HBox box = new HBox(maxWidth);
        box.setAlign(align);
        for(Gizmo c : elements) {
            box.add(c);
        }
        add(box);
    }
}
