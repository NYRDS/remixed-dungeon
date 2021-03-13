package com.watabou.pixeldungeon.ui;

import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.Scene;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.RemixedDungeon;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.windows.WndChallenges;
import com.watabou.pixeldungeon.windows.WndMessage;

public class ChallengeButton extends Button {

    private Scene parentScene;
    private Image image;

    public ChallengeButton(Scene startScene) {
        super();
        parentScene = startScene;

        width = image.width;
        height = image.height;

        image.am = Badges.isUnlocked(Badges.Badge.VICTORY) ? 1.0f : 0.5f;
    }

    @Override
    protected void createChildren() {

        super.createChildren();

        image = Icons
                .get(RemixedDungeon.challenges() > 0 ? Icons.CHALLENGE_ON
                        : Icons.CHALLENGE_OFF);
        add(image);
    }

    @Override
    protected void layout() {

        super.layout();

        image.x = PixelScene.align(x);
        image.y = PixelScene.align(y);
    }

    @Override
    protected void onClick() {
        if (Badges.isUnlocked(Badges.Badge.VICTORY) || Util.isDebug()) {
            parentScene.add(new WndChallenges(
                    RemixedDungeon.challenges(), true) {
                public void onBackPressed() {
                    super.onBackPressed();
                    image.copy(Icons.get(RemixedDungeon.challenges() > 0 ? Icons.CHALLENGE_ON
                            : Icons.CHALLENGE_OFF));
                }
            });
        } else {
            parentScene.add(new WndMessage(Game
                    .getVar(R.string.StartScene_WinGame)));
        }
    }

    @Override
    protected void onTouchDown() {
        Sample.INSTANCE.play(Assets.SND_CLICK);
    }
}
