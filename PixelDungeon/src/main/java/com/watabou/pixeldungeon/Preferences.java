/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon;

import com.nyrds.android.util.UserKey;
import com.watabou.noosa.Game;

import android.content.SharedPreferences;

enum Preferences {

	INSTANCE;
	
	public static final String KEY_LANDSCAPE	    = "landscape";
	public static final String KEY_IMMERSIVE	    = "immersive";
	public static final String KEY_SCALE_UP		    = "scaleup";
	public static final String KEY_MUSIC		    = "music";
	public static final String KEY_SOUND_FX		    = "soundfx";
	public static final String KEY_ZOOM			    = "zoom";
	public static final String KEY_LAST_CLASS	    = "last_class";
	public static final String KEY_CHALLENGES	    = "challenges";
	public static final String KEY_DONATED		    = "donated";
	public static final String KEY_INTRO		    = "intro";
	public static final String KEY_BRIGHTNESS	    = "brightness";
	public static final String KEY_LOCALE           = "locale";
	public static final String KEY_SECOND_QUICKSLOT = "second_quickslot";
	public static final String KEY_THIRD_QUICKSLOT  = "third_quickslot";
	public static final String KEY_VERSION          = "version";
	public static final String KEY_FONT_SCALE       = "font_scale";
	public static final String KEY_CLASSIC_FONT     = "classic_font";
	public static final String KEY_PREMIUM_SETTINGS = "premium_settings";
	public static final String KEY_REALTIME         = "realtime";
	public static final String KEY_ACTIVE_MOD       = "active_mod";
	
	
	private SharedPreferences prefs;
	
	private SharedPreferences get() {
		if (prefs == null) {
			prefs = Game.instance().getPreferences( Game.MODE_PRIVATE );
		}
		return prefs;
	}
	
	int getInt( String key, int defValue  ) {
		String defVal = Integer.toString(defValue);
		String propVal = getString(key, defVal);
		try{
			return Integer.parseInt(propVal);
		} catch (NumberFormatException e) {
			put(key, defValue);
			return defValue;
		}
	}
	
	boolean getBoolean( String key, boolean defValue  ) {
		String defVal = Boolean.toString(defValue);
		String propVal = getString(key, defVal);
		
		return Boolean.parseBoolean(propVal);
	}
	
	boolean checkString(String key) {
		try{
			get().getString( key, "");
		} catch(ClassCastException e) {
			return false;
		}
		return true;
	}
	
	boolean checkInt(String key) {
		try{
			get().getInt( key, 0);
		} catch(ClassCastException e) {
			return false;
		}
		return true;
	}

	boolean checkBoolean(String key) {
		try{
			get().getBoolean( key, false);
		} catch(ClassCastException e) {
			return false;
		}
		return true;
	}
	
	String getString( String key, String defValue ) {
		
		try{
			String scrambledKey = UserKey.encrypt(key);
			
			if(get().contains(scrambledKey)) {
				String encVal = get().getString( scrambledKey, UserKey.encrypt(defValue) );
				return UserKey.decrypt(encVal);
			} 
			
			if (get().contains(key)) {
				String val = "";
				
				if(checkString(key)){
					val = get().getString( key, defValue);
				}
				if(checkInt(key)){
					val = Integer.toString(get().getInt( key, Integer.parseInt(defValue)));
				}
				if(checkBoolean(key)){
					val = Boolean.toString(get().getBoolean( key, Boolean.parseBoolean(defValue)));
				}			
				
				get().edit().putString(scrambledKey, UserKey.encrypt(val)).commit();
				get().edit().remove(key).commit();
				return val;
			}
		} catch (ClassCastException e) {
			//just return default value when loading old preferences
		} catch (Exception e) {

		}
		return defValue;
	}
	
	void put( String key, int value ) {
		String val = Integer.toString(value);
		put(key, val);
	}
	
	void put( String key, boolean value ) {
		String val = Boolean.toString(value);
		put(key,val);
	}
	
	void put( String key, String value ) {
		String scrambledVal = UserKey.encrypt(value);
		String scrambledKey = UserKey.encrypt(key);
		get().edit().putString( scrambledKey, scrambledVal ).commit();
	}
}
