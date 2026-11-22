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
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
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

	private static byte[] sha256(byte[] data) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
