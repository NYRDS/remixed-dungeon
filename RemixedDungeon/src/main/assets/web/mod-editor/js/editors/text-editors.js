// Generic text-based editors: json, lua, strings pairs, plain text.
// Validation is server-side (the save endpoint parses before writing) -
// the client only pretty-prints json it can parse itself.

import * as api from '../api.js';
import { uploadPng, uploadFile } from '../upload.js';

function bar(content, crumbs) {
  const barEl = document.createElement('div');
  barEl.className = 'editor-bar';
  const path = document.createElement('span');
  path.textContent = crumbs;
  path.className = 'grow hint';
  const save = document.createElement('button');
  save.className = 'primary';
  save.textContent = 'Save';
  barEl.appendChild(path);
  barEl.appendChild(save);
  content.appendChild(barEl);
  return save;
}

function statusLine(content) {
  const status = document.createElement('div');
  content.appendChild(status);
  return status;
}

function textEditor(content, ctx, { mode }) {
  const save = bar(content, ctx.mod + '/' + ctx.file);

  const textarea = document.createElement('textarea');
  textarea.className = 'code';
  textarea.spellcheck = false;
  textarea.value = ctx.content;
  content.appendChild(textarea);

  const status = statusLine(content);

  save.addEventListener('click', async () => {
    const value = textarea.value;
    let toSave = value;
    if (mode === 'json') {
      try {
        toSave = JSON.stringify(JSON.parse(value), null, 2);
        textarea.value = toSave;
      } catch (e) {
        status.className = 'status-err';
        status.textContent = 'client parse failed: ' + e.message + ' (saving as-is, server re-checks)';
      }
    }
    try {
      await api.saveModFile(ctx.mod, ctx.file, toSave);
      status.className = 'status-ok';
      status.textContent = 'saved';
      ctx.onSaved && ctx.onSaved();
    } catch (e) {
      status.className = 'status-err';
      status.textContent = e.message;
    }
  });
}

export function jsonEditor(content, ctx) {
  textEditor(content, ctx, { mode: 'json' });
}

export function luaEditor(content, ctx) {
  textEditor(content, ctx, { mode: 'lua' });
}

export function textFileEditor(content, ctx) {
  textEditor(content, ctx, { mode: 'text' });
}

// strings_<lang>.json: one ["Key", "value"] pair per line
export function stringsEditor(content, ctx) {
  const save = bar(content, ctx.mod + '/' + ctx.file);

  const lines = ctx.content.split('\n').map(l => l.trim()).filter(l => l.length > 0);
  const pairs = [];
  for (const line of lines) {
    try {
      const entry = JSON.parse(line);
      pairs.push([String(entry[0]), String(entry[1])]);
    } catch (e) {
      pairs.push(['', 'malformed line: ' + line]);
    }
  }

  const table = document.createElement('table');
  table.className = 'pairs';
  content.appendChild(table);

  function addRow(key, value) {
    const tr = document.createElement('tr');
    const keyCell = document.createElement('td');
    const valCell = document.createElement('td');
    const keyInput = document.createElement('input');
    keyInput.value = key;
    keyInput.placeholder = 'Key';
    const valInput = document.createElement('input');
    valInput.value = value;
    valInput.placeholder = 'value';
    keyCell.appendChild(keyInput);
    valCell.appendChild(valInput);
    tr.appendChild(keyCell);
    tr.appendChild(valCell);
    const delCell = document.createElement('td');
    const del = document.createElement('button');
    del.textContent = '×';
    del.addEventListener('click', () => tr.remove());
    delCell.appendChild(del);
    tr.appendChild(delCell);
    table.appendChild(tr);
    return tr;
  }

  for (const [k, v] of pairs) { addRow(k, v); }

  const add = document.createElement('button');
  add.textContent = '+ pair';
  add.addEventListener('click', () => addRow('', ''));
  content.appendChild(add);
  content.appendChild(document.createTextNode(' '));

  const status = statusLine(content);

  save.addEventListener('click', async () => {
    const out = [];
    for (const tr of table.querySelectorAll('tr')) {
      const [keyInput, valInput] = tr.querySelectorAll('input');
      if (!keyInput.value && !valInput.value) { continue; }
      out.push(JSON.stringify([keyInput.value, valInput.value]));
    }
    try {
      await api.saveModFile(ctx.mod, ctx.file, out.join('\n') + '\n');
      status.className = 'status-ok';
      status.textContent = 'saved (' + out.length + ' pairs)';
    } catch (e) {
      status.className = 'status-err';
      status.textContent = e.message;
    }
  });
}

// png (and other images): preview + replace via upload + link to the
// mob-viewer sprite editor for pixel work
export function imageEditor(content, ctx) {
  const wrap = document.createElement('div');
  const img = document.createElement('img');
  img.className = 'sprite-preview';
  img.src = api.rawUrl(ctx.mod, ctx.file, Date.now());
  img.onload = () => {
    const info = document.createElement('div');
    info.className = 'hint';
    info.textContent = img.naturalWidth + '×' + img.naturalHeight;
    wrap.appendChild(info);
  };
  wrap.appendChild(img);
  content.appendChild(wrap);

  const replace = document.createElement('button');
  replace.textContent = 'Replace with upload…';
  const status = document.createElement('span');
  replace.addEventListener('click', async () => {
    const path = await uploadPng(ctx.mod, ctx.file);
    if (path) {
      img.src = api.rawUrl(ctx.mod, ctx.file, Date.now());
      status.className = 'status-ok';
      status.textContent = 'replaced';
    }
  });
  content.appendChild(replace);
  content.appendChild(status);

  const hint = document.createElement('p');
  hint.className = 'hint';
  hint.textContent = 'Pixel editing: open this sheet in the sprite editor ';
  const link = document.createElement('a');
  link.href = '/web/mob-viewer/';
  link.target = '_blank';
  link.textContent = '(mob-viewer)';
  hint.appendChild(link);
  hint.appendChild(document.createTextNode(
    ' - it saves into the ACTIVE mod, so activate ' + ctx.mod + ' first.'));
  content.appendChild(hint);
}

// sound files a mod ships in sound/: preview via <audio>, replace by upload
export function audioEditor(content, ctx) {
  const wrap = document.createElement('div');
  const audio = document.createElement('audio');
  audio.controls = true;
  audio.src = api.rawUrl(ctx.mod, ctx.file, Date.now());
  wrap.appendChild(audio);
  content.appendChild(wrap);

  const replace = document.createElement('button');
  replace.textContent = 'Replace with upload…';
  const status = document.createElement('span');
  replace.addEventListener('click', async () => {
    const path = await uploadFile(ctx.mod, ctx.file, 'audio/*', ['.ogg', '.mp3', '.wav']);
    if (path) {
      audio.src = api.rawUrl(ctx.mod, ctx.file, Date.now());
      status.className = 'status-ok';
      status.textContent = 'replaced';
    }
  });
  content.appendChild(replace);
  content.appendChild(status);

  const hint = document.createElement('p');
  hint.className = 'hint';
  hint.textContent = 'Referenced from lua as RPD.playSound("snd_name") - the game ' +
    'looks the name up in the resource overlay (mod first, then built-in).';
  content.appendChild(hint);
}

export function pickEditor(file) {
  const lower = file.toLowerCase();
  if (lower.startsWith('strings_') && lower.endsWith('.json')) {
    return stringsEditor;
  }
  if (lower.endsWith('.json')) { return jsonEditor; }
  if (lower.endsWith('.lua')) { return luaEditor; }
  if (lower.endsWith('.png') || lower.endsWith('.jpg')) { return imageEditor; }
  if (lower.endsWith('.mp3') || lower.endsWith('.ogg') || lower.endsWith('.wav')) {
    return audioEditor;
  }
  return textFileEditor;
}
