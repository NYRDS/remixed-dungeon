
package com.watabou.pixeldungeon;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.util.StringsManager;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Journal {

	@Deprecated
	public enum Feature {
		WELL_OF_HEALTH			(StringsManager.getVar(R.string.Journal_WellHealt)),
		WELL_OF_AWARENESS		(StringsManager.getVar(R.string.Journal_WellAwareness)),
		WELL_OF_TRANSMUTATION	(StringsManager.getVar(R.string.Journal_WellTransmut)),
		ALCHEMY					(StringsManager.getVar(R.string.Journal_Alchemy)),
		GARDEN					(StringsManager.getVar(R.string.Journal_Garden)),

		GHOST					(StringsManager.getVar(R.string.Journal_Ghost)),
		WANDMAKER				(StringsManager.getVar(R.string.Journal_Wandmaker)),
		TROLL					(StringsManager.getVar(R.string.Journal_Troll)),
		IMP						(StringsManager.getVar(R.string.Journal_Imp)),
		AZUTERRON				(StringsManager.getVar(R.string.Journal_Azuterron)),
		CAGEDKOBOLD				(StringsManager.getVar(R.string.Journal_Caged_Kobold)),
		SCARECROW				(StringsManager.getVar(R.string.Journal_ScarecrowNPC)),
		PLAGUEDOCTOR			(StringsManager.getVar(R.string.Journal_PlagueDoctorNPC));


		private final String desc;

		Feature(String desc) {
			this.desc = desc;
		}

		public String desc() {
			return desc;
		}
	}

	public static class Record implements Comparable<Record>, Bundlable {

		@Deprecated
		private static final String FEATURE	= "feature";

		@Packable
		private String feature;

		@Packable
		public int depth;

		public Record() {
		}

		public Record( String featureDesc, int depth ) {
			this.feature = featureDesc;
			this.depth = depth;
		}

		public String getFeature() {
			return StringsManager.maybeId(feature);
		}

		@Override
		public int compareTo(@NotNull Record another ) {
			return another.depth - depth;
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {

			try {
				feature = Feature.valueOf( bundle.getString( FEATURE ) ).desc();
				EventCollector.logException("old save: Journal.Feature");
			} catch (Exception ignored) {
			}

		}

		@Override
		public void storeInBundle( Bundle bundle ) {
		}

		public boolean dontPack() {
			return false;
		}
	}
	
	public static ArrayList<Record> records;
	
	public static void reset() {
		records = new ArrayList<>();
	}
	
	private static final String JOURNAL	= "journal";
	
	public static void storeInBundle( Bundle bundle ) {
		bundle.put( JOURNAL, records );
	}
	
	public static void restoreFromBundle( Bundle bundle ) {
		records = new ArrayList<>();
		records.addAll(bundle.getCollection(JOURNAL, Record.class));
	}
	
	public static boolean dontPack() {
		return false;
	}

	public static void add (int featureResourceId) {
		add(StringsManager.getVar(featureResourceId));
	}

	public static void add( String feature ) {
		int size = records.size();
		for (int i=0; i < size; i++) {
			Record rec = records.get( i );
			if (rec.feature.equals(feature) && rec.depth == Dungeon.depth) {
				return;
			}
		}
		
		records.add( new Record( feature, Dungeon.depth ) );
	}

	public static void remove( int featureResourceId) {
		remove(StringsManager.getVar(featureResourceId));
	}

	public static void remove( String feature ) {
		int size = records.size();
		for (int i=0; i < size; i++) {
			Record rec = records.get( i );
			if (rec.feature.equals(feature) && rec.depth == Dungeon.depth) {
				records.remove( i );
				return;
			}
		}
	}
}
