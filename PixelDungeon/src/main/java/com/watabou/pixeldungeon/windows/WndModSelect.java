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
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.DownloadProgressWindow;
import com.nyrds.pixeldungeon.windows.ScrollableList;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
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

public class WndModSelect extends Window implements DownloadStateListener.IDownloadComplete, UnzipStateListener {

	private String selectedMod;
	private String downloadTo;

	private Map<String, Mods.ModDesc> modsList;

	public WndModSelect() {
		super();

		resizeLimited(120);

		modsList = Mods.buildModsList();

		boolean haveInternet = Util.isConnectedToInternet();

		Text tfTitle = PixelScene.createMultiline(Game.getVar(R.string.ModsButton_SelectMod), GuiProperties.titleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.x = tfTitle.y = GAP;
		tfTitle.maxWidth(width - GAP * 2);
		add(tfTitle);

		ScrollableList list = new ScrollableList(new Component());
		add(list);

        float pos = 0;
		for (Map.Entry<String, Mods.ModDesc> entry : modsList.entrySet()) {
			final Mods.ModDesc desc = entry.getValue();
			float additionalMargin = Icons.get(Icons.CLOSE).width() + GAP;

			if (desc.installed && !ModdingMode.REMIXED.equals(desc.name)) {
				SimpleButton deleteBtn = new SimpleButton(Icons.get(Icons.CLOSE)) {
					protected void onClick() {
						onDelete(desc.name);
					}
				};
				deleteBtn.setPos(width - deleteBtn.width() - GAP, pos + (BUTTON_HEIGHT - deleteBtn.height())/2);
				list.content().add(deleteBtn);
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
                list.content().add(btn);

				pos += BUTTON_HEIGHT;
			}
		}

		if(pos + SMALL_GAP + tfTitle.height() + GAP < height ) {
			resize(120, (int) (pos + SMALL_GAP + tfTitle.height() + GAP));
		}

		list.content().setSize(width, pos);
		list.setRect(0, tfTitle.height() + GAP, width, height - tfTitle.height() - GAP);
		list.scrollTo(0,0);
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

				new DownloadTask(new DownloadProgressWindow(Utils.format("Downloading %s", selectedMod),this)).download(desc.url, downloadTo);

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
	public void DownloadComplete(String url, final Boolean result) {
		Game.pushUiTask(new Runnable() {
			@Override
			public void run() {
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
		Game.pushUiTask(new Runnable() {

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