package com.watabou.pixeldungeon.ui;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.scenes.StartScene;

import java.util.Locale;

public class ClassShield extends Button {

    private static final float MIN_BRIGHTNESS = 0.6f;

    private static final int BASIC_NORMAL = 0x8888888;
    private static final int BASIC_HIGHLIGHTED = 0xCACFC2;

    private static final int MASTERY_NORMAL = 0x7711AA;
    private static final int MASTERY_HIGHLIGHTED = 0xCC33FF;

    private static final int WIDTH = 24;
    private static final int HEIGHT = 28;
    private static float SCALE = 1.5f;

    private final StartScene startScene;
    public final HeroClass cl;

    private Image avatar;
    private Text name;
    private Emitter emitter;

    private float brightness;

    private final int normal;
    private final int highlighted;

    public ClassShield(StartScene startScene, HeroClass cl) {
        super();
        this.startScene = startScene;

        this.cl = cl;

        avatar.frame(cl.classIndex() * WIDTH, 0, WIDTH, HEIGHT);

        if(RemixedDungeon.landscape()) {
            SCALE = 1.25f;
        }

        avatar.setScale(SCALE);

        if (Badges.isUnlocked(cl.masteryBadge())) {
            normal = MASTERY_NORMAL;
            highlighted = MASTERY_HIGHLIGHTED;
        } else {
            normal = BASIC_NORMAL;
            highlighted = BASIC_HIGHLIGHTED;
        }

        name.text(cl.title().toUpperCase(Locale.getDefault()));
        name.hardlight(normal);
        name.alpha(0);

        brightness = MIN_BRIGHTNESS;
        updateBrightness();
    }

    @Override
    protected void createChildren() {

        super.createChildren();

        avatar = new Image(Assets.AVATARS);
        add(avatar);

        name = PixelScene.createText(GuiProperties.titleFontSize());
        add(name);

        emitter = new Emitter();
        add(emitter);
    }

    @Override
    public void layout() {

        super.layout();

        avatar.setX(PixelScene.align(x + (width - avatar.width()) / 2));
        avatar.setY(PixelScene.align(y + (height - avatar.height() - name.height()) / 2));



        name.setX(PixelScene.align(x + (width - name.width()) / 2));

        name.setY(avatar.getY() + avatar.height() + SCALE);
        /*
        if(cl.classIndex()%2==0 && RemixedDungeon.landscape()) {
            name.setY(avatar.getY() - name.height() - SCALE);
        }*/

        emitter.pos(avatar.getX(), avatar.getY(), avatar.width(), avatar.height());
    }

    @Override
    protected void onTouchDown() {

        emitter.revive();
        emitter.start(Speck.factory(Speck.LIGHT), 0.05f, 7);

        Sample.INSTANCE.play(Assets.SND_CLICK, 1, 1, 1.2f);
        startScene.updateShield(this);
    }

    @Override
    public void update() {
        super.update();

        if (brightness < 1.0f && brightness > MIN_BRIGHTNESS) {
            if ((brightness -= GameLoop.elapsed) <= MIN_BRIGHTNESS) {
                brightness = MIN_BRIGHTNESS;
            }
            updateBrightness();
        }
    }

    public void highlight(boolean value) {
        if (value) {
            brightness = 1.0f;
            name.hardlight(highlighted);
            name.alpha(1);

        } else {
            brightness = 0.999f;
            name.hardlight(normal);
            name.alpha(0);
        }

        updateBrightness();
    }

    private void updateBrightness() {
        avatar.gm = avatar.bm = avatar.rm = avatar.am = brightness;
    }
}
