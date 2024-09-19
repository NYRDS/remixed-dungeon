package com.nyrds.pixeldungeon.windows;

import com.watabou.noosa.Gizmo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mike on 13.05.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class VHBox extends VBox {
    private final List<HBox>  rows = new ArrayList<>();
    private final List<Gizmo> allMembers = new ArrayList<>();

    private HBox.Align rowsAlign = HBox.Align.Left;
    private HBox.Align rowsLoginLast = HBox.Align.Left;

    private int hGap = 0;

    private float maxWidth;

    public VHBox(float maxWidth) {
        this.maxWidth = maxWidth;
    }

    public void reset() {
        removeAll();
        rows.clear();

        for(Gizmo g:allMembers) {
            _add(g);
        }
    }

    private void _add(Gizmo g) {
        dirty = true;
        if(rows.isEmpty()) {
            putInNextRow(g);
        }

        HBox hBox = rows.get(rows.size()-1);
        hBox.add(g);

        if(hBox.width() > maxWidth) {
            hBox.remove(g);
            putInNextRow(g);
        }

    }

    @Override
    public Gizmo add(Gizmo g) {
        _add(g);
        allMembers.add(g);

        return g;
    }

    @Override
    public void remove(Gizmo g) {
        dirty = true;
        for (HBox row:rows) {
            row.remove(g);
        }
        allMembers.remove(g);
    }

    public Gizmo getByIndex(int index) {
        return allMembers.get(index);
    }

    private void putInNextRow(Gizmo g) {
        HBox hBox = new HBox(maxWidth);
        hBox.setGap(hGap);
        hBox.setAlign(rowsAlign);
        hBox.add(g);
        rows.add(hBox);
        super.add(hBox);
    }

    public void setAlign(HBox.Align rowsAlign) {
        this.rowsAlign = rowsAlign;
    }

    public void setLoginLast(HBox.Align rowsLoginLast) {
        this.rowsLoginLast = rowsLoginLast;
    }

    public void setHGap(int hGap) {
        this.hGap = hGap;
    }

    public void setMaxWidth(float maxWidth) {
        this.maxWidth = maxWidth;
    }

    public void wrapContent() {
        for(HBox row:rows){
            row.wrapContent();
        }
    }
}
