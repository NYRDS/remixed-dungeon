package com.watabou.pixeldungeon.ui;

import com.nyrds.platform.audio.Sample;
import com.watabou.noosa.Image;
import com.watabou.noosa.Scene;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.windows.WndGameplayCustomization;

public class ChallengeButton extends Button {

    private final Scene parentScene;
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
                .get(Dungeon.getChallenges() > 0 ? Icons.CHALLENGE_ON
                        : Icons.CHALLENGE_OFF);
        add(image);
    }

    @Override
    protected void layout() {

        super.layout();

        image.setX(PixelScene.align(x));
        image.setY(PixelScene.align(y));
    }

    @Override
    protected void onClick() {
        parentScene.add(new WndGameplayCustomization(
                Dungeon.getChallenges(), true) {
            public void onBackPressed() {
                super.onBackPressed();
                image.copy(Icons.get(Dungeon.getChallenges() > 0 ? Icons.CHALLENGE_ON
                        : Icons.CHALLENGE_OFF));
            }
        });
    }

    @Override
    protected void onTouchDown() {
        Sample.INSTANCE.play(Assets.SND_CLICK);
    }
}
