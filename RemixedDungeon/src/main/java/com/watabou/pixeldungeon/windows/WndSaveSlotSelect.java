package com.watabou.pixeldungeon.windows;

import androidx.annotation.NonNull;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.support.Ads;
import com.nyrds.pixeldungeon.support.Iap;
import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.ImageTab;
import com.nyrds.pixeldungeon.windows.WndDifficultyOptions;
import com.nyrds.pixeldungeon.windows.WndHelper;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.nyrds.util.ModdingMode;
import com.nyrds.util.Util;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Image;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.noosa.ReturnOnlyOnce;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.GamesInProgress;
import com.watabou.pixeldungeon.SaveUtils;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.DonateButton;
import com.watabou.pixeldungeon.ui.IconButton;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.SimpleButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.elements.Tab;

import java.util.ArrayList;


public class WndSaveSlotSelect extends WndTabbed implements InterstitialPoint {

    private boolean saving, _saving, start_scene;
    private String title;
    static private int difficulty;
    static private int initial_difficulty;

    private int baseMark;

    private String slot;
    private HBox bottomRow;
    private ArrayList<IconButton> buttons;

    private boolean creating = false;

    private ArrayList<Gizmo> tabElements = new ArrayList<Gizmo>();

    public WndSaveSlotSelect(final boolean _saving) {
        this(_saving, StringsManager.getVar(R.string.WndSaveSlotSelect_SelectSlot));
    }

    public WndSaveSlotSelect(final boolean _saving, String title) {
        this(_saving, title, false, GameLoop.getDifficulty());
    }

    public WndSaveSlotSelect(final boolean _saving, String title, final boolean start_scene, int difficulty) {

        this._saving = _saving;
        this.title = title;
        this.start_scene = start_scene;
        this.initial_difficulty = this.difficulty = difficulty;


        for (int i = 0; i < 2; i++) {
            Image img = MobFactory.avatar(WndDifficultyOptions.difficulties[i]);
            Tab tab = new DifficultyTab(img, i);
            tab.setSize(32, tabHeight());
            add(tab);
        }

        baseMark = members.size() - 2;
        select(difficulty);
    }

    private void createContent(int difficulty) {
        creating = true;

        this.difficulty = difficulty;

        GamesInProgress.Info[] options = slotInfos();

        HeroClass heroClass = Dungeon.heroClass;

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

        buttons = new ArrayList<>();

        final int columns = RemixedDungeon.landscape() ? 3 : 2;
        final int BUTTON_WIDTH = WIDTH / columns - GAP;

        for (int i = 0; i < options.length / columns + 1; i++) {

            for (int j = 0; j < columns; j++) {
                final int index = i * columns + j;
                if (!(index < options.length)) {
                    break;
                }

                GamesInProgress.Info info = options[index];

                float additionalMargin = 0;
                float xColumn = GAP + j * (BUTTON_WIDTH + GAP);
                float xBtn = xColumn;

                String btnText = "";
                if (info != null) {
                    btnText = Utils.format("d:%2d l:%2d", info.depth, info.level);
                }

                final IconButton btn = new SlotButton(btnText, index);

                buttons.add(btn);

                if (Game.instance().playGames.isConnected()) {

                    final String[] slotsDir = {SaveUtils.buildSlotFromTag(slotNameFromIndex(index), difficulty),
                            slotNameFromIndexAndMod(index),
                            slotNameFromIndex(index)};

                    final String modernSlotDir = slotsDir[0];

                    for (var slotDirProbe : slotsDir) {
                        final String snapshotId = slotDirProbe + "_" + heroClass.toString();
                        final String saveSnapshotId = modernSlotDir + "_" + heroClass.toString();

                        if ((_saving && (options[index] != null))
                                || (!_saving
                                && Game.instance().playGames.haveSnapshot(snapshotId)
                        )) {
                            Icons icon = _saving ? Icons.BTN_SYNC_OUT : Icons.BTN_SYNC_IN;
                            SimpleButton syncBtn = new SyncButton(icon, saveSnapshotId, modernSlotDir, heroClass, snapshotId);

                            syncBtn.setPos(xColumn, pos + BUTTON_HEIGHT / 2);
                            additionalMargin = syncBtn.width();
                            add(syncBtn);

                            xBtn = syncBtn.right() + GAP;
                        }
                    }
                }
                if (options[index] != null) {
                    SimpleButton deleteBtn = new DeleteSaveButton(index);
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

        createBottomRow(difficulty, BUTTON_WIDTH, pos);
        creating = false;
    }

    private void createBottomRow(int difficulty, int BUTTON_WIDTH, float pos) {
        bottomRow = new HBox(width - 2 * GAP);
        bottomRow.setAlign(HBox.Align.Width);
        bottomRow.setGap(2 * GAP);

        if (!saving) {

            RedButton autoLoadButton;

            if (!start_scene || initial_difficulty != difficulty)  {
                autoLoadButton = new RedButton(R.string.WndSaveSlotSelect_LoadAutoSave) {
                    @Override
                    protected void onClick() {
                        showAd(SaveUtils.getAutoSave(difficulty));
                    }
                };
                autoLoadButton.enable(SaveUtils.slotUsed(SaveUtils.getAutoSave(difficulty), Dungeon.heroClass));
            } else {
                GamesInProgress.Info info = GamesInProgress.check(Dungeon.heroClass);
                autoLoadButton = new RedButton(Utils.format(R.string.StartScene_Depth, info.depth,
                        info.level)) {
                    @Override
                    protected void onClick() {
                        InterlevelScene.Do(InterlevelScene.Mode.CONTINUE);
                    }
                };
            }

            autoLoadButton.setSize(BUTTON_WIDTH - GAP, BUTTON_HEIGHT);

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

        String slot = SaveUtils.buildSlotFromTag(slotNameFromIndex(index), difficulty);
        if (SaveUtils.slotUsed(slot, Dungeon.heroClass)) {
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


    private static GamesInProgress.Info[] slotInfos() {
        GamesInProgress.Info[] ret = new GamesInProgress.Info[10];

        for (int i = 0; i < ret.length; i++) {

            ret[i] = SaveUtils.slotInfo(getSlotToLoad(i), Dungeon.heroClass);
        }

        return ret;
    }

    protected void onSelect(int index) {
        if (saving) {
            Dungeon.save(false);
            slot = SaveUtils.buildSlotFromTag(slotNameFromIndex(index), difficulty);
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

    private class DeleteSaveButton extends SimpleButton {
        private final int index;

        public DeleteSaveButton(int index) {
            super(Icons.get(Icons.CLOSE));
            this.index = index;
        }

        protected void onClick() {
            final int slotIndex = index;
            WndOptions reallyDelete = new WndOptions(StringsManager.getVar(R.string.WndSaveSlotSelect_Delete_Title), Utils.EMPTY_STRING,
                    StringsManager.getVar(R.string.WndSaveSlotSelect_Delete_Yes),
                    StringsManager.getVar(R.string.WndSaveSlotSelect_Delete_No)) {
                @Override
                public void onSelect(int index) {
                    if (index == 0) {
                        while (isSlotIndexUsed(slotIndex)) {
                            SaveUtils.deleteSaveFromSlot(getSlotToLoad(slotIndex), Dungeon.heroClass);
                        }
                        WndSaveSlotSelect.this.hide();
                        GameScene.show(new WndSaveSlotSelect(_saving));
                    }
                }
            };
            GameScene.show(reallyDelete);
        }
    }

    private class SyncButton extends SimpleButton {
        private final String saveSnapshotId;
        private final String modernSlotDir;
        private final HeroClass heroClass;
        private final String snapshotId;

        public SyncButton(Icons icon, String saveSnapshotId, String modernSlotDir, HeroClass heroClass, String snapshotId) {
            super(Icons.get(icon));
            this.saveSnapshotId = saveSnapshotId;
            this.modernSlotDir = modernSlotDir;
            this.heroClass = heroClass;
            this.snapshotId = snapshotId;
        }

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
    }

    private class SlotButton extends IconButton {
        private final int index;

        public SlotButton(String btnText, int index) {
            super(btnText);
            this.index = index;
        }

        @Override
        protected void onClick() {
            onSelect(index);
        }
    }

    private class DifficultyTab extends ImageTab {
        private final int finalI;

        public DifficultyTab(Image img, int finalI) {
            super(WndSaveSlotSelect.this, img);
            this.finalI = finalI;
        }

        public void select(boolean value) {
            if (creating) {
                return;
            }
            super.select(value);
            GLog.debug("Selecting: " + finalI + " " + value);
            if (value) {

                GameLoop.pushUiTask(() -> {
                    for (int i = baseMark; i < WndSaveSlotSelect.this.members.size(); i++) {
                        tabElements.add(WndSaveSlotSelect.this.members.get(i));
                    }

                    for (var elem : tabElements) {
                        if (elem instanceof Tab) {
                            continue;
                        }
                        elem.killAndErase();
                    }
                    tabElements.clear();

                    createContent(finalI);
                });
                ;
            }
        }

    }
}
