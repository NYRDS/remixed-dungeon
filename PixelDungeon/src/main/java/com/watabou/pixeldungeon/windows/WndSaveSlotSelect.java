package com.watabou.pixeldungeon.windows;

import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.GuiProperties;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.retrodungeon.ml.R;
import com.nyrds.retrodungeon.support.Ads;
import com.nyrds.retrodungeon.support.PlayGames;
import com.nyrds.retrodungeon.windows.WndHelper;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.SaveUtils;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.DonateButton;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.SimpleButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

public class WndSaveSlotSelect extends Window implements InterstitialPoint {
	private static final String EMPTY_STRING = "";

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
		tfTitle.measure();
		add(tfTitle);

		if(!_saving && PlayGames.isConnected()) {
			SimpleButton refreshBtn = new SimpleButton(Icons.get(Icons.BTN_SYNC_REFRESH)) {
				@Override
				protected void onClick() {
					final Window refreshing = new WndMessage("Please wait a bit...")  {
						@Override
						public void onBackPressed() {
						}
					};

					Game.scene().add(refreshing);
					PlayGames.loadSnapshots(new Runnable() {
						@Override
						public void run() {
							Game.executeInGlThread(new Runnable() {
								@Override
								public void run() {
									refreshing.hide();
									refreshWindow();
								}
							});
						}
					});
				}
			};
			refreshBtn.setPos(WIDTH - refreshBtn.width() - GAP * 2, tfTitle.y);
			add(refreshBtn);
		}


		Text tfMesage = PixelScene.createMultiline(windowText(), GuiProperties.regularFontSize());
		tfMesage.maxWidth(maxW);
		tfMesage.measure();
		tfMesage.x = GAP;
		tfMesage.y = tfTitle.y + tfTitle.height() + GAP;
		add(tfMesage);

		float pos = tfMesage.y + tfMesage.height() + GAP;

		ArrayList<TextButton> buttons = new ArrayList<>();


		final int columns = PixelDungeon.landscape() ? 3 : 2;
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
						hide();
						onSelect(index);
					}
				};
				buttons.add(btn);

				if (PlayGames.isConnected()) {
					final String snapshotId = slotNameFromIndexAndMod(index) + "_" + Dungeon.hero.heroClass.toString();

					if ((_saving && !options[index].isEmpty())
							|| (!_saving
							&& PlayGames.haveSnapshot(snapshotId)
					)) {

						Icons icon = _saving ? Icons.BTN_SYNC_OUT : Icons.BTN_SYNC_IN;
						SimpleButton syncBtn = new SimpleButton(Icons.get(icon)) {
							protected void onClick() {
								File slotDir = FileSystem.getInternalStorageFile(slotNameFromIndexAndMod(index));
								boolean res;
								if (_saving) {
									res = PlayGames.packFilesToSnapshot(snapshotId, slotDir, new FileFilter() {
										@Override
										public boolean accept(File pathname) {
											return SaveUtils.isRelatedTo(pathname.getPath(), Dungeon.hero.heroClass);
										}
									});
								} else {
									res = PlayGames.unpackSnapshotTo(snapshotId, slotDir);
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

				btn.setRect(xBtn, pos, BUTTON_WIDTH - additionalMargin - GAP * 2, BUTTON_HEIGHT);
				add(btn);
			}
			pos += BUTTON_HEIGHT + GAP;
		}

		resize(WIDTH, (int) pos);

		saving = _saving;

		if (!saving) {
			for (int i = 0; i < 10; i++) {
				if (!isSlotIndexUsed(i)) {
					buttons.get(i).enable(false);
				}
			}
		}

		if (PixelDungeon.donated() == 0 && PixelDungeon.canDonate()) {
			DonateButton btn = new DonateButton();
			add(btn);
			btn.setPos(width / 2 - btn.width() / 2, height);
			resize(width, (int) (height + btn.height()));
		}
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
		if (PixelDungeon.donated() == 0 && PixelDungeon.canDonate()) {
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

	public static boolean haveSomethingToLoad() {
		String slots[] = slotInfos();
		for (String slot : slots) {
			if (!slot.equals("")) {
				return true;
			}
		}

		return false;
	}

	private static String[] slotInfos() {
		String[] ret = new String[10];

		for (int i = 0; i < ret.length; i++) {
			ret[i] = SaveUtils.slotInfo(getSlotToLoad(i), Dungeon.heroClass);
		}

		return ret;
	}

	protected void onSelect(int index) {
		final InterstitialPoint returnTo = this;

		if (saving) {
			try {
				Dungeon.saveAll();
				slot = slotNameFromIndexAndMod(index);
				SaveUtils.copySaveToSlot(slot, Dungeon.heroClass);

			} catch (Exception e) {
				throw new TrackedRuntimeException(e);
			}
		}

		Game.paused = true;

		slot = getSlotToLoad(index);

		if (PixelDungeon.donated() < 1) {
			Ads.displaySaveAndLoadAd(returnTo);
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
		Game.executeInGlThread(new Runnable() {
			@Override
			public void run() {
				Game.paused = false;

				if (!saving) {
					SaveUtils.loadGame(slot, Dungeon.hero.heroClass);
				}
			}
		});
	}

}
