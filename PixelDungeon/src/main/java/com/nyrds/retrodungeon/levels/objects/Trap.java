package com.nyrds.retrodungeon.levels.objects;

import com.nyrds.Packable;
import com.nyrds.android.lua.LuaEngine;
import com.nyrds.android.util.Util;
import com.watabou.noosa.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.traps.AlarmTrap;
import com.watabou.pixeldungeon.levels.traps.FireTrap;
import com.watabou.pixeldungeon.levels.traps.GrippingTrap;
import com.watabou.pixeldungeon.levels.traps.LightningTrap;
import com.watabou.pixeldungeon.levels.traps.ParalyticTrap;
import com.watabou.pixeldungeon.levels.traps.PoisonTrap;
import com.watabou.pixeldungeon.levels.traps.SummoningTrap;
import com.watabou.pixeldungeon.levels.traps.ToxicTrap;

import org.json.JSONException;
import org.json.JSONObject;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

/**
 * Created by mike on 01.07.2016.
 */

public class Trap extends LevelObject {

	private static final Class<?>[] traps = new Class<?>[]{
			ToxicTrap.class,
			FireTrap.class,
			ParalyticTrap.class,
			PoisonTrap.class,
			AlarmTrap.class,
			LightningTrap.class,
			SummoningTrap.class,
			GrippingTrap.class};

	@Packable
	private String kind;
	@Packable
	private int    targetCell;
	@Packable
	private int    uses;

	@Packable
	private String script;

	@Packable
	private String data;

	@Packable
	private boolean activatedByItem = false;

	@Packable
	private boolean activatedByMob = false;

	@Packable
	private boolean secret = false;

	@Packable(defaultValue = "-1")
	private int usedImageIndex;

	public Trap() {
		this(-1);
	}

	public Trap(int pos) {
		super(pos);
		imageIndex  = -1;
		textureFile = "levelObjects/traps.png";

		layer = -1;
	}

	@Override
	public boolean stepOn(Char chr) {
		interact(chr);

		if (chr instanceof Hero) {
			Hero hero = (Hero) chr;
			hero.interrupt();
		}
		return true;
	}

	@Override
	public void bump(Presser presser) {

		if(presser instanceof Hero) {
			interact((Hero)presser);
			return;
		}

		if (presser instanceof LevelObject) {
			interact(null);
			return;
		}

		if (presser instanceof Mob && activatedByMob) {
			interact(null);
			return;
		}

		if (presser instanceof Item && activatedByItem) {
			interact(null);
		}
	}

	@Override
	public boolean interact(Char hero) {
		discover();

		if (uses != 0) {
			uses--;
			ITrigger trigger;

			if (kind.equals("scriptFile")) {
				trigger = new ScriptTrap(script, data);
			} else {
				trigger = Util.byNameFromList(traps, kind);
			}

			if (trigger != null) {
				trigger.doTrigger(targetCell, hero);
			}
			if (uses == 0) {
				sprite.reset(usedImage());
			}
		}

		return super.interact(hero);
	}

	@Override
	void setupFromJson(Level level, JSONObject obj) throws JSONException {

		if (obj.has("target")) {
			JSONObject targetDesc = obj.getJSONObject("target");
			int x = targetDesc.getInt("x");
			int y = targetDesc.getInt("y");
			targetCell = level.cell(x, y);
		} else {
			targetCell = getPos();
		}

		kind = obj.optString("trapKind", "none");
		uses = obj.optInt("uses", 1);
		secret = obj.optBoolean("secret", false);
		activatedByItem = obj.optBoolean("activatedByItem", false);
		activatedByMob = obj.optBoolean("activatedByMob", false);

		script = obj.optString("script", "");
		data = StringsManager.maybeId(obj.optString("data", ""));

		usedImageIndex = obj.optInt("usedImageIndex", usedImageIndex);
	}

	@Override
	public void discover() {
		secret = false;
		sprite.setVisible(true);
	}

	@Override
	public boolean secret() {
		return secret;
	}

	@Override
	public String desc() {
		return "Trap";
	}

	@Override
	public String name() {
		return "Trap";
	}

	@Override
	public int image() {
		if (uses > 0) {
			if(imageIndex >= 0) {
				return imageIndex;
			}
			int nKind = Util.indexOf(traps, kind);
			return nKind + 1;
		} else {
			return usedImage();
		}
	}

	private int usedImage() {
		if(usedImageIndex >= 0) {
			return usedImageIndex;
		}
		return 0;
	}

	@Override
	public boolean nonPassable() {
		return !secret && uses > 0;
	}

	class ScriptTrap implements ITrigger {
		private String scriptFile;
		private String data;

		ScriptTrap(String _scriptFile, String _data) {
			scriptFile = _scriptFile;
			data = _data;
		}

		@Override
		public void doTrigger(int cell, Char ch) {
			LuaTable trap = LuaEngine.getEngine().call("require", scriptFile).checktable();

			trap.get("setData").call(trap,LuaValue.valueOf(data));
			trap.get("trigger").call(trap,LuaValue.valueOf(cell), CoerceJavaToLua.coerce(ch));
		}
	}
}
