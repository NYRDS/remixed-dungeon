package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.VKPlayEntitlements;
import com.nyrds.platform.support.GcAuth;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.SystemRedButton;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndTitledMessage;

public class WndVkPlayLink extends WndTitledMessage implements VKPlayEntitlements.StatusListener {

	private final SystemRedButton button;
	private Text statusText;

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

		String idleText;
		boolean desktopFlow = !Utils.isAndroid() && GcAuth.hasTicket();
		if (!Utils.isAndroid() && !GcAuth.hasTicket()) {
			idleText = StringsManager.getVar(R.string.VkPlayLink_needGameCenter);
		} else {
			idleText = StringsManager.getVar(R.string.VkPlayLink_idle);
		}

		statusText = PixelScene.createMultiline(idleText, GuiProperties.regularFontSize());
		statusText.maxWidth(width);
		statusText.setX(text.getX());
		statusText.setY(pos);
		add(statusText);
		pos += statusText.height() + GAP;

		if (desktopFlow) {
			button = new SystemRedButton(StringsManager.getVar(R.string.VkPlayLink_signIn)) {
				@Override
				protected void onClick() {
					enable(false);
					VKPlayEntitlements.desktopLink(GcAuth.persId(), GcAuth.otp(), WndVkPlayLink.this);
				}
			};
		} else if (Utils.isAndroid()) {
			button = new SystemRedButton(StringsManager.getVar(R.string.VkPlayLink_signIn)) {
				@Override
				protected void onClick() {
					enable(false);
					VKPlayEntitlements.startLink(WndVkPlayLink.this);
				}
			};
		} else {
			button = null;
		}

		if (button != null) {
			button.setRect(0, pos, width, BUTTON_HEIGHT);
			add(button);
			resize(width, (int) button.bottom() + GAP);
		} else {
			resize(width, (int) pos + GAP);
		}
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
}
