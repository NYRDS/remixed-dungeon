package com.watabou.pixeldungeon.scenes;

import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.ui.Toast;

class CellSelectorToast extends Toast {
    public CellSelectorToast(String text, Image icon) {
        super(text, icon);
    }

    @Override
    protected void onClose() {
        GameScene.cancel();
    }
}
