// Structured editor for scripts/buffs/*.lua (CustomBuff via
// scripts/lib/buff). Config over the shared lua desc-table editor; the
// attach/detach/act logic below the desc table is never touched.

import { descTableEditor } from './lua-desc-table.js';

// buff.defaultDesc() from scripts/lib/buff.lua
const BUFF_FIELDS = [
  ['name', 'name key', 'text'],
  ['info', 'info key', 'text'],
  ['icon', 'icon index', 'number'],
];

const DEFAULTS = {
  icon: 0, name: 'custom buff', info: 'unconfigured custom buff'
};

export async function buffEditor(content, ctx) {
  return descTableEditor(content, ctx, {
    kindLabel: 'Buff',
    fields: BUFF_FIELDS,
    defaults: DEFAULTS
  });
}
