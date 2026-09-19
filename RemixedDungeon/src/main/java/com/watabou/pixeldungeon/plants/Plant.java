
package com.watabou.pixeldungeon.plants;
import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.lua.LuaEngine;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModdingMode;
import com.watabou.noosa.Gizmo;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.LeafParticle;
import com.watabou.pixeldungeon.items.Dewdrop;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

// batch 20: all plants are data-defined. This class is the single factory-served
// implementation; per-kind behavior lives in scripts/plants/<Kind>.lua, sprite
// frame + string-id overrides in plantsDesc/<Kind>.json.
@LuaInterface
public class Plant extends LevelObject {

	static private final Map<String, JSONObject> defMap = new HashMap<>();

	// entity kind = factory key ("Firebloom", ...); @Packable for save round-trip
	@Packable
	public String kind;

	private LuaScript script;

	public Plant(int pos) {
		super(pos);
		textureFile = Assets.PLANTS;
	}

	public Plant(){
		this(Level.INVALID_CELL);
	}

	public void setKind(String newKind) {
		kind = newKind;
		applyDef();
	}

	private void applyDef() {
		if (kind == null || kind.isEmpty()) {
			return;
		}
		if (!defMap.containsKey(kind)) {
			if (ModdingMode.isResourceExists("plantsDesc/" + kind + ".json")) {
				defMap.put(kind, JsonHelper.readJsonFromAsset("plantsDesc/" + kind + ".json"));
			} else {
				defMap.put(kind, null);
			}
		}
		JSONObject def = defMap.get(kind);
		if (def != null) {
			imageIndex = def.optInt("imageIndex", imageIndex);
		}
	}

	private LuaScript script() {
		if (script == null && kind != null && !kind.isEmpty()
				&& ModdingMode.isResourceExists("scripts/plants/" + kind + ".lua")) {
			script = new LuaScript("scripts/plants/" + kind, this);
			script.asInstance();
		}
		return script;
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
	public boolean interact(Char ch) {
		if (ch.getSubClass() == HeroSubClass.WARDEN) {
			Buff barkskin = Buff.affect(ch, BuffFactory.BARKSKIN);
				if (barkskin.level() < ch.ht() / 3) barkskin.level(ch.ht() / 3);

			if (Random.Int(5) == 0) {
				Treasury.getLevelTreasury().random(Treasury.Category.SEED).dropAt(ch);
            }
			if (Random.Int(5) == 0) {
				new Dewdrop().dropAt(ch);
			}
		}

		return true;
	}

	@Override
	public void bump(Presser presser) {
		if(presser instanceof Char) {
			interact((Char)presser);
		}

		wither();
		effect(getPos(), presser, presser instanceof Char ? (Char) presser : null);
	}

	private void wither() {
		Dungeon.level.remove(this);
		lo_sprite.ifPresent(
			Gizmo::kill);
		if (Dungeon.isCellVisible(pos)) {
			CellEmitter.get(pos).burst(LeafParticle.GENERAL, 6);
		}
	}

	@Override
	public String desc() {
		return runInfoHook("info", kindParam("Desc"));
	}

	@Override
	public String name() {
		return runInfoHook("name", kindParam("Name"));
	}

	private String runInfoHook(String hook, String fallback) {
		LuaScript s = script();
		if (s != null) {
			String ret = s.runOptional(hook, fallback, level());
			if (ret != null && !ret.isEmpty()) {
				return ret;
			}
		}
		return fallback;
	}

	private String kindParam(String param) {
		String name = kind != null && !kind.isEmpty() ? kind : this.getClass().getSimpleName();
		return Utils.getClassParam(name, param, Utils.EMPTY_STRING, true);
	}

	@LuaInterface
	public void effect(int pos, Presser ch) {
		effect(pos, ch, ch instanceof Char ? (Char) ch : null);
	}

	// activator - who triggered the plant on the target: the same char when the
	// plant is stepped on, the attacking mob when a plant effect is applied by hit
	@LuaInterface
	public void effect(int pos, Presser ch, Char activator) {
		LuaScript s = script();
		if (s != null) {
			s.runOptionalNoRet("effect", pos, ch, activator);
		}
	}

	@Override
	protected void setupFromJson(Level level, JSONObject obj) throws JSONException {
		super.setupFromJson(level, obj);
		setKind(obj.optString("kind", kind));
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		LuaScript s = script();
		if (s != null) {
			bundle.put(LuaEngine.LUA_DATA, s.run("saveData").checkjstring());
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		// legacy saves carry no kind field; the tag names the same kind
		if (kind == null || kind.isEmpty()) {
			String tag = bundle.entityKind();
			if (tag != null) {
				setKind(tag);
			}
		}
		applyDef();

		String luaData = bundle.optString(LuaEngine.LUA_DATA, null);
		if (luaData != null) {
			LuaScript s = script();
			if (s != null) {
				s.runOptionalNoRet("loadData", luaData);
			}
		}
	}

	@Override
	public String getEntityKind() {
		if (kind != null && !kind.isEmpty()) {
			return kind;
		}
		return super.getEntityKind();
	}

	@Override
	public boolean affectItems() {
		return true;
	}
}
