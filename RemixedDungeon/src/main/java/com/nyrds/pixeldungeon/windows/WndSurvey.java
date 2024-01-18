package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.storage.Preferences;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndMessage;

import org.json.JSONArray;
import org.json.JSONObject;

public class WndSurvey extends Window {

    protected static final int    BTN_HEIGHT   = 18;
    protected static final int    GAP          = 2;
    private static final   String SURVEY_TAKEN = "survey_taken";
    private static final   String SURVEY       = "survey";

    protected int WIDTH = WndHelper.getFullscreenWidth();

    private Text      questionText;
    private Component answers;

    private JSONArray survey;
    private int       question;
    private String    questionString;

    public WndSurvey(final JSONObject survey) {
        super();

        String lang = GamePreferences.uiLanguage();

        if (!survey.has(lang)) {
            lang = "en";
        }

        try {
            String surveyId = survey.getString("survey_id");

            if (Preferences.INSTANCE.getString(SURVEY_TAKEN, Utils.EMPTY_STRING).equals(surveyId)) {
                GameLoop.addToScene(new WndMessage(StringsManager.getVar(R.string.SociologistNPC_AlreadyTaken)));
                super.hide();
                return;
            }
            Preferences.INSTANCE.put(SURVEY_TAKEN, surveyId);
            this.survey = survey.getJSONArray(lang);
        } catch (Exception e) {
            this.survey = new JSONArray();
        }

        question = 0;

        //Title text
        questionText = PixelScene.createMultiline(Utils.EMPTY_STRING, GuiProperties.mediumTitleFontSize());
        questionText.hardlight(TITLE_COLOR);
        questionText.maxWidth(WIDTH - GAP);
        questionText.setX(GAP);
        questionText.setY(GAP);
        add(questionText);

        answers = new Component();
        add(answers);
        resize(WIDTH, (int) (questionText.bottom() + GAP));
        NextQuestion();
    }

    @Override
    public void hide() {
        if (question < survey.length()) {
            EventCollector.logEvent(SURVEY, questionString, "skipped");
            NextQuestion();
            return;
        }
        super.hide();
    }

    private void NextQuestion() {
        if (question < survey.length()) {
            try {
                JSONObject questionDesc = survey.getJSONObject(question);
                questionString = questionDesc.getString("question");
                questionText.text(questionString);

                answers.clear();

                float y = questionText.bottom() + GAP * 2;

                final JSONArray answersArray = questionDesc.getJSONArray("answers");

                for (int i = 0; i < answersArray.length(); ++i) {
                    final String answer = answersArray.getString(i);
                    RedButton button = new RedButton(answer) {
                        @Override
                        protected void onClick() {
                            EventCollector.logEvent(SURVEY, questionString, answer);
                            NextQuestion();
                        }
                    };

                    button.setRect(GAP, y, WIDTH - 2 * GAP, BTN_HEIGHT);
                    answers.add(button);

                    y = button.bottom() + GAP;
                }

                resize(WIDTH, (int) (y + GAP));
            } catch (Exception e) {
                hide();
            } finally {
                question++;
            }
            return;
        }

        hide();
    }
}
