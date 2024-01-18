package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.ModDesc;
import com.nyrds.pixeldungeon.windows.DownloadProgressWindow;
import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.ScrollableList;
import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.pixeldungeon.windows.WndHelper;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.DownloadStateListener;
import com.nyrds.util.DownloadTask;
import com.nyrds.util.GuiProperties;
import com.nyrds.util.ModdingMode;
import com.nyrds.util.Mods;
import com.nyrds.util.UnzipStateListener;
import com.nyrds.util.UnzipTask;
import com.nyrds.util.Util;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
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

	private final Map<String, ModDesc> modsList;

	static private WndModSelect instance = null;

	public WndModSelect() {
		super();

		if(instance!=null) {
			instance.hide();
		}
		instance = this;

		resizeLimited(120);

		modsList = Mods.buildModsList();

		boolean haveInternet = Util.isConnectedToInternet();

        Text tfTitle = PixelScene.createMultiline(R.string.ModsButton_SelectMod, GuiProperties.titleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.setY(GAP);
		tfTitle.setX(GAP);
		tfTitle.maxWidth(width - GAP * 2);
		add(tfTitle);

		ScrollableList list = new ScrollableList(new Component());
		add(list);

        float pos = 0;

		VBox modsListContainer = new VBox();
		modsListContainer.setGap(GAP);

		for (Map.Entry<String, ModDesc> entry : modsList.entrySet()) {

			HBox modRow = new HBox(width - GAP * 2);
			modRow.setGap(GAP);

			final ModDesc desc = entry.getValue();
			float additionalMargin = Icons.get(Icons.CLOSE).width() + GAP;

			String option = desc.name;
			if (desc.needUpdate && haveInternet) {
				option = (desc.installed ? "Update " : "Install ") + option;
			}

			if (desc.installed || haveInternet) {
				RedButton btn = new RedButton(option) {
					@Override
					protected void onClick() {
						hide();
						onSelect(desc.installDir);
					}
				};

				btn.setSize(width - GAP * 2 - (additionalMargin * 2), BUTTON_HEIGHT);
				modRow.add(btn);
				modsListContainer.add(modRow);
				pos += BUTTON_HEIGHT + GAP;
			}

			var redQuestion = Icons.get(Icons.BTN_QUESTION);
			redQuestion.color(0.7f,0.3f,0.3f);
			SimpleButton modInfo = new SimpleButton(redQuestion) {
				@Override
				protected void onClick() {
					ModDesc infoDesc = Mods.getModDesc(desc.name, GamePreferences.uiLanguage());
					GameLoop.addToScene(new WndModInfo(infoDesc));
				}
			};

			modRow.add(modInfo);

			if (desc.installed && !ModdingMode.REMIXED.equals(desc.name)) {
				SimpleButton deleteBtn = new SimpleButton(Icons.get(Icons.CLOSE)) {
					protected void onClick() {
						GameLoop.addToScene(new WndOptions(StringsManager.getVar(R.string.WndModSelect_ReallyDelete),
								Utils.format(StringsManager.getVar(R.string.WndModSelect_AreYouSure), desc.name),
								StringsManager.getVar(R.string.Wnd_Button_Yes),
								StringsManager.getVar(R.string.Wnd_Button_No) ) {
							@Override
							public void onSelect(int index) {
								hide();
								if(index == 0) {
									onDelete(desc.installDir);
								}
							}
						});
					}
				};
				modRow.add(deleteBtn);
			}
		}

		pos+= 4*GAP;

		modsListContainer.setSize(width, pos);
		list.content().add(modsListContainer);

		resize(WndHelper.getLimitedWidth(120), WndHelper.getAlmostFullscreenHeight());

		list.content().setSize(width, pos);
		list.setRect(0, tfTitle.bottom() + GAP, width, height - tfTitle.height() - GAP);
		list.scrollTo(0,0);
	}

	private void onDelete(String name) {

		File modDir = FileSystem.getExternalStorageFile(name);

		if (modDir.exists() && modDir.isDirectory()) {
			FileSystem.deleteRecursive(modDir);
		}

		if (GamePreferences.activeMod().equals(name)) {
			SaveUtils.deleteGameAllClasses();
			SaveUtils.copyAllClassesFromSlot(ModdingMode.REMIXED);
			GamePreferences.activeMod(ModdingMode.REMIXED);
			RemixedDungeon.instance().doRestart();
		}

		GameLoop.addToScene(new WndModSelect());
	}

	protected void onSelect(String option) {

		ModDesc desc = modsList.get(option);
		if (!option.equals(ModdingMode.REMIXED) || desc.needUpdate) {

			if (desc.needUpdate) {
				FileSystem.deleteRecursive(FileSystem.getExternalStorageFile(desc.installDir));
				selectedMod = desc.installDir;
				downloadTo = FileSystem.getExternalStorageFile(selectedMod+".tmp").getAbsolutePath();
				desc.needUpdate = false;

				GameLoop.execute(new DownloadTask(new DownloadProgressWindow(Utils.format("Downloading %s", selectedMod),this),
						desc.url,
						downloadTo));

				return;
			}
		}

		String prevMod = GamePreferences.activeMod();

		if (option.equals(prevMod)) {
			return;
		}

		GameLoop.addToScene(new WndModDescription(option, prevMod));
	}


	@Override
	public void DownloadComplete(String url, final Boolean result) {
		GameLoop.pushUiTask(() -> {
			if (result) {
				GameLoop.execute(new UnzipTask(WndModSelect.this, downloadTo, true));
			} else {
				GameLoop.addToScene(new WndError(Utils.format("Downloading %s failed", selectedMod)));
			}
		});
	}


	private WndMessage        unzipProgress;

	@Override
	public void UnzipComplete(final Boolean result) {
		GameLoop.pushUiTask(() -> {
			if(unzipProgress!=null) {
				unzipProgress.hide();
				unzipProgress = null;
			}

			if (result) {
				GameLoop.addToScene(new WndModSelect());
			} else {
				GameLoop.addToScene(new WndError(Utils.format("unzipping %s failed", downloadTo)));
			}
		});

	}

	@Override
	public void UnzipProgress(Integer unpacked) {
		GameLoop.pushUiTask(() -> {
			if (unzipProgress == null) {
				unzipProgress = new WndMessage("Unpacking ..."){
					@Override
					public void onBackPressed() { }
				};
				GameLoop.addToScene(unzipProgress);
			}
			if (unzipProgress.getParent() == GameLoop.scene()) {
				unzipProgress.setText(Utils.format("Unpacking: %d", unpacked));
			}
		});
	}
}
