package com.nyrds.pixeldungeon.mechanics.dialog;

import com.nyrds.Packable;

import java.util.ArrayList;
import java.util.List;

public class DialogFactory {

	@Packable
	static private List<Dialog> mDialogList = new ArrayList<Dialog>();

	static {
		initDialogsMap();
	}

	private static void registerDialogClass(Dialog dialog) {
		mDialogList.add(dialog);
	}

	private static void initDialogsMap() {
		//getDialogsFromJson();
	}

	public static Dialog DialogByName(String selectedDialogId) {
		for (Dialog d : mDialogList) {
			if (d.getDialogID().equals(selectedDialogId)) {
				return d;
			}
		}
		return null;
	}

	private void getDialogsFromJson(){

	}
}
