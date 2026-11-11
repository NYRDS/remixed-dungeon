// Structured editor for scripts/items/*.lua (CustomItem via scripts/lib/item).
//
// A mod item is a lua module returning item.init{ desc = function()
// return { ...flat scalars... } end, ... }. This editor parses that desc
// table, edits the known CustomItem fields as a form, and rewrites ONLY the
// table body - the rest of the script (actions, execute...) is untouched.
// Anything it cannot parse falls back to the plain lua editor.

import * as api from '../api.js';

// item.defaultDesc() from scripts/lib/item.lua - shown as the blank-state
// values so the form never invents semantics
const ITEM_FIELDS = [
  ['name', 'name key', 'text'],
  ['info', 'info key', 'text'],
  ['image', 'image index', 'number'],
  ['imageFile', 'imageFile png', 'text'],
  ['price', 'price', 'number'],
  ['defaultAction', 'defaultAction', 'text'],
  ['stackable', 'stackable', 'check'],
  ['upgradable', 'upgradable', 'check'],
  ['identified', 'identified', 'check'],
  ['equipable', 'equipable slot', 'text'],
];

const DEFAULTS = {
  image: 14, imageFile: 'items/food.png', name: 'smth', info: 'smth',
  stackable: false, upgradable: false, identified: true,
  defaultAction: 'Item_ACThrow', price: 0, equipable: ''
};

// desc = function() return { ... } end
const DESC_RE = /(\bdesc\s*=\s*function\s*\([^)]*\)\s*\n(\s*)return\s*\{)([\s\S]*?)(\n\s*\}\s*\n\s*end)/;

export function parseItemDesc(luaText) {
  const m = luaText.match(DESC_RE);
  if (!m) {
    return null;
  }
  const body = m[3];
  const values = {};
  for (let line of body.split('\n')) {
    line = line.replace(/--.*$/, '').trim().replace(/,$/, '');
    if (!line) { continue; }
    const kv = line.match(/^([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.+)$/);
    if (!kv) { continue; } // blank/comment line
    let [, key, raw] = kv;
    raw = raw.trim();
    let value;
    if (/^-?\d+(\.\d+)?$/.test(raw)) { value = Number(raw); }
    else if (raw === 'true' || raw === 'false') { value = raw === 'true'; }
    else if (/^"[^"]*"$/.test(raw)) { value = raw.slice(1, -1); }
    else if (/^'[^']*'$/.test(raw)) { value = raw.slice(1, -1); }
    else { return null; } // non-scalar entry - stay out of the file
    values[key] = value;
  }
  return { prefix: m[1], indent: m[2] + '    ', suffix: m[4], values };
}

export function serializeItemDesc(parsed, values) {
  const lines = [];
  for (const [key] of ITEM_FIELDS) {
    if (!(key in values)) { continue; }
    const v = values[key];
    if (typeof v === 'string') { lines.push(key + ' = "' + v + '",'); }
    else if (typeof v === 'boolean') { lines.push(key + ' = ' + v + ','); }
    else { lines.push(key + ' = ' + v + ','); }
  }
  // keys we do not model (isArtifact, heapScale...) survive verbatim
  for (const key of Object.keys(parsed.values)) {
    if (ITEM_FIELDS.some(([k]) => k === key)) { continue; }
    const v = parsed.values[key];
    if (typeof v === 'string') { lines.push(key + ' = "' + v + '",'); }
    else if (typeof v === 'boolean') { lines.push(key + ' = ' + v + ','); }
    else { lines.push(key + ' = ' + v + ','); }
  }
  return parsed.prefix + '\n' + lines.map(l => parsed.indent + l).join('\n') + parsed.suffix;
}

export async function itemEditor(content, ctx) {
  const data = await api.readModFile(ctx.mod, ctx.file);
  let luaText = data.content;

  const parsed = parseItemDesc(luaText);
  if (!parsed) {
    // not the shape we model - stay safe, edit as text
    const { luaEditor } = await import('./text-editors.js');
    await luaEditor(content, Object.assign({}, ctx, { content: luaText }));
    return null;
  }
  const values = Object.assign({}, DEFAULTS, parsed.values);

  const kind = ctx.file.split('/').pop().replace(/\.lua$/, '');
  const barEl = document.createElement('div');
  barEl.className = 'editor-bar';
  const title = document.createElement('strong');
  title.textContent = 'Item: ' + kind;
  const save = document.createElement('button');
  save.className = 'primary';
  save.textContent = 'Save';
  barEl.appendChild(title);
  barEl.appendChild(save);
  content.appendChild(barEl);

  const status = document.createElement('div');
  content.appendChild(status);

  const inputs = {};
  let dirty = false;

  const panel = document.createElement('div');
  panel.className = 'panel';
  const h = document.createElement('h3');
  h.textContent = 'Item descriptor (desc table)';
  panel.appendChild(h);
  const grid = document.createElement('div');
  grid.className = 'field-grid';
  panel.appendChild(grid);
  content.appendChild(panel);

  for (const [key, labelText, type] of ITEM_FIELDS) {
    const label = document.createElement('label');
    label.textContent = labelText;
    if (type === 'check') {
      const wrap = document.createElement('div');
      const input = document.createElement('input');
      input.type = 'checkbox';
      input.checked = values[key] === true;
      input.addEventListener('change', () => { dirty = true; });
      inputs[key] = input;
      const span = document.createElement('label');
      span.textContent = key;
      span.style.marginLeft = '6px';
      wrap.appendChild(input);
      wrap.appendChild(span);
      grid.appendChild(label);
      grid.appendChild(wrap);
    } else {
      const input = document.createElement('input');
      if (type === 'number') { input.type = 'number'; input.step = 'any'; }
      input.value = values[key] === undefined ? '' : values[key];
      if (key === 'imageFile') { input.setAttribute('list', 'itemImageList'); }
      if (key === 'defaultAction') { input.setAttribute('list', 'itemActionList'); }
      input.addEventListener('input', () => { dirty = true; });
      inputs[key] = input;
      grid.appendChild(label);
      grid.appendChild(input);
    }
  }

  const imageList = document.createElement('datalist');
  imageList.id = 'itemImageList';
  grid.appendChild(imageList);
  for (const builtin of [true, false]) {
    api.modFiles(ctx.mod, 'items', builtin).then(list => {
      for (const entry of list.entries) {
        if (entry.dir || !entry.name.toLowerCase().endsWith('.png')) { continue; }
        const o = document.createElement('option');
        o.value = 'items/' + entry.name;
        imageList.appendChild(o);
      }
    }).catch(() => {});
  }
  api.modFiles(ctx.mod, '').then(list => {
    for (const entry of list.entries) {
      if (entry.dir || !entry.name.toLowerCase().endsWith('.png')) { continue; }
      const o = document.createElement('option');
      o.value = entry.name;
      imageList.appendChild(o);
    }
  }).catch(() => {});

  const actionList = document.createElement('datalist');
  actionList.id = 'itemActionList';
  for (const a of ['Item_ACThrow', 'Food_ACEat', 'Item_ACDrop', 'Item_ACPickUp', 'AC_EQUIP']) {
    const o = document.createElement('option');
    o.value = a;
    actionList.appendChild(o);
  }
  grid.appendChild(actionList);

  // --- icon preview: sheet grid, click a cell to pick the image index ------
  const previewPanel = document.createElement('div');
  previewPanel.className = 'panel';
  const ph = document.createElement('h3');
  ph.textContent = 'Icon (click a cell to set image index)';
  previewPanel.appendChild(ph);
  content.appendChild(previewPanel);

  const iconCanvas = document.createElement('canvas');
  iconCanvas.className = 'sprite-preview';
  iconCanvas.style.maxWidth = '384px';
  previewPanel.appendChild(iconCanvas);
  const iconHover = document.createElement('div');
  iconHover.className = 'hint';
  previewPanel.appendChild(iconHover);

  const FRAME = 16; // item icon sheets are 16px row-major (ItemSpritesDescription)
  let iconImage = null;

  function drawIcon() {
    if (!iconImage) { return; }
    iconCanvas.width = iconImage.naturalWidth;
    iconCanvas.height = iconImage.naturalHeight;
    const g = iconCanvas.getContext('2d');
    g.imageSmoothingEnabled = false;
    g.drawImage(iconImage, 0, 0);
    g.strokeStyle = 'rgba(255,255,255,0.25)';
    for (let x = 0; x <= iconCanvas.width; x += FRAME) {
      g.beginPath(); g.moveTo(x, 0); g.lineTo(x, iconCanvas.height); g.stroke();
    }
    for (let y = 0; y <= iconCanvas.height; y += FRAME) {
      g.beginPath(); g.moveTo(0, y); g.lineTo(iconCanvas.width, y); g.stroke();
    }
    const cols = Math.floor(iconImage.naturalWidth / FRAME);
    const idx = Number(inputs.image.value) || 0;
    g.strokeStyle = '#ff5050';
    g.lineWidth = 2;
    g.strokeRect((idx % cols) * FRAME, Math.floor(idx / cols) * FRAME, FRAME, FRAME);
    g.lineWidth = 1;
  }

  function reloadIcon() {
    const src = inputs.imageFile.value.trim();
    if (!src) { iconCanvas.width = 0; return; }
    iconImage = new Image();
    iconImage.onload = drawIcon;
    iconImage.onerror = () => { iconCanvas.width = 0; iconHover.textContent = 'imageFile failed to load: ' + src; };
    iconImage.src = api.rawUrl(ctx.mod, src, Date.now());
  }

  iconCanvas.addEventListener('click', e => {
    if (!iconImage) { return; }
    const rect = iconCanvas.getBoundingClientRect();
    const scaleX = iconCanvas.width / rect.width;
    const scaleY = iconCanvas.height / rect.height;
    const col = Math.floor((e.clientX - rect.left) * scaleX / FRAME);
    const row = Math.floor((e.clientY - rect.top) * scaleY / FRAME);
    const cols = Math.max(1, Math.floor(iconImage.naturalWidth / FRAME));
    const idx = row * cols + col;
    inputs.image.value = idx;
    dirty = true;
    drawIcon();
    iconHover.textContent = 'image index ' + idx;
  });
  iconCanvas.addEventListener('mousemove', e => {
    if (!iconImage) { return; }
    const rect = iconCanvas.getBoundingClientRect();
    const col = Math.floor((e.clientX - rect.left) * (iconCanvas.width / rect.width) / FRAME);
    const row = Math.floor((e.clientY - rect.top) * (iconCanvas.height / rect.height) / FRAME);
    const cols = Math.max(1, Math.floor(iconImage.naturalWidth / FRAME));
    iconHover.textContent = 'image index ' + (row * cols + col);
  });

  inputs.imageFile.addEventListener('change', reloadIcon);
  inputs.image.addEventListener('change', drawIcon);
  reloadIcon();

  // --- save ---------------------------------------------------------------
  save.addEventListener('click', async () => {
    const out = Object.assign({}, parsed.values);
    for (const [key, , type] of ITEM_FIELDS) {
      const input = inputs[key];
      if (type === 'check') {
        out[key] = input.checked;
      } else {
        const v = input.value.trim();
        out[key] = type === 'number' ? (v === '' ? 0 : Number(v)) : v;
      }
    }
    // equipable: empty string = not equipable, keep it explicit like the lib default
    const newText = luaText.replace(DESC_RE, () => serializeItemDesc(parsed, out));
    try {
      await api.saveModFile(ctx.mod, ctx.file, newText);
      luaText = newText;
      Object.assign(parsed.values, out);
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
