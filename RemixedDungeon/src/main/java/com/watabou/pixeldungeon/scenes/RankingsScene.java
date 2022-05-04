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

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Music;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Rankings;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.ui.Archs;
import com.watabou.pixeldungeon.ui.ExitButton;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.windows.WndError;
import com.watabou.pixeldungeon.windows.WndRanking;

import java.util.ArrayList;

public class RankingsScene extends PixelScene {

    private static final int DEFAULT_COLOR = 0xCCCCCC;
    private static final int HAPPY_COLOR   = 0x1CCC1C;

    private static final int recordsPerPage = 5;

    private static final float ROW_HEIGHT_L = 22;
    private static final float ROW_HEIGHT_P = 28;

    private static final float MAX_ROW_WIDTH = 180;

    private static final float GAP = 4;

    private int startFrom;

    private final ArrayList<Record> displayedRecords = new ArrayList<>();

    @Override
    public void create() {
        super.create();

        Music.INSTANCE.play(Assets.THEME, true);
        Music.INSTANCE.volume(1f);

        uiCamera.setVisible(false);

        int w = Camera.main.width;
        int h = Camera.main.height;

        Archs archs = new Archs();
        archs.setSize(w, h);
        add(archs);

        Rankings.INSTANCE.load();

        if (Rankings.INSTANCE.records.size() > 0) {

            float rowHeight = RemixedDungeon.landscape() ? ROW_HEIGHT_L : ROW_HEIGHT_P;

            float top = align(rowHeight / 2);

            Text title = PixelScene.createText(StringsManager.getVar(R.string.RankingsScene_Title), GuiProperties.titleFontSize());
            title.hardlight(Window.TITLE_COLOR);
            title.setX(align((w - title.width()) / 2));
            title.setY(align(top - title.height() - GAP));
            add(title);

            float btnHeight = rowHeight / 2;

            RedButton btnNext = new RedButton(">") {
                @Override
                protected void onClick() {
                    super.onClick();
                    startFrom += recordsPerPage;

                    if (startFrom > Rankings.TABLE_SIZE - recordsPerPage) {
                        startFrom = Rankings.TABLE_SIZE - recordsPerPage;
                    }

                    if (startFrom > Rankings.INSTANCE.records.size()) {
                        startFrom -= recordsPerPage;
                    }

                    switch (GamePreferences.donated()) {
                        case 0:
                            if (startFrom > 10) {
                                startFrom = 10;
                            }
                            break;

                        case 1:
                            if (startFrom > 25) {
                                startFrom = 25;
                            }
                            break;

                        case 2:
                            if (startFrom > 50) {
                                startFrom = 50;
                            }
                            break;
                    }

                    createRecords();
                }
            };
            btnNext.setRect(w / 2 + GAP, h - btnHeight, w / 2 - GAP, btnHeight);
            add(btnNext);

            RedButton btnPrev = new RedButton("<") {
                @Override
                protected void onClick() {
                    super.onClick();
                    startFrom -= recordsPerPage;
                    if (startFrom < 0) {
                        startFrom = 0;
                    }
                    createRecords();
                }
            };

            btnPrev.setRect(0, h - btnHeight, w / 2 - GAP, btnHeight);
            add(btnPrev);

            createRecords();

            Text label = PixelScene.createText(StringsManager.getVar(R.string.RankingsScene_Total), GuiProperties.titleFontSize());
            label.hardlight(DEFAULT_COLOR);
            add(label);

            Text happy = PixelScene.createText(Integer.toString(Rankings.INSTANCE.happyWonNumber), GuiProperties.titleFontSize());
            happy.hardlight(HAPPY_COLOR);
            add(happy);

            Text won = PixelScene.createText("/" + Rankings.INSTANCE.wonNumber, GuiProperties.titleFontSize());
            won.hardlight(Window.TITLE_COLOR);
            add(won);

            Text total = PixelScene.createText("/" + Rankings.INSTANCE.totalNumber, GuiProperties.titleFontSize());
            total.hardlight(DEFAULT_COLOR);
            total.setX(align((w - total.width()) / 2));
            float ypos = align(top + recordsPerPage * rowHeight + GAP);
            total.setY(ypos);
            add(total);

            float tw = label.width() + won.width() + happy.width() + total.width();
            label.setX(align((w - tw) / 2));
            happy.setX(label.getX() + label.width());
            won.setX(happy.getX() + happy.width());
            total.setX(won.getX() + won.width());

            total.setY(ypos);
            happy.setY(ypos);
            label.setY(ypos);
            won.setY(ypos);


        } else {

            Text title = PixelScene.createText(StringsManager.getVar(R.string.RankingsScene_NoGames), GuiProperties.titleFontSize());
            title.hardlight(DEFAULT_COLOR);
            title.setX(align((w - title.width()) / 2));
            title.setY(align((h - title.height()) / 2));
            add(title);

        }

        ExitButton btnExit = new ExitButton();
        btnExit.setPos(Camera.main.width - btnExit.width(), 0);
        add(btnExit);

        fadeIn();
    }

    private void createRecords() {
        int w = Camera.main.width;

        float rowHeight = RemixedDungeon.landscape() ? ROW_HEIGHT_L : ROW_HEIGHT_P;

        float left = (w - Math.min(MAX_ROW_WIDTH, w)) / 2 + GAP;
        float top = align(rowHeight / 2);

        int pos = 0;

        for (Record row : displayedRecords) {
            remove(row);
        }

        displayedRecords.clear();

        for (int i = startFrom; i < startFrom + recordsPerPage; ++i) {
            if (i > Rankings.INSTANCE.records.size() - 1) {
                break;
            }
            Rankings.Record rec = Rankings.INSTANCE.records.get(i);

            Record row = new Record(i, i == Rankings.INSTANCE.lastRecord, rec);
            row.setRect(left, top + pos * rowHeight, w - left * 2, rowHeight);
            displayedRecords.add(row);
            add(row);

            pos++;
        }
    }

    @Override
    protected void onBackPressed() {
        GameLoop.switchNoFade(TitleScene.class);
    }

    public static class Record extends Button {

        private static final float GAP = 4;

        private static final int TEXT_WIN   = 0xFFFF88;
        private static final int TEXT_LOSE  = 0xCCCCCC;
        private static final int FLARE_WIN  = 0x888866;
        private static final int FLARE_LOSE = 0x666666;

        private Rankings.Record rec;

        private ItemSprite shield;
        private Flare      flare;
        private BitmapText position;
        private Text       desc;
        private Image      classIcon;

        public Record(int pos, boolean latest, Rankings.Record rec) {
            super();

            this.rec = rec;

            if (latest) {
                flare = new Flare(6, 24);
                flare.angularSpeed = 90;
                flare.color(rec.win ? FLARE_WIN : FLARE_LOSE);
                addToBack(flare);
            }

            position.text(Integer.toString(pos + 1));

            desc.text(rec.mod + ": " + rec.info);

            if (rec.win) {
                shield.view(Assets.ITEMS, ItemSpriteSheet.AMULET, null);
                position.hardlight(TEXT_WIN);
                desc.hardlight(TEXT_WIN);
            } else {
                position.hardlight(TEXT_LOSE);
                desc.hardlight(TEXT_LOSE);
            }

            classIcon.copy(Icons.get(rec.heroClass));
        }

        @Override
        protected void createChildren() {

            super.createChildren();

            shield = new ItemSprite(Assets.ITEMS, ItemSpriteSheet.TOMB, null);
            add(shield);

            position = new BitmapText(PixelScene.font1x);
            add(position);

            desc = createMultiline(GuiProperties.regularFontSize());
            add(desc);

            classIcon = new Image();
            add(classIcon);
        }

        @Override
        protected void layout() {

            super.layout();

            shield.setX(x);
            shield.setY(y + (height - shield.height) / 2);

            position.setX(align(shield.getX() + (shield.width - position.width()) / 2));
            position.setY(align(shield.getY() + (shield.height - position.height()) / 2 + 1));

            if (flare != null) {
                flare.point(shield.center());
            }

            classIcon.setX(align(x + width - classIcon.width));
            classIcon.setY(shield.getY());

            desc.setX(shield.getX() + shield.width + GAP);
            desc.maxWidth((int) (classIcon.getX() - desc.getX()));
            desc.setY(position.getY() + position.baseLine() - desc.baseLine());
        }

        @Override
        protected void onClick() {
            if (rec.gameFile.length() > 0) {
                getParent().add(new WndRanking(rec.gameFile));
            } else {
                getParent().add(new WndError(StringsManager.getVar(R.string.RankingsScene_NoInfo)));
            }
        }
    }
}
