package com.nyrds.pixeldungeon.levels.objects;

import androidx.annotation.Keep;

import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.lua.LuaEngine;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.Util;
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

import org.jetbrains.annotations.NotNull;
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
	private boolean activatedByItem = false;

	@Packable
	private boolean activatedByMob = false;

	@Packable
	private boolean secret = false;

	@Packable(defaultValue = "-1")
	private int usedImageIndex = -1;

	@Keep
	public Trap() {
		this(Level.INVALID_CELL);
	}

	public Trap(int pos) {
		super(pos);
		imageIndex  = -1;
		textureFile = "levelObjects/traps.png";

		layer = -1;
	}

	static public @NotNull Trap makeSimpleTrap(int pos, String kind, boolean secret) {
		Trap ret = new Trap(pos);

		ret.kind = kind;
		ret.secret = secret;
		ret.uses = 1;
		ret.targetCell = ret.pos;
		ret.activatedByItem = true;
		ret.activatedByMob = true;

		return ret;
	}

	@LuaInterface
	public void reactivate(String kind, int uses) {
		this.kind = kind;
		this.uses = uses;
		lo_sprite.ifPresent(
				sprite -> sprite.reset(image()));
	}

	public void deactivate() {
		uses = 0;
		lo_sprite.ifPresent(
				sprite -> sprite.reset(usedImage()));

	}

	@Override
	public boolean stepOn(Char chr) {
		interact(chr);

		if (uses > 0 && !chr.isFlying() && chr instanceof Hero) {
			Hero hero = (Hero) chr;
			hero.interrupt();
		}
		return true;
	}

	@Override
	public void bump(Presser presser) {
		if(presser instanceof Hero) {
			activate((Char)presser);
			return;
		}

		if (presser instanceof LevelObject) {
			activate(null);
			return;
		}

		if (presser instanceof Mob && activatedByMob) {
			activate(null);
			return;
		}

		if (presser instanceof Item && activatedByItem) {
			activate(null);
		}
	}

	public void activate(Char hero) {
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
				lo_sprite.ifPresent(
						sprite -> sprite.reset(usedImage()));
				level().markObjectFlags(this, getPos());
			}
		}
	}

	@Override
	void setupFromJson(Level level, JSONObject obj) throws JSONException {
		super.setupFromJson(level,obj);

		int targetX = obj.getInt("x");
		int targetY = obj.getInt("y");

		if (obj.has("target")) {
			JSONObject targetDesc = obj.getJSONObject("target");
			targetX = targetDesc.getInt("x");
			targetY = targetDesc.getInt("y");
		}

		targetCell = level.cell(targetX, targetY);

		kind = obj.optString("trapKind", "none");
		uses = obj.optInt("uses", 1);
		secret = obj.optBoolean("secret", false);
		activatedByItem = obj.optBoolean("activatedByItem", false);
		activatedByMob = obj.optBoolean("activatedByMob", false);

		script = obj.optString("script", "");

		usedImageIndex = obj.optInt("usedImageIndex", usedImageIndex);
	}

	@Override
	public void discover() {
		secret = false;
		lo_sprite.ifPresent(
				sprite -> sprite.setVisible(true));
		level().markObjectFlags(this, getPos());
	}

	@Override
	public boolean secret() {
		return secret;
	}

	@Override
	public String desc() {
		if(uses <= 0) {
			return StringsManager.getVar(R.string.Level_TileDescInactiveTrap);
		}
		return StringsManager.getVar(R.string.Level_TileDescTrap);
	}

	@Override
	public String name() {
		if(uses <= 0) {
			return StringsManager.getVar(R.string.Level_TileInactiveTrap);
		}
		return StringsManager.maybeId("Level_Tile"+kind);
	}

	@Override
	public int image() {
		if (uses > 0) {
			if(imageIndex >= 0) {
				return imageIndex;
			}
			int nKind = Util.indexOf(traps, kind) + 16 * level().objectsKind();
			return nKind + 1;
		} else {
			return usedImage();
		}
	}

	private int usedImage() {
		if(usedImageIndex >= 0) {
			return usedImageIndex;
		}
		return 0 + 16 * level().objectsKind();
	}

	@Override
	public boolean nonPassable(Char ch) {
		if(ch instanceof Mob) {
			return !secret && uses > 0;
		}
		return false;
	}

	@Override
	public boolean avoid() {
		return !secret && uses > 0;
	}

	@Override
	public String getEntityKind() {
		return kind;
	}

	@Override
	public boolean affectItems() {
		return true;
	}

	static class ScriptTrap implements ITrigger {
		private final String scriptFile;
		private final String data;

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

	@Override
	public boolean ignoreIsometricShift() {
		return true;
	}
}
