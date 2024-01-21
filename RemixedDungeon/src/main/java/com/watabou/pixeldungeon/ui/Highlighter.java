package com.watabou.pixeldungeon.ui;

import androidx.annotation.NonNull;

import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Group;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Highlighter {

    private static final Pattern HIGHLIGHTER	= Pattern.compile( "_(.*?)_" );
    private static final Pattern STRIPPER		= Pattern.compile( "[ \n]" );

    public String text;

    public boolean[] mask;

    public Highlighter(String text ) {

        String stripped = STRIPPER.matcher( text ).replaceAll(Utils.EMPTY_STRING);
        mask = new boolean[stripped.length()];

        Matcher m = HIGHLIGHTER.matcher( stripped );

        int pos = 0;
        int lastMatch = 0;

        while (m.find()) {
            pos += (m.start() - lastMatch);
            int groupLen = m.group( 1 ).length();
            for (int i=pos; i < pos + groupLen; i++) {
                mask[i] = true;
            }
            pos += groupLen;
            lastMatch = m.end();
        }

        m.reset( text );
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement( sb, m.group( 1 ) );
        }
        m.appendTail( sb );

        this.text = sb.toString();
    }

    @NonNull
    public static Text addHilightedText(float x, float y, int maxWidth, Group parent, String message) {
        Highlighter hl = new Highlighter(message);

        Text normal = PixelScene.createMultiline(hl.text, GuiProperties.regularFontSize());
        if (hl.isHighlighted()) {
            normal.mask = hl.inverted();
        }

        normal.maxWidth(maxWidth);
        normal.setX(x);
        normal.setY(y);
        parent.add(normal);

        if (hl.isHighlighted()) {
            Text highlighted = PixelScene.createMultiline(hl.text, GuiProperties.regularFontSize());
            highlighted.baseText = normal;
            highlighted.mask = hl.mask;
            highlighted.maxWidth(normal.getMaxWidth());
            highlighted.setX(normal.getX());
            highlighted.setY(normal.getY());
            parent.add(highlighted);

            highlighted.hardlight(Window.TITLE_COLOR);
        }


        return normal;
    }

    public boolean[] inverted() {
        boolean[] result = new boolean[mask.length];
        for (int i=0; i < result.length; i++) {
            result[i] = !mask[i];
        }
        return result;
    }

    public boolean isHighlighted() {
        for (boolean aMask : mask) {
            if (aMask) {
                return true;
            }
        }
        return false;
    }
}
