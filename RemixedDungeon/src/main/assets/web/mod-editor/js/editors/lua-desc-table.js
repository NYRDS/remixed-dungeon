// Shared machinery for lua entity editors whose file shape is
// <lib>.init{ desc = function() return { ...flat scalars... } end, ... }.
// Parses ONLY that desc table, edits the known fields as a form, rewrites
// ONLY the table body - the rest of the script survives untouched.
// Anything non-scalar in the table means we do not understand the file:
// the caller falls back to the plain lua editor (returns null).
//
// Used by: item editor, spell editor, buff editor.

import * as api from '../api.js';

// desc = function() return { ... } end
const DESC_RE = /(\bdesc\s*=\s*function\s*\([^)]*\)\s*\n(\s*)return\s*\{)([\s\S]*?)(\n\s*\}\s*\n\s*end)/;

export function parseDescTable(luaText) {
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

export function serializeDescTable(parsed, values, fields) {
  const fieldKeys = fields.map(([k]) => k);
  const lines = [];
  const emit = (key, v) => {
    if (typeof v === 'string') { lines.push(key + ' = "' + v + '",'); }
    else { lines.push(key + ' = ' + v + ','); }
  };
  for (const key of fieldKeys) {
    if (key in values) { emit(key, values[key]); }
  }
  // keys we do not model (isArtifact, heapScale...) survive verbatim
  for (const key of Object.keys(parsed.values)) {
    if (fieldKeys.indexOf(key) >= 0) { continue; }
    emit(key, parsed.values[key]);
  }
  return parsed.prefix + '\n' + lines.map(l => parsed.indent + l).join('\n') + parsed.suffix;
}

// One icon-picker panel shared by item/spell editors: draws the imageFile
// sheet with a click-to-pick frame grid over it (16px row-major frames).
export function iconPicker(content, ctx, inputs, opts) {
  const frame = opts.frame || 16;
  const onPicked = opts.onPicked || (() => {});
  const imageFileKey = opts.imageFileKey;
  const imageKey = opts.imageKey;

  const previewPanel = document.createElement('div');
  previewPanel.className = 'panel';
  const ph = document.createElement('h3');
  ph.textContent = opts.title || 'Icon (click a cell to set the image index)';
  previewPanel.appendChild(ph);
  content.appendChild(previewPanel);

  const canvas = document.createElement('canvas');
  canvas.className = 'sprite-preview';
  canvas.style.maxWidth = '384px';
  previewPanel.appendChild(canvas);
  const hover = document.createElement('div');
  hover.className = 'hint';
  previewPanel.appendChild(hover);

  let image = null;

  function draw() {
    if (!image) { return; }
    canvas.width = image.naturalWidth;
    canvas.height = image.naturalHeight;
    const g = canvas.getContext('2d');
    g.imageSmoothingEnabled = false;
    g.drawImage(image, 0, 0);
    g.strokeStyle = 'rgba(255,255,255,0.25)';
    for (let x = 0; x <= canvas.width; x += frame) {
      g.beginPath(); g.moveTo(x, 0); g.lineTo(x, canvas.height); g.stroke();
    }
    for (let y = 0; y <= canvas.height; y += frame) {
      g.beginPath(); g.moveTo(0, y); g.lineTo(canvas.width, y); g.stroke();
    }
    const cols = Math.max(1, Math.floor(image.naturalWidth / frame));
    const idx = Number(inputs[imageKey].value) || 0;
    g.strokeStyle = '#ff5050';
    g.lineWidth = 2;
    g.strokeRect((idx % cols) * frame, Math.floor(idx / cols) * frame, frame, frame);
    g.lineWidth = 1;
  }

  function reload() {
    const src = inputs[imageFileKey].value.trim();
    if (!src) { canvas.width = 0; return; }
    image = new Image();
    image.onload = draw;
    image.onerror = () => { canvas.width = 0; hover.textContent = 'sheet failed to load: ' + src; };
    image.src = api.rawUrl(ctx.mod, src, Date.now());
  }

  canvas.addEventListener('click', e => {
    if (!image) { return; }
    const rect = canvas.getBoundingClientRect();
    const col = Math.floor((e.clientX - rect.left) * (canvas.width / rect.width) / frame);
    const row = Math.floor((e.clientY - rect.top) * (canvas.height / rect.height) / frame);
    const cols = Math.max(1, Math.floor(image.naturalWidth / frame));
    const idx = row * cols + col;
    inputs[imageKey].value = idx;
    draw();
    hover.textContent = 'image index ' + idx;
    onPicked(idx);
  });
  canvas.addEventListener('mousemove', e => {
    if (!image) { return; }
    const rect = canvas.getBoundingClientRect();
    const col = Math.floor((e.clientX - rect.left) * (canvas.width / rect.width) / frame);
    const row = Math.floor((e.clientY - rect.top) * (canvas.height / rect.height) / frame);
    const cols = Math.max(1, Math.floor(image.naturalWidth / frame));
    hover.textContent = 'image index ' + (row * cols + col);
  });

  inputs[imageFileKey].addEventListener('change', reload);
  inputs[imageKey].addEventListener('change', draw);
  reload();
}

// Build the full editor for a desc-table entity.
// cfg: { kindLabel, fields, defaults, icon: {frame, imageKey, imageFileKey, datalistDir} }
// Returns { isDirty } or null when the file is not the modeled shape.
export async function descTableEditor(content, ctx, cfg) {
  const data = await api.readModFile(ctx.mod, ctx.file);
  let luaText = data.content;

  const parsed = parseDescTable(luaText);
  if (!parsed) {
    // not the shape we model - stay safe, edit as text
    const { luaEditor } = await import('./text-editors.js');
    await luaEditor(content, Object.assign({}, ctx, { content: luaText }));
    return null;
  }
  const values = Object.assign({}, cfg.defaults, parsed.values);

  const kind = ctx.file.split('/').pop().replace(/\.lua$/, '');
  const barEl = document.createElement('div');
  barEl.className = 'editor-bar';
  const title = document.createElement('strong');
  title.textContent = cfg.kindLabel + ': ' + kind;
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
  h.textContent = cfg.kindLabel + ' descriptor (desc table)';
  panel.appendChild(h);
  const grid = document.createElement('div');
  grid.className = 'field-grid';
  panel.appendChild(grid);
  content.appendChild(panel);

  for (const [key, labelText, type] of cfg.fields) {
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
      if (cfg.datalists && cfg.datalists[key]) {
        // setAttribute, NOT input.list = ... - the IDL property is readonly
        input.setAttribute('list', cfg.datalists[key]);
      }
      input.addEventListener('input', () => { dirty = true; });
      inputs[key] = input;
      grid.appendChild(label);
      grid.appendChild(input);
    }
  }

  // datalist fillers: cfg.datalistData(key) -> Promise<[string]>
  if (cfg.datalists) {
    for (const [key, listId] of Object.entries(cfg.datalists)) {
      const dl = document.createElement('datalist');
      dl.id = listId;
      grid.appendChild(dl);
      if (cfg.datalistData) {
        cfg.datalistData(key).then(options => {
          for (const o of options) {
            const opt = document.createElement('option');
            opt.value = o;
            dl.appendChild(opt);
          }
        }).catch(() => {});
      }
    }
  }

  if (cfg.icon) {
    iconPicker(content, ctx, inputs, Object.assign({ onPicked: () => { dirty = true; } }, cfg.icon));
  }

  save.addEventListener('click', async () => {
    const out = Object.assign({}, parsed.values);
    for (const [key, , type] of cfg.fields) {
      const input = inputs[key];
      if (type === 'check') {
        out[key] = input.checked;
      } else {
        const v = input.value.trim();
        out[key] = type === 'number' ? (v === '' ? 0 : Number(v)) : v;
      }
    }
    const newText = luaText.replace(DESC_RE, () => serializeDescTable(parsed, out, cfg.fields));
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
