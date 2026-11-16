// Structured editor for plantsDesc/<Kind>.json - the sprite frame override
// for a data-defined plant (Plant.applyDef reads imageIndex; behavior lives
// in scripts/plants/<Kind>.lua). Unknown keys survive a save.

import * as api from '../api.js';
import { iconPicker } from './lua-desc-table.js';

export async function plantsEditor(content, ctx) {
  const data = await api.readModFile(ctx.mod, ctx.file);
  let obj;
  try {
    obj = JSON.parse(data.content);
  } catch (e) {
    const { jsonEditor } = await import('./text-editors.js');
    await jsonEditor(content, Object.assign({}, ctx, { content: data.content }));
    return null;
  }
  if (obj == null || typeof obj !== 'object' || Array.isArray(obj)
    || (obj.imageIndex !== undefined && !Number.isFinite(obj.imageIndex))) {
    const { jsonEditor } = await import('./text-editors.js');
    await jsonEditor(content, Object.assign({}, ctx, { content: data.content }));
    return null;
  }

  const kind = ctx.file.split('/').pop().replace(/\.json$/, '');
  const barEl = document.createElement('div');
  barEl.className = 'editor-bar';
  const title = document.createElement('strong');
  title.textContent = 'Plant: ' + kind;
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
  h.textContent = 'Plant descriptor';
  panelEl.appendChild(h);
  const grid = document.createElement('div');
  grid.className = 'field-grid';
  panelEl.appendChild(grid);
  content.appendChild(panelEl);

  let dirty = false;
  const markDirty = () => { dirty = true; };

  const label = document.createElement('label');
  label.textContent = 'imageIndex (frame in plants.png)';
  const input = document.createElement('input');
  input.type = 'number';
  input.value = Number.isFinite(obj.imageIndex) ? obj.imageIndex : 0;
  input.addEventListener('input', markDirty);
  grid.appendChild(label);
  grid.appendChild(input);

  const hint = document.createElement('p');
  hint.className = 'hint';
  hint.textContent = 'Behavior lives in scripts/plants/' + kind + '.lua (new-file template there); ' +
    'name/desc come from strings keys ' + kind + '_Name / ' + kind + '_Desc.';
  panelEl.appendChild(hint);

  // the sheet is fixed - a stand-in "input" satisfies the shared picker
  const fakeFileInput = {
    value: 'plants.png',
    addEventListener: () => {}
  };
  iconPicker(content, ctx, { imageIndex: input, imageFile: fakeFileInput }, {
    frame: 16,
    imageKey: 'imageIndex',
    imageFileKey: 'imageFile',
    title: 'Frame in plants.png (click to pick)',
    onPicked: markDirty
  });

  // API reference pane (form has no textarea - reference only); the behavior
  // template lives in templates.js and is offered by "+ new file" in scripts/plants/
  const { renderLuaHelp } = await import('./text-editors.js');
  renderLuaHelp(content, ctx, { snippets: false });

  save.addEventListener('click', async () => {
    const v = input.value.trim();
    if (v !== '') {
      const n = Number(v);
      if (!Number.isFinite(n)) {
        status.className = 'status-err';
        status.textContent = 'imageIndex is not a number';
        return;
      }
      obj.imageIndex = n;
    } else {
      delete obj.imageIndex;
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
