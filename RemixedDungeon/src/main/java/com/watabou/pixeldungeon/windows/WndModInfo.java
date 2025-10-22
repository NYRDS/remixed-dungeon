package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.ModDesc;
import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.input.Touchscreen;
import com.nyrds.platform.storage.AndroidSAF;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.noosa.TouchArea;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

import java.io.IOException;


public class WndModInfo extends Window {

    public static String exportedModDir;
    private final String prevMod;

    public WndModInfo(ModDesc desc) {
        super();
        resizeLimited(120);

        this.prevMod = GamePreferences.activeMod();

        VBox mainLayout = new VBox();

        // Use the mod's data if it's installed, otherwise use the provided description
        if (desc.installed) {
            // Set the mod context to get localized strings from the mod itself
            GamePreferences.activeMod(desc.installDir);

            Text modInfo = PixelScene.createMultiline(GuiProperties.titleFontSize());
            modInfo.maxWidth(width);
            modInfo.hardlight(Window.TITLE_COLOR);
            modInfo.text(StringsManager.getVar(R.string.Mod_Name)+"\n\n");
            mainLayout.add(modInfo);

            Text description = PixelScene.createMultiline(GuiProperties.titleFontSize());
            description.maxWidth(width);
            description.text(StringsManager.getVar(R.string.Mod_Description) + "\n\n");
            mainLayout.add(description);

            Text author = PixelScene.createMultiline(GuiProperties.titleFontSize());
            author.maxWidth(width);
            author.text(StringsManager.getVar(R.string.Mods_CreatedBy) + "\n" +  StringsManager.getVar(R.string.Mod_Author) + "\n\n");

            mainLayout.add(author);

            final String siteUrl = StringsManager.getVar(R.string.Mod_Link);

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
                        Game.openUrl(StringsManager.getVar(R.string.Mods_AuthorSite), siteUrl);
                    }
                };
                add(siteTouch);
            }
        } else {
            // Use the old method for non-installed mods
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
                        Game.openUrl(StringsManager.getVar(R.string.Mods_AuthorSite), siteUrl);
                    }
                };
                add(siteTouch);
            }
        }

        if(desc.installed && Utils.isAndroid()) {
            RedButton exportButton = new RedButton("Save on Device") {
                @Override
                protected void onClick() {
                    exportedModDir = desc.installDir;
                    AndroidSAF.pickDirectoryForModExport();
                    super.onClick();
                }
            };

            exportButton.setSize(width, Window.BUTTON_HEIGHT);
            mainLayout.add(exportButton);
        }
        add(mainLayout);

        mainLayout.setRect(0,0, width, mainLayout.childsHeight());

        resize(width, (int) mainLayout.childsHeight());
    }

    public static void onDirectoryPicked()  {
        if(AndroidSAF.mBaseDstPath != null) {
            GameLoop.execute(() -> {
                try {
                    var outputStream = AndroidSAF.outputStreamToDocument(Game.instance(), AndroidSAF.mBaseDstPath, exportedModDir + ".zip");
                    FileSystem.zipFolderTo(outputStream, FileSystem.getExternalStorageFile(exportedModDir), 99, pathname -> true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
    
    @Override
    public void hide() {
        super.hide();
        // Restore previous mod context if we switched to an installed mod
        GamePreferences.activeMod(prevMod);
    }
}
