// Structured editor for mobsDesc/<Kind>.json.
// Field set mirrors what the engine reads from mob json (Mob.fillMobStats).

import * as api from '../api.js';

const WALKING_TYPES = ['NORMAL', 'WALL', 'WATER', 'ABSOLUTE'];
const AI_STATES = ['Wandering', 'Sleeping', 'Hunting', 'Passive', 'Kite'];
const GENDERS = ['neuter', 'feminine', 'masculine'];

const NUMBER_FIELDS = [
  ['ht', 'HP'], ['defenseSkill', 'defense'], ['attackSkill', 'attack'],
  ['dmgMin', 'dmg min'], ['dmgMax', 'dmg max'], ['dr', 'armor'],
  ['baseSpeed', 'speed'], ['attackDelay', 'attack delay'],
  ['exp', 'exp'], ['maxLvl', 'max level'], ['viewDistance', 'view dist'],
  ['lootChance', 'loot chance'],
];

const CHECK_FIELDS = [['canBePet', 'can be pet'], ['flying', 'flying'], ['friendly', 'friendly']];

export async function mobEditor(content, ctx) {
  const kind = ctx.file.split('/').pop().replace(/\.json$/, '');

  let mob = {};
  try {
    const data = await api.readModFile(ctx.mod, ctx.file);
    mob = JSON.parse(data.content);
  } catch (e) {
    mob = { name: kind + '_Name', name_objective: kind + '_Name_Objective', description: kind + '_Desc' };
  }

  const barEl = document.createElement('div');
  barEl.className = 'editor-bar';
  const title = document.createElement('strong');
  title.textContent = 'Mob: ' + kind;
  const save = document.createElement('button');
  save.className = 'primary';
  save.textContent = 'Save';
  barEl.appendChild(title);
  barEl.appendChild(save);
  content.appendChild(barEl);

  const status = document.createElement('div');
  content.appendChild(status);

  const inputs = {};

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
    return grid;
  }

  function field(grid, labelText, key, value, attrs) {
    const label = document.createElement('label');
    label.textContent = labelText;
    const input = document.createElement('input');
    input.value = value === undefined ? '' : value;
    if (attrs) {
      for (const [attr, v] of Object.entries(attrs)) {
        // input.list is a readonly IDL property - the content attribute is what matters
        if (attr === 'list') { input.setAttribute('list', v); }
        else { input[attr] = v; }
      }
    }
    inputs[key] = input;
    grid.appendChild(label);
    grid.appendChild(input);
    return input;
  }

  function selectField(grid, labelText, key, value, options) {
    const label = document.createElement('label');
    label.textContent = labelText;
    const input = document.createElement('select');
    for (const opt of [''].concat(options)) {
      const o = document.createElement('option');
      o.value = opt;
      o.textContent = opt || '(default)';
      input.appendChild(o);
    }
    input.value = value || '';
    inputs[key] = input;
    grid.appendChild(label);
    grid.appendChild(input);
    return input;
  }

  function checkField(grid, labelText, key, value) {
    const label = document.createElement('label');
    label.textContent = '';
    const input = document.createElement('input');
    input.type = 'checkbox';
    input.checked = value === true;
    inputs[key] = input;
    const span = document.createElement('label');
    span.textContent = labelText;
    span.style.marginLeft = '6px';
    const wrap = document.createElement('div');
    wrap.appendChild(input);
    wrap.appendChild(span);
    grid.appendChild(label);
    grid.appendChild(wrap);
  }

  // --- strings -----------------------------------------------------------
  const stringsGrid = panel('Names (strings keys from strings_en.json, or plain text)');
  field(stringsGrid, 'name key', 'name', mob.name, { placeholder: kind + '_Name' });
  field(stringsGrid, 'name_objective key', 'name_objective', mob.name_objective, { placeholder: kind + '_Name_Objective' });
  field(stringsGrid, 'description key', 'description', mob.description, { placeholder: kind + '_Desc' });
  selectField(stringsGrid, 'gender', 'gender', mob.gender, GENDERS);

  // --- stats ---------------------------------------------------------------
  const statsGrid = panel('Stats');
  for (const [key, labelText] of NUMBER_FIELDS) {
    field(statsGrid, labelText, key, mob[key], { type: 'number', step: 'any' });
  }
  for (const [key, labelText] of CHECK_FIELDS) {
    checkField(statsGrid, labelText, key, mob[key]);
  }
  selectField(statsGrid, 'walking type', 'walkingType', mob.walkingType, WALKING_TYPES);
  selectField(statsGrid, 'ai state', 'aiState', mob.aiState, AI_STATES);
  field(statsGrid, 'loot item kind', 'loot', mob.loot && mob.loot.kind, { placeholder: 'Gold' });

  // --- sprite --------------------------------------------------------------
  const spriteGrid = panel('Sprite');
  const spriteInput = field(spriteGrid, 'spriteDesc', 'spriteDesc', mob.spriteDesc, {
    placeholder: 'spritesDesc/' + kind + '.json', list: 'spriteDescList'
  });
  const dataList = document.createElement('datalist');
  dataList.id = 'spriteDescList';
  spriteGrid.appendChild(dataList);
  try {
    const [modList, builtinList] = await Promise.all([
      api.modFiles(ctx.mod, 'spritesDesc'),
      api.modFiles(ctx.mod, 'spritesDesc', true)
    ]);
    const names = new Set();
    for (const list of [modList, builtinList]) {
      for (const entry of list.entries) {
        if (entry.name.endsWith('.json')) {
          names.add('spritesDesc/' + entry.name);
        }
      }
    }
    for (const n of names) {
      const o = document.createElement('option');
      o.value = n;
      dataList.appendChild(o);
    }
  } catch (e) { /* datalist stays empty, free text still works */ }

  // sprite preview
  const previewBox = document.createElement('div');
  spriteGrid.parentElement.appendChild(previewBox);
  async function refreshPreview() {
    previewBox.innerHTML = '';
    const sd = spriteInput.value.trim();
    if (!sd) { return; }
    try {
      const desc = await api.readModFile(ctx.mod, sd);
      const sprite = JSON.parse(desc.content);
      if (sprite.texture) {
        const img = document.createElement('img');
        img.className = 'sprite-preview';
        img.style.maxWidth = '384px';
        img.src = api.rawUrl(ctx.mod, sprite.texture, Date.now());
        previewBox.appendChild(img);
      }
    } catch (e) {
      const note = document.createElement('span');
      note.className = 'status-err';
      note.textContent = 'no preview: ' + e.message;
      previewBox.appendChild(note);
    }
  }
  spriteInput.addEventListener('change', refreshPreview);
  refreshPreview();

  // --- script --------------------------------------------------------------
  const scriptGrid = panel('Behavior script (optional)');
  field(scriptGrid, 'scriptFile', 'scriptFile', mob.scriptFile, {
    placeholder: 'scripts/mobs/' + kind, list: 'scriptList'
  });
  const scriptList = document.createElement('datalist');
  scriptList.id = 'scriptList';
  scriptGrid.appendChild(scriptList);
  try {
    const scripts = await api.modFiles(ctx.mod, 'scripts/mobs', true);
    for (const entry of scripts.entries) {
      if (entry.name.endsWith('.lua')) {
        const o = document.createElement('option');
        o.value = 'scripts/mobs/' + entry.name.replace(/\.lua$/, '');
        scriptList.appendChild(o);
      }
    }
  } catch (e) { /* optional */ }

  // --- save ----------------------------------------------------------------
  // Start from the parsed file and only touch the known form keys:
  // unknown json keys (drops arrays, tags, custom props) must survive a save.
  let dirty = false;
  for (const input of Object.values(inputs)) {
    input.addEventListener('input', () => { dirty = true; });
    input.addEventListener('change', () => { dirty = true; });
  }

  save.addEventListener('click', async () => {
    const out = mob;
    for (const [key, input] of Object.entries(inputs)) {
      if (input.type === 'checkbox') {
        if (input.checked) { out[key] = true; }
        else if (out[key] === true) { out[key] = false; }
        continue;
      }
      const v = input.value.trim();
      if (key === 'loot') {
        if (v === '') {
          delete out.loot;
        } else if (out.loot && typeof out.loot === 'object' && !Array.isArray(out.loot)) {
          out.loot.kind = v; // keep companion keys (chance, count...)
        } else {
          out.loot = { kind: v };
        }
        continue;
      }
      if (v === '') { delete out[key]; continue; }
      out[key] = input.type === 'number' ? Number(v) : v;
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
