package com.watabou.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.DonateButton;
import com.watabou.pixeldungeon.ui.RedButton;

class WndDontLikeAds extends WndQuest {
    private float time = 0;
    private final Text pleaseSupport;

    public WndDontLikeAds() {
        super(new Shopkeeper(), Game.getVar(R.string.WndSaveSlotSelect_dontLike));

        float y = height;

        DonateButton btnDonate = new DonateButton(this){
            @Override
            protected void onClick() {
                super.onClick();
                EventCollector.logEvent(EventCollector.SAVE_ADS_EXPERIMENT,"DonateButtonClicked");
            }
        };
        btnDonate.setPos((width - btnDonate.width()) / 2, y + GAP*2);
        add(btnDonate);

        y=btnDonate.bottom();

        pleaseSupport = PixelScene.createText(GuiProperties.titleFontSize());
        pleaseSupport.text(R.string.DonateButton_pleaseDonate);
        pleaseSupport.setPos((width - pleaseSupport.width()) / 2, y+GAP*4);
        add(pleaseSupport);

        RedButton btnNo = new RedButton(R.string.WndDontLikeAds_NotThisTime){
            @Override
            protected void onClick() {
                hide();
            }
        };
        btnNo.setSize(width-6*GAP, BUTTON_HEIGHT);
        btnNo.setPos((width - btnNo.width()) / 2, y + GAP*4);

        add(btnNo);

        resize(width, (int) btnNo.bottom()+GAP);
    }

    @Override
    public void update() {
        super.update();
        time += Game.elapsed;
        float cl = (float) Math.sin(time) * 0.5f + 0.5f;

        pleaseSupport.hardlight(cl, cl, cl);
    }

    @Override
    public void hide() {
        super.hide();
        EventCollector.logEvent(EventCollector.SAVE_ADS_EXPERIMENT,"NotThisTimeClicked");
    }
}
