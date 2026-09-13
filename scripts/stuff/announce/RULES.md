# Remixed Dungeon — release announce rules (Telegram)

How to write beta release announcements. Follow every rule below; the announce
for 32.4.beta.6 (in this directory) is the reference example.

## Sourcing (what goes in)

1. Source material: `git log <last-shipped-tag>..<new-tag> --oneline`. The last
   SHIPPED tag, not the previous tag — a failed publish means users are on an
   older baseline.
2. Check `docs/release_*.md` drafts: an unpublished release's proofread announce
   (e.g. `docs/release_32.4.beta.7.md`) IS the base for the next announce when
   the unpublished tag never shipped — adopt its phrasing and game terms, then
   compress to the caption limit.
3. Build the announce picture from `docs/txt2img_promo_library.md` (Qwen-Image
   blocks: SCENE/CAST/LIGHTING/PALETTE/STYLE/COMPOSITION/CONSTRAINTS + params).
   Do not improvise descriptions — paste library lines verbatim.
4. Filter out noise commits: Auto-wiki, wiki-data submodule bumps, l10n/translation
   bot commits, mob-viewer and other dev tooling, chore/docs/test/i18n.
5. Verify every candidate fix actually affected the last shipped version:
   find the buggy code (removed lines of the fix commit) and check it exists at
   the shipped tag (`git grep -F '<token>' <tag>`). Regressions introduced after
   the shipped tag and fixed before release were never seen by users — do NOT
   announce them (fixes whose message contains "regression" are prime suspects).
6. When a claim can't be verified, drop it. No dev-facing infrastructure
   (watchdog, debug web server, BuildConfig, LLM control surface, serverctl).

## Content bans (learned the hard way)

- NEVER frame as multiple releases ("N releases' worth of fixes") — the announce
  is always just the current release.
- NEVER announce translations — they are always incomplete.
- NEVER announce asset/sprite pipeline work (retro sprites, sprite tools) — internal.
- NEVER announce store-compliance bumps (target SDK, Play policy) — anti-features
  forced by Google, not user value.
- No crash IDs (snap-xxx), no commit shas, no internal codenames in the text.
- State the user-visible outcome, not the mechanism; cause after a dash only if
  it reads naturally.

## Format

- Two versions: EN and RU, same bullet structure, natural phrasing in each
  (not a literal translation).
- Use each locale's ACTUAL in-game names for items, mobs, spells and classes —
  verify against the shipped strings, never transliterate or invent:
  `RemixedDungeon/src/main/res/values/strings_all.xml` (EN) and
  `values-ru/strings_all.xml` (RU). Known traps: "Wand of Telekinesis" /
  «Палочка Телекинеза» (not «Жезл»), "Well of Transmutation" /
  «Источник Трансмутации» (not «Колодец»), "shadow lord" / «Владыка Теней»,
  "cold spirit" / «Дух мороза», glyph of multiplicity / «глиф умножения»,
  Root spell / «Корни», DwarfToken / «дворфийский жетон».
- Read the fix DIFF, not just the commit subject — subjects can mislead about
  semantics ("Dwarf King fight no longer pays DwarfTokens" was actually a
  quest-farm fix, not a cost).
- Each version must fit the Telegram photo caption limit: ≤1024 chars including
  newlines. Measure with python `len()` on the stripped block; aim ≤1010.
- Structure (Mike's preference, 2026-09-13 — the beta.7 draft format): thematic
  intro line carrying the version (`🐾 <theme> — <version>, straight from your
  beta feedback:`), flat emoji bullets (one per fix), an `Also:` paragraph for
  secondary items, an `As usual — beta:` old-save disclaimer when relevant,
  beta link, hashtags. No 🗡️ title line, no 🛠️/⚖️ section headers.
  Reference: `docs/release_32.4.beta.7.md`.
- Say it when bugs are self-inflicted: when most fixes target bugs introduced
  by the same beta series (new systems teething — e.g. pets/orders/STR landed
  during 32.4), state that in the `As usual — beta:` paragraph instead of
  letting them read as long-standing issues.
- Always include the beta opt-in link:
  `https://play.google.com/apps/testing/com.nyrds.pixeldungeon.ml`
- Hashtags: `#remixed_dungeon #beta`.

## Announce picture

Include a txt2img prompt with the announce:
- Build the scene from that release's changelog items — real in-game heroes,
  mobs and items (changelog mentions first). Verify names/looks in the repo:
  hero classes are WARRIOR, MAGE, ROGUE, HUNTRESS, ELF, NECROMANCER, GNOLL,
  PRIEST, DOCTOR (no paladins/alchemists — those are legacy/subclass gear names).
- Match the game's actual art style: top-down tile-based roguelike like classic
  Pixel Dungeon — grey stone brick tiles, chunky crisp pixels, dithered shading,
  limited palette, warm torchlight. Not isometric, not 3D, not a generic fantasy
  group shot.
- `no text` in the prompt AND "text/letters/logo" in the negative prompt.
- Suggested settings: 16:9 (1344×768) or 1:1 (1024×1024), steps 30–40, CFG 6–7.

## Deliverable

- One file per release: `scripts/stuff/announce/<version>.telegram.md`,
  containing EN block, RU block, txt2img prompt section.
- Leave untracked unless asked to commit.
