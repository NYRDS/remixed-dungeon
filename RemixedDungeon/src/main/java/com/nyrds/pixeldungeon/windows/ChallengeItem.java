package com.nyrds.pixeldungeon.windows;

import static com.watabou.pixeldungeon.ui.Window.GAP;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Challenges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Facilitations;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.ImageButton;
import com.watabou.pixeldungeon.windows.WndStory;

import java.util.Objects;

public class ChallengeItem extends Component {

    public static final String UI_CHALLENGES_PNG = "ui/challenges.png";
    protected ColorBlock bg = new ColorBlock(width, height, 0xFF4A4D44);
    protected ImageButton descIcon;
    protected ImageButton challengeIcon;

    private boolean state = false;

    protected Text label  = PixelScene.createText(GuiProperties.regularFontSize());

    HBox box;

    private final int index;

    ChallengeItem(int index, float maxWidth, boolean editable) {

        String title;
        String desc;
        Image icon;

        this.index = index;

        if(index >= 16) {
            title = StringsManager.getVars(R.array.Facilitations_Names)[index - 16];
            desc = StringsManager.getVars(R.array.Facilitations_Descriptions)[index - 16];
            icon = new Image(UI_CHALLENGES_PNG, 16, index + 16 - 16);
        } else {
            title = StringsManager.getVars(R.array.Challenges_Names)[index];
            desc = StringsManager.getVars(R.array.Challenges_Descriptions)[index];
            icon = new Image(UI_CHALLENGES_PNG, 16, index);
        }

        challengeIcon = new ImageButton(icon) {
            @Override
            protected void onClick() {
                if(!editable) {
                    return;
                }

                state = ! state;
                int mask = (int) Math.pow(2,index);
                if (state) {
                    if (index >= 16) {
                        Dungeon.setFacilitation(mask);
                        for(Integer msk: Objects.requireNonNull(Facilitations.conflictingChallenges.get(mask))) {
                            Dungeon.resetChallenge(msk);
                        }
                    } else {
                        if(Dungeon.setChallenge(mask)) {
                            for (Integer msk : Objects.requireNonNull(Challenges.conflictingFacilitations.get(mask))) {
                                Dungeon.resetFacilitation(msk);
                            }
                        } else {
                            state = ! state;
                        }
                    }
                } else {
                    if (index >= 16) {
                        Dungeon.resetFacilitation(mask);
                    } else {
                        Dungeon.resetChallenge(mask);
                    }
                }
            }
        };

        descIcon = new ImageButton(Icons.get(Icons.BTN_QUESTION)) {
            @Override
            protected void onClick() {
                GameLoop.addToScene(new WndStory(desc));
            }
        };

        box = new HBox(maxWidth - 2 * GAP);
        box.setAlign(HBox.Align.Width);
        box.setAlign(VBox.Align.Center);

        box.add(challengeIcon);
        box.add(label);
        box.add(descIcon);

        label.text(title);

        add(bg);
        add(box);
    }

    @Override
    protected void layout() {
        box.setPos(x + GAP,y + GAP);
        bg.setX(x);
        bg.setY(y);
        bg.size(box.getMaxWidth() + 2 * GAP, box.height() + 2 * GAP);
    }

    @Override
    public void update() {
        int mask = (int) Math.pow(2, index);

        if(index>=16) {
            state = Dungeon.isFacilitated(mask);
        } else {
            state = Dungeon.isChallenged(mask);
        }

        if (state) {
            if(index>=16) {
                challengeIcon.hardlight(0.6f, 0.9f, 0.6f);
            } else {
                challengeIcon.hardlight(0.9f, 0.9f, 0.6f);
            }
        } else {
            challengeIcon.hardlight(0.5f,0.5f,0.5f);
        }


        super.update();
    }

    @Override
    public void measure() {
        box.measure();
    }

    @Override
    public float width() {
        return box.width();
    }

    @Override
    public float height() {
        return box.height() + 4*GAP;
    }
}
