package com.watabou.pixeldungeon.windows;

import android.content.Intent;
import android.net.Uri;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.input.Touchscreen.Touch;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.noosa.TouchArea;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.SaveUtils;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndModDescription extends Window {

	private static final int WIDTH = 120;
	private static final int GAP = 2;

	private float yPos;
	private String prevMod;

	public WndModDescription(final String option, final String prevMod) {

		super();
		this.prevMod = prevMod;

		PixelDungeon.activeMod(option);
		
		PixelDungeon.instance().useLocale(PixelDungeon.uiLanguage());

		yPos = 0;
		
		if (!option.equals(ModdingMode.REMIXED)) {
			Text title = PixelScene.createMultiline(GuiProperties.titleFontSize());
			title.maxWidth(WIDTH);
			title.text(Game.getVar(R.string.Mod_Name) + "\n ");
			title.hardlight(Window.TITLE_COLOR);

			place(title);

			Text author = PixelScene.createMultiline(GuiProperties.regularFontSize());
			author.maxWidth(WIDTH);
			author.text(Game.getVar(R.string.Mods_CreatedBy) + "\n" + Game.getVar(R.string.Mod_Author) + "\n ");

			place(author);

			final String siteUrl = Game.getVar(R.string.Mod_Link);
			if (siteUrl.length() > 0) {
				Text site = PixelScene.createMultiline(GuiProperties.regularFontSize());
				site.maxWidth(WIDTH);
				site.text(Game.getVar(R.string.Mods_AuthorSite) + "\n" + siteUrl + "\n ");
				place(site);

				TouchArea siteTouch = new TouchArea(site) {
					@Override
					protected void onClick(Touch touch) {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(siteUrl));

						Game.instance().startActivity(Intent.createChooser(intent, siteUrl));
					}
				};
				add(siteTouch);
			}

			final String emailUri = Game.getVar(R.string.Mod_Email);

			if (emailUri.length() > 0) {
				Text email = PixelScene.createMultiline(GuiProperties.regularFontSize());
				email.maxWidth(WIDTH);
				email.text(Game.getVar(R.string.Mods_AuthorEmail) + emailUri + "\n ");
				place(email);

				TouchArea emailTouch = new TouchArea(email) {
					@Override
					protected void onClick(Touch touch) {
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.setType("message/rfc822");
						intent.putExtra(Intent.EXTRA_EMAIL, new String[] { emailUri });
						intent.putExtra(Intent.EXTRA_SUBJECT, Game.getVar(R.string.app_name)+":"+Game.getVar(R.string.Mod_Name));

						Game.instance().startActivity(Intent.createChooser(intent, emailUri));
					}
				};
				add(emailTouch);
			}

			Text description = PixelScene.createMultiline(GuiProperties.regularFontSize());
			description.maxWidth(WIDTH);
			description.text(Game.getVar(R.string.Mod_Description) + "\n ");
			place(description);
		}
		
		RedButton btn = new RedButton(Game.getVar(R.string.Mods_RestartRequired)) {
			@Override
			protected void onClick() {
				switchSaves(option, prevMod);
				PixelDungeon.instance().doRestart();
			}
		};
		
		btn.setSize( Math.min( WIDTH, btn.reqWidth() ), 16 );
		btn.setPos(WIDTH / 2 - btn.width() / 2, yPos);
		
		add(btn);

		yPos += btn.height();

		resize(WIDTH, (int) (yPos + GAP));
	}

	private static void switchSaves(String option, String prevMod) {
		SaveUtils.copyAllClassesToSlot(prevMod);
		SaveUtils.deleteGameAllClasses();
		SaveUtils.copyAllClassesFromSlot(option);
	}

	private void place(Text text) {
		text.measure();
		text.setPos(0, yPos);
		yPos += text.height();
		add(text);
	}
	
	public void onBackPressed() {
		PixelDungeon.activeMod(prevMod);
		hide();
		Game.scene().add(new WndModSelect());
	}
}
