package com.watabou.pixeldungeon.windows;

import com.nyrds.android.util.DownloadStateListener;
import com.nyrds.android.util.DownloadTask;
import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.ModdingMode;
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
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WndModSelect extends Window implements DownloadStateListener {

	private static ArrayList<String> mMods = new ArrayList<>();

	private static final int WIDTH = 120;
	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 20;

	private Text downloadProgress;

	static private class ModDesc {
		public String name;
		public String link;
	}

	private String selectedMod;
	private String downloadTo;

	private static Map<String, ModDesc> mModsMap = new HashMap<>();

	public WndModSelect() {
		Text tfTitle = PixelScene.createMultiline(Game.getVar(R.string.ModsButton_SelectMod), 9);
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.x = tfTitle.y = MARGIN;
		tfTitle.maxWidth(WIDTH - MARGIN * 2);
		tfTitle.measure();
		add(tfTitle);

		float pos = tfTitle.y + tfTitle.height() + MARGIN;

		if(!Util.isConnectedToInternet()) {
			tfTitle.text("Please enable Internet access to download mods");
			tfTitle.measure();
			pos = tfTitle.y + tfTitle.height() + MARGIN;
			resize(WIDTH, (int) pos);
			return;
		}

		ArrayList<String> options = buildModsList();

		for (int i = 0; i < options.size(); i++) {
			final int index = i;
			String option = options.get(index);

			float additionalMargin = 0;

			if (!option.equals(ModdingMode.REMIXED) && !option.contains("Download")) {

				SimpleButton deleteBtn = new SimpleButton(Icons.get(Icons.CLOSE)) {
					protected void onClick() {
						onDelete(index);
					}
				};
				deleteBtn.setPos(WIDTH - deleteBtn.width() - MARGIN, pos);
				additionalMargin = deleteBtn.width() + MARGIN;
				add(deleteBtn);
			}

			RedButton btn = new RedButton(options.get(index)) {

				@Override
				protected void onClick() {
					hide();
					onSelect(index);
				}

			};
			btn.setRect(MARGIN, pos, WIDTH - MARGIN * 2 - additionalMargin, BUTTON_HEIGHT);
			add(btn);

			pos += BUTTON_HEIGHT + MARGIN;
		}

		resize(WIDTH, (int) pos);
	}

	private static ArrayList<String> buildModsList() {
		mModsMap.clear();
		mMods.clear();

		File[] extList = FileSystem.listExternalStorage();
		final ArrayList<String> mods = new ArrayList<>();

		String[] knownMods = Game.getVars(R.array.known_mods);

		for (String knownMod : knownMods) {

			try {
				JSONArray modDesc = new JSONArray(knownMod);
				ModDesc desc = new ModDesc();
				desc.name = modDesc.getString(0);

				// TODO check versions...
				if (FileSystem.getExternalStorageFile(desc.name).exists()) {
					continue;
				}

				desc.link = modDesc.getString(1);

				String option = "Download " + desc.name;
				mModsMap.put(option, desc);
				mods.add(option);

			} catch (JSONException e) {
				GLog.w(e.getMessage());
			}
		}

		mods.add(ModdingMode.REMIXED);

		for (File file : extList) {
			if (file.isDirectory()) {
				mods.add(file.getName());
			}
		}
		mMods = mods;
		return mods;
	}

	protected void onDelete(int index) {
		String option = mMods.get(index);

		File modDir = FileSystem.getExternalStorageFile(option);

		if (modDir.exists()) {
			FileSystem.deleteRecursive(modDir);
		}

		PixelDungeon.activeMod(ModdingMode.REMIXED);
		if (getParent() != null) {
			hide();
		}
		Game.scene().add(new WndModDescription(ModdingMode.REMIXED));
	}

	protected void onSelect(int index) {
		String option = mMods.get(index);

		File modDir = FileSystem.getExternalStorageFile(option);

		if (!modDir.exists() && !option.equals(ModdingMode.REMIXED)) {
			selectedMod = mModsMap.get(option).name;
			downloadTo = FileSystem.getExternalStorageFile(selectedMod + ".zip").getAbsolutePath();

			new DownloadTask(this).execute(mModsMap.get(option).link, downloadTo);
			return;
		}
		
		String prevMod = PixelDungeon.activeMod();
		
		if (option.equals(prevMod)) {
			return;
		}
		
		SaveUtils.copyAllClassesToSlot(prevMod);
		SaveUtils.deleteGameAllClasses();
		SaveUtils.copyAllClassesFromSlot(option);
		
		if (getParent() != null) {
			hide();
		}
		Game.scene().add(new WndModDescription(option));
	}

	@Override
	public void DownloadProgress(String file, final Integer percent) {
		Game.executeInGlThread(new Runnable() {

			@Override
			public void run() {
				if (downloadProgress == null) {
					downloadProgress = GameScene.createMultiline(8);
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