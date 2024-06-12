package com.watabou.pixeldungeon.windows;

import androidx.annotation.NonNull;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.Ads;
import com.nyrds.pixeldungeon.support.Iap;
import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.WndHelper;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.nyrds.util.ModdingMode;
import com.nyrds.util.Util;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.noosa.ReturnOnlyOnce;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.SaveUtils;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.DonateButton;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.SimpleButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.ArrayList;



public class WndSaveSlotSelect extends Window implements InterstitialPoint {

    private final boolean saving;

    private String slot;

    public WndSaveSlotSelect(final boolean _saving) {
        this(_saving, StringsManager.getVar(R.string.WndSaveSlotSelect_SelectSlot));
    }

    public WndSaveSlotSelect(final boolean _saving, String title) {
        String[] options = slotInfos();

        HeroClass heroClass  = Dungeon.heroClass;

        final int WIDTH = WndHelper.getFullscreenWidth();
        final int maxW = WIDTH - GAP * 2;

        Text tfTitle = PixelScene.createMultiline(title, GuiProperties.titleFontSize());
        tfTitle.hardlight(TITLE_COLOR);
        tfTitle.setX(GAP);
        tfTitle.setY(GAP);
        tfTitle.maxWidth(maxW);
        add(tfTitle);

        if (!_saving && Game.instance().playGames.isConnected()) {
            SimpleButton refreshBtn = getRefreshBtn(WIDTH, tfTitle);
            add(refreshBtn);
        }


        Text tfMesage = PixelScene.createMultiline(windowText(), GuiProperties.regularFontSize());
        tfMesage.maxWidth(maxW);
        tfMesage.setX(GAP);
        tfMesage.setY(tfTitle.getY() + tfTitle.height() + GAP);
        add(tfMesage);

        float pos = tfMesage.getY() + tfMesage.height() + GAP;

        ArrayList<TextButton> buttons = new ArrayList<>();


        final int columns = RemixedDungeon.landscape() ? 3 : 2;
        final int BUTTON_WIDTH = WIDTH / columns - GAP;

        for (int i = 0; i < options.length / columns + 1; i++) {
            for (int j = 0; j < columns; j++) {
                final int index = i * columns + j;
                if (!(index < options.length)) {
                    break;
                }

                float additionalMargin = 0;
                float xColumn = GAP + j * (BUTTON_WIDTH + GAP);
                float xBtn = xColumn;

                final RedButton btn = new RedButton(options[index]) {
                    @Override
                    protected void onClick() {
                        onSelect(index);
                    }
                };
                buttons.add(btn);

                if (Game.instance().playGames.isConnected()) {

                    final String[] slotsDir = {SaveUtils.buildSlotFromTag(slotNameFromIndex(index)),
                            slotNameFromIndexAndMod(index),
                            slotNameFromIndex(index)};

                    final String modernSlotDir = slotsDir[0];

                    for (var slotDirProbe:slotsDir) {
                        final String snapshotId = slotDirProbe + "_" + heroClass.toString();
                        final String saveSnapshotId = modernSlotDir + "_" + heroClass.toString();

                        if ((_saving && !options[index].isEmpty())
                                || (!_saving
                                && Game.instance().playGames.haveSnapshot(snapshotId)
                        )) {
                            Icons icon = _saving ? Icons.BTN_SYNC_OUT : Icons.BTN_SYNC_IN;
                            SimpleButton syncBtn = new SimpleButton(Icons.get(icon)) {
                                protected void onClick() {

                                    if (_saving) {
                                        boolean res = Game.instance().playGames.packFilesToSnapshot(saveSnapshotId,
                                                FileSystem.getInternalStorageFile(modernSlotDir),
                                                pathname -> SaveUtils.isRelatedTo(
                                                        pathname.getPath(),
                                                        heroClass));
                                        showActionResult(res);
                                    } else {
                                        Game.instance().playGames.unpackSnapshotTo(snapshotId,
                                                FileSystem.getInternalStorageFile(modernSlotDir),
                                                res -> GameLoop.pushUiTask(() -> showActionResult(res)));
                                    }
                                }
                            };

                            syncBtn.setPos(xColumn, pos + BUTTON_HEIGHT / 2);
                            additionalMargin = syncBtn.width();
                            add(syncBtn);

                            xBtn = syncBtn.right() + GAP;
                        }
                    }
                }
                if (!options[index].isEmpty()) {
                    SimpleButton deleteBtn = new SimpleButton(Icons.get(Icons.CLOSE)) {
                        protected void onClick() {
                            final int slotIndex = index;
                            WndOptions reallyDelete = new WndOptions(StringsManager.getVar(R.string.WndSaveSlotSelect_Delete_Title), Utils.EMPTY_STRING,
                                    StringsManager.getVar(R.string.WndSaveSlotSelect_Delete_Yes),
                                    StringsManager.getVar(R.string.WndSaveSlotSelect_Delete_No)) {
                                @Override
                                public void onSelect(int index) {
                                    if (index == 0) {
                                        while(isSlotIndexUsed(slotIndex)) {
                                            SaveUtils.deleteSaveFromSlot(getSlotToLoad(slotIndex), Dungeon.heroClass);
                                        }
                                        WndSaveSlotSelect.this.hide();
                                        GameScene.show(new WndSaveSlotSelect(_saving));
                                    }
                                }
                            };
                            GameScene.show(reallyDelete);
                        }
                    };
                    deleteBtn.setPos(xColumn + BUTTON_WIDTH - deleteBtn.width() - GAP, pos);
                    additionalMargin += deleteBtn.width() + GAP;
                    add(deleteBtn);
                }

                btn.setRect(xBtn, pos, BUTTON_WIDTH - additionalMargin - GAP, BUTTON_HEIGHT);
                add(btn);
            }
            pos += BUTTON_HEIGHT;// + GAP;
        }

        resize(WIDTH, (int) pos);

        saving = _saving;

        if (!saving) {
            for (int i = 0; i < options.length; i++) {
                if (!isSlotIndexUsed(i)) {
                    buttons.get(i).enable(false);
                }
            }
        }

        HBox bottomRow = new HBox(width - 2 * GAP);
        bottomRow.setAlign(HBox.Align.Width);
        bottomRow.setGap(2 * GAP);

        if (!saving) {
            RedButton autoLoadButton = new RedButton(R.string.WndSaveSlotSelect_LoadAutoSave) {
                @Override
                protected void onClick() {
                    showAd(SaveUtils.getAutoSave());
                }
            };

            autoLoadButton.setSize(BUTTON_WIDTH - GAP, BUTTON_HEIGHT);

            autoLoadButton.enable(SaveUtils.slotUsed(SaveUtils.getAutoSave(),Dungeon.heroClass));

            bottomRow.add(autoLoadButton);
        }

        if (GamePreferences.donated() == 0 && RemixedDungeon.canDonate()) {
            DonateButton btn = new DonateButton(this);
            bottomRow.add(btn);
        }

        if (bottomRow.getLength() == 1) {
            bottomRow.setAlign(HBox.Align.Center);
        }

        bottomRow.setPos(GAP, pos);
        add(bottomRow);

        resize(width, (int) (height + bottomRow.height()));
    }

    @NonNull
    private SimpleButton getRefreshBtn(int WIDTH, Text tfTitle) {
        SimpleButton refreshBtn = new SimpleButton(Icons.get(Icons.BTN_SYNC_REFRESH)) {
            @Override
            protected void onClick() {
                final Window refreshing = new WndMessage("Please wait a bit...") {
                    @Override
                    public void onBackPressed() {
                    }
                };

                GameLoop.addToScene(refreshing);
                Game.instance().playGames.loadSnapshots(() -> GameLoop.pushUiTask(() -> {
                    refreshing.hide();
                    refreshWindow();
                }));
            }
        };
        refreshBtn.setPos(WIDTH - refreshBtn.width() - GAP * 2, tfTitle.getY());
        return refreshBtn;
    }


    private void refreshWindow() {
        WndSaveSlotSelect.this.hide();
        GameScene.show(new WndSaveSlotSelect(saving));
    }

    private static boolean isSlotIndexUsed(int index) {
        return SaveUtils.slotUsed(getSlotToLoad(index), Dungeon.heroClass);
    }

    private static String getSlotToLoad(int index) {

        String slot = SaveUtils.buildSlotFromTag(slotNameFromIndex(index));
        if(SaveUtils.slotUsed(slot,Dungeon.heroClass)) {
            return slot;
        }

        slot = slotNameFromIndexAndMod(index);
        if (SaveUtils.slotUsed(slot, Dungeon.heroClass)) {
            return slot;
        }

        return slotNameFromIndex(index);

    }

    private static String windowText() {
        if (GamePreferences.donated() == 0 && RemixedDungeon.canDonate()) {
            return StringsManager.getVar(R.string.WndSaveSlotSelect_dontLike);
        }
        return Utils.EMPTY_STRING;
    }

    private static String slotNameFromIndex(int i) {
        return Integer.toString(i + 1);
    }

    private static String slotNameFromIndexAndMod(int i) {
        return ModdingMode.activeMod() + "_" + slotNameFromIndex(i);
    }


    private static String[] slotInfos() {
        String[] ret = new String[10];

        for (int i = 0; i < ret.length; i++) {
            ret[i] = SaveUtils.slotInfo(getSlotToLoad(i), Dungeon.heroClass);
        }

        return ret;
    }

    protected void onSelect(int index) {
        if (saving) {
            Dungeon.save(false);
            slot = SaveUtils.buildSlotFromTag(slotNameFromIndex(index));
            SaveUtils.copySaveToSlot(slot, Dungeon.heroClass);
        }

        showAd(getSlotToLoad(index));
    }

    private void showAd(String slotName) {
        hide();
        slot = slotName;

        Game.softPaused = true;

        if (GamePreferences.donated() < 1) {
            Ads.displaySaveAndLoadAd(new ReturnOnlyOnce(this));
        } else {
            returnToWork(true);
        }
    }

    private void showActionResult(final boolean res) {
        refreshWindow();

        if (res) {
            GameLoop.addToScene(new WndMessage("ok!"));
        } else {
            GameLoop.addToScene(new WndMessage("something went wrong..."));
        }
    }

    @Override
    public void returnToWork(boolean res) {
        Game.softPaused = false;

        GameLoop.pushUiTask(() -> {
            if (!saving) {
                SaveUtils.loadGame(slot, Dungeon.hero.getHeroClass());
            } else {
                if (GamePreferences.donated() == 0 && RemixedDungeon.canDonate()) {

                    if (Math.random() < 0.1) {
                        GameLoop.pushUiTask(() -> {
                            Iap iap = Game.instance().iap;
                            if (iap != null && iap.isReady() || Util.isDebug()) {
                                EventCollector.logEvent(Util.SAVE_ADS_EXPERIMENT, "DialogShown");
                                Hero.doOnNextAction = () -> GameLoop.addToScene(new WndDontLikeAds());
                            } else {
                                EventCollector.logEvent(Util.SAVE_ADS_EXPERIMENT, "DialogNotShownIapNotReady");
                            }
                        });
                    }

                }
            }

        });
    }

}
