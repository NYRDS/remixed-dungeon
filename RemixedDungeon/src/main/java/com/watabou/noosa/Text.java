package com.watabou.noosa;

import com.nyrds.pixeldungeon.windows.IPlaceable;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.gfx.SystemText;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public abstract class Text extends Visual implements IPlaceable {

    @NotNull
    protected String text = Utils.EMPTY_STRING;

    protected int maxWidth = Integer.MAX_VALUE;
    protected float minHeight = 0;

    protected boolean dirty = true;

    public Text baseText;

    protected Text(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    public static Text createBasicText(Font font) {
        if (!ModdingMode.getClassicTextRenderingMode()) {
            return new SystemText(font.baseLine * 2);
        }
        return new BitmapText(font);
    }

    public static Text createBasicText(String text, Font font) {
        return new SystemText(text, font.baseLine * 2, false);
    }

    public static Text create(Font font) {
        return new SystemText(font.baseLine * 2);
    }

    public static Text create(String text, Font font) {
        return new SystemText(text, font.baseLine * 2, false);
    }

    public static Text createMultiline(String text, Font font) {
        return new SystemText(text, font.baseLine * 2, true);
    }

    @Override
    public void draw() {
        clean();
        super.draw();
    }

    protected void clean() {
        if (dirty) {
            measure();
            dirty = false;
        }
    }

    public void maxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        dirty = true;
    }

    @Override
    public float height() {
        clean();
        return super.height();
    }

    @Override
    public float width() {
        clean();
        return super.width();
    }

    protected abstract void measure();

    public abstract float baseLine();

    @NotNull
    public String text() {
        return text;
    }


    public void text(int id) {
        text(StringsManager.getVar(id));
    }

    public void text(@NotNull String str) {
        if (text.equals(str)) {
            return;
        }

        dirty = true;

        if (str == null) {
            text = Utils.EMPTY_STRING;
            EventCollector.logException("Trying to create null string!!!");
            return;
        }

        text = str;
    }

    public abstract int lines();

    public float minHeight() {
        return minHeight;
    }

    public void minHeight(float minHeight) {
        this.minHeight = minHeight;
        dirty = true;
    }

    @Override
    public IPlaceable shadowOf() {
        return baseText;
    }

    public static String color(String txt, String color) {
        return Utils.format("[%s:%s]", color, txt);
    }
}
