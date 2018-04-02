package com.nyrds.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.Preferences;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.windows.WndMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WndSurvey extends Window {

	protected static final int BTN_HEIGHT	= 18;
	protected static final int GAP		    = 2;
	private static final String SURVEY_TAKEN = "survey_taken";

	protected int WIDTH = WndHelper.getFullscreenWidth();

	private Text questionText;
	private Component answers;

	private JSONArray survey;
	private int       question;

	public WndSurvey(final JSONObject survey) {
		super();

		String lang = PixelDungeon.uiLanguage();

		if(!survey.has(lang)) {
			lang = "en";
		}

		try {
			String surveyId = survey.getString("survey_id");

			if(Preferences.INSTANCE.getString(SURVEY_TAKEN,"").equals(surveyId)) {
				Game.scene().add(new WndMessage(Game.getVar(R.string.SociologistNPC_AlreadyTaken)));
				hide();
				return;
			}
			Preferences.INSTANCE.put(SURVEY_TAKEN,surveyId);
			this.survey = survey.getJSONArray(lang);
		} catch (JSONException e) {
			this.survey = new JSONArray();
		}

		question = 0;

		//Title text
		questionText = PixelScene.createMultiline("", GuiProperties.mediumTitleFontSize());
		questionText.hardlight(TITLE_COLOR);
		questionText.maxWidth(WIDTH - GAP);
		questionText.measure();
		questionText.x = GAP;
		questionText.y = GAP;
		add(questionText);

		answers = new Component();
		add(answers);
		resize(WIDTH, (int) (questionText.bottom() + GAP));
		NextQuestion();
	}

	private void NextQuestion() {
		if(question < survey.length()) {
			try {
				JSONObject questionDesc = survey.getJSONObject(question);
				final String questionString = questionDesc.getString("question");
				questionText.text(questionString);

				answers.clear();

				float y = questionText.bottom() + GAP*2;

				final JSONArray answersArray = questionDesc.getJSONArray("answers");

				for(int i = 0;i<answersArray.length();++i) {
					final String answer = answersArray.getString(i);
					RedButton button = new RedButton(answer) {
						@Override
						protected void onClick() {
							EventCollector.logEvent("survey",questionString, answer);
							NextQuestion();
						}
					};

					button.setRect(GAP,y,WIDTH - 2*GAP, BTN_HEIGHT);
					answers.add(button);

					y = button.bottom() + GAP;
				}

				question++;

				resize( WIDTH, (int) (y + GAP));

				return;
			} catch (JSONException e) {
				hide();
			}
		}

		hide();
	}
}
