

package com.watabou.utils;

public interface Bundlable {

	void restoreFromBundle( Bundle bundle );
	void storeInBundle( Bundle bundle );

	boolean dontPack();

	/**
	 * Entity kind within its system (factory key). Null when the entity does not
	 * take part in kind-based save resolution.
	 */
	default String getEntityKind() {
		return null;
	}

	/**
	 * Saved-entity system tag ("mob", "item", "buff", "levelObject", ...).
	 * Null when the entity does not take part in kind-based save resolution.
	 */
	default String getEntitySystem() {
		return null;
	}

}
