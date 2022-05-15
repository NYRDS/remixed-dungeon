package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.EuConsent;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.IconTitle;

/**
 * Created by mike on 24.06.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public abstract class WndEuConsent extends Window {

    private static final int WIDTH		= 120;

    private boolean consentChoosen;

    public WndEuConsent() {
        super();

        VBox vbox = new VBox();
        add(vbox);

        IconTitle titlebar = new IconTitle();
        titlebar.icon( new ItemSprite(new Gold()) );
        titlebar.label( Utils.capitalize(StringsManager.getVar(R.string.gdpr_title)) );
        titlebar.setSize(WIDTH, 0 );
        vbox.add( titlebar );

        Text message = PixelScene.createMultiline(R.string.gdpr_text, GuiProperties.regularFontSize() );
        message.maxWidth(WIDTH);
        vbox.add( message );

        RedButton agree = new RedButton(R.string.gdpr_agree) {
            @Override
            protected void onClick() {
                super.onClick();
                EuConsent.setConsentLevel(EuConsent.PERSONALIZED);
                consentChoosen = true;
                done();
                hide();
            }
        };
        agree.setSize(WIDTH,18);
        vbox.add(agree);

        RedButton disagree = new RedButton(R.string.gdpr_disagree) {
            @Override
            protected void onClick() {
                super.onClick();
                EuConsent.setConsentLevel(EuConsent.NON_PERSONALIZED);
                consentChoosen = true;
                done();
                hide();
            }
        };
        disagree.setSize(WIDTH,18);
        vbox.add(disagree);

        vbox.layout();
        resize(WIDTH, (int) (vbox.height() + 4));
    }


    abstract public void done();

    @Override
    public void hide() {
        if(!consentChoosen) {
            Game.toast(StringsManager.getVar(R.string.gdpr_choose));
        } else {
            super.hide();
        }
    }
}
