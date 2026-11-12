// Structured editor for scripts/items/*.lua (CustomItem via scripts/lib/item).
// Thin config over the shared lua desc-table editor: parses the desc()
// table, edits the known CustomItem fields as a form, rewrites ONLY the
// table body. Non-standard shapes fall back to the plain lua editor.

import { descTableEditor, parseDescTable, serializeDescTable } from './lua-desc-table.js';
import * as api from '../api.js';

// item.defaultDesc() from scripts/lib/item.lua - shown as the blank-state
// values so the form never invents semantics
const ITEM_FIELDS = [
  ['name', 'name key', 'text'],
  ['info', 'info key', 'text'],
  ['image', 'image index', 'number'],
  ['imageFile', 'imageFile png', 'text'],
  ['price', 'price', 'number'],
  ['defaultAction', 'defaultAction', 'text'],
  ['stackable', 'stackable', 'check'],
  ['upgradable', 'upgradable', 'check'],
  ['identified', 'identified', 'check'],
  ['equipable', 'equipable slot', 'text'],
];

const DEFAULTS = {
  image: 14, imageFile: 'items/food.png', name: 'smth', info: 'smth',
  stackable: false, upgradable: false, identified: true,
  defaultAction: 'Item_ACThrow', price: 0, equipable: ''
};

function imageSheetOptions(ctx) {
  return async () => {
    const options = [];
    for (const builtin of [true, false]) {
      try {
        const list = await api.modFiles(ctx.mod, 'items', builtin);
        for (const entry of list.entries) {
          if (entry.dir || !entry.name.toLowerCase().endsWith('.png')) { continue; }
          options.push('items/' + entry.name);
        }
      } catch (e) { /* optional */ }
    }
    try {
      const list = await api.modFiles(ctx.mod, '');
      for (const entry of list.entries) {
        if (entry.dir || !entry.name.toLowerCase().endsWith('.png')) { continue; }
        options.push(entry.name);
      }
    } catch (e) { /* optional */ }
    return options;
  };
}

export function parseItemDesc(luaText) {
  return parseDescTable(luaText);
}

export function serializeItemDesc(parsed, values) {
  return serializeDescTable(parsed, values, ITEM_FIELDS);
}

export async function itemEditor(content, ctx) {
  return descTableEditor(content, ctx, {
    kindLabel: 'Item',
    fields: ITEM_FIELDS,
    defaults: DEFAULTS,
    datalists: { imageFile: 'itemImageList', defaultAction: 'itemActionList' },
    datalistData: async key => {
      if (key === 'imageFile') { return imageSheetOptions(ctx)(); }
      return ['Item_ACThrow', 'Food_ACEat', 'Item_ACDrop', 'Item_ACPickUp', 'AC_EQUIP'];
    },
    icon: { frame: 16, imageKey: 'image', imageFileKey: 'imageFile' }
  });
}
