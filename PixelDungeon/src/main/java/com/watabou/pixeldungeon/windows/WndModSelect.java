package com.watabou.pixeldungeon.windows;

import com.nyrds.android.util.DownloadStateListener;
import com.nyrds.android.util.DownloadTask;
import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.GuiProperties;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.Mods;
import com.nyrds.android.util.Unzip;
import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.SaveUtils;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.SimpleButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

import java.io.File;
import java.util.Map;

public class WndModSelect extends Window implements DownloadStateListener {

	private static final int WIDTH         = 120;
	private static final int MARGIN        = 2;
	private static final int BUTTON_HEIGHT = 20;

	private Text downloadProgress;

	private String selectedMod;
	private String downloadTo;

	private Map<String, Mods.ModDesc> modsList = Mods.buildModsList();

	public WndModSelect() {
		Text tfTitle = PixelScene.createMultiline(Game.getVar(R.string.ModsButton_SelectMod), GuiProperties.titleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.x = tfTitle.y = MARGIN;
		tfTitle.maxWidth(WIDTH - MARGIN * 2);
		tfTitle.measure();
		add(tfTitle);

		float pos = tfTitle.y + tfTitle.height() + MARGIN;

		int index = 0;
		for (Map.Entry<String, Mods.ModDesc> entry : modsList.entrySet()) {
			final Mods.ModDesc desc = entry.getValue();
			float additionalMargin = 0;

			if (desc.installed) {

				SimpleButton deleteBtn = new SimpleButton(Icons.get(Icons.CLOSE)) {
					protected void onClick() {
						onDelete(desc.name);
					}
				};
				deleteBtn.setPos(WIDTH - deleteBtn.width() - MARGIN, pos);
				additionalMargin = deleteBtn.width() + MARGIN;
				add(deleteBtn);
			}

			String option = desc.name;
			if(desc.needUpdate) {
				option = "Update " + option;
			}

			RedButton btn = new RedButton(option) {

				@Override
				protected void onClick() {
					hide();
					onSelect(desc.name);
				}

			};

			btn.setRect(MARGIN, pos, WIDTH - MARGIN * 2 - additionalMargin, BUTTON_HEIGHT);
			add(btn);

			pos += BUTTON_HEIGHT + MARGIN;


			index = index + 1;
		}

		resize(WIDTH, (int) pos);
	}

	private void onDelete(String name) {

		File modDir = FileSystem.getExternalStorageFile(name);

		if (modDir.exists() && modDir.isDirectory()) {
			FileSystem.deleteRecursive(modDir);
		}

		if(PixelDungeon.activeMod().equals(name)) {
			SaveUtils.deleteGameAllClasses();
			SaveUtils.copyAllClassesFromSlot(ModdingMode.REMIXED);
			PixelDungeon.activeMod(ModdingMode.REMIXED);
		}

		if (getParent() != null) {
			hide();
		}
		Game.scene().add(new WndModSelect());
	}

	protected void onSelect(String option) {

		Mods.ModDesc desc = modsList.get(option);
		if (!option.equals(ModdingMode.REMIXED) || desc.needUpdate) {
			if (!Util.isConnectedToInternet()) {
				PixelDungeon.scene().add(new WndError("Please enable Internet access to download or update mods"));
				return;
			}

			if(desc.needUpdate) {
				FileSystem.deleteRecursive(FileSystem.getExternalStorageFile(desc.name));
				selectedMod = desc.name;
				downloadTo = FileSystem.getExternalStorageFile(selectedMod + ".zip").getAbsolutePath();
				desc.needUpdate = false;
				new DownloadTask(this).execute(desc.url, downloadTo);
				return;
			}
		}

		String prevMod = PixelDungeon.activeMod();

		if (option.equals(prevMod)) {
			return;
		}

		if (getParent() != null) {
			hide();
		}
		Game.scene().add(new WndModDescription(option,prevMod));
	}

	@Override
	public void DownloadProgress(String file, final Integer percent) {
		Game.executeInGlThread(new Runnable() {

			@Override
			public void run() {
				if (downloadProgress == null) {
					downloadProgress = GameScene.createMultiline(GuiProperties.regularFontSize());
					downloadProgress.maxWidth(WIDTH);
					downloadProgress.setPos(0, 0);
					Game.scene().add(downloadProgress);
				}

				downloadProgress.text(Utils.format("Downloading %s %d%%", selectedMod, percent));
			}
		});
	}

	@Override
	public void DownloadComplete(String url, final Boolean result) {
		Game.executeInGlThread(new Runnable() {
			@Override
			public void run() {
				if (downloadProgress != null) {
					Game.scene().remove(downloadProgress);
					downloadProgress = null;
				}

				if (result) {

					String tmpDirName = "tmp";

					File tmpDirFile = FileSystem.getExternalStorageFile(tmpDirName);
					if (tmpDirFile.exists()) {
						tmpDirFile.delete();
					}

					if (Unzip.unzip(downloadTo, FileSystem.getExternalStorageFile(tmpDirName).getAbsolutePath())) {

						File[] unpackedList = tmpDirFile.listFiles();

						for (File file : unpackedList) {
							if (file.isDirectory()) {

								String modDir = downloadTo.substring(0, downloadTo.length() - 4);

								if (file.renameTo(new File(modDir))) {
									FileSystem.deleteRecursive(tmpDirFile);
									FileSystem.deleteRecursive(new File(downloadTo));
									break;
								} else {
									Game.scene()
											.add(new WndError(Utils.format(
													"Something gone wrong when placing mod in %s, please do so manually",
													modDir)));
								}
							}
						}
						Game.scene().add(new WndModSelect());
					} else {
						Game.scene().add(new WndError(Utils.format("unzipping %s failed", downloadTo)));
					}
				} else {
					Game.scene().add(new WndError(Utils.format("Downloading %s failed", selectedMod)));

				}
			}
		});
	}
}