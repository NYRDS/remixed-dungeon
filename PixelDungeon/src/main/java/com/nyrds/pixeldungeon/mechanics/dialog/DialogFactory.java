package com.nyrds.pixeldungeon.mechanics.dialog;

import com.nyrds.Packable;
import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.TrackedRuntimeException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DialogFactory {
	@Packable
	static private List<String> mDialogList = new ArrayList<String>();

	static private JSONObject initDialogs = JsonHelper.readJsonFromAsset("dialogDesc/Dialogs.json");

	static {
		initDialogsMap();
	}

	private static void initDialogsMap() {
		if (initDialogs.has("dialogs")) {
			try {
				JSONArray dialogs = initDialogs.getJSONArray("dialogs");
				for (int i = 0; i < dialogs.length(); ++i) {
					if (dialogs.getJSONObject(i).has("id")) {
						mDialogList.add(dialogs.getJSONObject(i).optString("id", "test"));
					}
				}
			} catch (JSONException e) {
				throw new TrackedRuntimeException(e);
			}
		}
	}

	public static String dialogByName(String selectedDialogId) {
		for (String d : mDialogList) {
			if (d.equals(selectedDialogId)) {
				return d;
			}
		}
		return null;
	}

	private void getDialogsFromJson(){
		
	}
}
