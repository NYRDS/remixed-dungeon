// Visual campaign graph editor for levelsDesc/Dungeon.json (bd c0i).
//
// Engine semantics (DungeonGenerator):
//   Levels: { id: {kind, depth, file?, music?, nMobs?, ...} }
//   Graph:  { id: [[exits...], [entered from...]] } - exit i on a level's
//           map leads to Graph[id][0][i]; the entered-from list drives the
//           way back up. Entrance ("0" by default) is a stub - the first
//           descend lands on Graph[Entrance][0][0].
// Nodes drag to arrange (layout persists in localStorage, never in the json);
// edges are drawn from a node's right anchor to another node. Double-click a
// node with a file opens it in the level painter. One-way edges are legal -
// deleting an edge also drops the mirrored entrance entry only when no other
// exit of the same source points at the same target.
// Unknown per-level keys survive a save (mutate in place, never rebuild) -
// only rename rebuilds the tables, preserving key order.

import * as api from '../api.js';
import { templateFor } from './templates.js';

// DungeonGenerator.initLevelsMap registerLevelClass set
const LEVEL_KINDS = ['PredesignedLevel', 'RandomLevel', 'SewerLevel', 'SewerBossLevel',
  'SpiderLevel', 'PrisonLevel', 'PrisonBossLevel', 'CavesLevel', 'CavesBossLevel',
  'CityLevel', 'CityBossLevel', 'LastShopLevel', 'HallsLevel', 'HallsBossLevel',
  'LastLevel', 'DeadEndLevel', 'GutsLevel', 'ShadowLordLevel', 'FakeLastLevel',
  'NecroLevel', 'NecroBossLevel', 'IceCavesLevel', 'IceCavesBossLevel',
  'TownShopLevel', 'TestLevel'];
const FEELINGS = ['NONE', 'CHASM', 'WATER', 'GRASS', 'UNDEFINED'];

const NODE_W = 158;
const NODE_H = 46;
const ANCHOR_R = 6;

// touch canvas interactions need chunkier draw + hit geometry
const coarsePointer = window.matchMedia && window.matchMedia('(pointer: coarse)').matches;

// gate: can the graph view model this campaign at all? exotic Graph shapes
// (hand-edited, legacy 3-tuples) fall back to the raw json editor
function campaignShape(obj) {
  if (obj == null || typeof obj !== 'object' || Array.isArray(obj)) { return false; }
  if (obj.Levels == null || typeof obj.Levels !== 'object' || Array.isArray(obj.Levels)) { return false; }
  const graph = obj.Graph;
  if (graph === undefined) { return true; }
  if (graph == null || typeof graph !== 'object' || Array.isArray(graph)) { return false; }
  for (const pair of Object.values(graph)) {
    // exactly two sides - legacy 3-tuples (old ascend exit index) bounce to
    // text: the editor would show a graph whose ascend semantics it ignores
    if (!Array.isArray(pair) || pair.length !== 2
        || !Array.isArray(pair[0]) || !Array.isArray(pair[1])) { return false; }
  }
  return true;
}

export async function campaignEditor(content, ctx) {
  const data = await api.readModFile(ctx.mod, ctx.file);
  let campaign;
  try {
    campaign = JSON.parse(data.content);
  } catch (e) {
    const { jsonEditor } = await import('./text-editors.js');
    await jsonEditor(content, Object.assign({}, ctx, { content: data.content }));
    return null;
  }
  if (!campaignShape(campaign)) {
    const { jsonEditor } = await import('./text-editors.js');
    await jsonEditor(content, Object.assign({}, ctx, { content: data.content }));
    return null;
  }

  let dirty = false;
  const markDirty = () => { dirty = true; };

  const entranceId = () => campaign.Entrance || '0';

  // ---- layout: auto by depth column, manual drags persisted locally -----
  const layoutKey = 'campaignLayout:' + ctx.mod + '/' + ctx.file;
  function loadSavedLayout() {
    try {
      const raw = localStorage.getItem(layoutKey);
      if (!raw) { return {}; }
      const parsed = JSON.parse(raw);
      return parsed && typeof parsed === 'object' ? parsed : {};
    } catch (e) { return {}; }
  }
  function saveLayout() {
    try { localStorage.setItem(layoutKey, JSON.stringify(layout)); } catch (e) { /* storage full/blocked - layout is cosmetic */ }
  }

  function autoLayout() {
    const byDepth = new Map();
    for (const id of Object.keys(campaign.Levels)) {
      const d = Number(campaign.Levels[id] && campaign.Levels[id].depth) || 0;
      if (!byDepth.has(d)) { byDepth.set(d, []); }
      byDepth.get(d).push(id);
    }
    // graph-only ids (dangling) park in the last column
    const danglingCol = Math.max(0, ...byDepth.keys()) + 1;
    for (const id of Object.keys(campaign.Graph || {})) {
      if (!campaign.Levels[id] && !byDepth.has(danglingCol)) { byDepth.set(danglingCol, []); }
      if (!campaign.Levels[id]) { byDepth.get(danglingCol).push(id); }
    }
    const cols = Math.max(1, byDepth.size);
    const rows = Math.max(1, ...[...byDepth.values()].map(v => v.length));
    // browsers cap canvas around 32k px - compress spacing for huge campaigns
    const colGap = Math.max(90, Math.min(210, Math.floor(15000 / cols)));
    const rowGap = Math.max(46, Math.min(68, Math.floor(9000 / rows)));
    const pos = {};
    const sortedDepths = [...byDepth.keys()].sort((a, b) => a - b);
    const colOf = new Map(sortedDepths.map((d, i) => [d, i]));
    for (const [d, ids] of byDepth) {
      ids.forEach((id, row) => {
        pos[id] = [30 + colOf.get(d) * colGap, 30 + row * rowGap];
      });
    }
    return pos;
  }

  let layout = Object.assign(autoLayout(), loadSavedLayout());

  function canvasSize() {
    let w = 400, h = 300;
    for (const [x, y] of Object.values(layout)) {
      w = Math.max(w, x + NODE_W + 40);
      h = Math.max(h, y + NODE_H + 40);
    }
    return [Math.min(w, 16000), Math.min(h, 16000)];
  }

  // ---- graph accessors (all mutations keep both directions honest) ------
  function graphPair(id, create) {
    if (!campaign.Graph) {
      if (!create) { return null; }
      campaign.Graph = {};
    }
    if (!campaign.Graph[id]) {
      if (!create) { return null; }
      campaign.Graph[id] = [[], []];
    }
    return campaign.Graph[id];
  }

  function connect(from, to) {
    if (to === entranceId() || to === '0') {
      status.className = 'status-err';
      status.textContent = 'the entrance stub has no map - exits cannot lead into it';
      return;
    }
    if (!campaign.Levels[to]) {
      status.className = 'status-err';
      status.textContent = 'unknown level: ' + to + ' (create it first)';
      return;
    }
    const pair = graphPair(from, true);
    if (!pair[0].includes(to)) { pair[0].push(to); markDirty(); }
    const target = graphPair(to, true);
    if (!target[1].includes(from)) { target[1].push(from); markDirty(); }
    sel = to; selEdge = null;
    status.className = 'status-ok';
    status.textContent = 'connected ' + from + ' -> ' + to + ' (exit #' + (pair[0].length - 1) + ')';
    renderPanel(); draw();
  }

  function removeEdge(from, idx) {
    const pair = graphPair(from, false);
    if (!pair) { return; }
    const to = pair[0][idx];
    pair[0].splice(idx, 1);
    const target = graphPair(to, false);
    // drop the mirrored entrance only when no parallel exit remains
    if (target && !pair[0].includes(to)) {
      const i = target[1].indexOf(from);
      if (i >= 0) { target[1].splice(i, 1); }
    }
    markDirty();
    selEdge = null;
    status.className = 'status-ok';
    status.textContent = 'removed exit ' + from + ' -> ' + to;
    renderPanel(); draw();
  }

  function deleteNode(id) {
    if (id === entranceId() || id === '0') { return; }
    delete campaign.Levels[id];
    if (campaign.Graph) {
      delete campaign.Graph[id];
      for (const pair of Object.values(campaign.Graph)) {
        for (let side = 0; side < 2; side++) {
          if (Array.isArray(pair[side])) {
            pair[side] = pair[side].filter(x => x !== id);
          }
        }
      }
    }
    delete layout[id];
    saveLayout();
    markDirty();
    if (sel === id) { sel = null; }
    renderPanel(); draw();
  }

  function renameLevel(from, to) {
    const rebuild = table => {
      const fresh = {};
      for (const [k, v] of Object.entries(table)) { fresh[k === from ? to : k] = v; }
      return fresh;
    };
    campaign.Levels = rebuild(campaign.Levels);
    if (campaign.Graph) {
      campaign.Graph = rebuild(campaign.Graph);
      for (const pair of Object.values(campaign.Graph)) {
        for (let side = 0; side < 2; side++) {
          if (Array.isArray(pair[side])) {
            pair[side] = pair[side].map(x => (x === from ? to : x));
          }
        }
      }
    }
    if (campaign.Entrance === from) { campaign.Entrance = to; }
    if (layout[from]) { layout[to] = layout[from]; delete layout[from]; }
    markDirty();
    if (sel === from) { sel = to; }
    renderPanel(); draw();
  }

  // ---- ui scaffold -------------------------------------------------------
  const barEl = document.createElement('div');
  barEl.className = 'editor-bar';
  const title = document.createElement('strong');
  title.textContent = 'Campaign graph: ' + ctx.file.split('/').pop();
  const save = document.createElement('button');
  save.className = 'primary';
  save.textContent = 'Save';
  const relayoutBtn = document.createElement('button');
  relayoutBtn.textContent = 'Re-layout';
  relayoutBtn.title = 'forget dragged positions, line levels up by depth';
  const addBtn = document.createElement('button');
  addBtn.textContent = '+ level';
  barEl.appendChild(title);
  barEl.appendChild(addBtn);
  barEl.appendChild(relayoutBtn);
  barEl.appendChild(save);
  content.appendChild(barEl);

  const status = document.createElement('div');
  content.appendChild(status);

  const wrap = document.createElement('div');
  wrap.className = 'campaign-wrap';
  // scroll box instead of CSS scaling: nodes stay 1:1 pixels, huge campaigns
  // scroll; canvasPos still normalizes coordinates in case anything scales
  const scroll = document.createElement('div');
  scroll.className = 'campaign-scroll';
  const canvas = document.createElement('canvas');
  canvas.className = 'campaign-canvas';
  canvas.id = 'campaign-canvas';
  scroll.appendChild(canvas);
  wrap.appendChild(scroll);
  const panel = document.createElement('div');
  panel.className = 'campaign-panel';
  wrap.appendChild(panel);
  content.appendChild(wrap);

  const hover = document.createElement('div');
  hover.className = 'hint';
  content.appendChild(hover);

  const issuesBox = document.createElement('details');
  issuesBox.className = 'panel';
  issuesBox.addEventListener('toggle', () => {
    if (!issuesBox.open) { issuesBox.dataset.userToggled = '1'; }
  });
  content.appendChild(issuesBox);

  // selection state shared by canvas + panel
  let sel = null;
  let selEdge = null;
  let drag = null; // {mode:'move'|'connect', id, ...}

  // ---- client-side mirrors of the server graph checks -------------------
  function graphIssues() {
    const issues = [];
    const graph = campaign.Graph || {};
    for (const id of Object.keys(graph)) {
      if (!campaign.Levels[id]) {
        issues.push({ err: true, msg: 'Graph entry ' + id + ' is not defined in Levels' });
        continue;
      }
      for (let side = 0; side < 2; side++) {
        for (const to of graph[id][side]) {
          if (!campaign.Levels[to]) {
            issues.push({ err: true, msg: 'Graph ' + (side === 0 ? 'exit' : 'ascend') + ' ' + id + ' -> ' + to + ' points at a level that is not defined in Levels' });
          }
        }
      }
    }
    for (const id of Object.keys(campaign.Levels)) {
      if (!graph[id]) {
        issues.push({ err: false, msg: 'level ' + id + ' has no Graph entry - its stairs are undefined' });
      }
    }
    return issues;
  }

  // ---- drawing -----------------------------------------------------------
  function nodeIds() {
    const ids = Object.keys(campaign.Levels);
    for (const id of Object.keys(campaign.Graph || {})) {
      if (!ids.includes(id)) { ids.push(id); }
    }
    return ids;
  }

  function allEdges() {
    const out = [];
    for (const from of Object.keys(campaign.Graph || {})) {
      (campaign.Graph[from][0] || []).forEach((to, idx) => out.push({ from, to, idx }));
    }
    return out;
  }

  // bezier anchors: source right-center -> just before the target's left
  // edge (arrowheads must stay visible - nodes paint over edges)
  function edgeGeom(e) {
    const s = layout[e.from] || [-100, -100];
    const t = layout[e.to] || [-100, -100];
    // bow parallel/duplicate edges apart
    const dup = allEdges().filter(x => (x.from === e.from && x.to === e.to) || (x.from === e.to && x.to === e.from));
    const k = dup.findIndex(x => x.idx === e.idx && x.from === e.from);
    const bow = dup.length > 1 ? (k - (dup.length - 1) / 2) * 40 : 0;
    const x1 = s[0] + NODE_W + 4, y1 = s[1] + NODE_H / 2;
    const x2 = t[0] - 10, y2 = t[1] + NODE_H / 2;
    const dx = Math.max(50, Math.abs(x2 - x1) / 2);
    return {
      x1, y1, x2, y2,
      c1x: x1 + dx, c1y: y1 + bow,
      c2x: x2 - dx, c2y: y2 + bow
    };
  }

  function bezPoint(g, t) {
    const mt = 1 - t;
    return {
      x: mt * mt * mt * g.x1 + 3 * mt * mt * t * g.c1x + 3 * mt * t * t * g.c2x + t * t * t * g.x2,
      y: mt * mt * mt * g.y1 + 3 * mt * mt * t * g.c1y + 3 * mt * t * t * g.c2y + t * t * t * g.y2
    };
  }

  function draw() {
    const [W, H] = canvasSize();
    canvas.width = W;
    canvas.height = H;
    const g = canvas.getContext('2d');
    g.fillStyle = '#14161c';
    g.fillRect(0, 0, W, H);

    const badTarget = id => !campaign.Levels[id];
    const entrance = entranceId();

    // edges + exit-index badges
    for (const e of allEdges()) {
      const geom = edgeGeom(e);
      const selected = selEdge && selEdge.from === e.from && selEdge.idx === e.idx;
      const bad = badTarget(e.to);
      g.strokeStyle = bad ? '#e05050' : selected ? '#ffb040' : '#7a8298';
      g.lineWidth = selected ? 3 : bad ? 2 : 1.5;
      if (bad) { g.setLineDash([6, 4]); }
      g.beginPath();
      g.moveTo(geom.x1, geom.y1);
      g.bezierCurveTo(geom.c1x, geom.c1y, geom.c2x, geom.c2y, geom.x2, geom.y2);
      g.stroke();
      g.setLineDash([]);
      // arrow head
      const tip = { x: geom.x2, y: geom.y2 };
      const before = bezPoint(geom, 0.97);
      const ang = Math.atan2(tip.y - before.y, tip.x - before.x);
      g.fillStyle = bad ? '#e05050' : selected ? '#ffb040' : '#7a8298';
      g.beginPath();
      g.moveTo(tip.x, tip.y);
      g.lineTo(tip.x - 10 * Math.cos(ang - 0.4), tip.y - 10 * Math.sin(ang - 0.4));
      g.lineTo(tip.x - 10 * Math.cos(ang + 0.4), tip.y - 10 * Math.sin(ang + 0.4));
      g.fill();
      // exit index: multiexit cell i leads to Graph[from][0][i]
      const mid = bezPoint(geom, 0.5);
      g.beginPath();
      g.arc(mid.x, mid.y, 9, 0, Math.PI * 2);
      g.fillStyle = '#14161c';
      g.fill();
      g.strokeStyle = bad ? '#e05050' : selected ? '#ffb040' : '#7a8298';
      g.lineWidth = 1;
      g.stroke();
      g.fillStyle = '#d8dce6';
      g.font = '10px monospace';
      g.textAlign = 'center';
      g.textBaseline = 'middle';
      g.fillText(String(e.idx), mid.x, mid.y + 0.5);
      g.textAlign = 'left';
      g.textBaseline = 'alphabetic';
    }

    // connection rubber band
    if (drag && drag.mode === 'connect' && drag.to) {
      const s = layout[drag.id];
      g.strokeStyle = '#40c0ff';
      g.lineWidth = 2;
      g.setLineDash([5, 4]);
      g.beginPath();
      g.moveTo(s[0] + NODE_W, s[1] + NODE_H / 2);
      g.lineTo(drag.to.x, drag.to.y);
      g.stroke();
      g.setLineDash([]);
    }

    // nodes
    for (const id of nodeIds()) {
      const [x, y] = layout[id] || [0, 0];
      const level = campaign.Levels[id];
      const isEntrance = id === entrance;
      const noGraph = !(campaign.Graph || {})[id];
      const selected = sel === id;

      g.beginPath();
      const r = 8;
      g.moveTo(x + r, y);
      g.arcTo(x + NODE_W, y, x + NODE_W, y + NODE_H, r);
      g.arcTo(x + NODE_W, y + NODE_H, x, y + NODE_H, r);
      g.arcTo(x, y + NODE_H, x, y, r);
      g.arcTo(x, y, x + NODE_W, y, r);
      g.closePath();
      g.fillStyle = level ? '#262b36' : '#361d1d';
      g.fill();
      if (!level) { g.setLineDash([6, 4]); }
      g.lineWidth = selected ? 3 : isEntrance ? 2 : noGraph ? 1.5 : 1;
      g.strokeStyle = !level ? '#e05050' : selected ? '#ffd060'
        : isEntrance ? '#40c0ff' : noGraph ? '#6a6a50' : '#5a6478';
      if (noGraph && level) { g.setLineDash([4, 4]); }
      g.stroke();
      g.setLineDash([]);

      g.fillStyle = '#e8ebf2';
      g.font = 'bold 13px monospace';
      g.fillText(id, x + 10, y + 17);
      g.font = '10px monospace';
      g.fillStyle = '#9aa2b5';
      const kind = isEntrance && !level ? 'ENTRANCE stub' : (level && level.kind) || '?';
      const fileTag = level && level.file ? ' \u{1F4C4}' : '';
      g.fillText(kind + fileTag, x + 10, y + 31);
      g.fillStyle = '#6f7890';
      g.fillText('d' + ((level && level.depth) || 0) + (level && level.music ? ' \u266B' : ''),
        x + 10, y + 42);

      // out-anchor: drag from here to another node to wire an exit
      g.beginPath();
      g.arc(x + NODE_W, y + NODE_H / 2, ANCHOR_R + (coarsePointer ? 4 : 0), 0, Math.PI * 2);
      g.fillStyle = drag && drag.mode === 'connect' && drag.id === id ? '#40c0ff' : '#3a4252';
      g.fill();
      g.strokeStyle = '#8a93a8';
      g.lineWidth = 1;
      g.stroke();
    }

    // live issue count (mirrors server validate)
    const issues = graphIssues();
    issuesBox.innerHTML = '';
    const summary = document.createElement('summary');
    summary.textContent = issues.length
      ? 'Graph issues: ' + issues.filter(i => i.err).length + ' error(s), '
        + issues.filter(i => !i.err).length + ' warning(s)'
      : 'Graph issues: none';
    issuesBox.appendChild(summary);
    if (issues.length) {
      const ul = document.createElement('ul');
      ul.className = 'error-list';
      for (const i of issues.slice(0, 50)) {
        const li = document.createElement('li');
        li.textContent = i.msg;
        if (!i.err) { li.classList.add('warn-entry'); }
        ul.appendChild(li);
      }
      issuesBox.appendChild(ul);
    }
    // auto-open only while there are hard errors and the user has not
    // collapsed it yet (drag redraws must not keep popping it open)
    if (issues.some(i => i.err) && !issuesBox.dataset.userToggled) {
      issuesBox.open = true;
    }
  }

  // ---- hit testing --------------------------------------------------------
  function hitNode(p) {
    const ids = nodeIds();
    for (let i = ids.length - 1; i >= 0; i--) {
      const id = ids[i];
      const [x, y] = layout[id] || [-999, -999];
      if (p.x >= x && p.x <= x + NODE_W && p.y >= y && p.y <= y + NODE_H) { return id; }
    }
    return null;
  }

  function hitAnchor(p) {
    const r = ANCHOR_R + (coarsePointer ? 16 : 4); // fingers need a real target
    for (const id of nodeIds()) {
      const [x, y] = layout[id] || [-999, -999];
      const ax = x + NODE_W, ay = y + NODE_H / 2;
      if ((p.x - ax) * (p.x - ax) + (p.y - ay) * (p.y - ay) <= r * r) {
        return id;
      }
    }
    return null;
  }

  function hitEdge(p) {
    let best = null;
    let bestD = 8;
    for (const e of allEdges()) {
      const geom = edgeGeom(e);
      for (let i = 0; i <= 20; i++) {
        const q = bezPoint(geom, i / 20);
        const d = Math.hypot(q.x - p.x, q.y - p.y);
        if (d < bestD) { bestD = d; best = e; }
      }
    }
    return best;
  }

  // ---- canvas events (pointer events: mouse + touch + pen, bd a5d) --------
  const canvasPos = e => {
    const rect = canvas.getBoundingClientRect();
    // normalize to canvas pixels - correct even under CSS scaling
    const sx = rect.width ? canvas.width / rect.width : 1;
    const sy = rect.height ? canvas.height / rect.height : 1;
    return { x: (e.clientX - rect.left) * sx, y: (e.clientY - rect.top) * sy };
  };

  // double-click (mouse) and double-tap (touch) both land here
  function openLevelFile(node) {
    const level = campaign.Levels[node];
    if (level && level.file) {
      // app.js owns the routing - ask it to open the level painter
      window.dispatchEvent(new CustomEvent('mod-editor:open-file', { detail: level.file }));
    } else {
      status.className = 'hint';
      status.textContent = node + ' has no level file (procedurally generated or missing)';
    }
  }

  let lastTap = null; // { id, t } - second tap on the same node opens its map
  let activePointer = null; // one gesture at a time (palm rejection)

  canvas.addEventListener('pointerdown', e => {
    if (activePointer !== null || e.button > 0) { return; }
    activePointer = e.pointerId;
    e.preventDefault(); // no synthetic mouse events, no text selection
    try { canvas.setPointerCapture(e.pointerId); } catch (err) { /* capture is best-effort */ }
    const p = canvasPos(e);
    const anchor = hitAnchor(p);
    if (anchor) {
      drag = { mode: 'connect', id: anchor, to: null };
      draw();
      return;
    }
    const node = hitNode(p);
    if (node) {
      sel = node; selEdge = null;
      drag = { mode: 'move', id: node, orig: (layout[node] || [0, 0]).slice(), start: p, moved: false };
      renderPanel(); draw();
      return;
    }
    const edge = hitEdge(p);
    if (edge) {
      selEdge = edge; sel = null;
      status.className = 'hint';
      status.textContent = 'edge ' + edge.from + ' -> ' + edge.to
        + ' (exit #' + edge.idx + ') selected - the panel button (or Delete key) removes it';
      renderPanel(); draw();
    } else {
      sel = null; selEdge = null;
      renderPanel(); draw();
      // touch has no scrollbars: dragging empty canvas pans the graph
      if (e.pointerType !== 'mouse') {
        drag = { mode: 'pan', sx: e.clientX, sy: e.clientY, sl: scroll.scrollLeft, st: scroll.scrollTop };
      }
    }
  });

  canvas.addEventListener('pointermove', e => {
    if (activePointer !== null && e.pointerId !== activePointer) { return; }
    const p = canvasPos(e);
    if (drag && drag.mode === 'move') {
      const nx = Math.max(0, Math.min(16000 - NODE_W, drag.orig[0] + p.x - drag.start.x));
      const ny = Math.max(0, Math.min(16000 - NODE_H, drag.orig[1] + p.y - drag.start.y));
      if (nx !== layout[drag.id][0] || ny !== layout[drag.id][1]) {
        layout[drag.id] = [nx, ny];
        drag.moved = true;
        draw();
      }
      return;
    }
    if (drag && drag.mode === 'connect') {
      drag.to = p;
      draw();
      return;
    }
    if (drag && drag.mode === 'pan') {
      scroll.scrollLeft = drag.sl - (e.clientX - drag.sx);
      scroll.scrollTop = drag.st - (e.clientY - drag.sy);
      return;
    }
    if (e.pointerType !== 'mouse') { return; } // hover text is a mouse thing
    const node = hitNode(p);
    if (node) {
      const level = campaign.Levels[node];
      hover.textContent = node + (level ? ' - ' + (level.kind || '?') + ' depth ' + (level.depth || 0)
        + (level.file ? ' - ' + level.file : '') : ' - NOT in Levels (dangling)')
        + ' - drag the right anchor onto another level to connect';
    } else {
      hover.textContent = hitAnchor(p)
        ? 'drag from the anchor to a level to wire an exit'
        : 'click a node to edit its campaign properties - double-click to open its level map';
    }
  });

  function pointerEnd(e) {
    if (activePointer !== null && e.pointerId !== activePointer) { return; }
    activePointer = null;
    if (!drag) { return; }
    if (drag.mode === 'connect') {
      const p = canvasPos(e);
      const target = hitNode(p);
      if (target && target !== drag.id) {
        connect(drag.id, target);
      } else if (target === drag.id) {
        status.className = 'hint';
        status.textContent = 'cannot connect a level to itself';
      }
    } else if (drag.mode === 'move') {
      if (drag.moved) {
        saveLayout(); // position only - content is not dirty
      } else {
        // tap without a drag: second tap on the same node opens its map
        const now = Date.now();
        if (lastTap && lastTap.id === drag.id && now - lastTap.t < 400) {
          lastTap = null;
          openLevelFile(drag.id);
        } else {
          lastTap = { id: drag.id, t: now };
        }
      }
    }
    drag = null;
    draw();
  }
  canvas.addEventListener('pointerup', pointerEnd);
  window.addEventListener('pointerup', pointerEnd); // capture-failure fallback
  canvas.addEventListener('pointercancel', e => {
    if (activePointer !== null && e.pointerId !== activePointer) { return; }
    activePointer = null;
    drag = null;
    draw();
  });

  canvas.addEventListener('dblclick', e => {
    const node = hitNode(canvasPos(e));
    if (node) { openLevelFile(node); }
  });

  // ---- side panel: per-level campaign properties --------------------------
  const MANAGED = ['kind', 'depth', 'file', 'music', 'fallbackMusic', 'script', 'treasury',
    'objectsKind', 'nMobs', 'viewDistance', 'maxBrightness', 'feeling', 'size',
    'isSafe', 'noFogOfWar', 'noBuild', 'isStatic'];

  const datalists = {};
  function ensureDatalist(id, dir, accept, transform) {
    if (datalists[id]) { return datalists[id]; }
    const dl = document.createElement('datalist');
    dl.id = id;
    datalists[id] = dl;
    panel.appendChild(dl);
    for (const builtin of [false, true]) {
      api.modFiles(ctx.mod, dir, builtin).then(list => {
        for (const entry of list.entries) {
          if (entry.dir || !accept.test(entry.name)) { continue; }
          const value = transform(entry.name);
          if (!value || [...dl.options].some(o => o.value === value)) { continue; }
          const o = document.createElement('option');
          o.value = value;
          dl.appendChild(o);
        }
      }).catch(() => { /* free text still works */ });
    }
    return dl;
  }

  function clearPanel() { panel.innerHTML = ''; for (const k of Object.keys(datalists)) { delete datalists[k]; } }

  function fieldRow(grid, labelText, node) {
    const label = document.createElement('label');
    label.textContent = labelText;
    grid.appendChild(label);
    grid.appendChild(node);
    return node;
  }

  // text/number input bound to a campaign level key; empty removes the key
  function boundInput(level, key, type) {
    const input = document.createElement('input');
    input.type = 'text'; // numbers too: free-typed junk must not corrupt json
    input.value = level[key] === undefined ? '' : String(level[key]);
    input.addEventListener('change', () => {
      const v = input.value.trim();
      if (v === '') { delete level[key]; }
      else if (type === 'int') { level[key] = Number.parseInt(v, 10); if (!Number.isFinite(level[key])) { delete level[key]; } }
      else if (type === 'float') { level[key] = Number.parseFloat(v); if (!Number.isFinite(level[key])) { delete level[key]; } }
      else { level[key] = v; }
      if (level[key] === undefined) { delete level[key]; }
      markDirty();
      draw(); // header badges (depth/music markers) may change
    });
    return input;
  }

  function boundCheck(level, key) {
    const box = document.createElement('input');
    box.type = 'checkbox';
    box.checked = level[key] === true || level[key] === 'true';
    box.addEventListener('change', () => {
      if (box.checked) { level[key] = true; } else { delete level[key]; }
      markDirty();
    });
    return box;
  }

  function renderPanel() {
    clearPanel();
    // a canvas-tapped edge gets its own mini panel - Delete-key parity for
    // touch users (bd a5d)
    if (selEdge) {
      const h = document.createElement('h3');
      h.textContent = 'Edge ' + selEdge.from + ' → ' + selEdge.to;
      panel.appendChild(h);
      const p = document.createElement('p');
      p.className = 'hint';
      p.textContent = 'Exit #' + selEdge.idx + ' of ' + selEdge.from + ' leads to '
        + selEdge.to + '. Deleting it also drops the mirrored entrance entry.';
      panel.appendChild(p);
      const row = document.createElement('div');
      row.className = 'btn-row';
      const del = document.createElement('button');
      del.className = 'danger';
      del.textContent = 'Delete edge';
      del.addEventListener('click', () => removeEdge(selEdge.from, selEdge.idx));
      row.appendChild(del);
      const clear = document.createElement('button');
      clear.textContent = 'Select nothing';
      clear.addEventListener('click', () => { selEdge = null; renderPanel(); draw(); });
      row.appendChild(clear);
      panel.appendChild(row);
      return;
    }
    if (!sel) {
      const p = document.createElement('p');
      p.className = 'hint';
      p.textContent = 'Select a level node to edit its campaign entry, or drag an anchor to wire an exit.'
        + (coarsePointer ? ' Drag empty space to pan the graph; double-tap a node to open its map.' : '');
      panel.appendChild(p);
      return;
    }

    // the stub id (usually "0") gets an explainer, not a level form - its
    // Levels entry (SewerLevel size 0x0) is engine scaffolding, not content
    if (sel === entranceId() && (!campaign.Levels[sel] || sel === '0')) {
      const h = document.createElement('h3');
      h.textContent = sel + ' (entrance stub)';
      panel.appendChild(h);
      const p = document.createElement('p');
      p.className = 'hint';
      p.textContent = 'The hero never occupies this node - the first descend lands on its first exit. Wire exits by dragging the anchor.';
      panel.appendChild(p);
      const levelsPanel = document.createElement('details');
      const summary = document.createElement('summary');
      summary.className = 'hint';
      summary.textContent = 'raw Levels entry (engine scaffolding)';
      levelsPanel.appendChild(summary);
      const pre = document.createElement('pre');
      pre.textContent = JSON.stringify(campaign.Levels[sel] || {}, null, 2);
      levelsPanel.appendChild(pre);
      panel.appendChild(levelsPanel);
      renderEntranceSelect();
      return;
    }

    const level = campaign.Levels[sel];
    if (!level) {
      const h = document.createElement('h3');
      h.textContent = sel + ' (dangling Graph entry)';
      panel.appendChild(h);
      const p = document.createElement('p');
      p.className = 'hint';
      p.textContent = 'The Graph references this id but Levels does not define it - navigation dead-ends here. Delete it or add a Levels entry.';
      panel.appendChild(p);
      const del = document.createElement('button');
      del.className = 'danger';
      del.textContent = 'Remove Graph entry';
      del.addEventListener('click', () => { delete campaign.Graph[sel]; markDirty(); sel = null; renderPanel(); draw(); });
      panel.appendChild(del);
      return;
    }

    const h = document.createElement('h3');
    h.textContent = sel;
    panel.appendChild(h);

    // rename + delete row
    const rowBtns = document.createElement('div');
    rowBtns.className = 'btn-row';
    const renameBtn = document.createElement('button');
    renameBtn.textContent = 'Rename id';
    renameBtn.addEventListener('click', () => {
      const to = (window.prompt('Rename level id ' + sel + ' to:', sel) || '').trim();
      if (!to || to === sel) { return; }
      if (!/^[\w.-]+$/.test(to)) {
        status.className = 'status-err';
        status.textContent = 'ids must be word characters (letters, digits, _ . -)';
        return;
      }
      if (campaign.Levels[to] || (campaign.Graph || {})[to]) {
        status.className = 'status-err';
        status.textContent = 'id already exists: ' + to;
        return;
      }
      renameLevel(sel, to);
      status.className = 'status-ok';
      status.textContent = 'renamed to ' + to + ' (all Graph references updated)';
    });
    rowBtns.appendChild(renameBtn);

    const delBtn = document.createElement('button');
    delBtn.className = 'danger';
    delBtn.textContent = 'Delete level';
    delBtn.addEventListener('click', () => {
      if (!window.confirm('Remove ' + sel + ' from the campaign? (the level file stays on disk)')) { return; }
      deleteNode(sel);
      status.className = 'status-ok';
      status.textContent = 'removed ' + sel + ' from Levels and Graph';
    });
    rowBtns.appendChild(delBtn);

    // touch-visible alternative to double-click/double-tap (bd a5d)
    if (level.file) {
      const openBtn = document.createElement('button');
      openBtn.textContent = 'Open level file';
      openBtn.addEventListener('click', () => openLevelFile(sel));
      rowBtns.appendChild(openBtn);
    }
    panel.appendChild(rowBtns);

    const grid = document.createElement('div');
    grid.className = 'field-grid';
    panel.appendChild(grid);

    // kind: closed select of registered kinds + free-text escape hatch
    const kindSel = document.createElement('select');
    const current = level.kind || '';
    for (const k of LEVEL_KINDS) {
      const o = document.createElement('option');
      o.value = k;
      o.textContent = k;
      kindSel.appendChild(o);
    }
    if (current && !LEVEL_KINDS.includes(current)) {
      const o = document.createElement('option');
      o.value = current;
      o.textContent = current + ' (custom)';
      kindSel.appendChild(o);
    }
    const o = document.createElement('option');
    o.value = '__custom__';
    o.textContent = 'custom...';
    kindSel.appendChild(o);
    kindSel.value = [...kindSel.options].some(o => o.value === (current || 'PredesignedLevel'))
      ? (current || 'PredesignedLevel') : '__custom__';
    kindSel.addEventListener('change', () => {
      if (kindSel.value === '__custom__') {
        const v = (window.prompt('Custom level kind (java class simple name):', current) || '').trim();
        if (v) { level.kind = v; markDirty(); }
      } else {
        level.kind = kindSel.value;
        markDirty();
      }
      renderPanel(); draw();
    });
    fieldRow(grid, 'kind', kindSel);

    fieldRow(grid, 'depth', boundInput(level, 'depth', 'int'));

    const fileInput = boundInput(level, 'file');
    fileInput.setAttribute('list', 'campaign-level-files');
    fieldRow(grid, 'file', fileInput);
    ensureDatalist('campaign-level-files', 'levelsDesc', /\.json$/i,
      name => name.toLowerCase() === 'dungeon.json' ? null : 'levelsDesc/' + name);

    // create + wire a fresh level file for this node in one click
    const newFileBtn = document.createElement('button');
    newFileBtn.textContent = 'New level file for this node';
    newFileBtn.addEventListener('click', async () => {
      const name = window.prompt('New level file name (under levelsDesc/):', sel + '.json');
      if (!name) { return; }
      const path = 'levelsDesc/' + name.replace(/^levelsDesc\//, '');
      const template = templateFor(path);
      try {
        await api.saveModFile(ctx.mod, path, template ? template.content : '');
        level.file = path;
        markDirty();
        ctx.onSaved && ctx.onSaved();
        renderPanel(); draw();
        status.className = 'status-ok';
        status.textContent = 'created ' + path + ' - double-click the node to paint it';
      } catch (e) {
        status.className = 'status-err';
        status.textContent = e.message;
      }
    });
    fieldRow(grid, '', newFileBtn);

    for (const key of ['music', 'fallbackMusic']) {
      const input = boundInput(level, key);
      input.setAttribute('list', 'campaign-music');
      fieldRow(grid, key, input);
    }
    ensureDatalist('campaign-music', 'sound', /\.(ogg|mp3|wav)$/i,
      name => name.replace(/\.(ogg|mp3|wav)$/i, ''));

    fieldRow(grid, 'script', boundInput(level, 'script'));
    const treasuryInput = boundInput(level, 'treasury');
    treasuryInput.setAttribute('list', 'campaign-treasury');
    fieldRow(grid, 'treasury', treasuryInput);
    ensureDatalist('campaign-treasury', 'levelsDesc', /treasury\.json$/i,
      name => 'levelsDesc/' + name);

    const feelSel = document.createElement('select');
    {
      const o = document.createElement('option');
      o.value = '';
      o.textContent = '(default)';
      feelSel.appendChild(o);
      for (const f of FEELINGS) {
        const opt = document.createElement('option');
        opt.value = f;
        opt.textContent = f;
        feelSel.appendChild(opt);
      }
      feelSel.value = level.feeling || '';
      feelSel.addEventListener('change', () => {
        if (feelSel.value === '') { delete level.feeling; } else { level.feeling = feelSel.value; }
        markDirty();
      });
    }
    fieldRow(grid, 'feeling', feelSel);

    for (const key of ['nMobs', 'viewDistance']) {
      fieldRow(grid, key, boundInput(level, key, 'int'));
    }
    fieldRow(grid, 'maxBrightness', boundInput(level, 'maxBrightness', 'float'));
    fieldRow(grid, 'objectsKind', boundInput(level, 'objectsKind'));

    // size = [w, h]
    const sizeRow = document.createElement('div');
    const wIn = document.createElement('input');
    const hIn = document.createElement('input');
    wIn.type = 'number'; hIn.type = 'number';
    wIn.style.width = '4.5em'; hIn.style.width = '4.5em';
    wIn.value = Array.isArray(level.size) ? level.size[0] : '';
    hIn.value = Array.isArray(level.size) ? level.size[1] : '';
    const sizeApply = () => {
      const w = Number.parseInt(wIn.value, 10);
      const h = Number.parseInt(hIn.value, 10);
      if (Number.isFinite(w) && Number.isFinite(h)) {
        level.size = [w, h];
      } else {
        delete level.size;
      }
      markDirty();
    };
    wIn.addEventListener('change', sizeApply);
    hIn.addEventListener('change', sizeApply);
    sizeRow.appendChild(wIn);
    sizeRow.appendChild(document.createTextNode(' \u00d7 '));
    sizeRow.appendChild(hIn);
    fieldRow(grid, 'size (gen levels)', sizeRow);

    const checks = document.createElement('div');
    checks.className = 'check-row';
    for (const key of ['isSafe', 'noFogOfWar', 'noBuild', 'isStatic']) {
      const label = document.createElement('label');
      label.className = 'check-label';
      const box = boundCheck(level, key);
      label.appendChild(box);
      label.appendChild(document.createTextNode(' ' + key));
      checks.appendChild(label);
    }
    grid.appendChild(document.createElement('span'));
    grid.appendChild(checks);

    // unknown keys the form does not model - shown, never dropped
    const unknown = Object.keys(level).filter(k => !MANAGED.includes(k));
    if (unknown.length) {
      const details = document.createElement('details');
      const summary = document.createElement('summary');
      summary.className = 'hint';
      summary.textContent = unknown.length + ' other key(s) preserved: ' + unknown.join(', ');
      details.appendChild(summary);
      const pre = document.createElement('pre');
      const copy = {};
      for (const k of unknown) { copy[k] = level[k]; }
      pre.textContent = JSON.stringify(copy, null, 2);
      details.appendChild(pre);
      panel.appendChild(details);
    }

    // exits: order matters - multiexit cell i leads to exits[i]
    renderExitsList(level);

    renderEntranceSelect();
  }

  function renderEntranceSelect() {
    const grid = document.createElement('div');
    grid.className = 'field-grid';
    const selEl = document.createElement('select');
    const current = entranceId();
    const ids = Object.keys(campaign.Levels).concat(['0']);
    if (!ids.includes(current)) { ids.unshift(current); }
    for (const id of ids) {
      const o = document.createElement('option');
      o.value = id;
      o.textContent = id + (id === '0' ? ' (stub)' : '');
      selEl.appendChild(o);
    }
    selEl.value = current;
    selEl.addEventListener('change', () => {
      campaign.Entrance = selEl.value;
      markDirty();
      draw();
    });
    const label = document.createElement('label');
    label.textContent = 'Entrance';
    grid.appendChild(label);
    grid.appendChild(selEl);
    panel.appendChild(grid);
  }

  function renderExitsList(level) {
    const box = document.createElement('div');
    box.className = 'panel';
    const h = document.createElement('h4');
    h.textContent = 'Exits (in multiexit order)';
    box.appendChild(h);
    const pair = graphPair(sel, false);
    const exits = pair ? pair[0] : [];
    if (!exits.length) {
      const p = document.createElement('p');
      p.className = 'hint';
      p.textContent = 'no exits - the hero cannot leave by stairs. Drag the node anchor onto another level.';
      box.appendChild(p);
    }
    exits.forEach((to, idx) => {
      const row = document.createElement('div');
      row.className = 'btn-row';
      const label = document.createElement('span');
      label.className = 'grow';
      label.textContent = '#' + idx + '  ' + to + (campaign.Levels[to] ? '' : '  (missing!)');
      if (!campaign.Levels[to]) { label.style.color = '#e05050'; }
      row.appendChild(label);

      const up = document.createElement('button');
      up.textContent = '↑';
      up.title = 'swap with the previous exit (multiexit cell order)';
      up.disabled = idx === 0;
      up.addEventListener('click', () => {
        const t = exits[idx - 1];
        exits[idx - 1] = exits[idx];
        exits[idx] = t;
        markDirty();
        renderPanel(); draw();
      });
      row.appendChild(up);

      const del = document.createElement('button');
      del.textContent = '✕';
      del.title = 'remove this exit (and its mirrored entrance)';
      del.addEventListener('click', () => removeEdge(sel, idx));
      row.appendChild(del);
      box.appendChild(row);
    });
    // physical reality check: exit i needs exit-cell i in the level file -
    // more Graph exits than exit+multiexit cells leaves the extras
    // unreachable (bd igf; validate warns too)
    const phys = document.createElement('p');
    phys.className = 'hint';
    box.appendChild(phys);
    if (level && level.file) {
      api.readModFile(ctx.mod, level.file).then(data => {
        if (!phys.isConnected) { return; } // panel re-rendered meanwhile
        const map = JSON.parse(data.content);
        const cells = (map.exit ? 1 : 0) + (Array.isArray(map.multiexit) ? map.multiexit.length : 0);
        phys.textContent = exits.length + ' graph exit(s) · ' + cells +
          ' physical exit cell(s) in ' + level.file.split('/').pop() +
          (exits.length > cells ? ' - paint more multiexit cells or the extra exits stay unreachable' : '');
        if (exits.length > cells) { phys.className = 'status-err'; }
      }).catch(() => { phys.remove(); });
    }
    // entered-from: informational + manual one-way surgery
    const enters = pair ? pair[1] : [];
    if (enters.length) {
      const h2 = document.createElement('h4');
      h2.textContent = 'Entered from';
      box.appendChild(h2);
      for (const from of enters) {
        const row = document.createElement('div');
        row.className = 'btn-row';
        const label = document.createElement('span');
        label.className = 'grow';
        label.textContent = from + (campaign.Levels[from] ? '' : '  (missing!)');
        if (!campaign.Levels[from]) { label.style.color = '#e05050'; }
        row.appendChild(label);
        const del = document.createElement('button');
        del.textContent = '\u2715';
        del.title = 'drop only this ascend-back link (one-way exit from ' + from + ')';
        del.addEventListener('click', () => {
          const i = enters.indexOf(from);
          if (i >= 0) { enters.splice(i, 1); }
          markDirty();
          renderPanel(); draw();
        });
        row.appendChild(del);
      }
    }
    panel.appendChild(box);
  }

  // ---- top-bar actions ----------------------------------------------------
  addBtn.addEventListener('click', () => {
    const id = (window.prompt('New level id:', 'level' + (Object.keys(campaign.Levels).length + 1)) || '').trim();
    if (!id) { return; }
    if (!/^[\w.-]+$/.test(id)) {
      status.className = 'status-err';
      status.textContent = 'ids must be word characters (letters, digits, _ . -)';
      return;
    }
    if (campaign.Levels[id] || (campaign.Graph || {})[id]) {
      status.className = 'status-err';
      status.textContent = 'id already exists: ' + id;
      return;
    }
    // extending from the selected node is the common case - offer the wire
    const prev = sel;
    let maxDepth = 0;
    for (const lv of Object.values(campaign.Levels)) {
      maxDepth = Math.max(maxDepth, Number(lv && lv.depth) || 0);
    }
    campaign.Levels[id] = { kind: 'PredesignedLevel', depth: maxDepth + 1 };
    graphPair(id, true);
    // park it in a fresh row of its depth column
    const sameCol = Object.keys(campaign.Levels).filter(
      x => (Number(campaign.Levels[x].depth) || 0) === maxDepth + 1);
    const gaps = autoLayout();
    const base = gaps[id] || [0, 0];
    layout[id] = [base[0], 30 + sameCol.length * 68];
    markDirty();
    if (prev && prev !== id && campaign.Levels[prev]) {
      connect(prev, id);
    } else {
      sel = id; selEdge = null;
      status.className = 'hint';
      status.textContent = 'created ' + id + ' - pick a level file (or create one) in the panel, then drag an anchor onto it from its parent';
      renderPanel(); draw();
    }
  });

  relayoutBtn.addEventListener('click', () => {
    try { localStorage.removeItem(layoutKey); } catch (e) { /* fine */ }
    layout = autoLayout();
    draw();
  });

  document.addEventListener('keydown', onDeleteKey);
  function onDeleteKey(e) {
    // Delete only - Backspace doubles as navigate-back in embedded browsers
    if (e.key !== 'Delete') { return; }
    const tag = (document.activeElement && document.activeElement.tagName) || '';
    if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT') { return; }
    if (selEdge) {
      removeEdge(selEdge.from, selEdge.idx);
    } else if (sel && sel !== entranceId() && campaign.Levels[sel]) {
      if (window.confirm('Remove ' + sel + ' from the campaign?')) { deleteNode(sel); }
    }
  }

  save.addEventListener('click', async () => {
    try {
      await api.saveModFile(ctx.mod, ctx.file, JSON.stringify(campaign, null, 3) + '\n');
      status.className = 'status-ok';
      status.textContent = 'saved';
      dirty = false;
      ctx.onSaved && ctx.onSaved();
    } catch (e) {
      status.className = 'status-err';
      status.textContent = e.message;
    }
  });

  renderPanel();
  draw();

  return {
    isDirty: () => dirty,
    dispose: () => document.removeEventListener('keydown', onDeleteKey)
  };
}
