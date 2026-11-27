package com.nyrds.pixeldungeon.support;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.network.HttpConnectionFactory;
import com.nyrds.platform.util.StringsManager;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;


/**
 * "Buy on VK Play PC, get ad-free mobile" entitlement.
 *
 * Stateless server flow (see tools/server/rpd_vkplay_service.py):
 * the game opens the VK ID authorize url with state = base64url(nonce |
 * pkce_verifier), the callback page mints a signed entitlement token and
 * keeps it under the nonce for a short while, the game polls
 * /rpd/vkplay/poll?nonce=... and finally validates the token via
 * /rpd/vkplay/verify (which also re-checks the VK Play purchase).
 */
public class VKPlayEntitlements {

	private static final String VK_ID_AUTHORIZE = "https://id.vk.ru/authorize";
	private static final String VK_ID_CLIENT_ID = "54913942";

	private static final String[] HOSTS = {
			"https://ru.nyrds.net",
			"https://nyrds.net"
	};

	private static final int POLL_INTERVAL_MS = 3000;
	private static final int POLL_TOTAL_MS = 4 * 60 * 1000;
	private static final int CONNECT_TIMEOUT_MS = 5000;
	private static final int READ_TIMEOUT_MS = 10000;

	private static volatile boolean linking;

	public interface StatusListener {
		void onStatus(String message);
	}

	@Nullable
	private static String pickHost() {
		for (String host : HOSTS) {
			try {
				JSONObject ping = httpGetJson(host + "/rpd/vkplay/ping", CONNECT_TIMEOUT_MS);
				if (ping.optBoolean("ok")) {
					return host;
				}
			} catch (Exception ignored) {
			}
		}
		return null;
	}

	private static String b64url(byte[] data) {
		final char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray();
		StringBuilder out = new StringBuilder();
		int val = 0;
		int bits = 0;
		for (byte b : data) {
			val = (val << 8) | (b & 0xff);
			bits += 8;
			while (bits >= 6) {
				out.append(alphabet[(val >> (bits - 6)) & 0x3f]);
				bits -= 6;
			}
		}
		if (bits > 0) {
			out.append(alphabet[(val << (6 - bits)) & 0x3f]);
		}
		return out.toString();
	}

	// caveman: pure-java SHA-256 - TeaVM's classlib has no
	// java.security.MessageDigest and the html gate died on this (PKCE
	// S256 challenge is the only consumer)
	private static final int[] K256 = {
			0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
			0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
			0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
			0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
			0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
			0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
			0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
			0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2 };

	private static int rotr(int x, int n) {
		return (x >>> n) | (x << (32 - n));
	}

	private static byte[] sha256(byte[] data) {
		int[] h = { 0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab,
				0x5be0cd19 };

		int msgLen = data.length;
		int totalLen = msgLen + 1;
		while (totalLen % 64 != 56) {
			totalLen++;
		}
		totalLen += 8;
		byte[] padded = new byte[totalLen];
		System.arraycopy(data, 0, padded, 0, msgLen);
		padded[msgLen] = (byte) 0x80;
		long bitLen = (long) msgLen * 8;
		for (int i = 0; i < 8; i++) {
			padded[totalLen - 1 - i] = (byte) (bitLen >>> (8 * i));
		}

		int[] w = new int[64];
		for (int block = 0; block < totalLen / 64; block++) {
			int base = block * 64;
			for (int i = 0; i < 16; i++) {
				int p = base + i * 4;
				w[i] = ((padded[p] & 0xff) << 24) | ((padded[p + 1] & 0xff) << 16) | ((padded[p + 2] & 0xff) << 8)
						| (padded[p + 3] & 0xff);
			}
			for (int i = 16; i < 64; i++) {
				int s0 = rotr(w[i - 15], 7) ^ rotr(w[i - 15], 18) ^ (w[i - 15] >>> 3);
				int s1 = rotr(w[i - 2], 17) ^ rotr(w[i - 2], 19) ^ (w[i - 2] >>> 10);
				w[i] = w[i - 16] + s0 + w[i - 7] + s1;
			}

			int a = h[0], b = h[1], c = h[2], d = h[3], e = h[4], f = h[5], g = h[6], hh = h[7];
			for (int i = 0; i < 64; i++) {
				int s1 = rotr(e, 6) ^ rotr(e, 11) ^ rotr(e, 25);
				int ch = (e & f) ^ (~e & g);
				int temp1 = hh + s1 + ch + K256[i] + w[i];
				int s0 = rotr(a, 2) ^ rotr(a, 13) ^ rotr(a, 22);
				int maj = (a & b) ^ (a & c) ^ (b & c);
				int temp2 = s0 + maj;
				hh = g;
				g = f;
				f = e;
				e = d + temp1;
				d = c;
				c = b;
				b = a;
				a = temp1 + temp2;
			}
			h[0] += a;
			h[1] += b;
			h[2] += c;
			h[3] += d;
			h[4] += e;
			h[5] += f;
			h[6] += g;
			h[7] += hh;
		}

		byte[] out = new byte[32];
		for (int i = 0; i < 8; i++) {
			out[i * 4] = (byte) (h[i] >>> 24);
			out[i * 4 + 1] = (byte) (h[i] >>> 16);
			out[i * 4 + 2] = (byte) (h[i] >>> 8);
			out[i * 4 + 3] = (byte) h[i];
		}
		return out;
	}

	@Nullable
	private static JSONObject httpGetJson(String url, int connectTimeout) throws Exception {
		HttpURLConnection conn = HttpConnectionFactory.create(new URL(url));
		conn.setConnectTimeout(connectTimeout);
		conn.setReadTimeout(READ_TIMEOUT_MS);
		try {
			return new JSONObject(readAll(conn.getInputStream()));
		} finally {
			conn.disconnect();
		}
	}

	@Nullable
	private static JSONObject httpPostJson(String url, JSONObject body) throws Exception {
		HttpURLConnection conn = HttpConnectionFactory.create(new URL(url));
		conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
		conn.setReadTimeout(READ_TIMEOUT_MS);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setDoOutput(true);
		try {
			byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
			conn.setFixedLengthStreamingMode(payload.length);
			try (OutputStream os = conn.getOutputStream()) {
				os.write(payload);
			}
			return new JSONObject(readAll(conn.getInputStream()));
		} finally {
			conn.disconnect();
		}
	}

	private static String readAll(InputStream in) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int n;
		while ((n = in.read(buf)) > 0) {
			out.write(buf, 0, n);
		}
		in.close();
		return out.toString("UTF-8");
	}

	public static boolean hasEntitlement() {
		return GamePreferences.vkPlayNoAds();
	}

	public static String expiryDate() {
		String token = GamePreferences.vkPlayToken();
		String[] parts = token.split("\\.");
		if (parts.length != 4 || !parts[2].matches("\\d+")) {
			return "";
		}
		try {
			Date d = new Date(Long.parseLong(parts[2]) * 1000L);
			return new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(d);
		} catch (Exception e) {
			return "";
		}
	}

	private static final String ERR_NOT_LINKED = "not_linked";

	/**
	 * Validate a token against both hosts: the bind lives on the host the
	 * desktop linked through, which the phone may probe second.
	 */
	@Nullable
	private static JSONObject verifyToken(String token) {
		JSONObject last = null;
		for (String host : HOSTS) {
			try {
				JSONObject verify = httpPostJson(host + "/rpd/vkplay/verify",
						new JSONObject().put("token", token));
				if (verify != null && verify.optBoolean("ok")) {
					return verify;
				}
				if (verify != null && !ERR_NOT_LINKED.equals(verify.optString("error"))) {
					return verify;
				}
				last = verify;
			} catch (Exception ignored) {
			}
		}
		return last;
	}

	/**
	 * Full link flow: pick a reachable host, open the VK ID consent page,
	 * poll for the grant and validate it. All network work happens on a
	 * background thread; UI updates go through listener on the main thread.
	 */
	public static synchronized void startLink(final StatusListener listener) {
		if (linking) {
			return;
		}
		linking = true;

		GameLoop.execute(() -> {
			try {
				String host = pickHost();
				if (host == null) {
					notify(listener, StringsManager.getVar(R.string.VkPlayLink_noConnection));
					return;
				}

				SecureRandom random = new SecureRandom();
				byte[] nonce = new byte[12];
				random.nextBytes(nonce);
				byte[] verifierBytes = new byte[48];
				random.nextBytes(verifierBytes);
				String verifier = b64url(verifierBytes);
				String state = b64url(concat(nonce, verifier.getBytes(StandardCharsets.US_ASCII)));

				String challenge = b64url(sha256(verifier.getBytes(StandardCharsets.US_ASCII)));
				String authorizeUrl = VK_ID_AUTHORIZE
						+ "?client_id=" + VK_ID_CLIENT_ID
						+ "&redirect_uri=" + URLEncoder.encode(host + "/rpd/vkplay/cb", "UTF-8")
						+ "&response_type=code"
						+ "&state=" + state
						+ "&code_challenge=" + challenge
						+ "&code_challenge_method=s256";

				Game.openUrl(StringsManager.getVar(R.string.VkPlayLink_row), authorizeUrl);

				notify(listener, StringsManager.getVar(R.string.VkPlayLink_waiting));

				String token = null;
				long deadline = System.currentTimeMillis() + POLL_TOTAL_MS;
				while (System.currentTimeMillis() < deadline) {
					Thread.sleep(POLL_INTERVAL_MS);
					JSONObject poll = httpGetJson(host + "/rpd/vkplay/poll?nonce=" + b64url(nonce), CONNECT_TIMEOUT_MS);
					if (poll != null && poll.optBoolean("ok")) {
						token = poll.optString("token");
						break;
					}
				}

				if (token == null || token.isEmpty()) {
					notify(listener, StringsManager.getVar(R.string.VkPlayLink_failed));
					return;
				}

				JSONObject verify = verifyToken(token);
				if (verify == null || !verify.optBoolean("ok")) {
					notify(listener, StringsManager.getVar(R.string.VkPlayLink_failed));
					return;
				}

				GamePreferences.vkPlayToken(token);
				GamePreferences.vkPlayNoAds(true);
				GameLoop.pushUiTask(Game::syncAdsState);

				notify(listener, StringsManager.getVar(R.string.VkPlayLink_done));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				notify(listener, StringsManager.getVar(R.string.VkPlayLink_failed));
			} finally {
				linking = false;
			}
		});
	}

	/**
	 * Desktop flow: proves the purchase with the GameCenter auth ticket
	 * (auth forwarding argv) and links it to the VK ID signed in right here.
	 * The callback binds the vk id to the GC uid; the token is confirmation.
	 */
	public static synchronized void desktopLink(String gcUid, String gcOtp, final StatusListener listener) {
		if (linking) {
			return;
		}
		linking = true;

		GameLoop.execute(() -> {
			try {
				String host = pickHost();
				if (host == null) {
					notify(listener, StringsManager.getVar(R.string.VkPlayLink_noConnection));
					return;
				}

				SecureRandom random = new SecureRandom();
				byte[] nonce = new byte[12];
				random.nextBytes(nonce);
				byte[] verifierBytes = new byte[48];
				random.nextBytes(verifierBytes);
				String verifier = b64url(verifierBytes);
				String nonceB64 = b64url(nonce);
				String state = b64url(concat(nonce, verifier.getBytes(StandardCharsets.US_ASCII)));

				JSONObject link = httpPostJson(host + "/rpd/vkplay/desktop_link",
						new JSONObject()
								.put("uid", gcUid)
								.put("otp", gcOtp)
								.put("nonce", nonceB64));
				if (link == null || !link.optBoolean("ok")) {
					String err = link == null ? "" : link.optString("error");
					if ("gas_no_payment".equals(err)) {
						notify(listener, StringsManager.getVar(R.string.VkPlayLink_notOwned));
					} else if ("gas_unreachable".equals(err)) {
						notify(listener, StringsManager.getVar(R.string.VkPlayLink_noConnection));
					} else {
						notify(listener, StringsManager.getVar(R.string.VkPlayLink_failed));
					}
					return;
				}

				String challenge = b64url(sha256(verifier.getBytes(StandardCharsets.US_ASCII)));
				String authorizeUrl = VK_ID_AUTHORIZE
						+ "?client_id=" + VK_ID_CLIENT_ID
						+ "&redirect_uri=" + URLEncoder.encode(host + "/rpd/vkplay/cb", "UTF-8")
						+ "&response_type=code"
						+ "&state=" + state
						+ "&code_challenge=" + challenge
						+ "&code_challenge_method=s256";

				Game.openUrl(StringsManager.getVar(R.string.VkPlayLink_row), authorizeUrl);

				notify(listener, StringsManager.getVar(R.string.VkPlayLink_waiting));

				String token = null;
				long deadline = System.currentTimeMillis() + POLL_TOTAL_MS;
				while (System.currentTimeMillis() < deadline) {
					Thread.sleep(POLL_INTERVAL_MS);
					JSONObject poll = httpGetJson(host + "/rpd/vkplay/poll?nonce=" + nonceB64, CONNECT_TIMEOUT_MS);
					if (poll != null && poll.optBoolean("ok")) {
						token = poll.optString("token");
						break;
					}
				}

				if (token == null || token.isEmpty()) {
					notify(listener, StringsManager.getVar(R.string.VkPlayLink_failed));
					return;
				}

				GamePreferences.vkPlayToken(token);
				GamePreferences.vkPlayNoAds(true);

				notify(listener, StringsManager.getVar(R.string.VkPlayLink_doneDesktop));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				notify(listener, StringsManager.getVar(R.string.VkPlayLink_failed));
			} finally {
				linking = false;
			}
		});
	}

	/**
	 * Startup re-validation: keeps the entitlement only if the token is still
	 * valid and the purchase is still there (refunds). Network errors keep
	 * the current state.
	 */
	public static void revalidate() {
		if (!hasEntitlement()) {
			return;
		}
		final String token = GamePreferences.vkPlayToken();
		if (token.isEmpty()) {
			GamePreferences.vkPlayNoAds(false);
			return;
		}
		GameLoop.execute(() -> {
			try {
				JSONObject verify = verifyToken(token);
				if (verify == null || !verify.optBoolean("ok")) {
					GamePreferences.vkPlayToken("");
					GamePreferences.vkPlayNoAds(false);
					GameLoop.pushUiTask(Game::syncAdsState);
				}
			} catch (Exception ignored) {
			}
		});
	}

	private static void notify(final StatusListener listener, final String message) {
		if (listener == null) {
			return;
		}
		GameLoop.pushUiTask(() -> listener.onStatus(message));
	}

	private static byte[] concat(byte[] a, byte[] b) {
		byte[] out = new byte[a.length + b.length];
		System.arraycopy(a, 0, out, 0, a.length);
		System.arraycopy(b, 0, out, a.length, b.length);
		return out;
	}
}
