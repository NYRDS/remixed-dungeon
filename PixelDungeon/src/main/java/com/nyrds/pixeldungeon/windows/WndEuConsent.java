package com.nyrds.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
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
public class WndEuConsent extends Window {

    private static final int WIDTH		= 120;

    public WndEuConsent(boolean donateOption) {
        super();

        VBox vbox = new VBox();
        add(vbox);

        IconTitle titlebar = new IconTitle();
        titlebar.icon( new ItemSprite(new Gold()) );
        titlebar.label( Utils.capitalize(Game.getVar(R.string.gdpr_title)) );
        titlebar.setSize(WIDTH, 0 );
        vbox.add( titlebar );

        Text message = PixelScene.createMultiline( Game.getVar(R.string.gdpr_main_text), GuiProperties.regularFontSize() );
        message.maxWidth(WIDTH);
        vbox.add( message );

        RedButton agree = new RedButton(R.string.gdpr_agree);
        agree.setSize(WIDTH,18);
        vbox.add(agree);

        RedButton disagree = new RedButton(R.string.gdpr_disagree);
        disagree.setSize(WIDTH,18);
        vbox.add(disagree);

        if(donateOption) {
            RedButton donate = new RedButton(R.string.gdpr_donate);
            donate.setSize(WIDTH,18);
            vbox.add(donate);
        }
        vbox.layout();
        resize(WIDTH, (int) (vbox.height() + 4));
    }

}
