package com.watabou.pixeldungeon.sprites;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.nyrds.android.util.ModdingMode;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.utils.GLog;

public class MobSpriteDef extends MobSprite {
	
	private int bloodColor;
	private boolean levitating;
	private int framesInRow;
	private int kind;
	
	static private Map<String, JSONObject> defMap = new HashMap<String, JSONObject>();
	
	private String name;
	
	public MobSpriteDef(String defName, int kind) throws Exception {
		super();
		
		name = defName;
		
		if(defMap.containsKey(name)) {
			 
		} else {
			InputStream stream = ModdingMode.getInputStream(name);
			StringBuilder animationDef = new StringBuilder();
			
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(stream));
			
			String line = reader.readLine();
			
			while (line != null) {
				animationDef.append(line);
				line = reader.readLine();
			}
			reader.close();
			
			defMap.put(name, (JSONObject) new JSONTokener(animationDef.toString()).nextValue());
		}
		
		selectKind(kind);
	}

	@Override
	public void selectKind(int kind) {
		this.kind = kind;
		JSONObject json = defMap.get(name);
		
		try {
			texture(json.getString("texture"));
			
			int width = json.getInt("width");
			
			TextureFilm film = new TextureFilm(texture, width, json.getInt("height"));
			
			bloodColor  = json.optInt("bloodColor",      0xFFBB0000);
			levitating  = json.optBoolean("levitating",  false);
			framesInRow = texture.width / width;
			
			idle   = readAnimation(json, "idle",   film);
			run    = readAnimation(json, "run",    film);
			attack = readAnimation(json, "attack", film);
			die    = readAnimation(json, "die",    film);
			zap    = attack.clone();
			
		} catch (Exception e) {
			GLog.w("Something bad happens when loading %s", name);
		}
		
		play(idle);
	}
	
	private Animation readAnimation(JSONObject root, String animKind, TextureFilm film) {
		try {
			
			JSONObject jsonAnim = root.getJSONObject(animKind);
			
			Animation anim = new Animation(jsonAnim.getInt("fps"), jsonAnim.getBoolean("looped"));
			
			List<Integer> framesSeq = new ArrayList<Integer>(16);
			
			JSONArray jsonFrames = jsonAnim.getJSONArray("frames");
			
			int nextFrame;
			
			for(int i = 0; (nextFrame = jsonFrames.optInt(i, -1))!= -1; ++i) {
				framesSeq.add(nextFrame);
			}
			
			anim.frames(film, framesSeq, kind * framesInRow);
			
			return anim;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public void link( Char ch ) {
		super.link( ch );
		if(levitating) {
			add( State.LEVITATING );
		}
	}
	
	@Override
	public void die() {
		super.die();
		if(levitating) {
			remove( State.LEVITATING );
		}
	}
	
	@Override
	public int blood() {
		return bloodColor;
	}

}
