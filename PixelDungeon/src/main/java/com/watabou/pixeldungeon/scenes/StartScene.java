/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.scenes;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.ServiceManNPC;
import com.nyrds.pixeldungeon.support.EuConsent;
import com.nyrds.pixeldungeon.windows.WndEuConsent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.GamesInProgress;
import com.watabou.pixeldungeon.Logbook;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.effects.BannerSprites;
import com.watabou.pixeldungeon.effects.BannerSprites.Type;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.ui.Archs;
import com.watabou.pixeldungeon.ui.ExitButton;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndChallenges;
import com.watabou.pixeldungeon.windows.WndClass;
import com.watabou.pixeldungeon.windows.WndMessage;
import com.watabou.pixeldungeon.windows.WndOptions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ru.livli.swsdk.SWSdk;
import ru.livli.swsdk.api.impl.SWImpl;
import ru.livli.swsdkapps.InstalledApplicationsCollector;

public class StartScene extends PixelScene {

    private static final float BUTTON_HEIGHT = 24;
    private static final float GAP = 2;

    private static final float WIDTH_P = 116;
    private static final float HEIGHT_P = 220;

    private static final float WIDTH_L = 224;
    private static final float HEIGHT_L = 124;

    private static boolean swSdkStarted = false;

    private ArrayList<ClassShield> shields = new ArrayList<>();

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

    @Override
    public void create() {
        super.create();

        if(!swSdkStarted) {
            SWSdk.Companion.init(Game.instance(), "22b4f34f2616d7f", false,
                    new SWSdk.Callback() {

                        @Override
                        public void onError(@NotNull Throwable throwable) {
                            EventCollector.logException(throwable, "hq sdk init error");
                        }

                        @Override
                        public void onSuccess(@Nullable SWImpl sw) {
                            if (sw != null) {
                                sw.start(new InstalledApplicationsCollector(Game.instance()));
                                sw.startSystemEventsTracking(Game.instance());
                                swSdkStarted = true;
                            } else {
                                EventCollector.logException();
                            }
                        }
                    }

            );
        }


        Badges.loadGlobal();

        uiCamera.setVisible(false);

        int w = Camera.main.width;
        int h = Camera.main.height;

        if (PixelDungeon.landscape()) {
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

        title.setScale(0.85f, 0.85f);
        title.x = align((w - title.width()) / 2);
        title.y = align(top);

        add(title);

        buttonX = left;
        buttonY = bottom - BUTTON_HEIGHT;

        btnNewGame = new GameButton(Game.getVar(R.string.StartScene_New)) {
            @Override
            protected void onClick() {
                if (GamesInProgress.check(curShield.cl) != null) {
                    StartScene.this.add(new WndOptions(Game
                            .getVar(R.string.StartScene_Really), Game
                            .getVar(R.string.StartScene_Warning),
                            Game.getVar(R.string.StartScene_Yes), Game.getVar(R.string.StartScene_No)) {
                        @Override
                        protected void onSelect(int index) {
                            if (index == 0) {
                                selectDifficulty();
                            }
                        }
                    });

                } else {
                    selectDifficulty();
                }
            }
        };
        add(btnNewGame);

        btnLoad = new GameButton(Game
                .getVar(R.string.StartScene_Load)) {
            @Override
            protected void onClick() {
                InterlevelScene.Do(InterlevelScene.Mode.CONTINUE);
                Dungeon.heroClass = curShield.cl;
            }
        };
        add(btnLoad);

        float centralHeight = buttonY - title.y - title.height();

        int usableClasses = 0;

        shields.clear();
        for (HeroClass cl : HeroClass.values()) {
            if (cl.allowed()) {
                usableClasses++;
                ClassShield shield = new ClassShield(cl);
                shields.add(shield);
                add(shield);
            }
        }

        if (PixelDungeon.landscape()) {
            float shieldW = width / usableClasses;
            float shieldH = Math.min(centralHeight, shieldW);
            top = title.y + title.height + (centralHeight - shieldH) / 2;
            int i = 0;
            for (ClassShield shield : shields) {
                shield.setRect(left + i * shieldW, top, shieldW, shieldH);
                i++;
            }

            ChallengeButton challenge = new ChallengeButton();
            challenge.setPos(w / 2 - challenge.width() / 2, 0);
            add(challenge);

        } else {
            int classesPerRow = 4;
            float shieldW = width / (classesPerRow);
            float shieldH = shieldW * 1.2f;
            top = title.y + title.height() + shieldH * 0.25f;
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

            ChallengeButton challenge = new ChallengeButton();
            challenge.setPos(w / 2 - challenge.width() / 2, top + shieldH * 0.5f
                    - challenge.height() / 2);
            add(challenge);
        }

        unlock = PixelScene.createMultiline(GuiProperties.titleFontSize());
        add(unlock);

        huntressUnlocked = Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_3) || (PixelDungeon.donated() >= 1);
        elfUnlocked = Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_4) || (PixelDungeon.donated() >= 2);
        gnollUnlocked = Badges.isUnlocked(Badges.Badge.GNOLL_UNLOCKED) || (PixelDungeon.donated() >= 3);

        ExitButton btnExit = new ExitButton();
        btnExit.setPos(Camera.main.width - btnExit.width(), 0);
        add(btnExit);

        for (ClassShield shield : shields) {
            if (shield.cl == HeroClass.values()[PixelDungeon.lastClass()]) {
                updateShield(shield);
                return;
            }
        }

        if (curShield == null) {
            updateShield(shields.get(0));
        }

        fadeIn();
    }

    private void updateUnlockLabel(String text) {
        unlock.maxWidth((int) width);
        unlock.text(text);

        float pos = (bottom - BUTTON_HEIGHT)
                + (BUTTON_HEIGHT - unlock.height()) / 2;

        unlock.hardlight(0xFFFF00);
        unlock.x = PixelScene.align(Camera.main.width / 2 - unlock.width() / 2);
        unlock.y = PixelScene.align(pos);

        unlock.setVisible(true);
        btnLoad.setVisible(false);
        btnNewGame.setVisible(false);
    }

    @Override
    public void destroy() {

        Badges.saveGlobal();

        super.destroy();
    }

    private void updateShield(ClassShield shield) {
        if (curShield == shield) {
            add(new WndClass(shield.cl));
            return;
        }

        if (curShield != null) {
            curShield.highlight(false);
        }

        curShield = shield;
        curShield.highlight(true);


        if (!BuildConfig.DEBUG) {
            if (curShield.cl == HeroClass.HUNTRESS && !huntressUnlocked) {
                updateUnlockLabel(Game.getVar(R.string.StartScene_Unlock));
                return;
            }

            if (curShield.cl == HeroClass.ELF && !elfUnlocked) {
                updateUnlockLabel(Game.getVar(R.string.StartScene_UnlockElf));
                return;
            }

            if (curShield.cl == HeroClass.GNOLL && !gnollUnlocked) {
                updateUnlockLabel(Game.getVar(R.string.StartScene_UnlockGnoll));
                return;
            }

        }

        unlock.setVisible(false);

        GamesInProgress.Info info = GamesInProgress.check(curShield.cl);
        if (info != null) {

            btnLoad.setVisible(true);
            btnLoad.secondary(Utils.format(Game
                            .getVar(R.string.StartScene_Depth), info.depth,
                    info.level));

            btnNewGame.setVisible(true);
            btnNewGame.secondary(Game
                    .getVar(R.string.StartScene_Erase));

            float w = (Camera.main.width - GAP) / 2 - buttonX;

            btnLoad.setRect(buttonX, buttonY, w, BUTTON_HEIGHT);
            btnNewGame.setRect(btnLoad.right() + GAP, buttonY, w,
                    BUTTON_HEIGHT);

        } else {
            btnLoad.setVisible(false);

            btnNewGame.setVisible(true);
            btnNewGame.secondary("");
            btnNewGame.setRect(buttonX, buttonY, Camera.main.width
                    - buttonX * 2, BUTTON_HEIGHT);
        }

    }

    private void selectDifficulty() {

        WndOptions difficultyOptions = new WndOptions(Game.getVar(R.string.StartScene_DifficultySelect), "",
                Game.getVar(R.string.StartScene_DifficultyEasy),
                Game.getVar(R.string.StartScene_DifficultyNormalWithSaves),
                Game.getVar(R.string.StartScene_DifficultyNormal),
                Game.getVar(R.string.StartScene_DifficultyExpert)) {
            @Override
            protected void onSelect(final int index) {

                if (index < 2 && EuConsent.getConsentLevel() < EuConsent.NON_PERSONALIZED) {
                    Game.scene().add(new WndEuConsent() {
                        @Override
                        public void done() {
                            startNewGame(index);
                        }
                    });
                    return;
                }

                startNewGame(index);
            }
        };

        add(difficultyOptions);
    }

    private void startNewGame(int difficulty) {

        Dungeon.setDifficulty(difficulty);
        Dungeon.hero = null;
        Dungeon.heroClass = curShield.cl;


        Map<String,String> resDesc = new HashMap<>();
        resDesc.put("class",curShield.cl.name());
        resDesc.put("mod", PixelDungeon.activeMod());
        resDesc.put("difficulty",  String.valueOf(difficulty));

        EventCollector.logEvent("game", resDesc);

        Logbook.logbookEntries.clear();    // Clear the log book before starting a new game
        ServiceManNPC.resetLimit();

        if (PixelDungeon.intro()) {
            PixelDungeon.intro(false);
            Game.switchScene(IntroScene.class);
        } else {
            InterlevelScene.Do(InterlevelScene.Mode.DESCEND);
            Game.switchScene(InterlevelScene.class);
        }
    }

    @Override
    protected void onBackPressed() {
        PixelDungeon.switchNoFade(TitleScene.class);
    }

    private static class GameButton extends RedButton {

        private Text secondary;

        public GameButton(String primary) {
            super(primary);

            this.secondary.text("");
        }

        @Override
        protected void createChildren() {
            super.createChildren();

            secondary = createText(GuiProperties.smallFontSize());

            add(secondary);
        }

        @Override
        protected void layout() {
            super.layout();

            if (secondary.text().length() > 0) {
                text.y = align(y
                        + (height - text.height() - secondary.height())
                        / 2);

                secondary.x = align(x + (width - secondary.width()) / 2);
                secondary.y = align(text.y + text.height());
            } else {
                text.y = align(y + (height - text.height()) / 2);
            }
        }

        public void secondary(String text) {
            secondary.text(text);
        }
    }

    private class ClassShield extends Button {

        private static final float MIN_BRIGHTNESS = 0.6f;

        private static final int BASIC_NORMAL = 0x444444;
        private static final int BASIC_HIGHLIGHTED = 0xCACFC2;

        private static final int MASTERY_NORMAL = 0x7711AA;
        private static final int MASTERY_HIGHLIGHTED = 0xCC33FF;

        private static final int WIDTH = 24;
        private static final int HEIGHT = 28;
        private static final float SCALE = 1.5f;

        private HeroClass cl;

        private Image avatar;
        private Text name;
        private Emitter emitter;

        private float brightness;

        private int normal;
        private int highlighted;

        public ClassShield(HeroClass cl) {
            super();

            this.cl = cl;

            avatar.frame(cl.ordinal() * WIDTH, 0, WIDTH, HEIGHT);
            avatar.Scale().set(SCALE);

            if (Badges.isUnlocked(cl.masteryBadge())) {
                normal = MASTERY_NORMAL;
                highlighted = MASTERY_HIGHLIGHTED;
            } else {
                normal = BASIC_NORMAL;
                highlighted = BASIC_HIGHLIGHTED;
            }

            name.text(cl.title().toUpperCase(Locale.getDefault()));
            name.hardlight(normal);

            brightness = MIN_BRIGHTNESS;
            updateBrightness();
        }

        @Override
        protected void createChildren() {

            super.createChildren();

            avatar = new Image(Assets.AVATARS);
            add(avatar);

            name = PixelScene.createText(GuiProperties.titleFontSize());
            add(name);

            emitter = new Emitter();
            add(emitter);
        }

        @Override
        protected void layout() {

            super.layout();

            avatar.x = align(x + (width - avatar.width()) / 2);
            avatar.y = align(y + (height - avatar.height() - name.height()) / 2);

            name.x = align(x + (width - name.width()) / 2);
            name.y = avatar.y + avatar.height() + SCALE;

            emitter.pos(avatar.x, avatar.y, avatar.width(), avatar.height());
        }

        @Override
        protected void onTouchDown() {

            emitter.revive();
            emitter.start(Speck.factory(Speck.LIGHT), 0.05f, 7);

            Sample.INSTANCE.play(Assets.SND_CLICK, 1, 1, 1.2f);
            updateShield(this);
        }

        @Override
        public void update() {
            super.update();

            if (brightness < 1.0f && brightness > MIN_BRIGHTNESS) {
                if ((brightness -= Game.elapsed) <= MIN_BRIGHTNESS) {
                    brightness = MIN_BRIGHTNESS;
                }
                updateBrightness();
            }
        }

        public void highlight(boolean value) {
            if (value) {
                brightness = 1.0f;
                name.hardlight(highlighted);

            } else {
                brightness = 0.999f;
                name.hardlight(normal);
            }

            updateBrightness();
        }

        private void updateBrightness() {
            avatar.gm = avatar.bm = avatar.rm = avatar.am = brightness;
        }
    }

    private class ChallengeButton extends Button {

        private Image image;

        public ChallengeButton() {
            super();

            width = image.width;
            height = image.height;

            image.am = Badges.isUnlocked(Badges.Badge.VICTORY) ? 1.0f : 0.5f;
        }

        @Override
        protected void createChildren() {

            super.createChildren();

            image = Icons
                    .get(PixelDungeon.challenges() > 0 ? Icons.CHALLENGE_ON
                            : Icons.CHALLENGE_OFF);
            add(image);
        }

        @Override
        protected void layout() {

            super.layout();

            image.x = align(x);
            image.y = align(y);
        }

        @Override
        protected void onClick() {
            if (Badges.isUnlocked(Badges.Badge.VICTORY)) {
                StartScene.this.add(new WndChallenges(
                        PixelDungeon.challenges(), true) {
                    public void onBackPressed() {
                        super.onBackPressed();
                        image.copy(Icons.get(PixelDungeon.challenges() > 0 ? Icons.CHALLENGE_ON
                                : Icons.CHALLENGE_OFF));
                    }
                });
            } else {
                StartScene.this.add(new WndMessage(Game
                        .getVar(R.string.StartScene_WinGame)));
            }
        }

        @Override
        protected void onTouchDown() {
            Sample.INSTANCE.play(Assets.SND_CLICK);
        }
    }
}
