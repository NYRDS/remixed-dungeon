package com.watabou.pixeldungeon.windows;

import android.content.Intent;
import android.net.Uri;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.ModDesc;
import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.input.Touchscreen;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.noosa.TouchArea;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Window;


public class WndModInfo extends Window {

    public WndModInfo(ModDesc desc) {
        super();
        resizeLimited(120);

        VBox mainLayout = new VBox();

        Text modInfo = PixelScene.createMultiline(GuiProperties.titleFontSize());
        modInfo.maxWidth(width);
        modInfo.hardlight(Window.TITLE_COLOR);
        modInfo.text(desc.name+"\n\n");
        mainLayout.add(modInfo);

        Text description = PixelScene.createMultiline(GuiProperties.titleFontSize());
        description.maxWidth(width);
        description.text(desc.description + "\n\n");
        mainLayout.add(description);

        Text author = PixelScene.createMultiline(GuiProperties.titleFontSize());
        author.maxWidth(width);
        author.text(StringsManager.getVar(R.string.Mods_CreatedBy) + "\n" +  desc.author + "\n\n");

        mainLayout.add(author);

        final String siteUrl = desc.url;

        if (!siteUrl.isEmpty()) {
            Text siteTitle = PixelScene.createMultiline(GuiProperties.titleFontSize());
            siteTitle.maxWidth(width);
            siteTitle.text(StringsManager.getVar(R.string.Mods_AuthorSite) + "\n");

            mainLayout.add(siteTitle);

            Text site = PixelScene.createMultiline();
            site.maxWidth(width);
            site.text( siteUrl + "\n\n");

            site.hardlight(Window.TITLE_COLOR);
            mainLayout.add(site);

            TouchArea siteTouch = new TouchArea(site) {
                @Override
                protected void onClick(Touchscreen.Touch touch) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(siteUrl));
                    Game.instance().startActivity(Intent.createChooser(intent, siteUrl));
                }
            };
            add(siteTouch);
        }

        HBox buttons = new HBox(width);
        mainLayout.add(buttons);
        add(mainLayout);

        mainLayout.setRect(0,0, width, mainLayout.childsHeight());

        resize(width, (int) mainLayout.childsHeight());
    }
}
