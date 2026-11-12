// Structured editor for levelObjects/*.json (CustomObject descriptors).
// Form over kind/script/layer + appearance.desc strings + appearance.sprite;
// unknown json keys survive a save (mutate the parsed object, never rebuild).
// Files the browser cannot parse (hjson-lenient ones) fall back to the
// plain json text editor.

import * as api from '../api.js';
import { iconPicker } from './lua-desc-table.js';

// flat form keys -> where they live in the json
const TEXT_FIELDS = [
  ['kind', 'kind', 'e.g. CustomObject'],
  ['script', 'script', 'scripts/objects/<name>'],
  ['appearance.desc.name', 'appearance name key', 'strings key'],
  ['appearance.desc.desc', 'appearance desc key', 'strings key'],
  ['appearance.sprite.textureFile', 'sprite textureFile png', ''],
];
const NUMBER_FIELDS = [
  ['layer', 'layer'],
  ['appearance.sprite.imageIndex', 'sprite imageIndex'],
];
const CHECK_FIELDS = [
  ['appearance.desc.nonPassable', 'nonPassable'],
];

function getPath(obj, dotted) {
  let cur = obj;
  for (const part of dotted.split('.')) {
    if (cur == null || typeof cur !== 'object') { return undefined; }
    cur = cur[part];
  }
  return cur;
}

function setPath(obj, dotted, value) {
  const parts = dotted.split('.');
  let cur = obj;
  for (let i = 0; i < parts.length - 1; i++) {
    if (cur[parts[i]] == null || typeof cur[parts[i]] !== 'object') {
      cur[parts[i]] = {};
    }
    cur = cur[parts[i]];
  }
  const last = parts[parts.length - 1];
  if (value === undefined) {
    delete cur[last];
  } else {
    cur[last] = value;
  }
}

export async function objectEditor(content, ctx) {
  const data = await api.readModFile(ctx.mod, ctx.file);
  let obj;
  try {
    obj = JSON.parse(data.content);
  } catch (e) {
    const { jsonEditor } = await import('./text-editors.js');
    await jsonEditor(content, Object.assign({}, ctx, { content: data.content }));
    return null;
  }
  if (obj == null || typeof obj !== 'object' || Array.isArray(obj)) {
    const { jsonEditor } = await import('./text-editors.js');
    await jsonEditor(content, Object.assign({}, ctx, { content: data.content }));
    return null;
  }

  const kind = ctx.file.split('/').pop().replace(/\.json$/, '');
  const barEl = document.createElement('div');
  barEl.className = 'editor-bar';
  const title = document.createElement('strong');
  title.textContent = 'Level object: ' + kind;
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
  h.textContent = 'Object descriptor';
  panelEl.appendChild(h);
  const grid = document.createElement('div');
  grid.className = 'field-grid';
  panelEl.appendChild(grid);
  content.appendChild(panelEl);

  const inputs = {};
  let dirty = false;
  const markDirty = () => { dirty = true; };

  function addInput(dotted, labelText, value, type) {
    const label = document.createElement('label');
    label.textContent = labelText;
    const input = document.createElement('input');
    if (type === 'number') { input.type = 'number'; input.step = 'any'; }
    input.value = value === undefined || value === null ? '' : value;
    if (dotted === 'appearance.sprite.textureFile') { input.setAttribute('list', 'objTextureList'); }
    if (dotted === 'script') { input.setAttribute('list', 'objScriptList'); }
    input.addEventListener('input', markDirty);
    input.addEventListener('change', markDirty);
    inputs[dotted] = input;
    grid.appendChild(label);
    grid.appendChild(input);
  }

  function addCheck(dotted, labelText, checked) {
    const label = document.createElement('label');
    label.textContent = labelText;
    const wrap = document.createElement('div');
    const input = document.createElement('input');
    input.type = 'checkbox';
    input.checked = checked === true;
    input.addEventListener('change', markDirty);
    inputs[dotted] = input;
    const span = document.createElement('label');
    span.textContent = dotted.split('.').pop();
    span.style.marginLeft = '6px';
    wrap.appendChild(input);
    wrap.appendChild(span);
    grid.appendChild(label);
    grid.appendChild(wrap);
  }

  for (const [dotted, labelText, placeholder] of TEXT_FIELDS) {
    const v = getPath(obj, dotted);
    addInput(dotted, labelText, typeof v === 'string' ? v : (typeof v === 'number' ? String(v) : ''), 'text');
    inputs[dotted].placeholder = placeholder;
  }
  for (const [dotted, labelText] of NUMBER_FIELDS) {
    const v = getPath(obj, dotted);
    addInput(dotted, labelText, typeof v === 'number' ? v : '', 'number');
  }
  for (const [dotted, labelText] of CHECK_FIELDS) {
    addCheck(dotted, labelText, getPath(obj, dotted));
  }

  const textureList = document.createElement('datalist');
  textureList.id = 'objTextureList';
  grid.appendChild(textureList);
  for (const builtin of [true, false]) {
    api.modFiles(ctx.mod, 'levelObjects', builtin).then(list => {
      for (const entry of list.entries) {
        if (entry.dir || !entry.name.toLowerCase().endsWith('.png')) { continue; }
        const o = document.createElement('option');
        o.value = 'levelObjects/' + entry.name;
        textureList.appendChild(o);
      }
    }).catch(() => {});
  }

  const scriptList = document.createElement('datalist');
  scriptList.id = 'objScriptList';
  grid.appendChild(scriptList);
  for (const builtin of [true, false]) {
    api.modFiles(ctx.mod, 'scripts/objects', builtin).then(list => {
      for (const entry of list.entries) {
        if (!entry.name.toLowerCase().endsWith('.lua')) { continue; }
        const o = document.createElement('option');
        o.value = entry.name.replace(/\.lua$/, '');
        scriptList.appendChild(o);
      }
    }).catch(() => {});
  }

  iconPicker(content, ctx, inputs, {
    frame: 16,
    imageKey: 'appearance.sprite.imageIndex',
    imageFileKey: 'appearance.sprite.textureFile',
    title: 'Sprite (click a cell to set imageIndex)',
    onPicked: markDirty
  });

  save.addEventListener('click', async () => {
    const out = obj;
    for (const [dotted] of TEXT_FIELDS) {
      const v = inputs[dotted].value.trim();
      if (v === '') {
        // empty text = remove the authored key; containers stay (harmless)
        setPath(out, dotted, undefined);
      } else {
        setPath(out, dotted, v);
      }
    }
    for (const [dotted] of NUMBER_FIELDS) {
      const v = inputs[dotted].value.trim();
      setPath(out, dotted, v === '' ? undefined : Number(v));
    }
    for (const [dotted] of CHECK_FIELDS) {
      setPath(out, dotted, inputs[dotted].checked ? true : undefined);
    }
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

  return { isDirty: () => dirty };
}
