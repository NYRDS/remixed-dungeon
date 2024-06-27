package com.watabou.pixeldungeon.windows.elements;

import static com.watabou.pixeldungeon.ui.Window.STD_WIDTH;

import com.watabou.noosa.ui.Component;

public class TabContent extends Component {
    protected int maxWidth = STD_WIDTH;

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }
}
