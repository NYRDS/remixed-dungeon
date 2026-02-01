

package com.watabou.noosa;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.input.Keys;
import com.watabou.pixeldungeon.ui.IWindow;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Signal;

import java.util.ArrayList;

public class Scene extends Group {

	public static final String LEVELS_TEST = "levelsTest";

	private Signal.Listener<Keys.Key> keyListener;
	private final ArrayList<IWindow> activeWindows = new ArrayList<>();

	static protected final LuaScript script = new LuaScript("scripts/services/scene", null);
	static public String sceneMode = "none";

	public void create() {
		GLog.debug("Creating scene %s", this.getClass().getSimpleName());
		Keys.event.add( keyListener = key -> {
			if (Game.instance() != null && key.pressed) {
				switch (key.code) {
				case Keys.BACK:
					onBackPressed();
					break;
				case Keys.MENU:
					onMenuPressed();
					break;
				default:
					onKeyPressed(key.code);
				}
			}
		});
	}

	protected void onKeyPressed(int code) {
	}

	@LuaInterface
	public IWindow getWindow(int i) {
		if(i < activeWindows.size()) {
			return activeWindows.get(i);
		}

		return null;
	}

	@LuaInterface
	public void enumerateWindows() {
		activeWindows.clear();
		int windowIndex = -1;
		while ((windowIndex = findByClass(IWindow.class, windowIndex + 1)) > 0) {
			activeWindows.add((IWindow) getMember(windowIndex));
		}
	}

	@Override
	public void update() {
		if(sceneMode.equals(Scene.LEVELS_TEST)) {
			enumerateWindows();
		}

		//GLog.debug("%s activeWindows: %d", this.getClass().getSimpleName(), activeWindows.size());

		totalGizmo = 0;
		nullGizmo = 0;

		script.runOptionalNoRet("onStep", this.getClass().getSimpleName());

		super.update();

		//GLog.debug("gizmos: %d %d", totalGizmo, nullGizmo);
	}


	@Override
	public void destroy() {
		GLog.debug("Destroying scene %s", this.getClass().getSimpleName());
		Keys.event.remove( keyListener );
		super.destroy();
	}
	
	public void pause() {
	}
	
	public void resume() {	
	}
	
	@Override
	public Camera camera() {
		return Camera.main;
	}
	
	protected void onBackPressed() {
		Game.shutdown();
	}
	
	protected void onMenuPressed() {
	}

	public static void setMode(String mode) {
		sceneMode = mode;
		script.run("setMode", mode);
	}

	public boolean cellClicked(int cell) {
		return script.runOptional("cellClicked",false, cell);
	}
}
