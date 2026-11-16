// Structured editor for levelsDesc/Bestiary.json - per-level-kind spawn
// tables: { "<LevelKind>": { "<depth|any>": { "<MobKind>": weight } } }
// plus the special "Feelings" section (Chance + terrain-elemental groups).
// Unknown keys survive a save; anything the form cannot model falls back to
// the plain json text editor.

import * as api from '../api.js';

const FEELING_GROUPS = ['CHASM', 'WATER', 'GRASS'];

function mobMapOk(value) {
  if (value == null || typeof value !== 'object' || Array.isArray(value)) { return false; }
  return Object.values(value).every(v => Number.isFinite(v));
}

// a level section is { depthKey: {mob: weight} }; Feelings is checked apart
function levelSectionOk(section) {
  if (section == null || typeof section !== 'object' || Array.isArray(section)) { return false; }
  return Object.values(section).every(mobMapOk);
}

function feelingsOk(feelings) {
  if (feelings == null || typeof feelings !== 'object' || Array.isArray(feelings)) { return false; }
  for (const [key, value] of Object.entries(feelings)) {
    if (key === 'Chance') {
      if (!Number.isFinite(value)) { return false; }
    } else if (!mobMapOk(value)) { return false; }
  }
  return true;
}

export async function bestiaryEditor(content, ctx) {
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
  for (const [key, section] of Object.entries(obj)) {
    const ok = key === 'Feelings' ? feelingsOk(section) : levelSectionOk(section);
    if (!ok) {
      const { jsonEditor } = await import('./text-editors.js');
      await jsonEditor(content, Object.assign({}, ctx, { content: data.content }));
      return null;
    }
  }

  const barEl = document.createElement('div');
  barEl.className = 'editor-bar';
  const title = document.createElement('strong');
  title.textContent = 'Bestiary (spawn tables)';
  const save = document.createElement('button');
  save.className = 'primary';
  save.textContent = 'Save';
  barEl.appendChild(title);
  barEl.appendChild(save);
  content.appendChild(barEl);

  const status = document.createElement('div');
  content.appendChild(status);

  let dirty = false;
  const markDirty = () => { dirty = true; };

  const mobList = document.createElement('datalist');
  mobList.id = 'bestiaryMobList';
  content.appendChild(mobList);
  for (const builtin of [true, false]) {
    api.modFiles(ctx.mod, 'mobsDesc', builtin).then(list => {
      for (const entry of list.entries) {
        if (entry.dir || !entry.name.toLowerCase().endsWith('.json')) { continue; }
        const o = document.createElement('option');
        o.value = entry.name.replace(/\.json$/, '');
        mobList.appendChild(o);
      }
    }).catch(() => {});
  }

  // rows for one {mob: weight} map, rendered inside the given container
  function mobRows(box, map) {
    const table = document.createElement('table');
    table.className = 'pairs';
    box.appendChild(table);
    function addRow(name, value) {
      const tr = document.createElement('tr');
      const kindCell = document.createElement('td');
      const wCell = document.createElement('td');
      const kind = document.createElement('input');
      kind.value = name;
      kind.setAttribute('list', 'bestiaryMobList');
      kind.placeholder = 'MobKind';
      const w = document.createElement('input');
      w.type = 'number';
      w.step = 'any';
      w.value = value;
      kind.addEventListener('input', markDirty);
      w.addEventListener('input', markDirty);
      kindCell.appendChild(kind);
      wCell.appendChild(w);
      tr.appendChild(kindCell);
      tr.appendChild(wCell);
      const delCell = document.createElement('td');
      const del = document.createElement('button');
      del.textContent = '×';
      del.addEventListener('click', () => { tr.remove(); markDirty(); });
      delCell.appendChild(del);
      tr.appendChild(delCell);
      table.appendChild(tr);
    }
    for (const [name, value] of Object.entries(map)) { addRow(name, value); }
    const add = document.createElement('button');
    add.textContent = '+ mob';
    add.addEventListener('click', () => addRow('', 1));
    box.appendChild(add);
    return table;
  }

  // collect a mob map back from its table rows
  function readMobMap(box, where) {
    const next = {};
    for (const tr of box.querySelectorAll('table tr')) {
      const [kind, w] = tr.querySelectorAll('input');
      const name = kind.value.trim();
      if (!name && !w.value.trim()) { continue; }
      if (!name) { throw new Error(where + ': a mob row has no kind'); }
      const n = Number(w.value);
      if (!Number.isFinite(n)) { throw new Error(where + '/' + name + ': weight is not a number'); }
      next[name] = n;
    }
    return next;
  }

  function levelPanel(levelKey) {
    const section = obj[levelKey];
    const panelEl = document.createElement('div');
    panelEl.className = 'panel';
    panelEl.dataset.level = levelKey;
    const h = document.createElement('h3');
    h.textContent = levelKey;
    panelEl.appendChild(h);
    const hint = document.createElement('p');
    hint.className = 'hint';
    hint.textContent = 'depth keys: a number, "any", or <depth>s (special variants); ' +
      'weights are relative (0 = never spawns at random)';
    panelEl.appendChild(hint);

    const depthsBox = document.createElement('div');
    panelEl.appendChild(depthsBox);

    for (const depthKey of Object.keys(section)) {
      const depthBox = document.createElement('div');
      depthBox.dataset.depth = depthKey;
      const head = document.createElement('strong');
      head.textContent = depthKey;
      depthBox.appendChild(head);
      depthBox.appendChild(document.createTextNode(' '));
      const delDepth = document.createElement('button');
      delDepth.textContent = '×';
      delDepth.addEventListener('click', () => { depthBox.remove(); markDirty(); });
      depthBox.appendChild(delDepth);
      mobRows(depthBox, section[depthKey]);
      depthsBox.appendChild(depthBox);
    }

    const addDepth = document.createElement('button');
    addDepth.textContent = '+ depth';
    addDepth.addEventListener('click', () => {
      const depthKey = window.prompt('Depth key (number, "any", or like 6s):', 'any');
      if (!depthKey) { return; }
      section[depthKey] = {};
      const depthBox = document.createElement('div');
      depthBox.dataset.depth = depthKey;
      const head = document.createElement('strong');
      head.textContent = depthKey;
      depthBox.appendChild(head);
      mobRows(depthBox, section[depthKey]);
      depthsBox.appendChild(depthBox);
      markDirty();
    });
    panelEl.appendChild(addDepth);

    const del = document.createElement('button');
    del.className = 'danger';
    del.textContent = 'delete level';
    del.addEventListener('click', () => { panelEl.remove(); markDirty(); });
    panelEl.appendChild(del);
    return panelEl;
  }

  const box = document.createElement('div');
  content.appendChild(box);

  for (const key of Object.keys(obj)) {
    if (key === 'Feelings') { continue; }
    box.appendChild(levelPanel(key));
  }

  // ---- Feelings ---------------------------------------------------------
  let feelingsPanel = null;
  if (obj.Feelings) {
    feelingsPanel = document.createElement('div');
    feelingsPanel.className = 'panel';
    feelingsPanel.dataset.feelings = '1';
    const h = document.createElement('h3');
    h.textContent = 'Feelings (rare terrain spawns)';
    feelingsPanel.appendChild(h);
    const chanceRow = document.createElement('div');
    chanceRow.className = 'field-grid';
    const chanceLabel = document.createElement('label');
    chanceLabel.textContent = 'Chance';
    const chance = document.createElement('input');
    chance.type = 'number';
    chance.step = 'any';
    chance.dataset.feelingsChance = '1';
    chance.value = Number.isFinite(obj.Feelings.Chance) ? obj.Feelings.Chance : 0.2;
    chance.addEventListener('input', markDirty);
    chanceRow.appendChild(chanceLabel);
    chanceRow.appendChild(chance);
    feelingsPanel.appendChild(chanceRow);

    const groupsBox = document.createElement('div');
    feelingsPanel.appendChild(groupsBox);
    for (const group of FEELING_GROUPS) {
      if (obj.Feelings[group] === undefined) { continue; }
      const groupBox = document.createElement('div');
      groupBox.dataset.feelingGroup = group;
      const head = document.createElement('strong');
      head.textContent = group;
      groupBox.appendChild(head);
      mobRows(groupBox, obj.Feelings[group]);
      groupsBox.appendChild(groupBox);
    }
    const addGroup = document.createElement('button');
    addGroup.textContent = '+ terrain group';
    addGroup.addEventListener('click', () => {
      const group = window.prompt('Terrain group (CHASM, WATER or GRASS):', 'WATER');
      if (!group || FEELING_GROUPS.includes(group) && obj.Feelings[group] !== undefined) { return; }
      obj.Feelings[group] = {};
      const groupBox = document.createElement('div');
      groupBox.dataset.feelingGroup = group;
      const head = document.createElement('strong');
      head.textContent = group;
      groupBox.appendChild(head);
      mobRows(groupBox, obj.Feelings[group]);
      groupsBox.appendChild(groupBox);
      markDirty();
    });
    feelingsPanel.appendChild(addGroup);
    box.appendChild(feelingsPanel);
  }

  const addLevel = document.createElement('button');
  addLevel.textContent = '+ level kind';
  addLevel.addEventListener('click', () => {
    const levelKey = window.prompt('Level kind (level id or level class name):', 'SewerLevel');
    if (!levelKey) { return; }
    obj[levelKey] = { any: {} };
    box.insertBefore(levelPanel(levelKey), feelingsPanel);
    markDirty();
  });
  content.appendChild(addLevel);

  // ---- save -------------------------------------------------------------
  save.addEventListener('click', async () => {
    try {
      for (const panelEl of box.querySelectorAll('[data-level]')) {
        const levelKey = panelEl.dataset.level;
        const section = obj[levelKey];
        if (!section) { throw new Error('level ' + levelKey + ' vanished'); }
        const next = {};
        for (const depthBox of panelEl.querySelectorAll('[data-depth]')) {
          const depthKey = depthBox.dataset.depth;
          next[depthKey] = readMobMap(depthBox, levelKey + '/' + depthKey);
        }
        for (const k of Object.keys(section)) { delete section[k]; }
        Object.assign(section, next);
      }

      if (feelingsPanel) {
        const chance = feelingsPanel.querySelector('[data-feelings-chance]');
        const chanceVal = Number(chance.value);
        if (!Number.isFinite(chanceVal)) { throw new Error('Feelings: Chance is not a number'); }
        const next = { Chance: chanceVal };
        for (const groupBox of feelingsPanel.querySelectorAll('[data-feeling-group]')) {
          const group = groupBox.dataset.feelingGroup;
          next[group] = readMobMap(groupBox, 'Feelings/' + group);
        }
        for (const k of Object.keys(obj.Feelings)) { delete obj.Feelings[k]; }
        Object.assign(obj.Feelings, next);
      }

      // levels deleted from the dom
      const liveLevels = Array.from(box.querySelectorAll('[data-level]'))
        .map(p => p.dataset.level);
      for (const key of Object.keys(obj)) {
        if (key !== 'Feelings' && !liveLevels.includes(key)) { delete obj[key]; }
      }
    } catch (e) {
      status.className = 'status-err';
      status.textContent = e.message;
      return;
    }

    try {
      await api.saveModFile(ctx.mod, ctx.file, JSON.stringify(obj, null, 3) + '\n');
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
