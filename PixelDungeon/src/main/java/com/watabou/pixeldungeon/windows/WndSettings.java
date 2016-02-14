/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
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
package com.watabou.pixeldungeon.windows;

import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.PixelDungeon;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.CheckBox;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndSettings extends Window {

	private static final String TXT_ZOOM_IN = Game
			.getVar(R.string.WndSettings_ZoomIn);
	private static final String TXT_ZOOM_OUT = Game
			.getVar(R.string.WndSettings_ZoomOut);
	private static final String TXT_ZOOM_DEFAULT = Game
			.getVar(R.string.WndSettings_ZoomDef);
	
	private static final String TXT_TEXT_SCALE_DEFAULT = Game
			.getVar(R.string.WndSettings_TextScaleDefault);

	private static final String TXT_SCALE_UP = Game
			.getVar(R.string.WndSettings_ScaleUp);
	private static final String TXT_IMMERSIVE = Game
			.getVar(R.string.WndSettings_Immersive);

	private static final String TXT_MUSIC = Game
			.getVar(R.string.WndSettings_Music);

	private static final String TXT_SOUND = Game
			.getVar(R.string.WndSettings_Sound);

	private static final String TXT_BRIGHTNESS = Game
			.getVar(R.string.WndSettings_Brightness);

	private static final String TXT_SWITCH_PORT = Game
			.getVar(R.string.WndSettings_SwitchPort);
	private static final String TXT_SWITCH_LAND = Game
			.getVar(R.string.WndSettings_SwitchLand);

	private static final String TXT_SElECT_LANGUAGE = Game
			.getVar(R.string.WndSettings_SelectLanguage);
	
	private static final String TXT_SECOND_QUICKSLOT = Game
			.getVar(R.string.WndSettings_SecondQuickslot);
	private static final String TXT_THIRD_QUICKSLOT = Game
			.getVar(R.string.WndSettings_ThirdQuickslot);

	private static final int WIDTH = 112;
	private static final int BTN_HEIGHT = 18;
	private static final int GAP = 1;
	
	private static final String TXT_EXPEREMENTAL_FONT = Game
			.getVar(R.string.WndSettings_ExperementalFont);
	private static final String TXT_CLASSIC_FONT = Game
			.getVar(R.string.WndSettings_ClassicFont);

	private RedButton btnZoomOut;
	private RedButton btnZoomIn;
	
	private RedButton btnStdFontScale;
	private RedButton btnScaleMinus;
	private RedButton btnScalePlus;

	RedButton btnFontMode;
	
	private boolean mInGame;

	
	public WndSettings(boolean inGame) {
		super();
		mInGame = inGame;

		float curY;
		
		if (mInGame) {
			curY = createZoomButtons();
		} else {

			CheckBox btnScaleUp = new CheckBox(TXT_SCALE_UP) {
				@Override
				protected void onClick() {
					super.onClick();
					PixelDungeon.scaleUp(checked());
				}
			};
			btnScaleUp.setRect(0, 0, WIDTH, BTN_HEIGHT);
			btnScaleUp.checked(PixelDungeon.scaleUp());
			add(btnScaleUp);

			CheckBox btnImmersive = new CheckBox(TXT_IMMERSIVE) {
				@Override
				protected void onClick() {
					super.onClick();
					PixelDungeon.immerse(checked());
				}
			};
			btnImmersive.setRect(0, btnScaleUp.bottom() + GAP, WIDTH,
					BTN_HEIGHT);
			btnImmersive.checked(PixelDungeon.immersed());
			btnImmersive.enable(android.os.Build.VERSION.SDK_INT >= 19);
			add(btnImmersive);
			curY = btnImmersive.bottom();
			
			curY = createTextScaleButtons(curY + GAP);
		}
		curY += GAP;
		
		CheckBox btnMusic = new CheckBox(TXT_MUSIC) {
			@Override
			protected void onClick() {
				super.onClick();
				PixelDungeon.music(checked());
			}
		};
		btnMusic.setRect(0, curY, WIDTH, BTN_HEIGHT);
		btnMusic.checked(PixelDungeon.music());
		add(btnMusic);

		CheckBox btnSound = new CheckBox(TXT_SOUND) {
			@Override
			protected void onClick() {
				super.onClick();
				PixelDungeon.soundFx(checked());
				Sample.INSTANCE.play(Assets.SND_CLICK);
			}
		};
		btnSound.setRect(0, btnMusic.bottom() + GAP, WIDTH, BTN_HEIGHT);
		btnSound.checked(PixelDungeon.soundFx());
		add(btnSound);

		if (!mInGame) {

			RedButton btnOrientation = new RedButton(orientationText()) {
				@Override
				protected void onClick() {
					PixelDungeon.landscape(!PixelDungeon.landscape());
				}
			};
			btnOrientation.setRect(0, btnSound.bottom() + GAP, WIDTH,
					BTN_HEIGHT);
			add(btnOrientation);
			
			CheckBox btnRealtime = new CheckBox("Realtime!") {
				@Override
				protected void onClick() {
					super.onClick();
					PixelDungeon.realtime(checked());
				}
			};
			btnRealtime.setRect(0, btnOrientation.bottom() + GAP, WIDTH,
					BTN_HEIGHT);
			btnRealtime.checked(PixelDungeon.realtime());
			if(!PixelDungeon.isAlpha()) {
				btnRealtime.enable(false);
			}
			add(btnRealtime);
			
			RedButton localeButton = new RedButton(TXT_SElECT_LANGUAGE) {
				@Override
				protected void onClick() {
					PixelDungeon.scene().add(
							new WndOptions(TXT_SElECT_LANGUAGE, "",true, "English",
									"Русский", "Français", "Polski", "Español","한국말","Português brasileiro") {
								
								@Override
								protected void onSelect(int index) {
									String lang[] = { "en", "ru", "fr", "pl", "es","ko","pt_BR"};
									if(lang[index].equals("ko")) {
										PixelDungeon.classicFont(false);
									}
									PixelDungeon.uiLanguage(lang[index]);
									
								}
							});
				}
			};
			
			localeButton.setRect(0, btnRealtime.bottom() + GAP, WIDTH,
					BTN_HEIGHT);
			add(localeButton);

			float y = createFontSelector(localeButton.bottom() + GAP);
			
			resize(WIDTH, (int) y);
		} else {

			CheckBox btnBrightness = new CheckBox(TXT_BRIGHTNESS) {
				@Override
				protected void onClick() {
					super.onClick();
					PixelDungeon.brightness(checked());
				}
			};
			btnBrightness
					.setRect(0, btnSound.bottom() + GAP, WIDTH, BTN_HEIGHT);
			btnBrightness.checked(PixelDungeon.brightness());
			add(btnBrightness);

			final CheckBox secondQuickslot = new CheckBox(TXT_SECOND_QUICKSLOT) {
				@Override
				protected void onClick() {
					super.onClick();
					PixelDungeon.secondQuickslot(checked());
				}
			};
			secondQuickslot.setRect(0, btnBrightness.bottom() + GAP, WIDTH,
					BTN_HEIGHT);
			secondQuickslot.checked(PixelDungeon.secondQuickslot());

			add(secondQuickslot);

			if (PixelDungeon.landscape()) {
				CheckBox thirdQuickslot = new CheckBox(TXT_THIRD_QUICKSLOT) {
					@Override
					protected void onClick() {
						super.onClick();
						secondQuickslot.enable(!checked());
						PixelDungeon.thirdQuickslot(checked());
					}
				};

				secondQuickslot.enable(!PixelDungeon.thirdQuickslot());
				thirdQuickslot.setRect(0, secondQuickslot.bottom() + GAP,
						WIDTH, BTN_HEIGHT);
				thirdQuickslot.checked(PixelDungeon.thirdQuickslot());
				add(thirdQuickslot);

				resize(WIDTH, (int) thirdQuickslot.bottom());
			} else {
				if(PixelDungeon.thirdQuickslot()){
					PixelDungeon.secondQuickslot(true);
					secondQuickslot.checked(PixelDungeon.secondQuickslot());
				}
				resize(WIDTH, (int) secondQuickslot.bottom());
			}
		}

	}
	
	private float createZoomButtons() {
		int w = BTN_HEIGHT;

		btnZoomOut = new RedButton(TXT_ZOOM_OUT) {
			@Override
			protected void onClick() {
				zoom(Camera.main.zoom - 1);
			}
		};
		add(btnZoomOut.setRect(0, 0, w, BTN_HEIGHT));

		btnZoomIn = new RedButton(TXT_ZOOM_IN) {
			@Override
			protected void onClick() {
				zoom(Camera.main.zoom + 1);
			}
		};
		add(btnZoomIn.setRect(WIDTH - w, 0, w, BTN_HEIGHT));

		add(new RedButton(TXT_ZOOM_DEFAULT) {
			@Override
			protected void onClick() {
				zoom(PixelScene.defaultZoom);
			}
		}.setRect(btnZoomOut.right(), 0, WIDTH - btnZoomIn.width()
				- btnZoomOut.width(), BTN_HEIGHT));
		
		return btnZoomIn.bottom();
	}
	
	private float createFontSelector(float y) {
		remove(btnFontMode);
		
		String text;
		
		if(PixelDungeon.classicFont()) {
			text = TXT_EXPEREMENTAL_FONT;
			
			btnStdFontScale.enable(false);
			btnScaleMinus.enable(false);
			btnScalePlus.enable(false);
			
		} else {
			text = TXT_CLASSIC_FONT;
			
			btnStdFontScale.enable(true);
			btnScaleMinus.enable(true);
			btnScalePlus.enable(true);
		}
		
		btnFontMode = new RedButton(text) {
			@Override
			protected void onClick() {
				PixelDungeon.classicFont(!PixelDungeon.classicFont());
				createFontSelector(y);
			}
		};
		
		if(PixelDungeon.uiLanguage().equals("ko") ) {
			btnFontMode.enable(false);
		}
		
		btnFontMode.setRect(0, y, WIDTH,
				BTN_HEIGHT);
		add(btnFontMode);
		
		return btnFontMode.bottom();
	}
	
	private float createTextScaleButtons(float y) {
		int w = BTN_HEIGHT;
		
		remove(btnScaleMinus);
		remove(btnScalePlus);
		remove(btnStdFontScale);
		
		btnScaleMinus = new RedButton(TXT_ZOOM_OUT) {
			@Override
			protected void onClick() {
				PixelDungeon.fontScale(PixelDungeon.fontScale()-1);
				createTextScaleButtons(y);
			}
		};
		add(btnScaleMinus.setRect(0, y, w, BTN_HEIGHT));

		btnScalePlus = new RedButton(TXT_ZOOM_IN) {
			@Override
			protected void onClick() {
				PixelDungeon.fontScale(PixelDungeon.fontScale()+1);
				createTextScaleButtons(y);
			}
		};
		add(btnScalePlus.setRect(WIDTH - w, y, w, BTN_HEIGHT));
		
		btnStdFontScale = new RedButton(TXT_TEXT_SCALE_DEFAULT) {
			@Override
			protected void onClick() {
				PixelDungeon.fontScale(0);
				createTextScaleButtons(y);
			}
		};
		btnStdFontScale.setRect(btnScaleMinus.right(), y, WIDTH - btnScalePlus.width()
				- btnScaleMinus.width(), BTN_HEIGHT);
		add(btnStdFontScale);
		
		return btnScaleMinus.bottom();
	}
	
	@Override
	public void onBackPressed() {
		hide();
		if(!mInGame) {
			PixelDungeon.resetScene();
		}
	}
	
	private void zoom(float value) {

		Camera.main.zoom(value);
		PixelDungeon.zoom((int) (value - PixelScene.defaultZoom));

		updateEnabled();
	}

	private void updateEnabled() {
		float zoom = Camera.main.zoom;
		btnZoomIn.enable(zoom < PixelScene.maxZoom);
		btnZoomOut.enable(zoom > PixelScene.minZoom);
	}

	private String orientationText() {
		return PixelDungeon.landscape() ? TXT_SWITCH_PORT : TXT_SWITCH_LAND;
	}
}
