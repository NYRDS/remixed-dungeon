package com.watabou.pixeldungeon.scenes;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.mobs.npc.ServiceManNPC;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.pixeldungeon.utils.GameControl;
import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.WndDifficultyOptions;
import com.nyrds.pixeldungeon.windows.WndLocalModInstall;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.storage.AndroidSAF;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.nyrds.util.ModdingBase;
import com.nyrds.util.Util;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.GamesInProgress;
import com.watabou.pixeldungeon.Logbook;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.effects.BannerSprites;
import com.watabou.pixeldungeon.effects.BannerSprites.Type;
import com.watabou.pixeldungeon.ui.Archs;
import com.watabou.pixeldungeon.ui.ChallengeButton;
import com.watabou.pixeldungeon.ui.ClassShield;
import com.watabou.pixeldungeon.ui.ExitButton;
import com.watabou.pixeldungeon.ui.FacilitaionButton;
import com.watabou.pixeldungeon.ui.GameButton;
import com.watabou.pixeldungeon.ui.IconButton;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndClass;
import com.watabou.pixeldungeon.windows.WndOptions;
import com.watabou.pixeldungeon.windows.WndSaveSlotSelect;

import java.util.ArrayList;

public class StartScene extends PixelScene {

    private static final float BUTTON_HEIGHT = 24;
    private static final float GAP = 2;

    private static final float WIDTH_P = 116;
    private static final float HEIGHT_P = 220;

    private static final float WIDTH_L = 224;
    private static final float HEIGHT_L = 124;

    private final ArrayList<ClassShield> shields = new ArrayList<>();
    private ChallengeButton challengeButton;
    private FacilitaionButton facilitaionButton;

    private float buttonX;
    private float buttonY;

    private GameButton btnLoad;
    private GameButton btnNewGame;

    private boolean huntressUnlocked;
    private boolean elfUnlocked;
    private boolean gnollUnlocked;

    private float width, height, bottom;

    private Text unlock;

    private ClassShield curShield;
    public int difficulty;

    @Override
    public void create() {
        super.create();

        Badges.loadGlobal();

        uiCamera.setVisible(false);

        int w = Camera.main.width;
        int h = Camera.main.height;

        if (RemixedDungeon.landscape()) {
            width = WIDTH_L;
            height = HEIGHT_L;
        } else {
            width = WIDTH_P;
            height = HEIGHT_P;
        }

        float left = (w - width) / 2;
        float top = (h - height) / 2;

        bottom = h - top;

        Archs archs = new Archs();
        archs.setSize(w, h);
        add(archs);

        Image title = BannerSprites.get(Type.SELECT_YOUR_HERO);

        title.setScaleXY(0.85f, 0.85f);
        title.setX(align((w - title.width()) / 2));
        title.setY(align(top));

        add(title);

        buttonX = left;
        buttonY = bottom - BUTTON_HEIGHT;

        btnNewGame = new GameButton(StringsManager.getVar(R.string.StartScene_New)) {
            @Override
            protected void onClick() {
                if (GamesInProgress.check(curShield.cl) != null) {
                    GameLoop.addToScene(new WndReallyStartNewGame());
                } else {
                    selectDifficulty();
                }
            }
        };
        add(btnNewGame);

        btnLoad = new GameButton(StringsManager.getVar(R.string.StartScene_Load)) {
            @Override
            protected void onClick() {
                Dungeon.hero = CharsList.DUMMY_HERO;
                Dungeon.heroClass = curShield.cl;

                GameLoop.addToScene(new LoadGameOptions());
            }
        };
        add(btnLoad);

        float centralHeight = buttonY - title.getY() - title.height();

        int usableClasses = 0;

        shields.clear();
        for (HeroClass cl : HeroClass.values()) {
            if (cl.allowed()) {
                usableClasses++;
                ClassShield shield = new ClassShield(this, cl);
                shields.add(shield);
                add(shield);
            }
        }

        challengeButton = new ChallengeButton(this);
        facilitaionButton = new FacilitaionButton(this);

        if (RemixedDungeon.landscape()) {
            float shieldW = width / (usableClasses);
            float shieldH = Math.min(centralHeight, shieldW);
            top = title.getY() + title.height + (centralHeight - shieldH) / 2;
            int i = 0;
            for (ClassShield shield : shields) {
                shield.setRect(left + i * shieldW, top, shieldW, shieldH);
                i++;
            }

            float y = title.getY() + title.height / 2;

            HBox customizations = new HBox(width);
            customizations.setAlign(HBox.Align.Center);
            customizations.setGap(2);

            customizations.add(challengeButton);
            customizations.add(title);
            customizations.add(facilitaionButton);

            customizations.setPos(left, y - customizations.height() / 2);
            add(customizations);

        } else {
            int classesPerRow = 4;
            float shieldW = width / (classesPerRow);
            float shieldH = shieldW * 1.2f;
            top = title.getY() + title.height() + shieldH * 0.25f;
            int i = 0;
            int j = 0;
            for (ClassShield shield : shields) {

                if (j == 0 && i == 1) {
                    i += 2;
                }

                shield.setRect(left + i * shieldW, top + (shieldH * 1.5f) * j,
                        shieldW, shieldH);
                i++;
                if (i == classesPerRow) {
                    j++;
                    i = 0;
                }
            }

            HBox customizations = new HBox(challengeButton.width() * 2.5f);
            customizations.setGap(2);

            customizations.add(challengeButton);
            customizations.add(facilitaionButton);

            customizations.setPos((float) (w / 2) - customizations.width() / 2, top + shieldH * 0.5f - customizations.height() / 2);
            add(customizations);
        }

        unlock = PixelScene.createMultiline(GuiProperties.titleFontSize());
        add(unlock);

        huntressUnlocked = Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_3) || (GamePreferences.donated() >= 1);
        elfUnlocked = Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_4) || (GamePreferences.donated() >= 2);
        gnollUnlocked = Badges.isUnlocked(Badges.Badge.GNOLL_UNLOCKED) || (GamePreferences.donated() >= 3);

        ExitButton btnExit = new ExitButton();
        btnExit.setPos(Camera.main.width - btnExit.width(), 0);
        add(btnExit);

        for (ClassShield shield : shields) {
            if (shield.cl.classIndex() == GamePreferences.lastClass()) {
                updateShield(shield);
                break;
            }
        }

        if (curShield == null) {
            updateShield(shields.get(0));
        }

        Logbook.logbookEntries.clear();    // Clear the log book before starting a new game
        ServiceManNPC.resetLimit();

        fadeIn();
        if(AndroidSAF.isAutoSyncMaybeNeeded(ModdingBase.activeMod())) {
            WndLocalModInstall.onDirectoryPicked();
        }
    }

    private void updateUnlockLabel(String text) {
        unlock.maxWidth((int) width);
        unlock.text(text);

        float pos = (bottom - BUTTON_HEIGHT)
                + (BUTTON_HEIGHT - unlock.height()) / 2;

        unlock.hardlight(0xFFFF00);
        unlock.setX(PixelScene.align(Camera.main.width / 2 - unlock.width() / 2));
        unlock.setY(PixelScene.align(pos));

        unlock.setVisible(true);
        btnLoad.setVisible(false);
        btnNewGame.setVisible(false);
    }

    public void updateShield(ClassShield shield) {
        if (curShield == shield) {
            add(new WndClass(shield.cl));
            return;
        }

        if (curShield != null) {
            curShield.highlight(false);
            GamesInProgress.delete(shield.cl);
        }

        curShield = shield;
        curShield.highlight(true);
        bringToFront(curShield);

        if (!Util.isDebug()) {
            if (curShield.cl == HeroClass.HUNTRESS && !huntressUnlocked) {
                updateUnlockLabel(StringsManager.getVar(R.string.StartScene_Unlock));
                return;
            }

            if (curShield.cl == HeroClass.ELF && !elfUnlocked) {
                updateUnlockLabel(StringsManager.getVar(R.string.StartScene_UnlockElf));
                return;
            }

            if (curShield.cl == HeroClass.GNOLL && !gnollUnlocked) {
                updateUnlockLabel(StringsManager.getVar(R.string.StartScene_UnlockGnoll));
                return;
            }

            if(curShield.cl == HeroClass.PLAGUE_DOCTOR || curShield.cl == HeroClass.PRIEST) {
                updateUnlockLabel(StringsManager.getVar(R.string.StartScene_ComingSoon));
                return;
            }

        }

        unlock.setVisible(false);
        btnLoad.setVisible(true);

        btnNewGame.setVisible(true);
        btnNewGame.secondary(StringsManager.getVar(R.string.StartScene_Erase));

        float w = (Camera.main.width - GAP) / 2 - buttonX;

        btnLoad.setRect(buttonX, buttonY, w, BUTTON_HEIGHT);
        btnNewGame.setRect(btnLoad.right() + GAP, buttonY, w,
                BUTTON_HEIGHT);
    }

    private void selectDifficulty() {
        WndOptions difficultyOptions = new WndDifficultyOptions(this);
        add(difficultyOptions);
    }

    public void startNewGame(int difficulty) {
        GameControl.startNewGame(curShield.cl.name(), difficulty, false);
    }

    @Override
    protected void onBackPressed() {
        RemixedDungeon.switchNoFade(TitleScene.class);
    }

    private class WndReallyStartNewGame extends WndOptions {
        public WndReallyStartNewGame() {
            super(StringsManager.getVar(R.string.StartScene_Really), StringsManager.getVar(R.string.StartScene_Warning), StringsManager.getVar(R.string.StartScene_Yes), StringsManager.getVar(R.string.StartScene_No));
            var info = GamesInProgress.check(curShield.cl);
            Text eraseWarning = message;
            if (info == null)  {
                selectDifficulty();
                hide();
            } else {
                eraseWarning.text(eraseWarning.text() + "\n" + Utils.format(R.string.StartScene_Depth, info.depth, info.level)+"\n" + StringsManager.getVar(WndDifficultyOptions.descs[info.difficulty])+"\n");
                layout();
            }

        }

        @Override
        public void onSelect(int index) {
            if (index == 0) {
                selectDifficulty();
            }
        }
    }

    private class LoadGameOptions extends WndOptions {
        public LoadGameOptions() {
            super(StringsManager.getVar(R.string.StartScene_Load), StringsManager.getVar(R.string.StartScene_ContinueLatest), "Continue_your_latest_game", StringsManager.getVar(R.string.StartScene_ChooseFromSlot));
            var info = GamesInProgress.check(curShield.cl);
            IconButton newGameButton  = ((IconButton)buttonsVbox.getMember(0));
            if (info == null)  {
                onSelect(1);
                hide();
            } else {
                newGameButton.text(Utils.format(R.string.StartScene_Depth, info.depth, info.level));
                newGameButton.icon(MobFactory.avatar(WndDifficultyOptions.difficulties[info.difficulty]));
            }

        }

        @Override
        public void onSelect(int i) {
            if (i == 0) {
                InterlevelScene.Do(InterlevelScene.Mode.CONTINUE);
            } else {
                var selectLoadSlot = new WndSaveSlotSelect(false, StringsManager.getVar(R.string.WndSaveSlotSelect_SelectSlot), true, difficulty);

                StartScene.this.add(selectLoadSlot);
                StartScene.this.bringToFront(selectLoadSlot);

            }
        }
    }
}
