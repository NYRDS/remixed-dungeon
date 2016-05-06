package com.watabou.pixeldungeon.scenes;

import com.nyrds.pixeldungeon.ml.R;
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

public class WelcomeScene extends PixelScene {

	private static final String TTL_Welcome = Game.getVar(R.string.Welcome_Title);

	private static final String TXT_Welcome = Game.getVar(R.string.Welcome_Text);
	private static final String TXT_Welcome_19 = Game.getVar(R.string.Welcome_Text_19);
	private static final String TXT_Welcome_20 = Game.getVar(R.string.Welcome_Text_20);
	private static final String TXT_Welcome_20_1 = Game.getVar(R.string.Welcome_Text_20_1);
	private static final String TXT_Welcome_21 = Game.getVar(R.string.Welcome_Text_20_2);
	private static final String TXT_Welcome_21_1 = Game.getVar(R.string.Welcome_Text_21_1);
	private static final String TXT_Welcome_21_2 = Game.getVar(R.string.Welcome_Text_21_2);
	private static final String TXT_Welcome_21_3 = Game.getVar(R.string.Welcome_Text_21_3);
	private static final String TXT_Welcome_21_4 = Game.getVar(R.string.Welcome_Text_21_4);
	private static final String TXT_Welcome_21_5 = Game.getVar(R.string.Welcome_Text_21_5);
	private static final String TXT_Welcome_22 = Game.getVar(R.string.Welcome_Text_22);
	private static final String TXT_Welcome_23 = Game.getVar(R.string.Welcome_Text_23);
	private static final String TXT_Welcome_23_1 = Game.getVar(R.string.Welcome_Text_23_1);

	private static final int GAP = 4;

	@Override
	public void create() {
		super.create();

		String[] upds = { TXT_Welcome, TXT_Welcome_19, TXT_Welcome_20, TXT_Welcome_20_1, TXT_Welcome_21,
				TXT_Welcome_21_1,TXT_Welcome_21_2, TXT_Welcome_21_3, TXT_Welcome_21_4, TXT_Welcome_21_5, TXT_Welcome_22, TXT_Welcome_23, TXT_Welcome_23_1 };

		Text[] updTexts = new Text[upds.length];

		for (int i = 0; i < upds.length; i++) {
			updTexts[i] = createMultiline(upds[upds.length - i - 1], 8);
		}

		Text title = createMultiline(TTL_Welcome, 16);

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
		for (int i = 0; i < upds.length; i++) {
			updTexts[i].maxWidth((int) panel.innerWidth());
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
				if(Preferences.INSTANCE.getInt(Preferences.KEY_COLLECT_STATS,0) == 0) {
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

		fadeIn();
	}
}
