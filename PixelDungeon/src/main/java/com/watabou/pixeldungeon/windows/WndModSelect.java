package com.watabou.pixeldungeon.windows;

import android.os.Build;

import com.nyrds.android.util.DownloadStateListener;
import com.nyrds.android.util.DownloadTask;
import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.GuiProperties;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.Mods;
import com.nyrds.android.util.UnzipStateListener;
import com.nyrds.android.util.UnzipTask;
import com.nyrds.android.util.Util;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.SaveUtils;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.SimpleButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

import java.io.File;
import java.util.Map;

public class WndModSelect extends Window implements DownloadStateListener, UnzipStateListener {

	private WndMessage downloadProgress;

	private String selectedMod;
	private String downloadTo;

	private Map<String, Mods.ModDesc> modsList = Mods.buildModsList();

	public WndModSelect() {
		super();

		resizeLimited(120);

		boolean haveInternet = Util.isConnectedToInternet();

		Text tfTitle = PixelScene.createMultiline(Game.getVar(R.string.ModsButton_SelectMod), GuiProperties.titleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.x = tfTitle.y = GAP;
		tfTitle.maxWidth(width - GAP * 2);
		tfTitle.measure();
		add(tfTitle);

		float pos = tfTitle.y + tfTitle.height() + GAP;

		for (Map.Entry<String, Mods.ModDesc> entry : modsList.entrySet()) {
			final Mods.ModDesc desc = entry.getValue();
			float additionalMargin = 0;

			if (desc.installed && !ModdingMode.REMIXED.equals(desc.name)) {
				SimpleButton deleteBtn = new SimpleButton(Icons.get(Icons.CLOSE)) {
					protected void onClick() {
						onDelete(desc.name);
					}
				};
				deleteBtn.setPos(width - deleteBtn.width() - GAP, pos);
				additionalMargin = deleteBtn.width() + GAP;
				add(deleteBtn);
			}

			String option = desc.name;
			if (desc.needUpdate && haveInternet) {
				option = "Update " + option;
			}

			if (desc.installed || haveInternet) {
				RedButton btn = new RedButton(option) {

					@Override
					protected void onClick() {
						hide();
						onSelect(desc.name);
					}

				};

				btn.setRect(GAP, pos, width - GAP * 2 - additionalMargin, BUTTON_HEIGHT);
				add(btn);

				pos += BUTTON_HEIGHT + SMALL_GAP;
			}
		}

		resize(width, (int) pos);
	}

	private void onDelete(String name) {

		File modDir = FileSystem.getExternalStorageFile(name);

		if (modDir.exists() && modDir.isDirectory()) {
			FileSystem.deleteRecursive(modDir);
		}

		if (PixelDungeon.activeMod().equals(name)) {
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

			if (desc.needUpdate) {
				FileSystem.deleteRecursive(FileSystem.getExternalStorageFile(desc.name));
				selectedMod = desc.name;
				downloadTo = FileSystem.getExternalStorageFile(selectedMod + ".zip").getAbsolutePath();
				desc.needUpdate = false;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					new DownloadTask(this).executeOnExecutor(Game.instance().executor,desc.url, downloadTo);
				} else {
					new DownloadTask(this).execute(desc.url, downloadTo);
				}
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
		Game.scene().add(new WndModDescription(option, prevMod));
	}

	@Override
	public void DownloadProgress(String file, final Integer percent) {
		Game.executeInGlThread(new Runnable() {

			@Override
			public void run() {
				if (downloadProgress == null) {
					downloadProgress = new WndMessage("");
					Game.scene().add(downloadProgress);
				}
				if (!Game.isPaused()) {
					downloadProgress.setText(Utils.format("Downloading %s %d%%", selectedMod, percent));
				}
			}
		});
	}

	@Override
	public void DownloadComplete(String url, final Boolean result) {
		Game.executeInGlThread(new Runnable() {
			@Override
			public void run() {
				if (downloadProgress != null) {
					downloadProgress.hide();
					downloadProgress = null;
				}
				if (result) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						new UnzipTask(WndModSelect.this).executeOnExecutor(Game.instance().executor,downloadTo);
					} else {
						new UnzipTask(WndModSelect.this).execute(downloadTo);
					}
				} else {
					Game.scene().add(new WndError(Utils.format("Downloading %s failed", selectedMod)));
				}
			}
		});
	}

	@Override
	public void UnzipComplete(final Boolean result) {
		Game.executeInGlThread(new Runnable() {

			@Override
			public void run() {
				if (result) {
					Game.scene().add(new WndModSelect());
				} else {
					Game.scene().add(new WndError(Utils.format("unzipping %s failed", downloadTo)));
				}
			}
		});

	}
}