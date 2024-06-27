package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.scenes.StartScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndOptions;

public class WndDifficultyOptions extends WndOptions {
    private final StartScene startScene;

    static public final String [] difficulties = {"Snail", "Rat", "Gnoll", "Crab"};
    static public final int [] descs  = {R.string.StartScene_DifficultyEasyNoAds,R.string.StartScene_DifficultyNormalWithSavesNoAds,R.string.StartScene_DifficultyNormal,R.string.StartScene_DifficultyExpert};

    public WndDifficultyOptions(StartScene startScene) {
        super(StringsManager.getVar(R.string.StartScene_DifficultySelect), Utils.EMPTY_STRING, StringsManager.getVar(GamePreferences.donated() > 0 ? R.string.StartScene_DifficultyEasyNoAds : R.string.StartScene_DifficultyEasy), StringsManager.getVar(GamePreferences.donated() > 0 ? R.string.StartScene_DifficultyNormalWithSavesNoAds : R.string.StartScene_DifficultyNormalWithSaves), StringsManager.getVar(R.string.StartScene_DifficultyNormal), StringsManager.getVar(R.string.StartScene_DifficultyExpert));
        this.startScene = startScene;

        for (int i = 0; i < buttonsVbox.getLength(); i++) {
            Image image = MobFactory.avatar(difficulties[i]);
            ((RedButton) buttonsVbox.getMember(i)).icon(image);
        }
    }

    @Override
    public void onSelect(final int index) {
        hide();
        startScene.startNewGame(index);
    }
}
