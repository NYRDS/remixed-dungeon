package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.input.Touchscreen.Touch;
import com.nyrds.platform.storage.SaveUtils;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.nyrds.util.ModdingBase;
import com.watabou.noosa.Text;
import com.watabou.noosa.TouchArea;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndModDescription extends Window {

    private float yPos;
    private final String prevMod;

    public WndModDescription(final String option, final String prevMod) {

        super();
        resizeLimited(120);

        this.prevMod = prevMod;

        GamePreferences.activeMod(option);

        yPos = 0;

        if (!option.equals(ModdingBase.REMIXED)) {
            Text title = PixelScene.createMultiline(GuiProperties.titleFontSize());
            title.maxWidth(width);
            title.text(StringsManager.getVar(R.string.Mod_Name) + "\n ");
            title.hardlight(Window.TITLE_COLOR);

            place(title);

            Text author = PixelScene.createMultiline(GuiProperties.regularFontSize());
            author.maxWidth(width);
            author.text(StringsManager.getVar(R.string.Mods_CreatedBy) + "\n" + StringsManager.getVar(R.string.Mod_Author) + "\n ");

            place(author);

            final String siteUrl = StringsManager.getVar(R.string.Mod_Link);
            if (!siteUrl.isEmpty()) {
                Text site = PixelScene.createMultiline(GuiProperties.regularFontSize());
                site.maxWidth(width);
                site.text(StringsManager.getVar(R.string.Mods_AuthorSite) + "\n" + siteUrl + "\n ");
                place(site);

                TouchArea siteTouch = new TouchArea(site) {
                    @Override
                    protected void onClick(Touch touch) {
                        Game.openUrl(StringsManager.getVar(R.string.Mods_AuthorSite), siteUrl);
                    }
                };
                add(siteTouch);
            }

            final String emailUri = StringsManager.getVar(R.string.Mod_Email);

            if (!emailUri.isEmpty()) {
                Text email = PixelScene.createMultiline(GuiProperties.regularFontSize());
                email.maxWidth(width);
                email.text(StringsManager.getVar(R.string.Mods_AuthorEmail) + emailUri + "\n ");
                place(email);

                TouchArea emailTouch = new TouchArea(email) {
                    @Override
                    protected void onClick(Touch touch) {
                        Game.sendEmail(emailUri, StringsManager.getVar(R.string.app_name) + ":" + StringsManager.getVar(R.string.Mod_Name));
                    }
                };
                add(emailTouch);
            }

            Text description = PixelScene.createMultiline(GuiProperties.regularFontSize());
            description.maxWidth(width);
            description.text(StringsManager.getVar(R.string.Mod_Description) + "\n ");
            place(description);
        }

        RedButton btn = new RedButton(R.string.Mods_RestartRequired) {
            @Override
            protected void onClick() {
                switchSaves(option, prevMod);
                RemixedDungeon.instance().doRestart();
            }
        };

        btn.setSize(Math.min(width, btn.reqWidth()), 16);
        btn.setPos(width / 2 - btn.width() / 2, yPos);

        add(btn);

        yPos += btn.height();

        resize(width, (int) (yPos + GAP));
    }

    private static void switchSaves(String option, String prevMod) {
        if (option.equals(prevMod)) {
            return;
        }

        SaveUtils.copyAllClassesToSlot(prevMod);
        SaveUtils.deleteGameAllClasses();
        SaveUtils.copyAllClassesFromSlot(option);
    }

    private void place(Text text) {
        text.setPos(0, yPos);
        yPos += text.height();
        add(text);
    }

    @Override
    public void hide() {
        super.hide();
        GamePreferences.activeMod(prevMod);
    }

    public void onBackPressed() {
        hide();
        GameLoop.addToScene(new WndModSelect());
    }
}
