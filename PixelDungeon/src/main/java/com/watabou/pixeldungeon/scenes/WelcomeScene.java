package com.watabou.pixeldungeon.scenes;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Chrome;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.Preferences;
import com.watabou.pixeldungeon.ui.Archs;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.utils.GLog;

public class WelcomeScene extends PixelScene {

	private static final int GAP = 4;

	@Override
	public void create() {
		super.create();

		long start = System.nanoTime();

		String[] upds = {
				Game.getVar(R.string.Welcome_Text),
				Game.getVar(R.string.Welcome_Text_19),
				Game.getVar(R.string.Welcome_Text_20),
				Game.getVar(R.string.Welcome_Text_20_1),
				Game.getVar(R.string.Welcome_Text_20_2),
				Game.getVar(R.string.Welcome_Text_21_1),
				Game.getVar(R.string.Welcome_Text_21_2),
				Game.getVar(R.string.Welcome_Text_21_3),
				Game.getVar(R.string.Welcome_Text_21_4),
				Game.getVar(R.string.Welcome_Text_21_5),
				Game.getVar(R.string.Welcome_Text_22),
				Game.getVar(R.string.Welcome_Text_23),
				Game.getVar(R.string.Welcome_Text_23_1),
				Game.getVar(R.string.Welcome_Text_23_2),
				Game.getVar(R.string.Welcome_Text_24),
				Game.getVar(R.string.Welcome_Text_24_1),
				Game.getVar(R.string.Welcome_Text_24_2),
				Game.getVar(R.string.Welcome_Text_25),
				Game.getVar(R.string.Welcome_Text_25_1),
				Game.getVar(R.string.Welcome_Text_25_2),
				Game.getVar(R.string.Welcome_Text_25_3),
				Game.getVar(R.string.Welcome_Text_25_4),
				Game.getVar(R.string.Welcome_Text_25_5),
				Game.getVar(R.string.Welcome_Text_26),
				Game.getVar(R.string.Welcome_Text_26_1),
				Game.getVar(R.string.Welcome_Text_26_2),
				Game.getVar(R.string.Welcome_Text_26_3),
				Game.getVar(R.string.Welcome_Text_26_4),
				Game.getVar(R.string.Welcome_Text_26_5),
				Game.getVar(R.string.Welcome_Text_26_6),
				Game.getVar(R.string.Welcome_Text_27),
				Game.getVar(R.string.Welcome_Text_27_1),
				Game.getVar(R.string.Welcome_Text_27_2),
				Game.getVar(R.string.Welcome_Text_27_3),
				Game.getVar(R.string.Welcome_Text_27_4)
		};

		int displayUpdates = Math.min(upds.length, 5);

		Text[] updTexts = new Text[displayUpdates];

		for (int i = 0; i < displayUpdates; i++) {
			updTexts[i] = createMultiline(GuiProperties.regularFontSize());
		}

		Text title = createMultiline(Game.getVar(R.string.Welcome_Title), GuiProperties.bigTitleFontSize());

		int w = Camera.main.width;
		int h = Camera.main.height;

		int pw = w - 10;

		title.maxWidth(pw);
		title.measure();

		title.x = align((w - title.width()) / 2);
		title.y = align(8);
		add(title);

		NinePatch panel = Chrome.get(Chrome.Type.WINDOW);

		panel.x = (w - pw) / 2;
		panel.y = title.y + title.height() + GAP * 2;
		int ph = (int) (h - panel.y - 22);

		panel.size(pw, ph);

		add(panel);

		ScrollPane list = new ScrollPane(new Component());
		add(list);
		list.setRect(panel.x + panel.marginLeft(), panel.y + panel.marginTop(), panel.innerWidth(),
				panel.innerHeight());
		list.scrollTo(0, 0);

		Component content = list.content();
		content.clear();

		float yPos = 0;
		for (int i = 0; i < displayUpdates; i++) {
			updTexts[i].maxWidth((int) panel.innerWidth());
			updTexts[i].text(upds[upds.length - i - 1]);
			updTexts[i].measure();

			updTexts[i].setPos(0, yPos);
			yPos += updTexts[i].height() + GAP;
			content.add(updTexts[i]);
		}

		content.setSize(panel.innerWidth(), yPos);

		RedButton okay = new RedButton(Game.getVar(R.string.Welcome_Ok)) {
			@Override
			protected void onClick() {
				PixelDungeon.version(Game.versionCode);

				if (Preferences.INSTANCE.getInt(Preferences.KEY_COLLECT_STATS, 1) == 0) {
					Game.switchScene(AllowStatisticsCollectionScene.class);
				} else {
					Game.switchScene(TitleScene.class);
				}
			}
		};

		okay.setRect((w - pw) / 2, h - 22, pw, 18);
		add(okay);

		Archs archs = new Archs();
		archs.setSize(Camera.main.width, Camera.main.height);
		addToBack(archs);

		long end = System.nanoTime();

		GLog.i("Time: %5.3f", (end-start)/100000.f);


		fadeIn();
	}
}
