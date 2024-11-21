package com.watabou.pixeldungeon.windows;

import android.content.Intent;
import android.net.Uri;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.ModDesc;
import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.input.Touchscreen;
import com.nyrds.platform.storage.AndroidSAF;
import com.nyrds.platform.FileSystem;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.noosa.TouchArea;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;


public class WndModInfo extends Window {

    public static String exportedModDir;

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

        if(desc.installed) {
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
            /*
            var wnd = new WndInstallingMod();
            AndroidSAF.setListener(wnd);
            GameLoop.pushUiTask(() -> {
                GameLoop.addToScene(wnd);
            });
             */
            GameLoop.execute(() -> {
                try {
                    var outputStream = AndroidSAF.outputStreamToDocument(Game.instance(), AndroidSAF.mBaseDstPath, exportedModDir + ".zip");
                    FileSystem.zipFolderTo(outputStream, FileSystem.getExternalStorageFile(exportedModDir), 99, new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return true;
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
