package com.watabou.pixeldungeon.items;

import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import org.jetbrains.annotations.NotNull;

/**
 * Kind-keyed variant of {@link ItemStatusHandler}: identification state
 * (random per-run image + known set) keyed by entity-kind strings instead of
 * classes, so framework classes can serve whole item categories
 * (potions/scrolls batches). Saves keep the legacy key format
 * ("class &lt;legacyPackage&gt;.&lt;Kind&gt;_image") verbatim - old saves
 * from the per-class era and new saves stay interchangeable in both
 * directions.
 */
public class ItemStatusHandlerKinds {

	private final String legacyPackage;
	private final String[] kinds;

	private final HashMap<String, Integer> images = new HashMap<>();
	private final HashSet<String> known           = new HashSet<>();

	public ItemStatusHandlerKinds(String legacyPackage, String[] kinds, Integer[] allImages) {
		this.legacyPackage = legacyPackage;
		this.kinds = kinds;

		ArrayList<Integer> imagesLeft = new ArrayList<>(Arrays.asList(allImages));
		for (String kind : kinds) {
			assignRandomImage(imagesLeft, kind);
		}
	}

	public ItemStatusHandlerKinds(String legacyPackage, String[] kinds, Integer[] images, Bundle bundle) {
		this.legacyPackage = legacyPackage;
		this.kinds = kinds;

		restore(bundle, images);
	}

	private String key(String kind) {
		return "class " + legacyPackage + "." + kind;
	}

	private static final String PFX_IMAGE	= "_image";
	private static final String PFX_KNOWN	= "_known";

	public void save(Bundle bundle) {
		for (String kind : kinds) {
			bundle.put(key(kind) + PFX_IMAGE, images.get(kind));
			bundle.put(key(kind) + PFX_KNOWN, known.contains(kind));
		}
	}

	private void restore(Bundle bundle, Integer[] allImages) {
		ArrayList<Integer> imagesLeft = new ArrayList<>(Arrays.asList(allImages));

		for (String kind : kinds) {
			String key = key(kind);

			if (bundle.contains(key + PFX_IMAGE)) {
				Integer image = bundle.getInt(key + PFX_IMAGE);

				if (imagesLeft.contains(image)) {
					images.put(kind, image);
					imagesLeft.remove(image);
				} else {
					assignRandomImage(imagesLeft, kind);
				}
				if (bundle.getBoolean(key + PFX_KNOWN)) {
					known.add(kind);
				}
			} else {
				assignRandomImage(imagesLeft, kind);
			}
		}
	}

	private void assignRandomImage(@NotNull ArrayList<Integer> imagesLeft, String kind) {
		int index = Random.Int(imagesLeft.size());

		images.put(kind, imagesLeft.get(index));
		imagesLeft.remove(index);
	}

	public int index(String kind) {
		Integer image = images.get(kind);
		if (image == null) {
			return 0;
		}
		return image;
	}

	public boolean isKnown(String kind) {
		return known.contains(kind);
	}

	public void know(String kind) {
		known.add(kind);

		if (known.size() == kinds.length - 1) {
			for (String k : kinds) {
				if (!known.contains(k)) {
					known.add(k);
					break;
				}
			}
		}
	}

	public HashSet<String> known() {
		return known;
	}

	public HashSet<String> unknown() {
		HashSet<String> result = new HashSet<>();
		for (String k : kinds) {
			if (!known.contains(k)) {
				result.add(k);
			}
		}
		return result;
	}

	public int kindsCount() {
		return kinds.length;
	}

	public boolean allKnown() {
		return known.size() == kinds.length;
	}
}
