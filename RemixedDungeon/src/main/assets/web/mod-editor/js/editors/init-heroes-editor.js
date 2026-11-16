// Structured editor for hero/initHeroes.json (+ initHeroesDebug.json):
// per-class loadouts (stats, equipment, starting items, quickslots) plus the
// common / non_expert shared blocks. Unknown section keys and unknown entry
// fields survive a save (mutate the parsed object; entry extras round-trip
// through an inline json field). Files the form cannot model fall back to
// the plain json text editor.

import * as api from '../api.js';

const SCALARS = [
  ['str', 'str'],
  ['hp', 'hp'],
  ['sp', 'sp'],
  ['maxSp', 'maxSp'],
  ['magicAffinity', 'magicAffinity'],
];
const CHECKS = [
  ['isSpellUser', 'isSpellUser'],
];
const EQUIP_SLOTS = ['weapon', 'armor', 'ring1', 'ring2'];
const OBJECT_ARRAYS = ['items', 'knownItems', 'quickslot'];
const STRING_ARRAYS = ['immunities', 'forbiddenActions', 'friendlyMobs'];

const KNOWN_SECTIONS = ['common', 'non_expert', 'WARRIOR', 'MAGE', 'ROGUE', 'HUNTRESS',
  'ELF', 'NECROMANCER', 'GNOLL', 'LICH', 'PRIEST', 'DOCTOR'];

// gate: every section is an object; modeled keys hold shapes the form can edit
function sectionsShape(obj) {
  if (obj == null || typeof obj !== 'object' || Array.isArray(obj)) { return false; }
  for (const section of Object.values(obj)) {
    if (section == null || typeof section !== 'object' || Array.isArray(section)) { return false; }
    for (const slot of EQUIP_SLOTS) {
      if (section[slot] !== undefined && (typeof section[slot] !== 'object' || Array.isArray(section[slot]))) { return false; }
    }
    for (const key of OBJECT_ARRAYS) {
      const list = section[key];
      if (list === undefined) { continue; }
      if (!Array.isArray(list) || list.some(e => e == null || typeof e !== 'object' || Array.isArray(e))) { return false; }
    }
    for (const key of STRING_ARRAYS) {
      const list = section[key];
      if (list === undefined) { continue; }
      if (!Array.isArray(list) || list.some(e => typeof e !== 'string')) { return false; }
    }
  }
  return true;
}

function extraProps(entry, skip) {
  const out = {};
  for (const [k, v] of Object.entries(entry)) {
    if (!skip.includes(k)) { out[k] = v; }
  }
  return out;
}

export async function initHeroesEditor(content, ctx) {
  const data = await api.readModFile(ctx.mod, ctx.file);
  let obj;
  try {
    obj = JSON.parse(data.content);
  } catch (e) {
    const { jsonEditor } = await import('./text-editors.js');
    await jsonEditor(content, Object.assign({}, ctx, { content: data.content }));
    return null;
  }
  if (!sectionsShape(obj)) {
    const { jsonEditor } = await import('./text-editors.js');
    await jsonEditor(content, Object.assign({}, ctx, { content: data.content }));
    return null;
  }

  const barEl = document.createElement('div');
  barEl.className = 'editor-bar';
  const title = document.createElement('strong');
  title.textContent = 'Hero classes: ' + ctx.file;
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

  const itemKindList = document.createElement('datalist');
  itemKindList.id = 'heroItemList';
  content.appendChild(itemKindList);
  for (const builtin of [true, false]) {
    api.modFiles(ctx.mod, 'scripts/items', builtin).then(list => {
      for (const entry of list.entries) {
        if (entry.dir || !entry.name.toLowerCase().endsWith('.lua')) { continue; }
        const o = document.createElement('option');
        o.value = entry.name.replace(/\.lua$/, '');
        itemKindList.appendChild(o);
      }
    }).catch(() => {});
  }

  function sectionPanel(sectionKey) {
    const section = obj[sectionKey];
    const panelEl = document.createElement('div');
    panelEl.className = 'panel';
    panelEl.dataset.section = sectionKey;
    const h = document.createElement('h3');
    h.textContent = sectionKey;
    panelEl.appendChild(h);

    // scalars + checks - inputs are tagged with their key, save reads the tags
    const grid = document.createElement('div');
    grid.className = 'field-grid';
    for (const [key] of SCALARS) {
      const label = document.createElement('label');
      label.textContent = key;
      const input = document.createElement('input');
      input.dataset.scalar = key;
      if (key === 'magicAffinity') {
        input.value = typeof section[key] === 'string' ? section[key] : '';
      } else {
        input.type = 'number';
        input.value = Number.isFinite(section[key]) ? section[key] : '';
      }
      input.addEventListener('input', markDirty);
      grid.appendChild(label);
      grid.appendChild(input);
    }
    for (const [key] of CHECKS) {
      const label = document.createElement('label');
      label.textContent = key;
      const wrap = document.createElement('div');
      const input = document.createElement('input');
      input.type = 'checkbox';
      input.dataset.check = key;
      input.checked = section[key] === true;
      input.addEventListener('change', markDirty);
      wrap.appendChild(input);
      grid.appendChild(label);
      grid.appendChild(wrap);
    }
    panelEl.appendChild(grid);

    // equipment slots: kind · identified · level (empty kind drops the slot);
    // any other slot fields round-trip untouched
    const slotsBox = document.createElement('div');
    for (const slot of EQUIP_SLOTS) {
      const cur = section[slot];
      const row = document.createElement('div');
      row.className = 'field-grid';
      row.dataset.slot = slot;
      const label = document.createElement('label');
      label.textContent = slot;
      const kind = document.createElement('input');
      kind.setAttribute('list', 'heroItemList');
      kind.dataset.slotKind = '1';
      kind.placeholder = 'ItemKind (empty = none)';
      kind.value = cur && typeof cur.kind === 'string' ? cur.kind : '';
      const identified = document.createElement('input');
      identified.type = 'checkbox';
      identified.dataset.slotIdentified = '1';
      identified.checked = cur ? cur.identified === true : false;
      const level = document.createElement('input');
      level.type = 'number';
      level.dataset.slotLevel = '1';
      level.style.width = '5em';
      level.placeholder = 'lvl';
      level.value = cur && Number.isFinite(cur.level) ? cur.level : '';
      [kind, identified, level].forEach(i => i.addEventListener('input', markDirty));
      row.appendChild(label);
      row.appendChild(kind);
      row.appendChild(identified);
      row.appendChild(level);
      slotsBox.appendChild(row);
    }
    panelEl.appendChild(slotsBox);

    // object arrays: primary kind/spell select + value + inline json extras
    // (rendered even when absent, so users can add lists to a new section)
    for (const key of OBJECT_ARRAYS) {
      const box = document.createElement('div');
      box.dataset.entryList = key;
      const h2 = document.createElement('p');
      h2.textContent = key + ':';
      box.appendChild(h2);
      const rows = document.createElement('div');
      box.appendChild(rows);

      function addRow(entry) {
        const tr = document.createElement('div');
        tr.className = 'field-grid';
        tr.dataset.entryRow = '1';
        let primaryKey = entry && typeof entry.kind === 'string' ? 'kind'
          : entry && typeof entry.spell === 'string' ? 'spell' : 'kind';
        const select = document.createElement('select');
        select.dataset.entryPrimary = '1';
        for (const opt of ['kind', 'spell']) {
          const o = document.createElement('option');
          o.value = opt;
          o.textContent = opt;
          select.appendChild(o);
        }
        select.value = primaryKey;
        const value = document.createElement('input');
        value.setAttribute('list', 'heroItemList');
        value.dataset.entryValue = '1';
        value.placeholder = 'ItemKind / spell name';
        value.value = entry ? String(entry[primaryKey] || '') : '';
        const extra = document.createElement('input');
        extra.dataset.entryExtra = '1';
        extra.placeholder = '{} extra fields as json (quantity, level, identified…)';
        extra.value = JSON.stringify(extraProps(entry || {}, [primaryKey]));
        extra.style.minWidth = '18em';
        [select, value, extra].forEach(i => i.addEventListener('input', markDirty));
        const del = document.createElement('button');
        del.textContent = '×';
        del.addEventListener('click', () => { tr.remove(); markDirty(); });
        tr.appendChild(select);
        tr.appendChild(value);
        tr.appendChild(extra);
        tr.appendChild(del);
        rows.appendChild(tr);
      }

      for (const entry of section[key] || []) { addRow(entry); }
      const add = document.createElement('button');
      add.textContent = '+ ' + key.replace(/s$/, '') + ' entry';
      add.addEventListener('click', () => addRow(null));
      box.appendChild(add);
      panelEl.appendChild(box);
    }

    // string arrays (rendered when present only - they are plain keyword lists)
    for (const key of STRING_ARRAYS) {
      if (section[key] === undefined) { continue; }
      const box = document.createElement('div');
      box.dataset.stringList = key;
      const h2 = document.createElement('p');
      h2.textContent = key + ':';
      box.appendChild(h2);
      for (const value of section[key]) {
        const row = document.createElement('div');
        const input = document.createElement('input');
        input.dataset.stringValue = '1';
        input.value = value;
        input.addEventListener('input', markDirty);
        const del = document.createElement('button');
        del.textContent = '×';
        del.addEventListener('click', () => { row.remove(); markDirty(); });
        row.appendChild(input);
        row.appendChild(del);
        box.appendChild(row);
      }
      const add = document.createElement('button');
      add.textContent = '+ entry';
      add.addEventListener('click', () => {
        const row = document.createElement('div');
        const input = document.createElement('input');
        input.dataset.stringValue = '1';
        input.addEventListener('input', markDirty);
        const del = document.createElement('button');
        del.textContent = '×';
        del.addEventListener('click', () => { row.remove(); markDirty(); });
        row.appendChild(input);
        row.appendChild(del);
        box.insertBefore(row, add);
        markDirty();
      });
      box.appendChild(add);
      panelEl.appendChild(box);
    }

    const del = document.createElement('button');
    del.className = 'danger';
    del.textContent = 'delete section';
    del.addEventListener('click', () => { panelEl.remove(); markDirty(); });
    panelEl.appendChild(del);

    return panelEl;
  }

  const sectionsBox = document.createElement('div');
  content.appendChild(sectionsBox);
  for (const sectionKey of Object.keys(obj)) {
    sectionsBox.appendChild(sectionPanel(sectionKey));
  }

  const addSec = document.createElement('button');
  addSec.textContent = '+ class section';
  addSec.addEventListener('click', () => {
    const known = KNOWN_SECTIONS.filter(k => !(k in obj));
    const name = window.prompt('Section key (class name, common or non_expert):',
      known.length ? known[0] : 'WARRIOR');
    if (!name) { return; }
    obj[name] = {};
    sectionsBox.appendChild(sectionPanel(name));
    markDirty();
  });
  content.appendChild(addSec);

  // ---- save -------------------------------------------------------------
  save.addEventListener('click', async () => {
    try {
      for (const panelEl of sectionsBox.querySelectorAll('[data-section]')) {
        const sectionKey = panelEl.dataset.section;
        const section = obj[sectionKey];
        if (!section) { throw new Error('section ' + sectionKey + ' vanished'); }

        for (const input of panelEl.querySelectorAll('[data-scalar]')) {
          const key = input.dataset.scalar;
          const v = input.value.trim();
          if (v === '') {
            delete section[key];
          } else if (key === 'magicAffinity') {
            section[key] = v;
          } else {
            const n = Number(v);
            if (!Number.isFinite(n)) { throw new Error(sectionKey + '.' + key + ' is not a number'); }
            section[key] = n;
          }
        }
        for (const input of panelEl.querySelectorAll('[data-check]')) {
          if (input.checked) { section[input.dataset.check] = true; }
          else { delete section[input.dataset.check]; }
        }

        for (const row of panelEl.querySelectorAll('[data-slot]')) {
          const slot = row.dataset.slot;
          const kind = row.querySelector('[data-slot-kind]').value.trim();
          const identified = row.querySelector('[data-slot-identified]').checked;
          const level = row.querySelector('[data-slot-level]').value.trim();
          if (kind === '') {
            delete section[slot];
            continue;
          }
          // rebuild the slot entry: kind + identified + level + untouched extras
          const prev = section[slot] && typeof section[slot] === 'object' ? section[slot] : {};
          const entry = {};
          for (const [k, v] of Object.entries(prev)) {
            if (k !== 'kind' && k !== 'identified' && k !== 'level') { entry[k] = v; }
          }
          entry.kind = kind;
          if (identified) { entry.identified = true; }
          if (level !== '') {
            const n = Number(level);
            if (!Number.isFinite(n)) { throw new Error(sectionKey + '.' + slot + '.level is not a number'); }
            entry.level = n;
          }
          section[slot] = entry;
        }

        for (const box of panelEl.querySelectorAll('[data-entry-list]')) {
          const key = box.dataset.entryList;
          const next = [];
          for (const tr of box.querySelectorAll('[data-entry-row]')) {
            const primary = tr.querySelector('[data-entry-primary]').value;
            const value = tr.querySelector('[data-entry-value]').value.trim();
            const extraRaw = tr.querySelector('[data-entry-extra]').value.trim();
            if (!value && !extraRaw) { continue; }
            if (!value) { throw new Error(sectionKey + '.' + key + ': an entry has no kind/spell'); }
            let extra = {};
            if (extraRaw !== '') {
              try {
                extra = JSON.parse(extraRaw);
              } catch (e) {
                throw new Error(sectionKey + '.' + key + ': extra fields are not valid json');
              }
              if (extra == null || typeof extra !== 'object' || Array.isArray(extra)) {
                throw new Error(sectionKey + '.' + key + ': extra fields must be a json object');
              }
            }
            const entry = {};
            entry[primary] = value;
            Object.assign(entry, extra);
            next.push(entry);
          }
          // keep absent lists absent when the user added nothing
          if (next.length || section[key] !== undefined) { section[key] = next; }
          else { delete section[key]; }
        }

        for (const box of panelEl.querySelectorAll('[data-string-list]')) {
          const key = box.dataset.stringList;
          const next = [];
          for (const input of box.querySelectorAll('[data-string-value]')) {
            if (input.value.trim() !== '') { next.push(input.value.trim()); }
          }
          section[key] = next;
        }
      }

      // sections deleted from the dom
      const liveSections = Array.from(sectionsBox.querySelectorAll('[data-section]'))
        .map(p => p.dataset.section);
      for (const sectionKey of Object.keys(obj)) {
        if (!liveSections.includes(sectionKey)) { delete obj[sectionKey]; }
      }
    } catch (e) {
      status.className = 'status-err';
      status.textContent = e.message;
      return;
    }

    try {
      await api.saveModFile(ctx.mod, ctx.file, JSON.stringify(obj, null, 2) + '\n');
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
