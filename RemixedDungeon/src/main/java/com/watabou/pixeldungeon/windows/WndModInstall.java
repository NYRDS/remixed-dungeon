package com.watabou.pixeldungeon.windows;

import android.content.Intent;
import android.net.Uri;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.android.util.Mods;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.VBox;
import com.watabou.input.Touchscreen;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.noosa.TouchArea;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;

import lombok.var;

public class WndModInstall extends Window {

    public WndModInstall(Mods.ModDesc desc, onAgree action) {
        super();
        resizeLimited(120);

        VBox mainLayout = new VBox();

        Text title = PixelScene.createMultiline(GuiProperties.titleFontSize());
        title.maxWidth(width);
        title.text("Installing mod: "+desc.name + "\n\n");
        title.hardlight(Window.TITLE_COLOR);

        mainLayout.add(title);

        Text author = PixelScene.createMultiline(GuiProperties.regularFontSize());
        author.maxWidth(width);
        author.text(Game.getVar(R.string.Mods_CreatedBy) + ":" + desc.author + "\n\n");

        mainLayout.add(author);

        final String siteUrl = desc.url;

        if (!siteUrl.isEmpty()) {
            Text site = PixelScene.createMultiline(GuiProperties.regularFontSize());
            site.maxWidth(width);
            site.text(Game.getVar(R.string.Mods_AuthorSite) + ":\n" + siteUrl + "\n\n");
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

        Text description = PixelScene.createMultiline(GuiProperties.regularFontSize());
        description.maxWidth(width);
        description.text(desc.description + "\n\n");
        mainLayout.add(description);

        HBox buttons = new HBox(width);

        var ok = new RedButton(R.string.Wnd_Button_Yes) {
            @Override
            protected void onClick() {
                action.onAgree();
            }
        };
        ok.setSize(width/2f - 4 , BUTTON_HEIGHT );

        buttons.add(ok);

        var no = new RedButton(R.string.Wnd_Button_Cancel) {
            @Override
            protected void onClick() {
                hide();
            }
        };
        no.setSize(width/2f - 4, BUTTON_HEIGHT );


        buttons.setGap(2);
        buttons.add(no);

        buttons.setAlign(HBox.Align.Width);
        buttons.setRect(0,0, width, ok.height());

        mainLayout.add(buttons);

        add(mainLayout);

        mainLayout.setRect(0,0, width, mainLayout.childsHeight());

        resize(width, (int) mainLayout.childsHeight());
    }

    public interface onAgree {
        void onAgree();
    }
}
