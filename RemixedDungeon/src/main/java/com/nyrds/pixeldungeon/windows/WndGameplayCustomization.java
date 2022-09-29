package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.mechanics.spells.SpellFactory;
import com.nyrds.pixeldungeon.mechanics.spells.SpellHelper;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.nyrds.util.Util;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Challenges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Facilitations;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.CheckBox;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.ImageButton;
import com.watabou.pixeldungeon.ui.ImageTextButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.windows.IconTitle;

import java.util.ArrayList;

import lombok.var;

public class WndGameplayCustomization extends Window {

    private final int WIDTH = 120;

    public WndGameplayCustomization( boolean editable ) {

        super();

        Text title = PixelScene.createText(StringsManager.getVar(R.string.WndCustomizations_Title), GuiProperties.titleFontSize() );
        title.hardlight( TITLE_COLOR );
        title.setX(PixelScene.align( camera, (WIDTH - title.width()) / 2 ));
        add( title );

        resize(WndHelper.getLimitedWidth(WIDTH), WndHelper.getAlmostFullscreenHeight());

        var customizationsSet = new VHBox(WndHelper.getLimitedWidth(WIDTH) - chrome.marginHor());
        customizationsSet.setAlign(HBox.Align.Width);
        customizationsSet.setGap(4);

        var listBox = new VBox();
        listBox.setAlign(VBox.Align.Top);

        ScrollableList list = new ScrollableList(listBox);

        add(list);

        for (int i = 0; i < Facilitations.MASKS.length; i++) {
            var item = new ChallengeItem(i+16,
                    chrome.innerWidth(),
                    editable);
            listBox.add(item);
        }

        for (int i = 0; i < Challenges.MASKS.length; i++) {
            var item = new ChallengeItem(i,
                    chrome.innerWidth(),
                    editable);

            listBox.add(item);
        }

        listBox.layout();

        list.setRect(0, Math.max(title.bottom(), title.bottom()) + 2, chrome.innerWidth(),
                chrome.innerHeight() - title.bottom());
        list.scrollTo(0, 0);
    }
}