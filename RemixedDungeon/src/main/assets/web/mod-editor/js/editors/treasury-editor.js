// Structured editor for levelsDesc/*Treasury.json loot tables.
// v0: categories at the top level, per-category "categoryChance" key.
// v1: {version:1, settings:{decayFactor, probabilities:{CAT:..}}, categories:{CAT:{item:prob}}}.
// Unknown keys survive a save (mutate the parsed object, never rebuild);
// level maps and files the browser cannot parse fall back to the text editor.

import * as api from '../api.js';

// gate: is this json a treasury table and can the form model all of it?
function treasuryShape(obj) {
  if (obj == null || typeof obj !== 'object' || Array.isArray(obj) || Array.isArray(obj.map)) {
    return null;
  }
  const version = obj.version === undefined ? 0 : obj.version;
  if (version !== 0 && version !== 1) { return null; }

  const catObjs = [];
  if (version === 1) {
    if (obj.categories == null || typeof obj.categories !== 'object' || Array.isArray(obj.categories)) { return null; }
    if (obj.settings == null || typeof obj.settings !== 'object' || Array.isArray(obj.settings)) { return null; }
    catObjs.push(...Object.values(obj.categories));
  } else {
    for (const [key, value] of Object.entries(obj)) {
      if (key === 'version') { continue; }
      if (value == null || typeof value !== 'object' || Array.isArray(value)) { return null; }
      catObjs.push(value);
    }
  }
  // every category entry must be a number (the engine getDouble()s them all;
  // non-numbers were the a9c92c38 crash family - edit those as text)
  for (const cat of catObjs) {
    for (const value of Object.values(cat)) {
      if (!Number.isFinite(value)) { return null; }
    }
  }
  return { version };
}

export async function treasuryEditor(content, ctx) {
  const data = await api.readModFile(ctx.mod, ctx.file);
  let obj;
  try {
    obj = JSON.parse(data.content);
  } catch (e) {
    const { jsonEditor } = await import('./text-editors.js');
    await jsonEditor(content, Object.assign({}, ctx, { content: data.content }));
    return null;
  }
  const shape = treasuryShape(obj);
  if (!shape) {
    const { jsonEditor } = await import('./text-editors.js');
    await jsonEditor(content, Object.assign({}, ctx, { content: data.content }));
    return null;
  }
  const version = shape.version;

  const barEl = document.createElement('div');
  barEl.className = 'editor-bar';
  const title = document.createElement('strong');
  title.textContent = 'Treasury: ' + ctx.file.split('/').pop() + ' (v' + version + ')';
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

  // where categories live for this version (mutated in place)
  const categories = version === 1 ? obj.categories : obj;

  // ---- v1 settings ------------------------------------------------------
  let probGrid = null;
  if (version === 1) {
    const panelEl = document.createElement('div');
    panelEl.className = 'panel';
    const h = document.createElement('h3');
    h.textContent = 'Settings';
    panelEl.appendChild(h);

    const decayRow = document.createElement('div');
    decayRow.className = 'hint';
    decayRow.textContent = 'decayFactor (how fast loot quality decays with depth; empty = 1)';
    panelEl.appendChild(decayRow);
    const decay = document.createElement('input');
    decay.type = 'number';
    decay.step = 'any';
    decay.dataset.treasuryDecay = '1';
    decay.value = Number.isFinite(obj.settings.decayFactor) ? obj.settings.decayFactor : '';
    decay.addEventListener('input', markDirty);
    panelEl.appendChild(decay);

    const ph = document.createElement('p');
    ph.className = 'hint';
    ph.textContent = 'Category probabilities (relative weights, 0 = never rolled):';
    panelEl.appendChild(ph);
    probGrid = document.createElement('div');
    probGrid.className = 'field-grid';
    panelEl.appendChild(probGrid);
    content.appendChild(panelEl);

    renderProbabilities(probGrid, obj, categories, markDirty);
  }

  // ---- categories -------------------------------------------------------
  const catsPanel = document.createElement('div');
  catsPanel.className = 'panel';
  const ch = document.createElement('h3');
  ch.textContent = 'Categories';
  catsPanel.appendChild(ch);
  const catsHint = document.createElement('p');
  catsHint.className = 'hint';
  catsHint.textContent = 'Each category maps item kinds to relative weights. Weight 0 keeps the kind ' +
    'listed but never drops. The level property "treasury" picks which table a level uses.';
  catsPanel.appendChild(catsHint);
  const catsBox = document.createElement('div');
  catsPanel.appendChild(catsBox);
  content.appendChild(catsPanel);

  const itemKindList = document.createElement('datalist');
  itemKindList.id = 'treasuryItemList';
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

  function renderCategory(catBox, cat) {
    const rows = document.createElement('table');
    rows.className = 'pairs';
    catBox.appendChild(rows);

    function addItemRow(name, value) {
      const tr = document.createElement('tr');
      const kindCell = document.createElement('td');
      const probCell = document.createElement('td');
      const kind = document.createElement('input');
      kind.value = name;
      kind.setAttribute('list', 'treasuryItemList');
      kind.placeholder = 'ItemKind';
      const prob = document.createElement('input');
      prob.type = 'number';
      prob.step = 'any';
      prob.value = value;
      kind.addEventListener('input', markDirty);
      prob.addEventListener('input', markDirty);
      kindCell.appendChild(kind);
      probCell.appendChild(prob);
      tr.appendChild(kindCell);
      tr.appendChild(probCell);
      const delCell = document.createElement('td');
      const del = document.createElement('button');
      del.textContent = '×';
      del.addEventListener('click', () => { tr.remove(); markDirty(); });
      delCell.appendChild(del);
      tr.appendChild(delCell);
      rows.appendChild(tr);
    }

    for (const [name, value] of Object.entries(cat)) {
      if (version === 0 && name === 'categoryChance') { continue; }
      addItemRow(name, value);
    }

    const add = document.createElement('button');
    add.textContent = '+ item';
    add.addEventListener('click', () => addItemRow('', 1));
    catBox.appendChild(add);

    return rows;
  }

  for (const catName of Object.keys(categories)) {
    const catBox = document.createElement('div');
    catBox.className = 'panel';
    const head = document.createElement('strong');
    head.textContent = catName;
    catBox.appendChild(head);
    catBox.appendChild(document.createTextNode(' '));

    if (version === 0) {
      const chanceLabel = document.createElement('span');
      chanceLabel.className = 'hint';
      chanceLabel.textContent = ' categoryChance:';
      catBox.appendChild(chanceLabel);
      const chance = document.createElement('input');
      chance.type = 'number';
      chance.step = 'any';
      chance.style.width = '6em';
      chance.value = Number.isFinite(categories[catName].categoryChance) ? categories[catName].categoryChance : '';
      chance.addEventListener('input', markDirty);
      chance.dataset.catChance = catName;
      catBox.appendChild(chance);
    }

    const del = document.createElement('button');
    del.textContent = 'delete category';
    del.className = 'danger';
    del.addEventListener('click', () => { catBox.remove(); markDirty(); });
    catBox.appendChild(del);

    catBox.dataset.category = catName;
    renderCategory(catBox, categories[catName]);
    catsBox.appendChild(catBox);
  }

  const addCat = document.createElement('button');
  addCat.textContent = '+ category';
  addCat.addEventListener('click', () => {
    const name = window.prompt('Category name (e.g. WEAPON, POTION, MY_LOOT):');
    if (!name) { return; }
    const cat = {};
    if (version === 0) { cat.categoryChance = 10; }
    // v1 also needs a probability entry - add one so the category can roll
    if (version === 1) {
      obj.settings.probabilities = obj.settings.probabilities || {};
      if (!(name in obj.settings.probabilities)) {
        obj.settings.probabilities[name] = 10;
        if (probGrid) { renderProbabilities(probGrid, obj, { [name]: cat }, markDirty); }
      }
    }
    categories[name] = cat;
    const catBox = document.createElement('div');
    catBox.className = 'panel';
    catBox.dataset.category = name;
    const head = document.createElement('strong');
    head.textContent = name;
    catBox.appendChild(head);
    const del = document.createElement('button');
    del.textContent = 'delete category';
    del.className = 'danger';
    del.addEventListener('click', () => { catBox.remove(); markDirty(); });
    catBox.appendChild(del);
    renderCategory(catBox, cat);
    catsBox.appendChild(catBox);
    markDirty();
  });
  content.appendChild(addCat);

  // ---- save -------------------------------------------------------------
  save.addEventListener('click', async () => {
    // rebuild each category from its rows; parse errors refuse the save
    const boxes = catsBox.querySelectorAll('[data-category]');
    try {
      for (const catBox of boxes) {
        const catName = catBox.dataset.category;
        const cat = categories[catName];
        if (!cat) { throw new Error('category ' + catName + ' vanished'); }
        const next = {};
        if (version === 0) {
          const chance = catBox.querySelector('[data-cat-chance]');
          const chanceVal = chance ? chance.value.trim() : '';
          if (chanceVal !== '') {
            const n = Number(chanceVal);
            if (!Number.isFinite(n)) { throw new Error(catName + ': categoryChance is not a number'); }
            next.categoryChance = n;
          }
        }
        for (const tr of catBox.querySelectorAll('table tr')) {
          const [kind, prob] = tr.querySelectorAll('input');
          const name = kind.value.trim();
          if (!name && !prob.value.trim()) { continue; }
          if (!name) { throw new Error(catName + ': an item row has no kind'); }
          const n = Number(prob.value);
          if (!Number.isFinite(n)) { throw new Error(catName + '/' + name + ': weight is not a number'); }
          next[name] = n;
        }
        // replace contents in place (keeps the object identity in obj)
        for (const k of Object.keys(cat)) { delete cat[k]; }
        Object.assign(cat, next);
      }

      if (version === 1) {
        // decayFactor: empty omits the key (engine default), NaN never saved
        const decayInput = content.querySelector('[data-treasury-decay]');
        const v = decayInput ? decayInput.value.trim() : '';
        if (v === '') {
          delete obj.settings.decayFactor;
        } else {
          const n = Number(v);
          if (!Number.isFinite(n)) { throw new Error('decayFactor is not a number'); }
          obj.settings.decayFactor = n;
        }
        // probabilities: read back from the settings grid inputs
        for (const input of content.querySelectorAll('[data-prob-cat]')) {
          const n = Number(input.value);
          if (!Number.isFinite(n)) { throw new Error(input.dataset.probCat + ' probability is not a number'); }
          obj.settings.probabilities[input.dataset.probCat] = n;
        }
        // categories added at runtime need a probability entry to ever roll
        for (const catBox of boxes) {
          const catName = catBox.dataset.category;
          obj.settings.probabilities = obj.settings.probabilities || {};
          if (!(catName in obj.settings.probabilities)) {
            obj.settings.probabilities[catName] = 0;
          }
        }
      } else {
        // v0: drop categories that were deleted from the dom
        for (const catName of Object.keys(categories)) {
          if (!Array.from(boxes).some(b => b.dataset.category === catName)) {
            delete categories[catName];
          }
        }
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

// v1 settings.probabilities editor - one number input per known category,
// keyed by data-prob-cat so save can find them back
function renderProbabilities(grid, obj, categories, markDirty) {
  function probRow(catName) {
    const label = document.createElement('label');
    label.textContent = catName;
    const input = document.createElement('input');
    input.type = 'number';
    input.step = 'any';
    input.dataset.probCat = catName;
    const current = obj.settings.probabilities ? obj.settings.probabilities[catName] : undefined;
    input.value = Number.isFinite(current) ? current : 0;
    input.addEventListener('input', markDirty);
    grid.appendChild(label);
    grid.appendChild(input);
  }
  for (const catName of Object.keys(categories)) { probRow(catName); }
}
