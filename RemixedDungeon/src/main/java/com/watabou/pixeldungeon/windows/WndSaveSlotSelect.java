package com.watabou.pixeldungeon.windows;

import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.GuiProperties;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.ml.RemixedDungeonApp;
import com.nyrds.pixeldungeon.support.Ads;
import com.nyrds.pixeldungeon.support.Iap;
import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.WndHelper;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.noosa.ReturnOnlyOnce;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.RemixedDungeon;
import com.watabou.pixeldungeon.SaveUtils;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.DonateButton;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.SimpleButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

import java.io.File;
import java.util.ArrayList;

public class WndSaveSlotSelect extends Window implements InterstitialPoint {
	private static final String EMPTY_STRING = "";
	private static final String AUTO_SAVE = "autoSave";

	private boolean saving;

	private String  slot;

	public WndSaveSlotSelect(final boolean _saving) {
		this(_saving, Game.getVar(R.string.WndSaveSlotSelect_SelectSlot));
	}

	public WndSaveSlotSelect(final boolean _saving, String title) {
		String options[] = slotInfos();

		final int WIDTH = WndHelper.getFullscreenWidth();
		final int maxW = WIDTH - GAP * 2;

		Text tfTitle = PixelScene.createMultiline(title, GuiProperties.titleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.x = tfTitle.y = GAP;
		tfTitle.maxWidth(maxW);
		add(tfTitle);

		if(!_saving && Game.instance().playGames.isConnected()) {
			SimpleButton refreshBtn = new SimpleButton(Icons.get(Icons.BTN_SYNC_REFRESH)) {
				@Override
				protected void onClick() {
					final Window refreshing = new WndMessage("Please wait a bit...")  {
						@Override
						public void onBackPressed() {
						}
					};

					Game.scene().add(refreshing);
					Game.instance().playGames.loadSnapshots(() -> Game.pushUiTask(() -> {
						refreshing.hide();
						refreshWindow();
					}));
				}
			};
			refreshBtn.setPos(WIDTH - refreshBtn.width() - GAP * 2, tfTitle.y);
			add(refreshBtn);
		}


		Text tfMesage = PixelScene.createMultiline(windowText(), GuiProperties.regularFontSize());
		tfMesage.maxWidth(maxW);
		tfMesage.x = GAP;
		tfMesage.y = tfTitle.y + tfTitle.height() + GAP;
		add(tfMesage);

		float pos = tfMesage.y + tfMesage.height() + GAP;

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
					final String snapshotId = slotNameFromIndexAndMod(index) + "_" + Dungeon.hero.heroClass.toString();

					if ((_saving && !options[index].isEmpty())
							|| (!_saving
							&& Game.instance().playGames.haveSnapshot(snapshotId)
					)) {

						Icons icon = _saving ? Icons.BTN_SYNC_OUT : Icons.BTN_SYNC_IN;
						SimpleButton syncBtn = new SimpleButton(Icons.get(icon)) {
							protected void onClick() {
								File slotDir = FileSystem.getInternalStorageFile(slotNameFromIndexAndMod(index));
								boolean res;
								if (_saving) {
									res = Game.instance().playGames.packFilesToSnapshot(snapshotId, slotDir, pathname -> SaveUtils.isRelatedTo(pathname.getPath(), Dungeon.hero.heroClass));
								} else {
									res = Game.instance().playGames.unpackSnapshotTo(snapshotId, slotDir);
								}
								refreshWindow();
								showActionResult(res);
							}
						};

						syncBtn.setPos(xColumn, pos + BUTTON_HEIGHT / 2);
						additionalMargin = syncBtn.width();
						add(syncBtn);

						xBtn = syncBtn.right() + GAP;
					}
				}

				if (!options[index].isEmpty()) {
					SimpleButton deleteBtn = new SimpleButton(Icons.get(Icons.CLOSE)) {
						protected void onClick() {
							final int slotIndex = index;
							WndOptions reallyDelete = new WndOptions(Game.getVar(R.string.WndSaveSlotSelect_Delete_Title), "",
									Game.getVar(R.string.WndSaveSlotSelect_Delete_Yes),
									Game.getVar(R.string.WndSaveSlotSelect_Delete_No)) {
								@Override
								protected void onSelect(int index) {
									if (index == 0) {
										SaveUtils.deleteSaveFromSlot(slotNameFromIndexAndMod(slotIndex), Dungeon.heroClass);
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
		bottomRow.setGap(2*GAP);

		if(!saving) {
            RedButton autoLoadButton = new RedButton(R.string.WndSaveSlotSelect_LoadAutoSave) {
                @Override
                protected void onClick() {
                    showAd(AUTO_SAVE);
                }
            };

            autoLoadButton.setSize(BUTTON_WIDTH - GAP, BUTTON_HEIGHT);

            bottomRow.add(autoLoadButton);
        }

		if (RemixedDungeon.donated() == 0 && RemixedDungeon.canDonate()) {
			DonateButton btn = new DonateButton(this);
			bottomRow.add(btn);
		}

		if(bottomRow.getLength()==1) {
			bottomRow.setAlign(HBox.Align.Center);
		}

		bottomRow.setPos(GAP, pos);
		add(bottomRow);

		resize(width, (int) (height + bottomRow.height()));
	}


	private void refreshWindow() {
		WndSaveSlotSelect.this.hide();
		GameScene.show(new WndSaveSlotSelect(saving));
	}

	private static boolean isSlotIndexUsed(int index) {
		return SaveUtils.slotUsed(slotNameFromIndex(index), Dungeon.heroClass)
				|| SaveUtils.slotUsed(slotNameFromIndexAndMod(index), Dungeon.heroClass);
	}

	private static String getSlotToLoad(int index) {
		String slot = slotNameFromIndexAndMod(index);
		if (SaveUtils.slotUsed(slot, Dungeon.heroClass)) {
			return slot;
		} else {
			return slotNameFromIndex(index);
		}
	}

	private static String windowText() {
		if (RemixedDungeon.donated() == 0 && RemixedDungeon.canDonate()) {
			return Game.getVar(R.string.WndSaveSlotSelect_dontLike);
		}
		return EMPTY_STRING;
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
			try {
				Dungeon.save();
				slot = slotNameFromIndexAndMod(index);
				SaveUtils.copySaveToSlot(slot, Dungeon.heroClass);

			} catch (Exception e) {
				EventCollector.logException(e, "bug in save");
				throw new TrackedRuntimeException(e);
			}
		}

		showAd(getSlotToLoad(index));
	}

	private void showAd(String slotName) {
		hide();
		slot = slotName;

		Game.softPaused = true;

		if (RemixedDungeon.donated() < 1) {
			Ads.displaySaveAndLoadAd(new ReturnOnlyOnce(this));
		} else {
			returnToWork(true);
		}
	}

	private void showActionResult(final boolean res) {
		if (res) {
			Game.scene().add(new WndMessage("ok!"));
		} else {
			Game.scene().add(new WndMessage("something went wrong..."));
		}
	}

	@Override
	public void returnToWork(boolean res) {
		Game.softPaused = false;

		Game.pushUiTask(() -> {
			if (!saving) {
				if(slot.equals(AUTO_SAVE)) {
					InterlevelScene.Do(InterlevelScene.Mode.CONTINUE);
				} else {
					SaveUtils.loadGame(slot, Dungeon.hero.heroClass);
				}
			} else {
                if(Math.random()<0.2)
                {
					if(RemixedDungeon.donated() == 0 && RemixedDungeon.canDonate()) {
						int group = RemixedDungeonApp.getExperimentSegment("hqSaveAdsExperiment", 2);
						if(group == 0) {
							return;
						}

                        Game.pushUiTask(() -> {
                        	Iap iap = Game.instance().iap;
                        	if(iap!=null && iap.isReady() || BuildConfig.DEBUG ) {
								EventCollector.logEvent(EventCollector.SAVE_ADS_EXPERIMENT,"DialogShown");
								Game.scene().add(new WndDontLikeAds());
							} else {
								EventCollector.logEvent(EventCollector.SAVE_ADS_EXPERIMENT,"DialogNotShownIapNotReady");
							}
                        });
                    }
                }
            }

		});
	}

}
