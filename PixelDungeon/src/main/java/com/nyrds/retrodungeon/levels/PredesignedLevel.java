package com.nyrds.retrodungeon.levels;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.retrodungeon.items.common.ItemFactory;
import com.nyrds.retrodungeon.levels.objects.LevelObjectsFactory;
import com.nyrds.retrodungeon.mobs.common.MobFactory;
import com.watabou.noosa.StringsManager;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.utils.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PredesignedLevel extends CustomLevel {

	private boolean useCustomTiles;

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

			readLevelParams();

			placeObjects();
			
			setupLinks();

		} catch (JSONException e) {
			throw new TrackedRuntimeException(e);
		}
		buildFlagMaps();
		cleanWalls();
		createMobs();
		createItems();
		createScript();
	}

	private void readLevelParams() throws JSONException {
		fillMapLayer("baseTileVar", baseTileVariant);
		fillMapLayer("decoTileVar", decoTileVariant);

		useCustomTiles = mLevelDesc.optBoolean("customTiles",false);
	}

	private void fillMapLayer(String layerName, int[] baseTileVariant) throws JSONException {
		if(mLevelDesc.has(layerName)) {
			JSONArray layer = mLevelDesc.getJSONArray(layerName);

			for (int i = 0; i < layer.length(); i++) {
				baseTileVariant[i]=layer.getInt(i);
			}
		}
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

		// Set compass target if the level description has it
		if(mLevelDesc.has("compassTarget")){
			JSONArray compassTargetCoordinates = mLevelDesc.getJSONArray("compassTarget");
			int x, y;	// Calculate cell ID from coordinates
			x = compassTargetCoordinates.getInt(0);
			y = compassTargetCoordinates.getInt(1);

			setCompassTarget(x,y);
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
						Mob mob = MobFactory.mobByName(kind);
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

	@Override
	public void discover() {
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		try {
			readLevelParams();
		} catch (JSONException e) {
			throw new TrackedRuntimeException(e);
		}

	}

	@Override
	public String tileDescByCell(int cell) {
		if(mLevelDesc.has("decoDesc")) {
			try {
				int tile = decoTileVariant[cell];
				String descId = mLevelDesc.getJSONArray("decoDesc").getString(tile);
				if(!descId.isEmpty()) {
					return StringsManager.maybeId(descId);
				}
			} catch (JSONException e) {
				return super.tileDescByCell(cell);
			}
		}
		return super.tileDescByCell(cell);
	}

	@Override
	public String tileNameByCell(int cell) {
		if(mLevelDesc.has("decoName")) {
			try {
				int tile = decoTileVariant[cell];
				String nameId = mLevelDesc.getJSONArray("decoName").getString(tile);
				if(!nameId.isEmpty()) {
					return StringsManager.maybeId(nameId);
				}
			} catch (JSONException e) {
				return super.tileNameByCell(cell);
			}
		}
		return super.tileNameByCell(cell);
	}

	@Override
	public boolean customTiles() {
		return useCustomTiles;
	}
}
