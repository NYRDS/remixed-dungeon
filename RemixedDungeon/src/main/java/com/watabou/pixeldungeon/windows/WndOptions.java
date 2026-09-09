
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.pixeldungeon.windows.WndHelper;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.IconButton;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.Window;

public abstract class WndOptions extends Window {

    protected final VBox buttonsVbox;
    protected final Text title;
    protected final Text message;
	protected final VBox vbox;

	private ScrollPane scroll;

	public WndOptions(String title, String message, String... options) {
        super();

		int width = WndHelper.getLimitedWidth(stdWidth());

		vbox = new VBox();
        vbox.setGap(GAP);

        this.title = PixelScene.createMultiline(StringsManager.maybeId(title), GuiProperties.titleFontSize());
        this.title.hardlight(TITLE_COLOR);
        this.title.maxWidth(width - GAP * 2);
        vbox.add(this.title);

        this.message = PixelScene.createMultiline(StringsManager.maybeId(message), GuiProperties.regularFontSize());
        this.message.maxWidth(width - GAP * 2);
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

            btn.setSize(width - GAP * 2, BUTTON_HEIGHT);
            buttonsVbox.add(btn);
        }

        buttonsVbox.setRect(0, 0, width - GAP * 2, buttonsVbox.childsHeight());
        vbox.add(buttonsVbox);

        vbox.setRect(0, 0, width - GAP * 2, vbox.childsHeight());

		int contentH = (int) vbox.height();
		int availH = WndHelper.getAlmostFullscreenHeight();

		if (contentH > availH) {
			// content taller than the screen: scroll it, keep the window on screen
			scroll = new ScrollPane(vbox);
			scroll.setRect(GAP, 0, width - GAP * 2, availH);
			add(scroll);
			resize(width, availH);
		} else {
			vbox.setRect(GAP, 0, width - GAP * 2, contentH);
			add(vbox);
			resize(width, contentH);
		}
	}

	@Override
	public void layout() {
		int availH = WndHelper.getAlmostFullscreenHeight();
		if (scroll != null) {
			vbox.setRect(0, 0, width - GAP * 2, vbox.childsHeight());
			scroll.setRect(GAP, 0, width - GAP * 2, Math.min(height, availH));
		} else {
			vbox.setRect(GAP, 0, width - GAP * 2, vbox.childsHeight());
			resize(width, Math.min((int) vbox.height(), availH));
		}
	}

    abstract public void onSelect(int index);
}
