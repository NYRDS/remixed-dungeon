# Mod editor: mobile usability review (2026-11-17)

Scope: `RemixedDungeon/src/main/assets/web/mod-editor/` as shipped on master
(55949db54, rounds 1-6). Method: static code review of all 22 js/css files +
live walkthrough at a 390x844 phone viewport against the desktop debug web
server (`runDesktopGameWithWebServer --webserver=8093`), with a 1280x800
desktop pass as contrast. Screenshots referenced below are from the live pass.

## How the editor is reached on mobile (fine as-is)

`WndModSelect.java:146` opens the editor via `Game.openUrl` -> Android
`ACTION_VIEW` chooser (`Game.java:377`) - i.e. the user's **external browser**
(Chrome/Safari), not an in-app WebView. Consequences, all verified OK:

- `window.prompt` / `window.confirm` / `alert` / `beforeunload` work.
- `<input type=file>` uploads and `<a download>` export work.
- The server is the phone's own localhost, no LAN dependency.

So the deployment story needs no work; every problem below is web-side.

## P1 - blockers on a phone

### 1. No touch support in any canvas interaction

All four interactive canvases bind **mouse events only**; there is not a
single `touch*`/`pointer*` handler and no `touch-action` CSS anywhere in the
app (`grep` across `js/` and `css/` returns zero hits).

| Editor | Interaction | Code | Touch result |
|---|---|---|---|
| campaign graph | node drag-to-arrange, anchor drag-to-connect | `campaign-editor.js:529,557,587` | impossible: drag pans the scrollable ancestor instead (`.campaign-scroll` is `overflow:auto`) |
| campaign graph | double-click node opens level painter | `campaign-editor.js:605` | unreliable: double-tap-zoom competes (no `touch-action:manipulation`) |
| level painter | paint strokes by drag | `level-editor.js:543-562` | single tap paints (verified live: hover readout `x=3 y=3 cell=51 terrain=2` updated on click), stroke painting breaks - the gesture pans `#content` |
| level painter / sprite editor | hover previews (`mousemove`) | `level-editor.js:724`, `sprite-editor.js:330` | never shown on touch |
| tileset / icon-grid pickers | **click**-based | `level-editor.js:710-723`, `lua-desc-table.js:114` | work (tap fires synthetic mouse events) |

Edge deletion is Delete-key-only on canvas selection
(`campaign-editor.js:1087-1097`) - no keyboard on touch. The per-exit `x`
buttons in the side panel are a working touch path, but the status line
actively tells users to press Delete.

Fix direction: `pointerdown/pointermove/pointerup` + `touch-action:none` on
canvases (keeps mouse working), plus `touch-action:manipulation` app-wide to
kill double-tap-zoom lag on the rest of the UI. Note: painting/node-drag must
set `touch-action:none` or pointermove streams stop at the first move.

### 2. Layout never adapts below desktop width

Zero `@media` queries; `main` is a fixed flex row with a 260px sidebar
(`style.css:42-53`). Measured live at 390x844:

| Element | Measured | Effect |
|---|---|---|
| `#content` | **115px** wide | validation text wraps 2-3 chars/line, unreadable |
| `.campaign-scroll` | **2px** wide | graph canvas (438px intrinsic) invisible |
| `.campaign-panel` | 280px, `min-width:280px`, x=286 | overflows the right edge (viewport ends at 390) |
| `#topbar` | **90px** tall (wraps to 3 rows) | `main` height is `calc(100% - 41px)` (`style.css:42`) -> 49px of content clipped at the bottom |
| level painter canvas | 288px at x=272 | only 118px of the map visible; toolbar clipped off-screen |

The dashboard is equally broken: the empty sidebar still occupies 260px and
all mod action buttons (Edit/Play/Duplicate/Delete) sit off-viewport; the page
needs horizontal scrolling to reach them.

Fix direction: stack sidebar above/below content under ~700px (or a collapsible
tree), let `.campaign-wrap` wrap so the panel drops under the canvas, derive
`main` height from the real topbar height (flex column + `flex:1`), replace
fixed pixel assumptions. The campaign panel `min-width:280px` must relax to
`100%` on narrow screens.

## P2 - degraded but workable

3. **Touch targets far below 44px**: canvas anchor hit radius is 10px
   (`ANCHOR_R+4`, `campaign-editor.js:31,499` - the connect gesture is
   effectively pointer-only even after pointer events land); tree rename `x`/
   delete `x` are bare 14px glyphs (`file-tree.js:97-119`); all buttons are
   ~32px tall (`style.css:55-64`, measured live); exits-panel arrows same.
4. **Gesture help is mousemove-only**: the only explanation of the graph
   gestures lives in `mousemove` hover text (`campaign-editor.js:577-584`) -
   a touch user never sees it. The static panel hint
   ("drag an anchor to wire an exit", `campaign-editor.js:689`) is the sole
   touch-visible guidance; keep that pattern, add a static hint line for the
   level painter too.
5. **iOS input auto-zoom**: every input inherits the 14px base font
   (`style.css:10,69-76`); Safari zooms into any focused input with
   font-size <16px, jarring the layout on every field. Inputs should be >=16px
   on coarse pointers.
6. **dblclick affordances without alternatives**: opening a node's level file
   from the graph is double-click-only (`campaign-editor.js:605-616`); a
   panel button ("Open level file") would serve both worlds. (Workaround
   exists: open the file from the tree.)

## P3 - minor

7. `textarea.code` is `white-space:pre` with no soft-wrap toggle
   (`style.css:77-82`) - long lua lines are horizontal-scroll-only on a
   phone; acceptable for a fallback editor, a wrap toggle is cheap.
8. `field-grid` is `grid-template-columns: max-content 1fr`
   (`style.css:120`) - long labels force overflow at narrow widths; needs
   `minmax(0,1fr)` or single-column stacking under a media query.
9. No pinch-zoom on the campaign canvas: nodes render 1:1 (by design,
   `campaign-editor.js:245-247` scroll-instead-of-scale), fine on desktop,
   small on phones. A coarse-pointer zoom multiplier (e.g. 1.5x CSS scale)
   would help node legibility; low priority.
10. `#busy` chip is fixed at `top:44px` (`style.css:31-40`) and overlaps the
    wrapped topbar at narrow widths - cosmetic once the topbar height bug
    (#2) is fixed.

## What already works on a phone (verified live or by code reading)

- Dashboard browsing, create-from-template form, prompts/confirms.
- Workspace tree (tap to expand/open), validate list with clickable entries.
- All form editors (mob/item/buff/effect/plants/treasury/initHeroes) - plain
  DOM forms, tap-friendly.
- Strings pairs editor - full-width table inputs.
- Text/json/lua textarea editors (with P3 caveats).
- Uploads (native file picker), Export `.zip`, `Play this mod`
  (confirm + server restart + auto-reconnect probe).

## Bottom line

On a phone today the editor is a read-only-ish file browser with working form
editors, while its two flagship visual tools - the campaign graph and the
level painter - are effectively unusable (layout hides the canvas entirely;
touch cannot drag anything). Both blockers are contained: pointer events +
`touch-action` for interaction, one responsive layout pass + topbar height fix
for rendering. No engine/server work required.

Live evidence (ZCode session artifacts, 390x844 unless noted):
dashboard/workspace/campaign/painter screenshots and the 1280x800 contrast
shot were captured during this review round.
