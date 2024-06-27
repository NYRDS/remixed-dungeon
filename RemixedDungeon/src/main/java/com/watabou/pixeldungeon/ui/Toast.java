
package com.watabou.pixeldungeon.ui;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Chrome;
import com.watabou.pixeldungeon.scenes.PixelScene;

import org.jetbrains.annotations.Nullable;

public class Toast extends Component implements IWindow {

	private static final float MARGIN_HOR	= 4;
	private static final float MARGIN_VER	= 4;

	protected final NinePatch bg;
	protected final SimpleButton close;
	protected final Text text;

	@Nullable
	protected Image icon;

	final HBox hBox;

	public Toast( String text) {
		this(text, null);
	}

	public Toast( String text, @Nullable Image icon) {
		super();

		hBox = new HBox(Window.STD_WIDTH);

		hBox.setAlign(VBox.Align.Center);
		hBox.setGap((int) MARGIN_HOR);

		bg = Chrome.get( Chrome.Type.TOAST_TR );
		add( bg );

		if(icon!=null) {
			this.icon = icon;
			hBox.add(icon);
		}

		this.text = PixelScene.createText(text, GuiProperties.regularFontSize());
		hBox.add( this.text );

		close = new SimpleButton( Icons.get( Icons.CLOSE ) ) {
			protected void onClick() {
				onClose();
			}
		};

		hBox.add( close );
		add(hBox);

		width = hBox.width() + MARGIN_HOR * 2;
		height = hBox.height() + MARGIN_VER * 2;
	}

	@LuaInterface
	public void close() {
		onClose();
	}

	@Override
	protected void layout() {
		super.layout();

		bg.setX(x);
		bg.setY(y);
		bg.size( width, height );
		hBox.setPos(x + MARGIN_HOR, y + MARGIN_VER);
	}

	protected void onClose() {}
}