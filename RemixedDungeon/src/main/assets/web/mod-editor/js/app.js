// Mod editor app: hash routing, dashboard, per-mod workspace.
// #/            - dashboard: installed mods, create/duplicate
// #/mod/<name>  - workspace: file tree + editors + validate/export/play

import * as api from './api.js';
import { renderTree } from './file-tree.js';
import { pickEditor } from './editors/text-editors.js';
import { mobEditor } from './editors/mob-editor.js';
import { levelEditor } from './editors/level-editor.js';

const sidebar = document.getElementById('sidebar');
const content = document.getElementById('content');
const crumbs = document.getElementById('crumbs');
const actions = document.getElementById('actions');

api.onBusy(b => { document.getElementById('busy').hidden = !b; });

let currentEditorDirty = null; // () => boolean, set by open editors

window.addEventListener('beforeunload', e => {
  if (currentEditorDirty && currentEditorDirty()) {
    e.preventDefault();
    e.returnValue = '';
  }
});

function confirmDirty() {
  if (currentEditorDirty && currentEditorDirty()) {
    return window.confirm('Discard unsaved changes?');
  }
  return true;
}

function route() {
  const hash = location.hash || '#/';
  const modMatch = hash.match(/^#\/mod\/([A-Za-z0-9_-]+)$/);
  if (modMatch) {
    openWorkspace(modMatch[1]);
  } else {
    openDashboard();
  }
}
window.addEventListener('hashchange', () => { if (confirmDirty()) { route(); } });

// ---------------------------------------------------------------------
// dashboard
// ---------------------------------------------------------------------

async function openDashboard() {
  currentEditorDirty = null;
  crumbs.textContent = 'mods';
  actions.innerHTML = '';
  sidebar.innerHTML = '';

  const data = await api.listMods();

  const h = document.createElement('h2');
  h.textContent = 'Installed mods';
  content.innerHTML = '';
  content.appendChild(h);

  const ul = document.createElement('ul');
  ul.className = 'mod-list';
  for (const mod of data.mods) {
    const li = document.createElement('li');
    const info = document.createElement('div');
    info.className = 'grow';
    const title = document.createElement('div');
    title.className = 'title';
    title.textContent = mod.name + (mod.active ? '  ● active' : '');
    const meta = document.createElement('div');
    meta.className = 'meta';
    meta.textContent = `dir: ${mod.installDir} · v${mod.hrVersion} · by ${mod.author}` +
      (mod.builtin ? ' · built-in' : '');
    info.appendChild(title);
    info.appendChild(meta);
    li.appendChild(info);

    if (!mod.builtin) {
      const edit = document.createElement('button');
      edit.className = 'primary';
      edit.textContent = 'Edit';
      edit.addEventListener('click', () => { location.hash = '#/mod/' + mod.installDir; });
      li.appendChild(edit);
    }
    ul.appendChild(li);
  }
  content.appendChild(ul);

  const panel = document.createElement('div');
  panel.className = 'panel';
  panel.innerHTML = '<h3>New mod</h3>';

  const grid = document.createElement('div');
  grid.className = 'field-grid';
  const fields = [
    ['Mod id (dir name)', 'name', 'MyMod'],
    ['Title', 'title', 'My Mod'],
    ['Author', 'author', ''],
    ['Description', 'description', ''],
  ];
  const inputs = {};
  for (const [labelText, key, placeholder] of fields) {
    const label = document.createElement('label');
    label.textContent = labelText;
    const input = document.createElement('input');
    input.placeholder = placeholder;
    inputs[key] = input;
    grid.appendChild(label);
    grid.appendChild(input);
  }
  panel.appendChild(grid);

  const create = document.createElement('button');
  create.className = 'primary';
  create.textContent = 'Create from template';
  const status = document.createElement('span');
  create.addEventListener('click', async () => {
    try {
      await api.createMod({
        name: inputs.name.value.trim(),
        title: inputs.title.value.trim() || inputs.name.value.trim(),
        author: inputs.author.value.trim() || 'Unknown',
        description: inputs.description.value.trim()
      });
      location.hash = '#/mod/' + inputs.name.value.trim();
    } catch (e) {
      status.className = 'status-err';
      status.textContent = e.message;
    }
  });
  panel.appendChild(create);
  panel.appendChild(status);
  content.appendChild(panel);
}

// ---------------------------------------------------------------------
// workspace
// ---------------------------------------------------------------------

async function openWorkspace(modName) {
  currentEditorDirty = null;
  crumbs.textContent = modName;
  content.innerHTML = '<p class="hint">loading…</p>';

  // sidebar: file tree (renderTree clears its container, so re-rendering refreshes)
  sidebar.innerHTML = '';
  const treeBox = document.createElement('div');
  sidebar.appendChild(treeBox);

  function refreshTree() {
    renderTree(treeBox, {
      mod: modName,
      onOpen: file => openFile(modName, file),
      onNewFile: async dir => {
        const name = window.prompt('New file name (relative to ' + (dir || 'mod root') + ')');
        if (!name) { return; }
        const path = dir ? dir + '/' + name : name;
        try {
          await api.saveModFile(modName, path, '');
          refreshTree();
          openFile(modName, path);
        } catch (e) { alert(e.message); }
      },
      onDelete: async path => {
        if (!window.confirm('Delete ' + path + '?')) { return; }
        try {
          await api.deleteModFile(modName, path);
          refreshTree();
        } catch (e) { alert(e.message); }
      }
    });
  }
  refreshTree();

  // actions: validate / export / play (select+restart)
  actions.innerHTML = '';

  const validate = document.createElement('button');
  validate.textContent = 'Validate';
  validate.addEventListener('click', () => runValidate(modName));
  actions.appendChild(validate);

  const exportBtn = document.createElement('button');
  exportBtn.textContent = 'Export .zip';
  exportBtn.addEventListener('click', () => {
    window.open(api.exportUrl(modName), '_blank');
  });
  actions.appendChild(exportBtn);

  const play = document.createElement('button');
  play.className = 'primary';
  play.textContent = 'Play this mod';
  play.addEventListener('click', async () => {
    if (!confirmDirty()) { return; }
    if (!window.confirm('Switch the game to ' + modName + ' and restart it?')) { return; }
    try {
      const result = await api.selectMod(modName);
      content.innerHTML = '<p>Game restarting into <b>' + modName + '</b>… ' +
        '<span class="hint">(the page will reconnect when the server is back)</span></p>';
      if (!result.restarting && !result.alreadyActive) { return; }
      setTimeout(() => { window.location.reload(); }, 4000);
    } catch (e) { alert(e.message); }
  });
  actions.appendChild(play);

  // landing pane
  content.innerHTML = '';
  const h = document.createElement('h2');
  h.textContent = modName;
  content.appendChild(h);
  const hint = document.createElement('p');
  hint.className = 'hint';
  hint.textContent = 'Pick a file on the left, or use the actions above. ' +
    'mobsDesc/*.json get a form editor; levelsDesc/*.json open the level editor; ' +
    'everything else opens a text editor. "Play this mod" activates it and restarts the game.';
  content.appendChild(hint);

  await runValidate(modName, true);
}

async function runValidate(modName, quiet) {
  let box = document.getElementById('validate-box');
  if (!box) {
    box = document.createElement('div');
    box.id = 'validate-box';
    box.className = 'panel';
    content.prepend(box);
  }
  box.innerHTML = '<h3>Validation</h3><p class="hint">running…</p>';
  try {
    const result = await api.validateMod(modName);
    box.innerHTML = '<h3>Validation</h3>';
    const stats = document.createElement('p');
    stats.className = 'hint';
    stats.textContent = `${result.stats.jsonFiles} json · ${result.stats.luaFiles} lua files checked`;
    box.appendChild(stats);
    if (result.errors.length) {
      const ul = document.createElement('ul');
      ul.className = 'error-list';
      for (const e of result.errors.slice(0, 30)) {
        const li = document.createElement('li');
        li.textContent = e;
        ul.appendChild(li);
      }
      box.appendChild(ul);
    } else if (!quiet) {
      const ok = document.createElement('p');
      ok.className = 'status-ok';
      ok.textContent = 'no problems found';
      box.appendChild(ok);
    }
  } catch (e) {
    box.innerHTML = '<h3>Validation</h3><p class="status-err">' + e.message + '</p>';
  }
}

// ---------------------------------------------------------------------
// file editors
// ---------------------------------------------------------------------

async function openFile(modName, file) {
  if (!confirmDirty()) { return; }
  const lower = file.toLowerCase();

  // structured editors load their own data and report dirtiness themselves
  try {
    if (lower.startsWith('mobsdesc/') && lower.endsWith('.json')) {
      content.innerHTML = '';
      const ed = await mobEditor(content, { mod: modName, file, api, onSaved: () => {} });
      currentEditorDirty = ed ? ed.isDirty : null;
      return;
    }
    if (lower.startsWith('levelsdesc/') && lower.endsWith('.json') && !file.endsWith('Dungeon.json')) {
      content.innerHTML = '';
      const ed = await levelEditor(content, { mod: modName, file, api, onSaved: () => {} });
      currentEditorDirty = ed ? ed.isDirty : null;
      return;
    }
  } catch (e) {
    // a half-rendered editor must not die silently (e.g. readonly IDL property crash)
    content.textContent = 'Editor failed: ' + e.message;
    currentEditorDirty = null;
    return;
  }

  const data = await api.readModFile(modName, file);
  content.innerHTML = '';
  const editor = pickEditor(file);
  let dirty = false;
  editor(content, {
    mod: modName,
    file,
    content: data.content,
    source: data.source,
    onSaved: () => { dirty = false; }
  });
  content.addEventListener('input', () => { dirty = true; }, { once: true });
  currentEditorDirty = () => dirty;
}

route();
