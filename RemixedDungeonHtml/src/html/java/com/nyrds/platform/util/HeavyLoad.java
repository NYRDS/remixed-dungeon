package com.nyrds.platform.util;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.typedarrays.Int8Array;

/**
 * Progress splash for lazily fetched heavy resources (the CJK fallback font
 * today; anything else that can stall on a slow link later). The overlay
 * lives in index.html (window.__rdLoad) and only appears once a load has
 * been in flight for a few hundred ms, so fast localhost fetches never
 * flash it. fetchBytes() downloads with progress events and hands the bytes
 * to the callback (null on failure) - run the continuation on the game
 * thread yourself (Gdx.app.postRunnable), the callback fires on the XHR
 * thread context.
 */
public class HeavyLoad {

	@JSFunctor
	public interface BytesCallback extends JSObject {
		void accept(Int8Array data);
	}

	@JSBody(params = {"id", "label"}, script =
			"if (window.__rdLoad) { window.__rdLoad.start(id, label); }")
	private static native void jsStart(String id, String label);

	@JSBody(params = {"id"}, script =
			"if (window.__rdLoad) { window.__rdLoad.end(id); }")
	private static native void jsEnd(String id);

	@JSBody(params = {"url", "id", "cb"}, script =
			"var x = new XMLHttpRequest();"
			+ "x.open('GET', url, true);"
			+ "x.responseType = 'arraybuffer';"
			+ "x.onprogress = function(e) {"
			+ " if (window.__rdLoad) { window.__rdLoad.progress(id, e.loaded, e.total || 0); } };"
			+ "x.onload = function() {"
			+ " if (x.status >= 200 && x.status < 300 && x.response) {"
			+ "  cb(new Int8Array(x.response));"
			+ " } else { cb(null); } };"
			+ "x.onerror = function() { cb(null); };"
			+ "x.send();")
	private static native void jsFetchBytes(String url, String id, BytesCallback cb);

	/**
	 * Fetches url as bytes with splash progress under the given load id.
	 * The callback receives the bytes, or null if the download failed;
	 * the splash entry is always retired, even if the callback throws.
	 */
	public static void fetchBytes(String url, String id, String label, BytesCallback cb) {
		jsStart(id, label);
		jsFetchBytes(url, id, data -> {
			try {
				cb.accept(data);
			} finally {
				jsEnd(id);
			}
		});
	}
}
