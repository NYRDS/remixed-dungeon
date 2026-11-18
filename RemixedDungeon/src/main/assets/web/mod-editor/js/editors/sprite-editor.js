// Structured editor for spritesDesc/<Kind>.json.
//
// Field set mirrors what the engine reads (MobSpriteDef/JsonHelper.readAnimation):
//   texture, width, height (frame size), visualWidth/visualHeight (optional),
//   one animation object per key: { fps, looped, frames[] }
// Frame ids index the sheet row-major with cols = texture.width / width
// (TextureFilm). The preview replays a picked animation at its fps.
// Unknown json keys (layers, kind tables...) are preserved on save.

import * as api from '../api.js';
import { uploadPng } from '../upload.js';

const STANDARD_ANIMS = ['idle', 'run', 'attack', 'die', 'zap'];

// {ItemKind: {file, index, fliesStraight, fliesFastRotating}} - the
// spritesDesc/items.json shape (ItemSpritesDescription)
function looksLikeItemMap(obj) {
  const values = Object.values(obj);
  return values.length > 0 && values.some(v =>
    v && typeof v === 'object' && !Array.isArray(v) &&
    ('file' in v || 'index' in v || 'fliesStraight' in v || 'fliesFastRotating' in v));
}

// form editor for spritesDesc/items.json: one row per item kind
// (file/index/fliesStraight/fliesFastRotating mirror the engine defaults)
export async function itemSpritesEditor(content, ctx) {
  let raw = '{}';
  let items = {};
  try {
    const data = await api.readModFile(ctx.mod, ctx.file);
    raw = data.content;
    items = JSON.parse(raw);
    if (typeof items !== 'object' || Array.isArray(items) || !items) { throw new Error('not an object'); }
  } catch (e) {
    items = {};
  }
  // a sheet descriptor under this name (texture key) is not an item map -
  // editing it as text is safer than mis-rendering it in the per-item form
  if (items.texture !== undefined) {
    const { jsonEditor } = await import('./text-editors.js');
    await jsonEditor(content, Object.assign({}, ctx, { content: raw }));
    return null;
  }

  const barEl = document.createElement('div');
  barEl.className = 'editor-bar';
  const title = document.createElement('strong');
  title.textContent = 'Item sprites (per-item overrides)';
  const save = document.createElement('button');
  save.className = 'primary';
  save.textContent = 'Save';
  barEl.appendChild(title);
  barEl.appendChild(save);
  content.appendChild(barEl);

  const status = document.createElement('div');
  content.appendChild(status);

  const panelEl = document.createElement('div');
  panelEl.className = 'panel';
  const hint = document.createElement('p');
  hint.className = 'hint';
  hint.textContent = 'Overrides how an item kind renders when thrown/flown: sprite file + frame index. ' +
    'Item kinds without an entry fall back to their own desc.';
  panelEl.appendChild(hint);
  const table = document.createElement('table');
  table.className = 'pairs';
  panelEl.appendChild(table);
  content.appendChild(panelEl);

  const rows = [];
  function itemRow(kind, desc) {
    desc = desc || {};
    const tr = document.createElement('tr');
    const cells = [];
    for (let i = 0; i < 4; i++) { cells.push(document.createElement('td')); tr.appendChild(cells[i]); }
    const kindInput = document.createElement('input');
    kindInput.value = kind;
    kindInput.placeholder = 'ItemKind (e.g. Dart)';
    cells[0].appendChild(kindInput);
    const fileInput = document.createElement('input');
    fileInput.value = desc.file !== undefined ? desc.file : '';
    fileInput.placeholder = 'items.png';
    cells[1].appendChild(fileInput);
    const indexInput = document.createElement('input');
    indexInput.type = 'number';
    indexInput.step = 1;
    indexInput.value = desc.index !== undefined ? desc.index : 0;
    indexInput.style.width = '5em';
    cells[2].appendChild(indexInput);
    const checks = document.createElement('div');
    const straight = document.createElement('input');
    straight.type = 'checkbox';
    straight.checked = desc.fliesStraight === true;
    const straightLabel = document.createElement('label');
    straightLabel.textContent = 'straight ';
    straightLabel.style.marginLeft = '4px';
    const rotating = document.createElement('input');
    rotating.type = 'checkbox';
    rotating.checked = desc.fliesFastRotating === true;
    const rotatingLabel = document.createElement('label');
    rotatingLabel.textContent = 'spin';
    rotatingLabel.style.marginLeft = '4px';
    checks.appendChild(straight); checks.appendChild(straightLabel);
    checks.appendChild(rotating); checks.appendChild(rotatingLabel);
    cells[3].appendChild(checks);
    const delCell = document.createElement('td');
    const del = document.createElement('button');
    del.textContent = '×';
    del.addEventListener('click', () => { tr.remove(); });
    delCell.appendChild(del);
    tr.appendChild(delCell);
    table.appendChild(tr);
    rows.push({ tr, kindInput, fileInput, indexInput, straight, rotating });
  }

  for (const kind of Object.keys(items)) {
    if (items[kind] && typeof items[kind] === 'object' && !Array.isArray(items[kind])) {
      itemRow(kind, items[kind]);
    }
  }

  const add = document.createElement('button');
  add.textContent = '+ item override';
  add.addEventListener('click', () => itemRow('', {}));
  panelEl.appendChild(add);

  let dirty = false;
  const markDirty = () => { dirty = true; };
  table.addEventListener('input', markDirty);
  table.addEventListener('change', markDirty);

  save.addEventListener('click', async () => {
    const out = {};
    for (const row of rows) {
      if (!row.tr.isConnected) { continue; }
      const kind = row.kindInput.value.trim();
      if (!kind) { continue; }
      const entry = {};
      const file = row.fileInput.value.trim();
      if (file) { entry.file = file; }
      const index = parseInt(row.indexInput.value, 10);
      if (Number.isFinite(index) && index !== 0) { entry.index = index; }
      if (row.straight.checked) { entry.fliesStraight = true; }
      if (row.rotating.checked) { entry.fliesFastRotating = true; }
      out[kind] = entry;
    }
    try {
      await api.saveModFile(ctx.mod, ctx.file, JSON.stringify(out, null, 3) + '\n');
      status.className = 'status-ok';
      status.textContent = 'saved (' + Object.keys(out).length + ' items)';
      dirty = false;
      ctx.onSaved && ctx.onSaved();
    } catch (e) {
      status.className = 'status-err';
      status.textContent = e.message;
    }
  });

  return { isDirty: () => dirty };
}

export async function spriteEditor(content, ctx) {
  const kind = ctx.file.split('/').pop().replace(/\.json$/, '');

  let sprite = {};
  let existed = true;
  try {
    const data = await api.readModFile(ctx.mod, ctx.file);
    sprite = JSON.parse(data.content);
  } catch (e) {
    existed = false;
    sprite = {
      texture: '', width: 16, height: 16,
      idle: { fps: 2, looped: true, frames: [0] }
    };
  }
  if (typeof sprite !== 'object' || Array.isArray(sprite) || !sprite) {
    content.textContent = ctx.file + ' is not a sprite descriptor object - editing as text is safer.';
    return;
  }
  // per-item sprite maps (spritesDesc/items.json shape) have no sheet fields
  // of their own - the animation form would inject texture/width/height junk
  // (bd a3u). The named file gets its own form; other map shapes fall back.
  if (sprite.texture === undefined && looksLikeItemMap(sprite)) {
    content.textContent = ctx.file + ' is a per-item sprite map, not a sheet descriptor - editing as text is safer.';
    return;
  }

  const barEl = document.createElement('div');
  barEl.className = 'editor-bar';
  const title = document.createElement('strong');
  title.textContent = 'Sprite: ' + kind + (existed ? '' : ' (new)');
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
  const markDirty = () => { dirty = true; };

  function panel(name) {
    const p = document.createElement('div');
    p.className = 'panel';
    const h = document.createElement('h3');
    h.textContent = name;
    p.appendChild(h);
    content.appendChild(p);
    const grid = document.createElement('div');
    grid.className = 'field-grid';
    p.appendChild(grid);
    return p;
  }

  function field(grid, labelText, key, value, attrs) {
    const label = document.createElement('label');
    label.textContent = labelText;
    const input = document.createElement('input');
    input.value = value === undefined ? '' : value;
    if (attrs) {
      for (const [attr, v] of Object.entries(attrs)) {
        if (attr === 'list') { input.setAttribute('list', v); }
        else { input[attr] = v; }
      }
    }
    inputs[key] = input;
    input.addEventListener('input', markDirty);
    input.addEventListener('change', markDirty);
    grid.appendChild(label);
    grid.appendChild(input);
    return input;
  }

  // --- texture -------------------------------------------------------------
  const texPanel = panel('Texture');
  const texGrid = texPanel.querySelector('.field-grid');
  const textureInput = field(texGrid, 'texture png', 'texture', sprite.texture, {
    placeholder: kind + '.png', list: 'textureList'
  });
  const dataList = document.createElement('datalist');
  dataList.id = 'textureList';
  texGrid.appendChild(dataList);
  function fillTextureList(dir) {
    Promise.all([
      api.modFiles(ctx.mod, dir),
      api.modFiles(ctx.mod, dir, true)
    ]).then(lists => {
      const names = new Set();
      for (const list of lists) {
        for (const entry of list.entries) {
          if (!entry.dir && entry.name.toLowerCase().endsWith('.png')) {
            names.add(dir + entry.name);
          }
        }
      }
      for (const n of names) {
        const o = document.createElement('option');
        o.value = n;
        dataList.appendChild(o);
      }
    }).catch(() => { /* datalist stays partial, free text works */ });
  }
  // every dir the mod exposes (builtin overlay included) - textures are not
  // limited to mobs/ and items/
  api.modFiles(ctx.mod, '').then(root => {
    for (const entry of root.entries) {
      if (entry.dir) { fillTextureList(entry.name + '/'); }
    }
  }).catch(() => { /* root listing unavailable - keep the builtin dirs */ });
  for (const dir of ['', 'mobs/', 'items/']) {
    fillTextureList(dir);
  }

  field(texGrid, 'frame width', 'width', sprite.width, { type: 'number', min: 1, step: 1 });
  field(texGrid, 'frame height', 'height', sprite.height, { type: 'number', min: 1, step: 1 });
  field(texGrid, 'visual width (optional)', 'visualWidth', sprite.visualWidth, { type: 'number', step: 'any' });
  field(texGrid, 'visual height (optional)', 'visualHeight', sprite.visualHeight, { type: 'number', step: 'any' });

  const uploadBtn = document.createElement('button');
  uploadBtn.textContent = 'Upload png into mod…';
  uploadBtn.addEventListener('click', async () => {
    const suggested = kind.replace(/([a-z0-9])([A-Z])/g, '$1_$2').toLowerCase() + '.png';
    const path = await uploadPng(ctx.mod, suggested);
    if (path) {
      textureInput.value = path;
      markDirty();
      reloadSheet();
    }
  });
  texPanel.appendChild(uploadBtn);

  // --- animations ------------------------------------------------------------
  const animPanel = panel('Animations (fps / looped / frame ids)');
  const animTable = document.createElement('table');
  animTable.className = 'pairs';
  animPanel.appendChild(animTable);

  function isAnimObject(v) {
    return v && typeof v === 'object' && !Array.isArray(v)
      && (v.frames !== undefined || v.fps !== undefined);
  }

  const animRows = [];

  function animRow(name, anim) {
    const tr = document.createElement('tr');
    const cells = [];
    for (let i = 0; i < 4; i++) { cells.push(document.createElement('td')); tr.appendChild(cells[i]); }

    const nameInput = document.createElement('input');
    nameInput.value = name;
    nameInput.placeholder = 'idle';
    nameInput.style.width = '8em';
    cells[0].appendChild(nameInput);

    const fpsInput = document.createElement('input');
    fpsInput.type = 'number';
    fpsInput.step = 'any';
    fpsInput.min = 0;
    fpsInput.value = anim && anim.fps !== undefined ? anim.fps : 10;
    fpsInput.style.width = '5em';
    cells[1].appendChild(fpsInput);

    const loopedInput = document.createElement('input');
    loopedInput.type = 'checkbox';
    loopedInput.checked = anim ? anim.looped !== false : true;
    cells[2].appendChild(loopedInput);

    const framesInput = document.createElement('input');
    framesInput.value = anim && anim.frames ? anim.frames.join(', ') : '0';
    framesInput.placeholder = '0, 1, 2';
    cells[3].appendChild(framesInput);

    const delCell = document.createElement('td');
    const del = document.createElement('button');
    del.textContent = '×';
    del.addEventListener('click', () => { tr.remove(); markDirty(); rebuildPreviewList(); });
    delCell.appendChild(del);
    tr.appendChild(delCell);

    for (const input of [nameInput, fpsInput, framesInput]) {
      input.addEventListener('input', markDirty);
    }
    loopedInput.addEventListener('change', markDirty);
    framesInput.addEventListener('change', rebuildPreviewList);

    animTable.appendChild(tr);
    animRows.push({ tr, nameInput, fpsInput, loopedInput, framesInput });
    return tr;
  }

  // known animation keys first, then any extra anim-looking keys
  const animKeys = [];
  for (const key of STANDARD_ANIMS) {
    if (sprite[key] !== undefined) { animKeys.push(key); }
  }
  for (const key of Object.keys(sprite)) {
    if (!animKeys.includes(key) && isAnimObject(sprite[key])) { animKeys.push(key); }
  }
  for (const key of animKeys) { animRow(key, sprite[key]); }

  const addAnim = document.createElement('button');
  addAnim.textContent = '+ animation';
  addAnim.addEventListener('click', () => {
    const name = window.prompt('Animation name (idle / run / attack / die / zap or custom)', 'idle');
    if (!name) { return; }
    animRow(name.trim(), null);
    markDirty();
    rebuildPreviewList();
  });
  animPanel.appendChild(addAnim);

  // --- sheet preview + animated playback --------------------------------------
  const sheetBox = document.createElement('div');
  texPanel.appendChild(sheetBox);
  const sheetCanvas = document.createElement('canvas');
  sheetCanvas.className = 'sprite-preview';
  sheetCanvas.style.maxWidth = '384px';
  sheetBox.appendChild(sheetCanvas);
  const sheetHover = document.createElement('div');
  sheetHover.className = 'hint';
  sheetBox.appendChild(sheetHover);

  const playPanel = panel('Animation preview');
  const playRow = document.createElement('div');
  playPanel.appendChild(playRow);
  const animSelect = document.createElement('select');
  playRow.appendChild(animSelect);
  playRow.appendChild(document.createTextNode(' scale '));
  const scaleSelect = document.createElement('select');
  for (const s of [1, 2, 3, 4]) {
    const o = document.createElement('option');
    o.value = s;
    o.textContent = s + 'x';
    scaleSelect.appendChild(o);
  }
  scaleSelect.value = 3;
  playRow.appendChild(scaleSelect);
  playRow.appendChild(document.createTextNode(' frame offset '));
  const offsetInput = document.createElement('input');
  offsetInput.type = 'number';
  offsetInput.value = 0;
  offsetInput.style.width = '5em';
  offsetInput.title = 'kind * framesInRow for multi-kind sheets';
  playRow.appendChild(offsetInput);

  const playCanvas = document.createElement('canvas');
  playCanvas.className = 'sprite-preview';
  playPanel.appendChild(playCanvas);
  const playNote = document.createElement('div');
  playNote.className = 'hint';
  playPanel.appendChild(playNote);

  function rebuildPreviewList() {
    const selected = animSelect.value;
    animSelect.innerHTML = '';
    for (const row of animRows) {
      if (!row.tr.isConnected) { continue; }
      const name = row.nameInput.value.trim();
      if (!name) { continue; }
      const o = document.createElement('option');
      o.value = name;
      o.textContent = name;
      animSelect.appendChild(o);
    }
    if ([...animSelect.options].some(o => o.value === selected)) {
      animSelect.value = selected;
    }
  }

  let sheetImage = null;
  let playTimer = null;

  function reloadSheet() {
    const src = textureInput.value.trim();
    if (playTimer) { clearInterval(playTimer); playTimer = null; }
    if (!src) { sheetCanvas.width = 0; playCanvas.width = 0; return; }
    sheetImage = new Image();
    sheetImage.onload = () => { drawSheet(); startPreview(); };
    sheetImage.onerror = () => {
      sheetCanvas.width = 0;
      playCanvas.width = 0;
      playNote.textContent = 'texture failed to load: ' + src;
    };
    sheetImage.src = api.rawUrl(ctx.mod, src, Date.now());
  }

  function frameRect(idx) {
    // engine math: TextureFilm - cols = texW / width, row-major
    const w = Number(inputs.width.value) || 1;
    const h = Number(inputs.height.value) || 1;
    const cols = Math.max(1, Math.floor(sheetImage.naturalWidth / w));
    const rows = Math.max(1, Math.floor(sheetImage.naturalHeight / h));
    const col = idx % cols;
    const row = Math.floor(idx / cols);
    if (row >= rows) { return null; }
    return { x: col * w, y: row * h, w, h, cols };
  }

  function drawSheet() {
    if (!sheetImage) { return; }
    const w = Number(inputs.width.value) || 1;
    const h = Number(inputs.height.value) || 1;
    sheetCanvas.width = sheetImage.naturalWidth;
    sheetCanvas.height = sheetImage.naturalHeight;
    const g = sheetCanvas.getContext('2d');
    g.imageSmoothingEnabled = false;
    g.drawImage(sheetImage, 0, 0);
    g.strokeStyle = 'rgba(255,255,255,0.25)';
    g.lineWidth = 1;
    for (let x = 0; x <= sheetCanvas.width; x += w) {
      g.beginPath(); g.moveTo(x, 0); g.lineTo(x, sheetCanvas.height); g.stroke();
    }
    for (let y = 0; y <= sheetCanvas.height; y += h) {
      g.beginPath(); g.moveTo(0, y); g.lineTo(sheetCanvas.width, y); g.stroke();
    }
  }

  // frame-id readout: hover for mouse, tap for touch (bd a5d)
  function frameAt(e) {
    const rect = sheetCanvas.getBoundingClientRect();
    const w = Number(inputs.width.value) || 1;
    const h = Number(inputs.height.value) || 1;
    const scaleX = sheetCanvas.width / rect.width;
    const scaleY = sheetCanvas.height / rect.height;
    const col = Math.floor((e.clientX - rect.left) * scaleX / w);
    const row = Math.floor((e.clientY - rect.top) * scaleY / h);
    const cols = Math.max(1, Math.floor(sheetImage.naturalWidth / w));
    return row * cols + col;
  }
  sheetCanvas.style.touchAction = 'manipulation';
  sheetCanvas.addEventListener('mousemove', e => {
    if (!sheetImage) { return; }
    sheetHover.textContent = 'frame id ' + frameAt(e);
  });
  sheetCanvas.addEventListener('click', e => {
    if (!sheetImage) { return; }
    sheetHover.textContent = 'frame id ' + frameAt(e);
  });

  function startPreview() {
    if (playTimer) { clearInterval(playTimer); playTimer = null; }
    if (!sheetImage) { return; }
    const row = animRows.find(r => r.tr.isConnected && r.nameInput.value.trim() === animSelect.value);
    if (!row) { playCanvas.width = 0; playNote.textContent = 'no animation selected'; return; }

    const fps = Math.max(0.1, Number(row.fpsInput.value) || 2);
    const looped = row.loopedInput.checked;
    let frames = row.framesInput.value.split(',').map(s => parseInt(s.trim(), 10)).filter(n => Number.isFinite(n));
    if (!frames.length) { frames = [0]; }
    const offset = Number(offsetInput.value) || 0;

    const scale = Number(scaleSelect.value) || 1;
    const fw = (Number(inputs.width.value) || 1) * scale;
    const fh = (Number(inputs.height.value) || 1) * scale;
    playCanvas.width = fw;
    playCanvas.height = fh;
    const g = playCanvas.getContext('2d');
    g.imageSmoothingEnabled = false;

    let i = 0;
    playNote.textContent = animSelect.value + ': ' + frames.length + ' frames @ ' + fps + ' fps'
      + (offset ? ' (offset ' + offset + ')' : '');

    function draw() {
      if (!playCanvas.isConnected) { clearInterval(playTimer); playTimer = null; return; }
      const rect = frameRect(frames[i] + offset);
      g.fillStyle = '#222';
      g.fillRect(0, 0, fw, fh);
      if (rect) {
        g.drawImage(sheetImage, rect.x, rect.y, rect.w, rect.h, 0, 0, fw, fh);
      } else {
        g.fillStyle = '#a33';
        g.font = '10px monospace';
        g.fillText('frame ' + (frames[i] + offset) + ' outside sheet', 2, fh / 2);
      }
      i++;
      if (i >= frames.length) {
        if (looped) { i = 0; } else { clearInterval(playTimer); playTimer = null; }
      }
    }
    draw();
    playTimer = setInterval(draw, Math.round(1000 / fps));
  }

  rebuildPreviewList();
  animSelect.addEventListener('change', startPreview);
  scaleSelect.addEventListener('change', startPreview);
  offsetInput.addEventListener('change', startPreview);
  textureInput.addEventListener('change', reloadSheet);
  inputs.width.addEventListener('change', () => { drawSheet(); startPreview(); });
  inputs.height.addEventListener('change', () => { drawSheet(); startPreview(); });
  reloadSheet();

  // --- save -----------------------------------------------------------------
  save.addEventListener('click', async () => {
    const out = sprite; // preserve unknown keys (layers etc.)
    out.texture = textureInput.value.trim();
    out.width = Number(inputs.width.value) || 1;
    out.height = Number(inputs.height.value) || 1;
    for (const key of ['visualWidth', 'visualHeight']) {
      const v = inputs[key].value.trim();
      if (v === '') { delete out[key]; } else { out[key] = Number(v); }
    }

    // rebuild animation keys from the table; a blank name drops the row
    for (const key of Object.keys(out)) {
      if (isAnimObject(out[key])) { delete out[key]; }
    }
    let bad = null;
    for (const row of animRows) {
      if (!row.tr.isConnected) { continue; }
      const name = row.nameInput.value.trim();
      if (!name) { continue; }
      const frames = row.framesInput.value.split(',')
        .map(s => parseInt(s.trim(), 10))
        .filter(n => Number.isFinite(n));
      if (!frames.length) { bad = 'animation ' + (name || '(unnamed)') + ' needs at least one frame id'; continue; }
      out[name] = {
        fps: Number(row.fpsInput.value) || 1,
        looped: row.loopedInput.checked,
        frames
      };
    }
    if (bad) {
      status.className = 'status-err';
      status.textContent = bad + ' (row skipped)';
    }

    try {
      await api.saveModFile(ctx.mod, ctx.file, JSON.stringify(out, null, 2) + '\n');
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
