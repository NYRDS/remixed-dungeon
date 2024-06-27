
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.IconButton;
import com.watabou.pixeldungeon.ui.Window;

public abstract class WndOptions extends Window {

    protected final VBox buttonsVbox;
    protected final Text title;
    protected final Text message;
	protected final VBox vbox;

	public WndOptions(String title, String message, String... options) {
        super();

		vbox = new VBox();
        vbox.setGap(GAP);

        this.title = PixelScene.createMultiline(StringsManager.maybeId(title), GuiProperties.titleFontSize());
        this.title.hardlight(TITLE_COLOR);
        this.title.setX(GAP);
        this.title.maxWidth(STD_WIDTH - GAP * 2);
        vbox.add(this.title);

        this.message = PixelScene.createMultiline(StringsManager.maybeId(message), GuiProperties.regularFontSize());
        this.message.maxWidth(STD_WIDTH - GAP * 2);
        this.message.setX(GAP);
        vbox.add(this.message);

		buttonsVbox = new VBox();
        for (int i = 0; i < options.length; i++) {
            final int index = i;
            var btn = new IconButton(StringsManager.maybeId(options[i])) {
                @Override
                protected void onClick() {
                    hide();
                    onSelect(index);
                }
            };

            btn.setSize(STD_WIDTH - GAP * 2, BUTTON_HEIGHT);
            buttonsVbox.add(btn);
        }

        buttonsVbox.setRect(GAP, 0, STD_WIDTH, buttonsVbox.childsHeight());
        vbox.add(buttonsVbox);

        vbox.setRect(GAP, 0, STD_WIDTH, vbox.childsHeight());
        add(vbox);
        resize(STD_WIDTH, (int) vbox.height());
    }

	@Override
	public void layout() {
		buttonsVbox.setRect(GAP, 0, STD_WIDTH, buttonsVbox.childsHeight());
		vbox.setRect(GAP, 0, STD_WIDTH, vbox.childsHeight());
		resize(STD_WIDTH, (int) vbox.height());

	}

    abstract public void onSelect(int index);
}
