// Level map editor for levelsDesc/<X>.json predesigned levels.
//
// v1 renders terrain as a color-coded grid (editor view, not WYSIWYG -
// the game's auto-tiling lives in VariativeDungeonTilemap). Painting:
//  - terrain palette -> map[]
//  - "entrance"/"exit" tools -> entrance / multiexit
//  - mob tool -> click places the picked mob kind
//  - tile-var painting onto baseTileVar from the tileset preview
// Unknown json keys are preserved on save.

import * as api from '../api.js';

const TERRAIN_COLORS = {
  CHASM: '#0a0a12', EMPTY: '#3a3a30', GRASS: '#3a5a30', EMPTY_WELL: '#3a3a38',
  WALL: '#6a6a60', DOOR: '#8a6a30', OPEN_DOOR: '#aa8a40', ENTRANCE: '#40c0ff',
  EXIT: '#ffb040', EMBERS: '#5a4030', LOCKED_DOOR: '#7a5a20', PEDESTAL: '#8a8a80',
  WALL_DECO: '#7a7a90', BARRICADE: '#8a6a3a', EMPTY_SP: '#4a4a40', HIGH_GRASS: '#2a6a20',
  EMPTY_DECO: '#4a4a38', LOCKED_EXIT: '#a07020', UNLOCKED_EXIT: '#c09030', SIGN: '#9a9a8a',
  WELL: '#3060a0', STATUE: '#8a8a9a', STATUE_SP: '#9a9aa8', BOOKSHELF: '#7a6040',
  ALCHEMY: '#30a080', CHASM_FLOOR: '#20202a', CHASM_FLOOR_SP: '#282834',
  CHASM_WALL: '#15151f', CHASM_WATER: '#153040', SECRET_DOOR: '#6a6a60',
  WATER_TILES: '#204050', WATER: '#2050a0'
};

const TOOLS = ['terrain', 'entrance', 'exit', 'mob', 'erase-mob', 'var'];
const LAYERS = ['baseTileVar', 'decoTileVar', 'deco2TileVar'];

export async function levelEditor(content, ctx) {
  const data = await api.readModFile(ctx.mod, ctx.file);
  let level;
  try {
    level = JSON.parse(data.content);
  } catch (e) {
    content.textContent = 'Cannot parse ' + ctx.file + ': ' + e.message;
    return;
  }
  level.map = level.map || [];
  level.width = level.width || 0;
  level.height = level.height || 0;
  level.mobs = level.mobs || [];
  level.multiexit = level.multiexit || [];

  const terrain = (await api.terrainTable());
  const terrainByValue = {};
  for (const t of terrain.terrain) { terrainByValue[t.value] = t.name; }

  let paintTerrain = 1; // EMPTY
  let tool = 'terrain';
  let varLayer = 'baseTileVar';
  let varIndex = -1;
  let mobKind = '';
  let zoom = 18;
  let dirty = false;

  const barEl = document.createElement('div');
  barEl.className = 'editor-bar';
  const save = document.createElement('button');
  save.className = 'primary';
  save.textContent = 'Save';
  barEl.appendChild(save);
  content.appendChild(barEl);

  const status = document.createElement('div');
  content.appendChild(status);

  // ------------------------------------------------------------------ tools
  const toolPanel = document.createElement('div');
  toolPanel.className = 'panel';
  const toolGrid = document.createElement('div');
  toolGrid.className = 'field-grid';
  toolPanel.appendChild(toolGrid);
  content.appendChild(toolPanel);

  function toolRow(labelText, node) {
    const label = document.createElement('label');
    label.textContent = labelText;
    toolGrid.appendChild(label);
    toolGrid.appendChild(node);
  }

  const toolSelect = document.createElement('select');
  for (const t of TOOLS) {
    const o = document.createElement('option');
    o.value = t;
    o.textContent = t;
    toolSelect.appendChild(o);
  }
  toolSelect.addEventListener('change', () => { tool = toolSelect.value; updateToolHint(); });
  toolRow('tool', toolSelect);

  const terrainSelect = document.createElement('select');
  const grouped = terrain.terrain
    .filter(t => !t.name.includes('TRAP') || t.name.startsWith('SECRET'))
    .sort((a, b) => a.value - b.value);
  for (const t of grouped) {
    const o = document.createElement('option');
    o.value = t.value;
    o.textContent = t.value + ' ' + t.name;
    terrainSelect.appendChild(o);
  }
  terrainSelect.value = paintTerrain;
  terrainSelect.addEventListener('change', () => { paintTerrain = Number(terrainSelect.value); });
  toolRow('terrain', terrainSelect);

  const mobSelect = document.createElement('select');
  toolRow('mob kind', mobSelect);
  api.modFiles(ctx.mod, 'mobsDesc').then(list => {
    for (const entry of list.entries) {
      if (!entry.name.endsWith('.json')) { continue; }
      const o = document.createElement('option');
      o.value = entry.name.replace(/\.json$/, '');
      o.textContent = o.value + ' (this mod)';
      mobSelect.appendChild(o);
    }
  }).catch(() => {});
  api.modFiles(ctx.mod, 'mobsDesc', true).then(list => {
    for (const entry of list.entries) {
      if (!entry.name.endsWith('.json')) { continue; }
      const kind = entry.name.replace(/\.json$/, '');
      if ([...mobSelect.options].some(o => o.value === kind)) { continue; }
      const o = document.createElement('option');
      o.value = kind;
      o.textContent = kind + ' (builtin)';
      mobSelect.appendChild(o);
    }
  }).catch(() => {});
  mobSelect.addEventListener('change', () => { mobKind = mobSelect.value; });

  const layerSelect = document.createElement('select');
  for (const l of LAYERS) {
    const o = document.createElement('option');
    o.value = l;
    o.textContent = l;
    layerSelect.appendChild(o);
  }
  layerSelect.addEventListener('change', () => { varLayer = layerSelect.value; });
  toolRow('var layer', layerSelect);

  // picking a tile always yields >= 0, so clearing needs its own affordance
  const varClearBtn = document.createElement('button');
  varClearBtn.textContent = 'pick -1 (clear)';
  varClearBtn.addEventListener('click', () => {
    varIndex = -1;
    tool = 'var';
    toolSelect.value = 'var';
    updateToolHint();
    drawTileset();
    tileHover.textContent = 'picked ' + varLayer + ' index -1 - painting now resets cells';
  });
  toolRow('var pick', varClearBtn);

  const zoomSelect = document.createElement('select');
  for (const z of [12, 18, 24, 32]) {
    const o = document.createElement('option');
    o.value = z;
    o.textContent = z + 'px';
    zoomSelect.appendChild(o);
  }
  zoomSelect.value = zoom;
  zoomSelect.addEventListener('change', () => { zoom = Number(zoomSelect.value); drawGrid(); });
  toolRow('zoom', zoomSelect);

  const sizeRow = document.createElement('div');
  const wInput = document.createElement('input');
  wInput.type = 'number';
  wInput.value = level.width;
  wInput.style.width = '5em';
  const hInput = document.createElement('input');
  hInput.type = 'number';
  hInput.value = level.height;
  hInput.style.width = '5em';
  const resizeBtn = document.createElement('button');
  resizeBtn.textContent = 'Resize';
  resizeBtn.addEventListener('click', () => resize(Number(wInput.value), Number(hInput.value)));
  sizeRow.appendChild(wInput);
  sizeRow.appendChild(document.createTextNode(' × '));
  sizeRow.appendChild(hInput);
  sizeRow.appendChild(resizeBtn);
  toolRow('size', sizeRow);

  const toolHint = document.createElement('span');
  toolHint.className = 'hint';
  toolPanel.appendChild(toolHint);

  // wire this level into levelsDesc/Dungeon.json: Levels entry + Graph edge
  // from an existing level (the manual json dance the live test called the
  // biggest UX gap)
  const campaignBtn = document.createElement('button');
  campaignBtn.textContent = 'Add to campaign (Dungeon.json)';
  campaignBtn.addEventListener('click', async () => {
    try {
      const base = ctx.file.split('/').pop().replace(/\.json$/, '');
      const defaultId = base.charAt(0).toLowerCase() + base.slice(1);
      const d = await api.readModFile(ctx.mod, 'levelsDesc/Dungeon.json');
      const campaign = JSON.parse(d.content);
      campaign.Levels = campaign.Levels || {};
      campaign.Graph = campaign.Graph || {};

      const id = (window.prompt('Level id in Dungeon.json:', defaultId) || '').trim();
      if (!id) { return; }
      if (campaign.Levels[id]) {
        status.className = 'status-err';
        status.textContent = 'level id already exists: ' + id;
        return;
      }
      const fromIds = Object.keys(campaign.Levels);
      const from = (window.prompt('Connect an exit FROM which level id?',
        fromIds.includes('start') ? 'start' : (fromIds[0] || '')) || '').trim();
      if (!from || !campaign.Levels[from]) {
        status.className = 'status-err';
        status.textContent = 'unknown source level: ' + from;
        return;
      }
      let depth = 1;
      const fromDepth = Number(campaign.Levels[from].depth);
      if (Number.isFinite(fromDepth)) { depth = fromDepth + 1; }
      const dt = window.prompt('Depth (difficulty scale):', String(depth));
      if (dt === null) { return; }

      campaign.Levels[id] = {
        kind: 'PredesignedLevel',
        depth: Number(dt) || depth,
        file: ctx.file
      };
      if (!campaign.Graph[from]) { campaign.Graph[from] = [[], []]; }
      if (!campaign.Graph[from][0].includes(id)) { campaign.Graph[from][0].push(id); }
      campaign.Graph[id] = [[], [from]];

      await api.saveModFile(ctx.mod, 'levelsDesc/Dungeon.json', JSON.stringify(campaign, null, 3) + '\n');
      status.className = 'status-ok';
      status.textContent = 'campaign: ' + from + ' -> ' + id + ' wired, depth ' + (Number(dt) || depth);
    } catch (e) {
      status.className = 'status-err';
      status.textContent = e.message;
    }
  });
  toolPanel.appendChild(campaignBtn);
  function updateToolHint() {
    const hints = {
      terrain: 'drag to paint the selected terrain value',
      entrance: 'click to set the hero entrance',
      exit: 'click to toggle an exit tile (multiexit)',
      mob: 'click to place the selected mob kind',
      'erase-mob': 'click a mob to remove it',
      var: 'click to paint the picked tile index onto the selected var layer (-1 clears)'
    };
    toolHint.textContent = hints[tool] || '';
  }
  updateToolHint();

  // ------------------------------------------------------------------ canvas
  const canvas = document.createElement('canvas');
  canvas.className = 'sprite-preview';
  content.appendChild(canvas);
  const hover = document.createElement('div');
  hover.className = 'hint';
  content.appendChild(hover);

  function cellAt(event) {
    const rect = canvas.getBoundingClientRect();
    const x = Math.floor((event.clientX - rect.left) / zoom);
    const y = Math.floor((event.clientY - rect.top) / zoom);
    if (x < 0 || y < 0 || x >= level.width || y >= level.height) { return null; }
    return { x, y, cell: y * level.width + x };
  }

  function drawGrid() {
    canvas.width = level.width * zoom;
    canvas.height = level.height * zoom;
    const g = canvas.getContext('2d');
    g.fillStyle = '#000';
    g.fillRect(0, 0, canvas.width, canvas.height);
    for (let y = 0; y < level.height; y++) {
      for (let x = 0; x < level.width; x++) {
        const cell = y * level.width + x;
        const t = level.map[cell];
        const name = terrainByValue[t] || '';
        g.fillStyle = TERRAIN_COLORS[name] || (t === undefined ? '#000' : '#803030');
        g.fillRect(x * zoom, y * zoom, zoom, zoom);
        // var overlay marker
        if (level[varLayer] && level[varLayer][cell] >= 0) {
          g.fillStyle = 'rgba(255,255,255,0.25)';
          g.fillRect(x * zoom, y * zoom, zoom, 3);
        }
      }
    }
    // entrance / exits
    function mark(pos, color, label) {
      g.strokeStyle = color;
      g.lineWidth = 2;
      g.strokeRect(pos[0] * zoom + 1, pos[1] * zoom + 1, zoom - 2, zoom - 2);
      if (label) {
        g.fillStyle = color;
        g.font = (zoom / 2) + 'px monospace';
        g.fillText(label, pos[0] * zoom + 3, pos[1] * zoom + zoom - 3);
      }
    }
    if (level.entrance) { mark(level.entrance, '#40c0ff', 'E'); }
    for (const exit of level.multiexit || []) { mark(exit, '#ffb040', 'X'); }
    // mobs
    for (const mob of level.mobs) {
      g.fillStyle = '#ff5050';
      g.beginPath();
      g.arc(mob.x * zoom + zoom / 2, mob.y * zoom + zoom / 2, zoom / 4, 0, Math.PI * 2);
      g.fill();
    }
    // grid lines
    g.strokeStyle = 'rgba(255,255,255,0.08)';
    g.lineWidth = 1;
    for (let x = 0; x <= level.width; x++) {
      g.beginPath(); g.moveTo(x * zoom, 0); g.lineTo(x * zoom, canvas.height); g.stroke();
    }
    for (let y = 0; y <= level.height; y++) {
      g.beginPath(); g.moveTo(0, y * zoom); g.lineTo(canvas.width, y * zoom); g.stroke();
    }
  }

  function applyTool(pos) {
    dirty = true;
    if (tool === 'terrain') {
      level.map[pos.cell] = paintTerrain;
    } else if (tool === 'entrance') {
      level.entrance = [pos.x, pos.y];
    } else if (tool === 'exit') {
      const idx = (level.multiexit || []).findIndex(e => e[0] === pos.x && e[1] === pos.y);
      if (idx >= 0) { level.multiexit.splice(idx, 1); } else { level.multiexit.push([pos.x, pos.y]); }
    } else if (tool === 'mob') {
      if (mobKind && !level.mobs.some(m => m.x === pos.x && m.y === pos.y)) {
        level.mobs.push({ kind: mobKind, x: pos.x, y: pos.y });
      }
    } else if (tool === 'erase-mob') {
      const idx = level.mobs.findIndex(m => m.x === pos.x && m.y === pos.y);
      if (idx >= 0) { level.mobs.splice(idx, 1); }
    } else if (tool === 'var') {
      if (!level[varLayer]) { level[varLayer] = new Array(level.map.length).fill(-1); }
      level[varLayer][pos.cell] = varIndex;
    }
    drawGrid();
  }

  let painting = false;
  canvas.addEventListener('mousedown', e => {
    const pos = cellAt(e);
    if (!pos) { return; }
    painting = true;
    applyTool(pos);
  });
  canvas.addEventListener('mousemove', e => {
    const pos = cellAt(e);
    if (!pos) { return; }
    const name = terrainByValue[level.map[pos.cell]] || '?';
    let extra = '';
    if (level[varLayer] && level[varLayer][pos.cell] >= 0) {
      extra += ' ' + varLayer + '=' + level[varLayer][pos.cell];
    }
    const mob = level.mobs.find(m => m.x === pos.x && m.y === pos.y);
    if (mob) { extra += ' mob=' + mob.kind; }
    hover.textContent = `x=${pos.x} y=${pos.y} cell=${pos.cell} terrain=${level.map[pos.cell]} (${name})${extra}`;
    if (painting && (tool === 'terrain' || tool === 'var')) { applyTool(pos); }
  });
  window.addEventListener('mouseup', () => { painting = false; });

  function resize(w, h) {
    if (w <= 0 || h <= 0 || w > 200 || h > 200) {
      status.className = 'status-err';
      status.textContent = 'size out of range (1..200)';
      return;
    }
    const newMap = new Array(w * h).fill(1);
    for (let y = 0; y < Math.min(h, level.height); y++) {
      for (let x = 0; x < Math.min(w, level.width); x++) {
        newMap[y * w + x] = level.map[y * level.width + x];
      }
    }
    for (const layer of LAYERS) {
      if (level[layer]) {
        const fresh = new Array(w * h).fill(-1);
        for (let y = 0; y < Math.min(h, level.height); y++) {
          for (let x = 0; x < Math.min(w, level.width); x++) {
            fresh[y * w + x] = level[layer][y * level.width + x];
          }
        }
        level[layer] = fresh;
      }
    }
    level.width = w;
    level.height = h;
    level.map = newMap;
    level.mobs = level.mobs.filter(m => m.x < w && m.y < h);
    level.multiexit = (level.multiexit || []).filter(e => e[0] < w && e[1] < h);
    dirty = true;
    drawGrid();
  }

  drawGrid();

  // ------------------------------------------------------------- tileset
  const tilePanel = document.createElement('div');
  tilePanel.className = 'panel';
  tilePanel.innerHTML = '<h3>Tileset (tile-var picking)</h3>';
  content.appendChild(tilePanel);

  const tileGrid = document.createElement('div');
  tileGrid.className = 'field-grid';
  tilePanel.appendChild(tileGrid);

  const tilesInput = document.createElement('input');
  tilesInput.value = level.tiles || '';
  tilesInput.placeholder = 'tilesets/tiles_sewers_xyz.png';
  tilesInput.setAttribute('list', 'tilesetList');
  function tileLabel(text, node) {
    const label = document.createElement('label');
    label.textContent = text;
    tileGrid.appendChild(label);
    tileGrid.appendChild(node);
  }
  tileLabel('tiles', tilesInput);

  // datalist: builtin tilesets + any tilesets shipped in the mod
  const tilesetList = document.createElement('datalist');
  tilesetList.id = 'tilesetList';
  tileGrid.appendChild(tilesetList);
  for (const builtin of [true, false]) {
    api.modFiles(ctx.mod, 'tilesets', builtin).then(list => {
      for (const entry of list.entries) {
        if (entry.dir || !entry.name.toLowerCase().endsWith('.png')) { continue; }
        const o = document.createElement('option');
        o.value = 'tilesets/' + entry.name;
        tilesetList.appendChild(o);
      }
    }).catch(() => { /* free text still works */ });
  }

  // copy the referenced tileset into the mod: the exported zip then carries
  // its own tiles, and the mod does not silently depend on builtin assets
  const copyTilesBtn = document.createElement('button');
  copyTilesBtn.textContent = 'Copy tileset into mod';
  copyTilesBtn.addEventListener('click', async () => {
    const src = tilesInput.value.trim();
    if (!src) { return; }
    try {
      const resp = await fetch(api.rawUrl(ctx.mod, src));
      if (!resp.ok) { throw new Error('HTTP ' + resp.status + ' fetching ' + src); }
      const buffer = await resp.arrayBuffer();
      const bytes = new Uint8Array(buffer);
      let binary = '';
      for (let i = 0; i < bytes.length; i += 0x8000) {
        binary += String.fromCharCode.apply(null, bytes.subarray(i, i + 0x8000));
      }
      await api.saveBinary(ctx.mod, src, btoa(binary));
      status.className = 'status-ok';
      status.textContent = 'copied ' + src + ' into the mod (' + bytes.length + ' bytes)';
      loadTileset();
    } catch (e) {
      status.className = 'status-err';
      status.textContent = e.message;
    }
  });
  tileGrid.parentElement.appendChild(copyTilesBtn);

  const tileCanvas = document.createElement('canvas');
  tileCanvas.className = 'sprite-preview';
  tilePanel.appendChild(tileCanvas);

  let tileImage = null;
  function drawTileset() {
    if (!tileImage) { return; }
    const scale = 1;
    tileCanvas.width = tileImage.naturalWidth * scale;
    tileCanvas.height = tileImage.naturalHeight * scale;
    const g = tileCanvas.getContext('2d');
    g.imageSmoothingEnabled = false;
    g.drawImage(tileImage, 0, 0);
    // grid
    g.strokeStyle = 'rgba(255,255,255,0.25)';
    const ts = terrain.tileSize;
    for (let x = 0; x <= tileCanvas.width; x += ts) {
      g.beginPath(); g.moveTo(x, 0); g.lineTo(x, tileCanvas.height); g.stroke();
    }
    for (let y = 0; y <= tileCanvas.height; y += ts) {
      g.beginPath(); g.moveTo(0, y); g.lineTo(tileCanvas.width, y); g.stroke();
    }
    if (varIndex >= 0) {
      const cols = Math.floor(tileImage.naturalWidth / ts);
      g.strokeStyle = '#ff5050';
      g.lineWidth = 2;
      g.strokeRect((varIndex % cols) * ts, Math.floor(varIndex / cols) * ts, ts, ts);
    }
  }

  function loadTileset() {
    const src = tilesInput.value.trim();
    if (!src) { tileCanvas.width = 0; return; }
    tileImage = new Image();
    tileImage.onload = drawTileset;
    tileImage.src = api.rawUrl(ctx.mod, src, Date.now());
  }
  tilesInput.addEventListener('change', loadTileset);
  loadTileset();

  const tileHover = document.createElement('div');
  tileHover.className = 'hint';
  tilePanel.appendChild(tileHover);
  tileCanvas.addEventListener('click', e => {
    if (!tileImage) { return; }
    const rect = tileCanvas.getBoundingClientRect();
    const ts = terrain.tileSize;
    const col = Math.floor((e.clientX - rect.left) / ts);
    const row = Math.floor((e.clientY - rect.top) / ts);
    const cols = Math.floor(tileImage.naturalWidth / ts);
    varIndex = row * cols + col;
    tool = 'var';
    toolSelect.value = 'var';
    updateToolHint();
    drawTileset();
    tileHover.textContent = 'picked ' + varLayer + ' index ' + varIndex + ' - now paint on the map';
  });
  tileCanvas.addEventListener('mousemove', e => {
    if (!tileImage) { return; }
    const rect = tileCanvas.getBoundingClientRect();
    const ts = terrain.tileSize;
    const col = Math.floor((e.clientX - rect.left) / ts);
    const row = Math.floor((e.clientY - rect.top) / ts);
    const cols = Math.floor(tileImage.naturalWidth / ts);
    tileHover.textContent = 'tile index ' + (row * cols + col);
  });

  // other keys worth exposing (preserved on save even when empty)
  const metaGrid = document.createElement('div');
  metaGrid.className = 'field-grid';
  const metaInputs = {};
  for (const key of ['water', 'music', 'decoName']) {
    if (key === 'decoName') { continue; } // per-cell arrays - edit as json
    const label = document.createElement('label');
    label.textContent = key;
    const input = document.createElement('input');
    input.value = level[key] || '';
    metaInputs[key] = input;
    metaGrid.appendChild(label);
    metaGrid.appendChild(input);
  }
  tilePanel.appendChild(document.createElement('hr'));
  tilePanel.appendChild(metaGrid);

  // ------------------------------------------------------------------ save
  save.addEventListener('click', async () => {
    level.tiles = tilesInput.value.trim() || undefined;
    for (const [key, input] of Object.entries(metaInputs)) {
      if (input.value.trim()) {
        level[key] = input.value.trim();
      } else {
        delete level[key];
      }
    }
    if (level.tiles === undefined) { delete level.tiles; }
    if (level.map.length !== level.width * level.height) {
      status.className = 'status-err';
      status.textContent = 'map length ' + level.map.length + ' != w*h ' + (level.width * level.height);
      return;
    }
    try {
      await api.saveModFile(ctx.mod, ctx.file, JSON.stringify(level, null, 1) + '\n');
      status.className = 'status-ok';
      status.textContent = 'saved';
      dirty = false;
      ctx.onSaved && ctx.onSaved();
    } catch (e) {
      status.className = 'status-err';
      status.textContent = e.message;
    }
  });

  return { isDirty: () => dirty };
}
