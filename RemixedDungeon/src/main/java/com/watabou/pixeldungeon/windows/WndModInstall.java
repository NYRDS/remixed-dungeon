package com.watabou.pixeldungeon.windows;

import android.content.Intent;
import android.net.Uri;

import com.nyrds.android.InstallMod;
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

        Text title = PixelScene.createMultiline(GuiProperties.mediumTitleFontSize());
        title.maxWidth(width);
        title.text(Game.getVar(R.string.WndModInstall_InstallingMod)+"\n\n");
        title.hardlight(Window.TITLE_COLOR);
        mainLayout.add(title);

        Text modInfo = PixelScene.createMultiline(GuiProperties.titleFontSize());
        modInfo.maxWidth(width);
        modInfo.text("\""+desc.name + "\" " + Game.getVar(R.string.WndModInstall_Version) + " " + desc.hrVersion +  "\n\n");
        mainLayout.add(modInfo);

        Text description = PixelScene.createMultiline();
        description.maxWidth(width);
        description.text(desc.description + "\n\n");
        mainLayout.add(description);

        Text author = PixelScene.createMultiline();
        author.maxWidth(width);
        author.text(Game.getVar(R.string.Mods_CreatedBy) + "\n" +  desc.author + "\n\n");

        mainLayout.add(author);

        final String siteUrl = desc.url;

        if (!siteUrl.isEmpty()) {
            Text siteTitle = PixelScene.createMultiline();
            siteTitle.maxWidth(width);
            siteTitle.text(Game.getVar(R.string.Mods_AuthorSite) + "\n");

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
        if (desc.isCompatible()) {

            var ok = new RedButton(R.string.Wnd_Button_Yes) {
                @Override
                protected void onClick() {
                    action.onAgreed();
                }
            };
            ok.setSize(width / 2f - 4, BUTTON_HEIGHT);

            buttons.add(ok);

            var no = new RedButton(R.string.Wnd_Button_Cancel) {
                @Override
                protected void onClick() {
                    hide();
                }
            };
            no.setSize(width / 2f - 4, BUTTON_HEIGHT);

            buttons.setGap(2);
            buttons.add(no);

            buttons.setAlign(HBox.Align.Width);
            buttons.setRect(0, 0, width, ok.height());

        } else {
            Text pleaseUpdate = PixelScene.createMultiline(GuiProperties.titleFontSize());
            pleaseUpdate.maxWidth(width);
            pleaseUpdate.text(Game.getVar(R.string.WndModInstall_PleaseUpdate) + "\n\n");

            mainLayout.add(pleaseUpdate);

            var pleaseUpdateButton = new RedButton(R.string.Wnd_Button_Ok) {
                @Override
                protected void onClick() {
                    InstallMod.openPlayStore();
                }
            };
            pleaseUpdateButton.setSize(width / 2f - 4, BUTTON_HEIGHT);

            buttons.add(pleaseUpdateButton);

            buttons.setAlign(HBox.Align.Center);
            buttons.setRect(0, 0, width, pleaseUpdate.height());

        }
        mainLayout.add(buttons);
        add(mainLayout);

        mainLayout.setRect(0,0, width, mainLayout.childsHeight());

        resize(width, (int) mainLayout.childsHeight());
    }

    public interface onAgree {
        void onAgreed();
    }
}
