package com.nyrds.pixeldungeon.levels;

import androidx.annotation.Keep;

import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.ModError;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.utils.Bundle;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lombok.SneakyThrows;
import lombok.var;

public class PredesignedLevel extends CustomLevel {

	private boolean useCustomTiles;

	private final LayerId[] descOrder = {LayerId.Roof_Deco,LayerId.Roof_Base,LayerId.Deco2,LayerId.Deco,LayerId.Base};

	@Keep
	public PredesignedLevel() {
		super();
	}

	public PredesignedLevel(String fileName) {
		mDescFile = fileName;
		readDescFile(mDescFile);
	}

	@Override
	@SneakyThrows
	public void create(int w, int h) {
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

		buildFlagMaps();
		cleanWalls();
		createMobs();
		createItems();
		createScript();
	}

	private void readLevelParams() throws JSONException {
		fillMapLayer("baseTileVar", customLayers.get(LayerId.Base));
		fillMapLayer("decoTileVar", customLayers.get(LayerId.Deco));
		fillMapLayer("deco2TileVar", customLayers.get(LayerId.Deco2));

		fillMapLayer("roofBaseTileVar", customLayers.get(LayerId.Roof_Base));
		fillMapLayer("roofDecoTileVar", customLayers.get(LayerId.Roof_Deco));

		useCustomTiles = mLevelDesc.optBoolean("customTiles",false);
	}

	private void fillMapLayer(String layerName, int[] tiles) throws JSONException {
		if(mLevelDesc.has(layerName)) {
			JSONArray layer = mLevelDesc.getJSONArray(layerName);

			for (int i = 0; i < layer.length(); i++) {
				tiles[i]=layer.getInt(i);
			}
		}
	}

	@SneakyThrows
	private void placeObjects() {
		if(mLevelDesc.has("objects")) {
			JSONArray objects = mLevelDesc.getJSONArray("objects");

			for (int i = 0; i < objects.length(); i++) {
				JSONObject object = objects.getJSONObject(i);
				addLevelObject(LevelObjectsFactory.createLevelObject(this, object));
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
	@SneakyThrows
	protected void createMobs() {
		try {
			if (mLevelDesc.has("mobs")) {
				JSONArray mobsDesc = mLevelDesc.getJSONArray("mobs");

				for (int i = 0; i < mobsDesc.length(); ++i) {
					JSONObject mobDesc = mobsDesc.optJSONObject(i);

					String kind = mobDesc.getString("kind");

					int x = mobDesc.getInt("x");
					int y = mobDesc.getInt("y");

					Char chr = Actor.findChar(cell(x,y));

					if(chr!=null) {
						ModError.doReport(kind + ": cell "+ x + "," + y + " already occupied by "+chr.getEntityKind(),
								new Exception("Mobs block error"));
						continue;
					}

					if (cellValid(x, y)) {
						Mob mob = MobFactory.mobByName(kind);
						mob.fromJson(mobDesc);
						mob.setPos(cell(x, y));
						spawnMob(mob);
					} else {
						ModError.doReport(kind+":cell "+ x + "," + y + " are outside valid level area",
								new Exception("Mobs block error"));
						continue;
					}
				}
			}
		} catch (JSONException e) {
			throw ModdingMode.modException("bad mob description",e);
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
						drop(item, cell(x, y), Heap.Type.HEAP);
					}
				}
			}
		} catch (JSONException e) {
			throw ModdingMode.modException("bad items description", e);
		}
	}

	@Override
	protected int nTraps() {
		return 0;
	}

	@Override
	public void discover() {
	}

	@SneakyThrows
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		readLevelParams();
	}

	@Nullable
	private String tilePropertyByCell(int cell, String property, LayerId layerId) {
		if(mLevelDesc.has(property)) {
			try {
				int tile = customLayers.get(layerId)[cell];
				String descId = mLevelDesc.getJSONArray(property).getString(tile);
				if(!descId.isEmpty()) {
					return StringsManager.maybeId(descId);
				}
			} catch (JSONException e) {
				return null;
			}
		}
		return null;
	}


	@Override
	public String tileDescByCell(int cell) {
		String ret;
		for(LayerId layerId:descOrder) {
			 ret = tilePropertyByCell(cell, "decoDesc",layerId);
			 if(ret!=null) {
			 	return ret;
			 }
		}
		return super.tileDescByCell(cell);
	}

	@Override
	public String tileNameByCell(int cell) {
		String ret;
		for(LayerId layerId:descOrder) {
			ret = tilePropertyByCell(cell, "decoName",layerId);
			if(ret!=null) {
				return ret;
			}
		}
		return super.tileNameByCell(cell);
	}

	@Override
	public boolean customTiles() {
		return useCustomTiles;
	}

	@Override
	public boolean isPlainTile(int cell) {
		var baseLayer = getTileLayer(LayerId.Base);
		return baseLayer[cell] <= INVALID_CELL;
	}
}
