// Structured editor for scripts/spells/*.lua (CustomSpell via
// scripts/lib/spell). Config over the shared lua desc-table editor; the
// cast/castOnCell/... logic below the desc table is never touched.

import { descTableEditor } from './lua-desc-table.js';
import * as api from '../api.js';

// spell.defaultDesc() from scripts/lib/spell.lua
const SPELL_FIELDS = [
  ['name', 'name key', 'text'],
  ['info', 'info key', 'text'],
  ['image', 'image index', 'number'],
  ['imageFile', 'imageFile png', 'text'],
  ['magicAffinity', 'magicAffinity', 'text'],
  ['targetingType', 'targetingType', 'text'],
  ['level', 'level', 'number'],
  ['castTime', 'castTime', 'number'],
  ['spellCost', 'spellCost (MP)', 'number'],
  ['cooldown', 'cooldown', 'number'],
];

const DEFAULTS = {
  image: 0, imageFile: 'spellsIcons/common.png', name: 'custom spell',
  info: 'unconfigured custom spell', magicAffinity: 'Common',
  targetingType: 'self', cooldown: 10, spellCost: 5, level: 1, castTime: 1
};

export async function spellEditor(content, ctx) {
  return descTableEditor(content, ctx, {
    kindLabel: 'Spell',
    fields: SPELL_FIELDS,
    defaults: DEFAULTS,
    datalists: {
      imageFile: 'spellImageList',
      magicAffinity: 'spellAffinityList',
      targetingType: 'spellTargetingList'
    },
    datalistData: async key => {
      if (key === 'imageFile') {
        const options = [];
        for (const builtin of [true, false]) {
          try {
            const list = await api.modFiles(ctx.mod, 'spellsIcons', builtin);
            for (const entry of list.entries) {
              if (entry.dir || !entry.name.toLowerCase().endsWith('.png')) { continue; }
              options.push('spellsIcons/' + entry.name);
            }
          } catch (e) { /* optional */ }
        }
        return options;
      }
      if (key === 'magicAffinity') {
        // CustomSpellsList.lua categories
        return ['Common', 'Combat', 'Necromancy', 'Witchcraft', 'Rogue',
          'Huntress', 'Elf', 'Priest', 'PlagueDoctor'];
      }
      return ['self', 'cell', 'char'];
    },
    icon: { frame: 16, imageKey: 'image', imageFileKey: 'imageFile' }
  });
}
