# Boosty supporter page + "no ads for supporters" investigation

Date: 2026-11-22. Page: https://boosty.to/nyrds

## What is on the page now (all live-verified)

- Banner/avatar already had game art; title «Разработка open-source рогалика Remixed Dungeon».
- About description (RU, LanguageTool-checked): short pitch, no perk promises.
- 4 subscription levels mirroring the in-game donation ladder (`WndDonate` silver/gold/ruby/royal):
  | Boosty level | id | price |
  |---|---|---|
  | Серебряный уровень | 4185194 | 99 ₽/mo |
  | Золотой уровень | 4185195 | 199 ₽/mo |
  | Рубиновый уровень | 4185196 | 499 ₽/mo |
  | Королевский уровень | 4185197 | 999 ₽/mo |
- First public post «Поддержка Remixed Dungeon на Boosty» with GitHub / Google Play / RuStore links.
- Left for Mike: **Cash withdrawal tutorial step** (payout method + KYC) — without it
  `POST /v1/stat/sales/` answers 404 `Category disabled` (monetization section disabled).
  Also unresolved tutorial item in "First steps" (4/5).

## How ads are gated in the game today

`GamePreferences.donated()` (0–4, `CommonPrefs.KEY_DONATED`):
- `donated() > 0` → **no ads** (`WndDifficultyOptions`, `MovieRewardTask`), easy+normal-with-saves difficulties unlocked;
- `>= 1..3` unlocks huntress/elf/gnoll+doctor+priest; `== 4` all accessories.

Set only by Google Play / RuStore IAP purchases `supporter_level_1..4`
(`RemixedDungeon/src/android/java/com/nyrds/platform/support/Iap.java:55-70`, one-time SKUs).

A Boosty-driven grant only needs to reach `GamePreferences.setDonationLevel(int)` after some
verification step. No account system exists in the game.

## Boosty API findings (verified live with the creator token, 2026-11-22)

- **No official public API docs, no webhooks.** `hasAccessToDevApi:false` in
  `GET /v1/user/current` suggests a gated developer program exists, but it is not
  publicly documented anywhere (web search found nothing); treat as unavailable until
  Boosty support grants it.
- Internal REST API works fine with the creator's Bearer token (from `localStorage.auth`
  on boosty.to; access token lives ~1 month, **refresh tokens rotate on every refresh —
  a server must persist the new pair or the session dies**).
- Live-verified endpoints (trailing slash is significant):
  - `GET /v1/blog/{blog}` — public blog profile;
  - `GET /v1/blog/{blog}/subscription_level/` — levels with `currencyPrices` per currency;
  - `GET /v1/blog/{blog}/subscribers?limit=..&offset=..` — **owner-only subscriber list**;
    each entry: user + email, `payments` (total paid), `price`, `status` ("active"...),
    `onTime`, `nextPayTime`, `levelId` + `level` object. This is enough for verification.
  - `POST /v1/stat/sales/` (form-urlencoded, type=subscription) — needs monetization enabled.
  - Messaging: `POST /v1/dialog/`, `POST /v1/dialog/{id}/message` — **DM to paid subscribers
    is allowed** by current `dialogSettings` (`canSendPaidSubscribers:true`) → codes can be
    delivered automatically.
- Native supporter-granting integrations exist only for Discord/Telegram/Max roles —
  nothing for a game client.
- Community API reference used: beekamai/boosty-api (TS, live-verified paths),
  barsikus007/boosty (py). Unofficial, may change without notice.

## Integration options for "Boosty → no ads"

**DECIDED 2026-11-22 (Mike): option A with full-mirror lapse — entitlement active only while
the Boosty subscription is active** (expires at `nextPayTime`, everything reverts on lapse;
ruling recorded in docs/gameplay_design_decisions.md).

Concrete design (rides the VK Play entitlement stack, bd remixed-dungeon-c90q):

1. **Server** `tools/server/rpd_boosty_service.py` (deploy like `rpd-vkplay.service`, secrets in env):
   - Poller every ~6h: `GET /v1/blog/nyrds/subscribers` (owner Bearer, paged) → snapshot
     `sub_id → {levelId, status, nextPayTime}`; **persists the rotated refresh-token pair on
     every refresh** (session dies otherwise).
   - Level map: 4185194→1, 4185195→2, 4185196→3, 4185197→4.
   - Codes derived, not stored: `code = base32(HMAC(CODE_SECRET, sub_id))[:10]`.
   - `POST /rpd/boosty/redeem {code, device}`: status=active + device cap (3/code) → mint
     the VK Play-style token `v1.b{sub_id}.{exp=nextPayTime}.{hmac}` (game-side `expiryDate()`
     parser already reads parts[2]).
   - `POST /rpd/boosty/verify {token}`: signature + expiry + still active in snapshot →
     startup revalidation + lapse handling.
   - Code delivery: Boosty DM per subscriber (manual while the list is small;
     `canSendPaidSubscribers` is already allowed, dialog API can automate it).
2. **Game** `BoostyEntitlements.java` mirroring `VKPlayEntitlements.java`:
   - redeem → prefs (`boostyToken`, `boostyNoAds`, `boostyLevel`) → `syncAdsState()`;
     `revalidate()` wired at startup (VK Play's own revalidate has no call site yet — wire both).
   - UI: Boosty tab/row in `WndDonate` (code field + Activate + expiry line), `WndVkPlayLink` shape.
   - Ads gates: `Game.java:183`, `WndSaveSlotSelect.java:329`, `AdsUtilsCommon.java:104/113`
     get `|| boostyNoAds()`.
   - Unlocks: `GamePreferences.supporterLevel()` = `max(donated(), boostyLevel())` replaces
     bare `donated()` reads in `StartScene:216-220` and `Accessory.java:140` (IAP and Boosty compose).
   - l10n en/ru + LanguageTool. Window lives in main source — fdroid/desktop get class unlocks too
     (no IAP there, Boosty becomes the only supporter path).

Original options analysis (kept for the record):

**A. Server-verified promo codes** — chosen above.
**B. In-game Boosty login (OAuth)** — blocked on the undocumented `hasAccessToDevApi` program.
**C. Manual** — Mike checks subscribers and DMs codes; same game-side UI as A, still needs the server for expiry.
- Player-side friction: copy a code from the Boosty DM into the game.
- Server-side friction: token rotation handling, that's it.

**B. In-game Boosty login (OAuth) — blocked.** Needs per-user OAuth = the gated DevApi.
If Boosty ever grants it, this is the cleanest (verify subscription directly, no codes).

**C. Manual.** Mike checks subscribers and emails/DMs codes; same game-side UI as A.

NOT doable: silent client-side check — no public API for the *player* identity.

## Pricing note

Boosty is monthly-recurring, in-game `donated()` is a one-time IAP concept. If Boosty
supporters are meant to keep ad-free status while subscribed, the server-side expiry in
option A step 4 is the honest mapping. Alternatively treat a Boosty payment as a one-time
grant mirroring IAP (no expiry) — simpler, but a 99 ₽ month would then permanently beat the
IAP price; Mike's call (gameplay_design_decisions.md material).
