package com.watabou.noosa.ui;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.input.Keys;
import com.nyrds.platform.input.Touchscreen.Touch;
import com.watabou.noosa.TouchArea;
import com.watabou.utils.Signal;

public class Button extends Component {

	private static final float longClick = 1f;

	protected TouchArea hotArea;

	private boolean pressed;
	private float   pressTime;

	private boolean processed;
	
	private int hotKey = -1; // No hotkey by default
	private Signal.Listener<Keys.Key> keyListener;

	@Override
	protected void createChildren() {
		hotArea = new TouchArea( 0, 0, 0, 0 ) {
			@Override
			protected void onTouchDown(Touch touch) {
				pressed = true;
				pressTime = 0;
				processed = false;
				Button.this.onTouchDown();
			}
			@Override
			protected void onTouchUp(Touch touch) {
				pressed = false;
				Button.this.onTouchUp();
			}
			@Override
			protected void onClick( Touch touch ) {
				if (!processed) {
					Button.this.onClick();
				}
			}

			@Override
			public void onSignal(Touch touch) {
				if(!isVisible() || !Button.this.isActive()) {
					return;
				}
				super.onSignal(touch);
			}
		};
		add( hotArea );
	}
	
	@Override
	public void update() {
		super.update();
		
		hotArea.setActive(getVisible());

		if (pressed && ((pressTime += GameLoop.elapsed) >= longClick)) {
			pressed = false;
			if (onLongClick()) {

				hotArea.reset();
				processed = true;
				onTouchUp();

				Game.vibrate(50);
			}
		}
	}
	
	protected void onTouchDown() {}
	protected void onTouchUp() {}
	protected void onClick() {}
	
	protected boolean onLongClick() {
		return false;
	}

	public void simulateClick() {
		onClick();
	}
	
	public void setHotKey(int keyCode) {
		// Remove existing listener if it exists
		if (keyListener != null) {
			Keys.event.remove(keyListener);
			keyListener = null;
		}
		
		hotKey = keyCode;
		
		// Only create and add listener if we have a valid hotkey
		if (hotKey != -1) {
			keyListener = new Signal.Listener<Keys.Key>() {
				private boolean keyDown = false;
				private long pressStartTime = 0;
				
				@Override
				public void onSignal(Keys.Key key) {
					if (key.code == hotKey && isVisible() && isActive()) {
						if (key.pressed && !keyDown) {
							keyDown = true;
							pressStartTime = System.currentTimeMillis();
						} else if (!key.pressed && keyDown) {
							keyDown = false;
							long pressDuration = System.currentTimeMillis() - pressStartTime;

							if (pressDuration > 500) {
								onLongClick();
							} else {
								onClick();
							}
						}
					}
				}
			};
			Keys.event.add(keyListener);
		}
	}
	
	public int getHotKey() {
		return hotKey;
	}

	@Override
    public void layout() {
		//GLog.debug("hot area: %3.0f %3.0f %3.0f %3.0f", x, y, width, height);
		hotArea.setX(x);
		hotArea.setY(y);
		hotArea.setWidth(width);
		hotArea.setHeight(height);
	}
	
	@Override
	public void destroy() {
		super.destroy();
		if (keyListener != null) {
			Keys.event.remove(keyListener);
			keyListener = null;
		}
	}
}
