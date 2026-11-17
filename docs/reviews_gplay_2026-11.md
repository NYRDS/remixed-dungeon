# Google Play reviews & developer replies — audit 2026-11-17

Data source: RemixedGuardian bot on dot35 (`~/RemixedGuardian`). Reviews it relayed to
Discord `#reviews` since 2023-10-25 are in the `reviews` shelve dbm (446 entries); dev
replies are not written back to the dbm, so replies were reconstructed from the
`#reviews` Discord channel history (850 messages, oldest 2023-10-25) via the
message→reviewId map, cross-checked with Play-stored `developerComment` fields (48)
and `feedback.db` (8 gplay rows since 2026-10-13). A fresh read-only
`reviews.list()` fetch added 2 new reviews; note the API only returns reviews that
have no developer reply yet, so it can't be used as an archive.

Raw dataset archived at dot35: `~/RemixedGuardian/audit-2026-11-17/`
(merged_slim.json, replies_slim.json, summary.json, unanswered lists).

## Headline numbers (446 reviews, 2023-10 → 2026-11-17)

| Metric | Value |
|---|---|
| Average rating (bot window) | 4.11 (5★ 273 / 4★ 71 / 3★ 31 / 2★ 22 / 1★ 49) |
| Answered (by Mike: 291 via Discord + 19+ console) | 310 (70%) |
| Reply latency | median 4.6d, mean 34d, p90 134d |
| Reviews by year (avg / answered%) | 2023: 46, 4.20, 98% · 2024: 204, 4.04, 94% · 2025: 118, 4.24, 53% · 2026: 78, 4.08, **15%** |
| Languages | ru 186 (42%), en 76, es 59, pt 33, id 17, vi 12, tr 12, fr 8, pl 8, uk 5, zh-TW 4, de 3, th 3, ko 3, it 2 |

**The single biggest finding: replying has effectively stopped.** Through 2024 nearly
every review got a same-week reply. Since mid-2025 only sporadic bursts remain
(2025-10: 2, 2025-12: 2, 2026-01: 2, 2026-05: 2, 2026-08: 4, 2026-09: 3, 2026-11: 1).
136 reviews are unanswered all-time; 12 in the last 90 days; 18 of the last 12 months'
reviews are ≤3★ and unanswered (list below).

## Reply history — three eras

1. **2023-10 → 2025-01, handwritten.** Personal, witty, RU/EN mixed, often asks for
   specifics ("Thank you for review, may you please list some of said bugs, so I cloud
   fix them?"), explains design («Классы сделаны разными специально…»), gently pushes
   back on 1★ («Отзыв хороший, но конкретику про баги… в нем конечно никто не напишет»).
   Players noticed — one review says they read the dev replies longer than they played.
2. **2025-02-22, one LLM-generated batch (~60 replies).** Corporate register
   ("We're thrilled… your support means a lot 😊"), visible duplicates: review
   [368d277b] got 3 replies, [38aa193e] and [32c3cfe1] repeat texts. Some replies
   address reviewers by first name parsed from usernames. A few 1★ answers turn
   mocking ("too advanced for your unique playstyle") — riskier than the old dry wit.
3. **2025-03 → 2025-06, hybrid batches.** Short, playful, emoji'd, per-review-language
   («Skill issue, tal vez? 😏» originated here). Then the cadence dies after 2025-06.

Non-admin community replies (faneqiu 18, others) are safely **not** forwarded to Play —
`on_message` gates on `GOOGLE_PLAY_ADMINS` before calling `reviews().reply`.

## Themes (keyword-matched over text+originalText; avg star per theme)

| Theme | Mentions | Avg ★ | Notes |
|---|---|---|---|
| Positive praise | 184 | 4.56 | "best PD fork" is the most common phrase |
| Ads | 171 | 3.99 | top 1★ driver (10 of 49 one-stars): banners on save-difficulties, long cinema ads, food-for-ad gate, ads crash the game (01b9f02e) |
| "vs Shattered/orig" comparisons | 69 | 4.03 | split between "better than SPD" and "worse/derivative"; worth pinned differentiation |
| Crash/bug reports | 51 | 3.78 | see bug clusters below |
| Graphics/art | 50 | 4.14 | "dull colors", "eye-popping", city design |
| Difficulty/balance | 40 | 3.67 | Yog heal+adds impossibly tanky (2 reports), Dwarven City spike, elf/gnoll unplayable claims, necromancer OP |
| Performance | 32 | 3.88 | per-turn micro-freezes (8129c267), FPS dips on flagships (c7898d20), walking lag zh-TW |
| Save/progress loss | 24 | **3.29** | lowest-rated theme; recurring on 31.x-32.x, multiple "uninstalled" |
| Mods | 24 | 4.54 | can't stack mods, mods fail to download after update (25-09-28), crash-with-mod anecdotes |
| UI/controls | 23 | 4.04 | smooth camera toggle requested since 2024, still the top UI gripe (5+ reviews incl. 26-05-17 «блевотный эффект») |
| Multiplayer requests | 11 | **4.73** | highest-star theme — fans keep asking year over year |
| Translation | 10 | 4.1 | Italian entirely missing (f7b39870), CJK overflow, pt-BR quality |
| Monetization/IAP | 7 | 3.71 | donations blocked (Google vs RU devs — replied), "paid and still see ads" (337a818e) |

## Recurring bug clusters worth filing (bd)

- **CJK text overflow** — zh-TW dialog text truncated/off-screen (two th5 4★ reviews),
  ja review says text unreadable. Rendering/wrap bug, blocks two of the top languages.
- **Black screen patches** — today's 3★ (b2b1c32d, 26-11-17) + 25-05-18 ru (ad refresh
  → screen goes black, matches "screen constantly goes black in places").
- **GUI occluded by Android nav buttons** — d447ae91 (1★, 26-08): emailed months ago,
  no response. Insets/cutout handling.
- **Darts do nothing** — no effect/animation/damage (2e78105d).
- **Telekinesis wand doesn't disarm traps** — reported twice (9f52f851 1★ with a list:
  piranha hits 2 tiles, satiety ring +5 behaves like +2, fire-in-water asymmetry).
- **Necromancer minions freeze** mid-run (58f99824).
- **GP achievements don't register** — 3 independent reports (925402db, 25-06-16, 26-01-07).
- **Ad network crash** — ad can't be closed, crashes the game (01b9f02e).

Known good outcomes when replying: trap-step QoL (th41 review, fixed in fix.7),
merchant/chest/skull break (fixed 29.5.fix.5 — reviewer promised to raise rating),
save-UI confusion.

## Feature requests to keep on the roadmap radar

multiplayer (4.73★ theme, oldest ask), mod stacking, level editor for players (we now
have the mod editor — reviewers don't know), item-storage chest in town, more
one-handed weapons, infinite mode, joystick support, Italian translation, priest/plague
doctor (shipped on master; one 26-09-16 review asks — answerable now), smooth-camera
toggle.

## Suggested reply queue (unanswered ≤3★, last 12 months)

`b2b1c32d` 3★ black screen (today, ru) · `d447ae91` 1★ nav-bar GUI + ignored email ·
`b3bbf191` 1★ endgame heal-forever mobs · `925402db` 2★ achievements/alchemy ·
`9f52f851` 1★ bug list · `2e78105d` 2★ darts · `29c85946` 1★ Yog · `2ed7b9a6` 3★ ja
text overflow · `07270236` 1★ ads+camera · `58f99824` 2★ necro minions ·
`d9ab0bf8` 2★ graphics · `11c13241` 3★ easy-mode deaths · `5d80ffdb` 3★ controls ·
`13aa8b0a` 1★ balance/derivative · `c097ae87` 1★ worse-version · `f7b39870` 1★ no
Italian · `01b9f02e` 2★ ad network crash. Plus 5★ gratitude backlog (52 in last year)
and two 5★ asks that deserve real answers: priest ETA (59df3dc0), multiplayer search
message (7c18cc9f).

## Reply style (analysis of Mike's 294 replies)

Voice formula: fixed thanks-opener (3 phrasings ≈ 50%) → specific response → optional
tip/roadmap. 12% of handwritten-era replies interrogate vague reviews; bug fixes are
always answered with the version ("fixed in fix.7, please update") — the two confirmed
cases both recovered the rating. Wit is dry and self-deprecating; sarcasm works when
one line and aimed at the situation. Language always matches the review
(RU/EN/ES/PT/TR). Median 22 words.

Failure modes: the 2025-02-22 LLM batch tripled length (44 words avg), corporatized the
register, duplicated texts (one sent 10×, one review answered 3×), mocked two 1★
authors. The sparse 2025+ era keeps efficiency but loses the voice (no questions, no
wit). Systematic typo: `cloud` for `could` — 9/9 occurrences, zero correct.

Rules for future replies: restore cadence first; vague ≤3★ → one question back;
confirmed bug → name fix version; sarcasm = one line at the situation; dedupe batches;
spellcheck before send.

## Mechanics that would keep this from rotting again

- The bot already has everything needed: `async_publish_fresh_reviews` could post a
  weekly digest of unanswered ≤3★ reviews to `GOOGLE_PLAY_ADMINS` as a DM.
- Batching replies is fine, but the 2025-02-22 batch shows the sender must dedupe
  (3 replies to one review) and spot-check tone.
- Dataset refresh: rerun the dbm+Discord dump (dbm is locked while the bot runs —
  copy first). All dump/digest scripts used here are one-off heredocs; nothing to
  commit besides this report.
