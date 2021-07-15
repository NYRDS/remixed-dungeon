package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.watabou.pixeldungeon.ui.IconButton;
import com.watabou.pixeldungeon.ui.Icons;

class WndDontLikeAds extends WndQuest {

    public WndDontLikeAds() {
        super(new Shopkeeper(), StringsManager.getVar(R.string.WndSaveSlotSelect_dontLike));

        float y = height;

        IconButton btnDonate = new IconButton(R.string.DonateButton_pleaseDonate,Icons.SUPPORT.get() ){
            @Override
            protected void onClick() {
                WndDontLikeAds.this.add(new WndDonate());
                EventCollector.logEvent(Util.SAVE_ADS_EXPERIMENT,"DonateButtonClicked");
            }
        };
        btnDonate.setSize(width-6*GAP, BUTTON_HEIGHT);
        btnDonate.setPos((width - btnDonate.width()) / 2, y + GAP*4);
        add(btnDonate);

        resize(width, (int) btnDonate.bottom()+GAP);
    }

    @Override
    public void hide() {
        super.hide();
        EventCollector.logEvent(Util.SAVE_ADS_EXPERIMENT,"DialogClosed");
    }
}
