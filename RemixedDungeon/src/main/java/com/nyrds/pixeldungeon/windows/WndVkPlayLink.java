package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.VKPlayEntitlements;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.SystemRedButton;
import com.watabou.pixeldungeon.windows.WndTitledMessage;

public class WndVkPlayLink extends WndTitledMessage implements VKPlayEntitlements.StatusListener {

	private final SystemRedButton button;

	public WndVkPlayLink() {
		super(Icons.get(Icons.VK_PLAY),
				StringsManager.getVar(R.string.VkPlayLink_title),
				StringsManager.getVar(R.string.VkPlayLink_desc));

		if (VKPlayEntitlements.hasEntitlement()) {
			setText(String.format(StringsManager.getVar(R.string.VkPlayLink_linked),
					VKPlayEntitlements.expiryDate()));
			button = null;
			return;
		}

		float pos = text.getY() + text.height() + GAP;

		statusText = PixelScene.createMultiline(StringsManager.getVar(R.string.VkPlayLink_idle),
				GuiProperties.regularFontSize());
		statusText.maxWidth(width);
		statusText.setX(text.getX());
		statusText.setY(pos);
		add(statusText);
		pos += statusText.height() + GAP;

		button = new SystemRedButton(StringsManager.getVar(R.string.VkPlayLink_signIn)) {
			@Override
			protected void onClick() {
				enable(false);
				VKPlayEntitlements.startLink(WndVkPlayLink.this);
			}
		};
		button.setRect(0, pos, width, BUTTON_HEIGHT);
		add(button);

		resize(width, (int) button.bottom() + GAP);
	}

	@Override
	public void onStatus(String message) {
		if (statusText == null) {
			return;
		}
		statusText.text(message);
		float pos = statusText.getY() + statusText.height() + GAP;
		if (button != null) {
			button.setRect(0, pos, width, BUTTON_HEIGHT);
			button.enable(!message.equals(StringsManager.getVar(R.string.VkPlayLink_waiting)));
			pos += button.height();
		}
		resize(width, (int) pos + GAP);
	}

	private Text statusText;
}
