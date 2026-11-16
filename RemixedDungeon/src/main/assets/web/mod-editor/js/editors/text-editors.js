// Generic text-based editors: json, lua, strings pairs, plain text.
// Validation is server-side (the save endpoint parses before writing) -
// the client only pretty-prints json it can parse itself.

import * as api from '../api.js';
import { uploadPng, uploadFile } from '../upload.js';
import { TEMPLATES } from './templates.js';

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
  renderLuaHelp(content, ctx);
  textEditor(content, ctx, { mode: 'lua' });
}

// ---- lua authoring help: snippet inserts + API reference pane ----------

const SNIPPET_KEYS = ['scripts/items/', 'scripts/buffs/', 'scripts/mobs/', 'scripts/ai/',
  'scripts/spells/', 'scripts/traps/', 'scripts/objects/', 'scripts/npc/', 'scripts/actors/'];

// snippets + API reference pane. opts.snippets=false renders the reference
// only (form editors have no textarea to insert skeletons into)
export function renderLuaHelp(content, ctx, opts) {
  const details = document.createElement('details');
  details.className = 'panel lua-help';

  const summary = document.createElement('summary');
  summary.textContent = 'Lua authoring help - snippets & API reference';
  details.appendChild(summary);

  if (!opts || opts.snippets !== false) {
    const snippetBox = document.createElement('div');
    snippetBox.className = 'hint';
    snippetBox.textContent = 'insert a working skeleton at the cursor: ';
    details.appendChild(snippetBox);
    for (const key of SNIPPET_KEYS) {
      const t = TEMPLATES[key];
      if (!t) { continue; }
      const b = document.createElement('button');
      b.textContent = key.split('/')[1];
      b.title = t.hint;
      b.addEventListener('click', () => {
        const textarea = content.querySelector('textarea.code');
        if (!textarea) { return; }
        const at = textarea.selectionStart;
        const before = textarea.value.slice(0, at);
        const after = textarea.value.slice(textarea.selectionEnd);
        const pad = before && !before.endsWith('\n') ? '\n\n' : '';
        textarea.value = before + pad + t.content.trimEnd() + '\n' + after;
        textarea.dispatchEvent(new Event('input', { bubbles: true }));
      });
      snippetBox.appendChild(b);
    }
  }

  const refBox = document.createElement('div');
  refBox.className = 'hint';
  refBox.textContent = 'loading reference…';
  details.appendChild(refBox);
  // same-origin static asset (vendored copy of docs/modding.md - a python
  // test keeps it in sync); zero third-party requests by design
  fetch('/web/mod-editor/api-reference.md', { cache: 'no-store' })
    .then(r => { if (!r.ok) { throw new Error('HTTP ' + r.status); } return r.text(); })
    .then(md => { refBox.innerHTML = renderMarkdown(md); refBox.className = 'api-ref'; })
    .catch(e => {
      refBox.textContent = 'API reference unavailable: ' + e.message +
        ' (see docs/modding.md in the repo)';
    });

  content.appendChild(details);
}

// minimal markdown rendering for the reference doc: headings, lists, code
// fences + inline code; everything html-escaped first
function renderMarkdown(md) {
  const esc = s => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  const inline = s => esc(s)
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    // [text](target): in-page anchors and plain hrefs users click - never
    // auto-fetched resources, so the zero-third-party rule holds
    .replace(/\[([^\]]+)\]\(([^)\s]+)\)/g, '<a href="$2">$1</a>');
  const out = [];
  let inCode = false;
  for (const line of md.split('\n')) {
    const fence = line.match(/^\s*```/);
    if (fence) {
      out.push(inCode ? '</pre>' : '<pre>');
      inCode = !inCode;
      continue;
    }
    if (inCode) { out.push(esc(line)); continue; }
    const h = line.match(/^(#{1,4})\s+(.*)/);
    if (h) {
      out.push('<h' + (h[1].length + 1) + '>' + inline(h[2]) + '</h' + (h[1].length + 1) + '>');
      continue;
    }
    const li = line.match(/^\s*[-*]\s+(.*)/);
    if (li) { out.push('<li>' + inline(li[1]) + '</li>'); continue; }
    if (line.trim() === '') { out.push(''); continue; }
    out.push('<p>' + inline(line) + '</p>');
  }
  if (inCode) { out.push('</pre>'); }
  return out.join('\n');
}

export function textFileEditor(content, ctx) {
  textEditor(content, ctx, { mode: 'text' });
}

// ---- per-language strings manager --------------------------------------
// pairs-table editor stays per-file; this strip gives the cross-language view

function parsePairs(text) {
  const keys = new Set();
  const map = new Map();
  for (const line of text.split('\n')) {
    const l = line.trim();
    if (!l) { continue; }
    try {
      const entry = JSON.parse(l);
      keys.add(String(entry[0]));
      map.set(String(entry[0]), String(entry[1]));
    } catch (e) { /* malformed lines stay the pairs editor's business */ }
  }
  return map;
}

async function renderStringsManager(content, ctx) {
  const panelEl = document.createElement('div');
  panelEl.className = 'panel';
  const h = document.createElement('h3');
  h.textContent = 'Languages';
  panelEl.appendChild(h);
  const statusEl = document.createElement('div');
  panelEl.appendChild(statusEl);

  let root;
  try {
    root = await api.modFiles(ctx.mod, '');
  } catch (e) {
    statusEl.className = 'hint';
    statusEl.textContent = 'language overview unavailable: ' + e.message;
    content.appendChild(panelEl);
    return;
  }
  const langFiles = root.entries
    .filter(e => !e.dir && /^strings_.+\.json$/i.test(e.name))
    .map(e => e.name);

  // reference = the mod's own english file, else the builtin english strings
  let refMap = null;
  let refName = 'strings_en.json';
  if (langFiles.some(f => f.toLowerCase() === 'strings_en.json')) {
    refMap = parsePairs((await api.readModFile(ctx.mod, 'strings_en.json')).content);
  } else {
    try {
      refMap = parsePairs((await api.readModFile(ctx.mod, 'strings_en.json', true)).content);
      refName = 'strings_en.json (built-in)';
    } catch (e) { /* no reference - coverage column just shows - */ }
  }

  const table = document.createElement('table');
  table.className = 'pairs';
  panelEl.appendChild(table);
  for (const name of langFiles) {
    let map = null;
    if (name === ctx.file) {
      map = parsePairs(ctx.content);
    } else {
      try { map = parsePairs((await api.readModFile(ctx.mod, name)).content); }
      catch (e) { /* unreadable file: show counts as unknown */ }
    }
    const tr = document.createElement('tr');
    const nameCell = document.createElement('td');
    nameCell.textContent = name + (name === ctx.file ? '  ●' : '');
    const countCell = document.createElement('td');
    countCell.textContent = map ? map.size + ' pairs' : '?';
    const missCell = document.createElement('td');
    if (refMap && map) {
      const missing = [...refMap.keys()].filter(k => !map.has(k));
      missCell.textContent = missing.length
        ? missing.length + ' missing vs ' + refName
        : 'complete vs ' + refName;
      missCell.className = missing.length ? 'status-err' : 'status-ok';
    } else {
      missCell.textContent = '-';
    }
    const openCell = document.createElement('td');
    if (name !== ctx.file) {
      const open = document.createElement('button');
      open.textContent = 'open';
      open.addEventListener('click', () => {
        window.dispatchEvent(new CustomEvent('mod-editor:open-file', { detail: name }));
      });
      openCell.appendChild(open);
      const del = document.createElement('button');
      del.textContent = '×';
      del.title = 'delete ' + name;
      del.style.color = '#a55';
      del.style.marginLeft = '6px';
      del.addEventListener('click', async () => {
        if (!window.confirm('Delete ' + name + '? The file is removed from the mod.')) { return; }
        try {
          await api.deleteModFile(ctx.mod, name);
          statusEl.className = 'status-ok';
          statusEl.textContent = 'deleted ' + name;
          // re-render the manager by reopening the current file
          window.dispatchEvent(new CustomEvent('mod-editor:open-file', { detail: ctx.file }));
        } catch (e) {
          statusEl.className = 'status-err';
          statusEl.textContent = e.message;
        }
      });
      openCell.appendChild(del);
    }
    tr.appendChild(nameCell);
    tr.appendChild(countCell);
    tr.appendChild(missCell);
    tr.appendChild(openCell);
    table.appendChild(tr);
  }

  const add = document.createElement('button');
  add.textContent = '+ language';
  add.addEventListener('click', async () => {
    const code = window.prompt('Language code (file will be strings_<code>.json):', 'ru');
    if (!code) { return; }
    const file = 'strings_' + code.trim().replace(/^strings_|\.json$/g, '') + '.json';
    if (langFiles.includes(file)) {
      statusEl.className = 'status-err';
      statusEl.textContent = file + ' already exists';
      return;
    }
    // seed with every reference key and an empty value to translate into
    const out = [];
    if (refMap) {
      for (const k of refMap.keys()) { out.push(JSON.stringify([k, ''])); }
    }
    try {
      await api.saveModFile(ctx.mod, file, out.length ? out.join('\n') + '\n' : '');
      statusEl.className = 'status-ok';
      statusEl.textContent = 'created ' + file + ' with ' + out.length +
        ' empty pairs - open it from the languages table or the file tree';
      window.dispatchEvent(new CustomEvent('mod-editor:open-file', { detail: file }));
    } catch (e) {
      statusEl.className = 'status-err';
      statusEl.textContent = e.message;
    }
  });
  panelEl.appendChild(add);
  content.appendChild(panelEl);
}

// strings_<lang>.json: one ["Key", "value"] pair per line.
// diffs key coverage against the mod's (or builtin) english file and can
// seed a new language file with the missing keys.
export async function stringsEditor(content, ctx) {
  const save = bar(content, ctx.mod + '/' + ctx.file);

  await renderStringsManager(content, ctx);

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
