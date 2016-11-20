package com.nyrds.pixeldungeon.levels;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PredesignedLevel extends CustomLevel {

	//for restoreFromBundle
	public PredesignedLevel() {
		super();
	}

	public PredesignedLevel(String fileName) {
		mDescFile = fileName;
		readDescFile(mDescFile);
	}

	@Override
	public void create(int w, int h) {
		try {
			width = mLevelDesc.getInt("width");
			height = mLevelDesc.getInt("height");

			initSizeDependentStuff();

			JSONArray map = mLevelDesc.getJSONArray("map");

			for (int i = 0; i < map.length(); i++) {
				set(i, map.getInt(i));
			}

			placeObjects();
			
			setupLinks();

		} catch (JSONException e) {
			throw new TrackedRuntimeException(e);
		}
		buildFlagMaps();
		cleanWalls();
		createMobs();
		createItems();
	}

	private void placeObjects() throws JSONException {
		if(mLevelDesc.has("objects")) {
			JSONArray objects = mLevelDesc.getJSONArray("objects");

			for (int i = 0; i < objects.length(); i++) {
				JSONObject object = objects.getJSONObject(i);
				addLevelObject(LevelObjectsFactory.createObject(this, object));
			}
		}
	}

	private void setupLinks() throws JSONException {
		JSONArray entranceDesc = mLevelDesc.getJSONArray("entrance");

		entrance = cell(entranceDesc.getInt(0), entranceDesc.getInt(1));

		if(mLevelDesc.has("exit")) {
			JSONArray exitDesc = mLevelDesc.getJSONArray("exit");
			setExit(cell(exitDesc.getInt(0), exitDesc.getInt(1)), 0);
			return;
		}

		if(mLevelDesc.has("multiexit")){
			JSONArray multiExitDesc = mLevelDesc.getJSONArray("multiexit");
			for(int i=0;i<multiExitDesc.length();++i) {
				JSONArray exitDesc = multiExitDesc.getJSONArray(i);
				setExit(cell(exitDesc.getInt(0), exitDesc.getInt(1)), i);
			}
		}
	}

	@Override
	protected boolean build() {
		return true;
	}

	@Override
	protected void decorate() {
	}

	@Override
	protected void createMobs() {
		try {
			if (mLevelDesc.has("mobs")) {
				JSONArray mobsDesc = mLevelDesc.getJSONArray("mobs");

				for (int i = 0; i < mobsDesc.length(); ++i) {
					JSONObject mobDesc = mobsDesc.optJSONObject(i);
					int x = mobDesc.getInt("x");
					int y = mobDesc.getInt("y");

					if(Actor.findChar(cell(x,y))!=null) {
						continue;
					}

					if (cellValid(x, y)) {
						String kind = mobDesc.getString("kind");
						Mob mob = MobFactory.mobClassByName(kind).newInstance();
						mob.fromJson(mobDesc);
						mob.setPos(cell(x, y));
						spawnMob(mob);
					}
				}
			}
		} catch (JSONException e) {
			throw new TrackedRuntimeException("bad mob description", e);
		} catch (Exception e) {
			throw new TrackedRuntimeException(e);
		}
	}

	@Override
	protected void createItems() {
		try {
			if (mLevelDesc.has("items")) {
				JSONArray itemsDesc = mLevelDesc.getJSONArray("items");

				for (int i = 0; i < itemsDesc.length(); ++i) {
					JSONObject itemDesc = itemsDesc.optJSONObject(i);
					int x = itemDesc.getInt("x");
					int y = itemDesc.getInt("y");

					if (cellValid(x, y)) {
						Item item = ItemFactory.createItemFromDesc(itemDesc);

						drop(item, cell(x, y));
					}
				}
			}
		} catch (JSONException e) {
			throw new TrackedRuntimeException("bad items description", e);
		} catch (Exception e) {
			throw new TrackedRuntimeException(e);
		}
	}

	@Override
	protected int nTraps() {
		return 0;
	}
}
