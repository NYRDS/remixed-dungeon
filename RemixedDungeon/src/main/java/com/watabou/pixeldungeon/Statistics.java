
package com.watabou.pixeldungeon;

import com.watabou.utils.Bundle;

public class Statistics {

	public static int goldCollected;
	public static int deepestFloor;
	public static int enemiesSlain;
	public static int foodEaten;
	public static int potionsCooked;
	public static int piranhasKilled;
	public static int nightHunt;
	public static int ankhsUsed;
	
	public static float duration;

	public static boolean qualifiedForNoKilling = false;
	public static boolean completedWithNoKilling = false;
	
	public static boolean amuletObtained = false;
	
	public static void reset() {
		
		goldCollected	= 0;
		deepestFloor	= 0;
		enemiesSlain	= 0;
		foodEaten		= 0;
		potionsCooked	= 0;
		piranhasKilled	= 0;
		nightHunt		= 0;	
		ankhsUsed		= 0;
		
		duration	= 0;
		
		qualifiedForNoKilling = false;
		
		amuletObtained = false;
		
	}
	
	private static final String GOLD		= "score";
	public static final String DEEPEST		= "maxDepth";
	private static final String SLAIN		= "enemiesSlain";
	private static final String FOOD		= "foodEaten";
	private static final String ALCHEMY		= "potionsCooked";
	private static final String PIRANHAS	= "piranhas";
	private static final String NIGHT		= "nightHunt";
	private static final String ANKHS		= "ankhsUsed";
	private static final String DURATION	= "duration";
	private static final String AMULET		= "amuletObtained";

	private static final String COMPLETED_WITH_NO_KILLING = "completedWithNoKilling";
	private static final String QUALIFIED_FOR_NO_KILLING = "qualifiedForNoKilling";

	public static void storeInBundle( Bundle bundle ) {
		bundle.put( GOLD,		goldCollected );
		bundle.put( DEEPEST,	deepestFloor );
		bundle.put( SLAIN,		enemiesSlain );
		bundle.put( FOOD,		foodEaten );
		bundle.put( ALCHEMY,	potionsCooked );
		bundle.put( PIRANHAS,	piranhasKilled );
		bundle.put( NIGHT,		nightHunt );
		bundle.put( ANKHS,		ankhsUsed );
		bundle.put( DURATION,	duration );
		bundle.put( AMULET,		amuletObtained );

		bundle.put(QUALIFIED_FOR_NO_KILLING, qualifiedForNoKilling);
		bundle.put(COMPLETED_WITH_NO_KILLING,completedWithNoKilling);
	}
	
	public static void restoreFromBundle( Bundle bundle ) {
		goldCollected	= bundle.getInt( GOLD );
		deepestFloor	= bundle.getInt( DEEPEST );
		enemiesSlain	= bundle.getInt( SLAIN );
		foodEaten		= bundle.getInt( FOOD );
		potionsCooked	= bundle.getInt( ALCHEMY );
		piranhasKilled	= bundle.getInt( PIRANHAS );
		nightHunt		= bundle.getInt( NIGHT );
		ankhsUsed		= bundle.getInt( ANKHS );
		duration		= bundle.getFloat( DURATION );
		amuletObtained	= bundle.getBoolean( AMULET );

		qualifiedForNoKilling  = bundle.getBoolean(QUALIFIED_FOR_NO_KILLING);
		completedWithNoKilling = bundle.getBoolean(COMPLETED_WITH_NO_KILLING);
	}

}
