// Structured editor for effects/*.json (animated effect descriptors:
// texture + frame size + one anim). Unknown json keys survive a save;
// hjson-lenient files fall back to the plain json text editor.

import * as api from '../api.js';

export async function effectEditor(content, ctx) {
  const data = await api.readModFile(ctx.mod, ctx.file);
  let obj;
  try {
    obj = JSON.parse(data.content);
  } catch (e) {
    const { jsonEditor } = await import('./text-editors.js');
    await jsonEditor(content, Object.assign({}, ctx, { content: data.content }));
    return null;
  }
  if (obj == null || typeof obj !== 'object' || Array.isArray(obj) || !obj.anim) {
    const { jsonEditor } = await import('./text-editors.js');
    await jsonEditor(content, Object.assign({}, ctx, { content: data.content }));
    return null;
  }

  const kind = ctx.file.split('/').pop().replace(/\.json$/, '');
  const barEl = document.createElement('div');
  barEl.className = 'editor-bar';
  const title = document.createElement('strong');
  title.textContent = 'Effect: ' + kind;
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
  const h = document.createElement('h3');
  h.textContent = 'Effect descriptor';
  panelEl.appendChild(h);
  const grid = document.createElement('div');
  grid.className = 'field-grid';
  panelEl.appendChild(grid);
  content.appendChild(panelEl);

  const inputs = {};
  let dirty = false;
  const markDirty = () => { dirty = true; };

  function addInput(key, labelText, value, type, listId) {
    const label = document.createElement('label');
    label.textContent = labelText;
    const input = document.createElement('input');
    if (type === 'number') { input.type = 'number'; input.step = 'any'; }
    input.value = value === undefined || value === null ? '' : value;
    if (listId) { input.setAttribute('list', listId); }
    input.addEventListener('input', markDirty);
    input.addEventListener('change', markDirty);
    inputs[key] = input;
    grid.appendChild(label);
    grid.appendChild(input);
  }

  addInput('texture', 'texture png', typeof obj.texture === 'string' ? obj.texture : '', 'text', 'fxTextureList');
  addInput('width', 'frame width', obj.width, 'number');
  addInput('height', 'frame height', obj.height, 'number');
  addInput('anim.fps', 'fps', obj.anim.fps, 'number');
  addInput('anim.frames', 'frames (comma-separated indices)',
    Array.isArray(obj.anim.frames) ? obj.anim.frames.join(',') : '', 'text');

  const loopedWrap = document.createElement('div');
  const loopedLabel = document.createElement('label');
  loopedLabel.textContent = 'looped';
  const looped = document.createElement('input');
  looped.type = 'checkbox';
  looped.checked = obj.anim.looped !== false;
  looped.addEventListener('change', markDirty);
  inputs['anim.looped'] = looped;
  loopedWrap.appendChild(looped);
  loopedWrap.appendChild(document.createTextNode(' looped'));
  grid.appendChild(loopedLabel);
  grid.appendChild(loopedWrap);

  const textureList = document.createElement('datalist');
  textureList.id = 'fxTextureList';
  grid.appendChild(textureList);
  for (const builtin of [true, false]) {
    api.modFiles(ctx.mod, 'effects', builtin).then(list => {
      for (const entry of list.entries) {
        if (entry.dir || !entry.name.toLowerCase().endsWith('.png')) { continue; }
        const o = document.createElement('option');
        o.value = 'effects/' + entry.name;
        textureList.appendChild(o);
      }
    }).catch(() => {});
  }

  // --- animated preview ----------------------------------------------------
  const previewPanel = document.createElement('div');
  previewPanel.className = 'panel';
  const ph = document.createElement('h3');
  ph.textContent = 'Preview';
  previewPanel.appendChild(ph);
  content.appendChild(previewPanel);

  const canvas = document.createElement('canvas');
  canvas.className = 'sprite-preview';
  canvas.style.maxWidth = '256px';
  previewPanel.appendChild(canvas);
  const note = document.createElement('div');
  note.className = 'hint';
  previewPanel.appendChild(note);

  let image = null;
  let timer = null;
  let framePos = 0;

  function frames() {
    return (inputs['anim.frames'].value.match(/-?\d+/g) || []).map(Number);
  }

  function drawFrame() {
    const seq = frames();
    const w = Number(inputs.width.value) || 16;
    const hgt = Number(inputs.height.value) || 16;
    if (!image || !seq.length) {
      canvas.width = w;
      canvas.height = hgt;
      const g = canvas.getContext('2d');
      g.clearRect(0, 0, w, hgt);
      note.textContent = image ? 'no frames' : 'texture failed to load';
      return;
    }
    const cols = Math.max(1, Math.floor(image.naturalWidth / w));
    const idx = seq[Math.min(framePos, seq.length - 1)];
    const sx = (idx % cols) * w;
    const sy = Math.floor(idx / cols) * hgt;
    canvas.width = w * 4;
    canvas.height = hgt * 4;
    const g = canvas.getContext('2d');
    g.imageSmoothingEnabled = false;
    g.clearRect(0, 0, canvas.width, canvas.height);
    // frames beyond the sheet are a config bug - show the empty frame + note
    const onSheet = sx + w <= image.naturalWidth && sy + hgt <= image.naturalHeight && idx >= 0;
    if (onSheet) {
      g.drawImage(image, sx, sy, w, hgt, 0, 0, canvas.width, canvas.height);
    }
    note.textContent = 'frame ' + (framePos + 1) + '/' + seq.length + ' (index ' + idx + ')'
      + (onSheet ? '' : ' - outside the sheet');
  }

  function restartAnim() {
    if (timer) { clearInterval(timer); }
    framePos = 0;
    drawFrame();
    const fps = Math.max(1, Number(inputs['anim.fps'].value) || 10);
    timer = setInterval(() => {
      const seq = frames();
      if (!seq.length) { return; }
      framePos = (framePos + 1) % seq.length;
      drawFrame();
    }, 1000 / fps);
  }

  function reloadTexture() {
    const src = inputs.texture.value.trim();
    if (!src) { image = null; drawFrame(); return; }
    image = new Image();
    image.onload = () => { drawFrame(); };
    image.onerror = () => { image = null; drawFrame(); };
    image.src = api.rawUrl(ctx.mod, src, Date.now());
  }

  inputs.texture.addEventListener('change', reloadTexture);
  ['anim.fps', 'anim.frames', 'width', 'height'].forEach(k => {
    inputs[k].addEventListener('change', restartAnim);
  });
  reloadTexture();
  restartAnim();

  save.addEventListener('click', async () => {
    const out = obj;
    out.texture = inputs.texture.value.trim();
    out.width = Number(inputs.width.value) || 0;
    out.height = Number(inputs.height.value) || 0;
    out.anim = out.anim || {};
    out.anim.fps = Number(inputs['anim.fps'].value) || 0;
    out.anim.looped = inputs['anim.looped'].checked;
    out.anim.frames = frames();
    try {
      await api.saveModFile(ctx.mod, ctx.file, JSON.stringify(out, null, 3) + '\n');
      status.className = 'status-ok';
      status.textContent = 'saved';
      dirty = false;
      ctx.onSaved && ctx.onSaved();
    } catch (e) {
      status.className = 'status-err';
      status.textContent = e.message;
    }
  });

  return {
    isDirty: () => dirty,
    dispose: () => { if (timer) { clearInterval(timer); } }
  };
}
