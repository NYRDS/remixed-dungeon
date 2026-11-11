// New-file content templates, modeled on real builtin scripts.

export const TEMPLATES = {
  'scripts/items/': {
    hint: 'custom item (scripts/lib/item)',
    content: `local RPD  = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"

return item.init{
    desc  = function ()
        return {
            image     = 0,
            imageFile = "items/food.png",
            name      = "MyItem_Name",
            info      = "MyItem_Info",
            stackable = true,
            price     = 10
        }
    end,
    actions = function() return {RPD.Actions.eat} end,
    execute = function(self, item, hero, action)
        if action == RPD.Actions.eat then
            hero:eat(item, RPD.Buffs.Hunger.HUNGRY / 4, "MyItem_Taste")
        end
    end
}
`
  },
  'scripts/buffs/': {
    hint: 'custom buff (scripts/lib/buff)',
    content: `local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon = -1,
            name = "MyBuff_Name",
            info = "MyBuff_Info",
        }
    end,

    hasteLevel = function(self, buff)
        return 1
    end
}
`
  },
  'scripts/mobs/': {
    hint: 'mob behavior script (scripts/lib/mob)',
    content: `local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    attackProc = function(self, enemy, dmg)
        return dmg
    end
}
`
  },
  'scripts/ai/': {
    hint: 'custom ai state machine (scripts/lib/ai)',
    content: `local ai = require "scripts/lib/ai"

return ai.init({
    act = function(self, mob)
        -- goal-directed behavior goes here; see scripts/lib/ai.lua
        return true
    end
})
`
  },
  'levelsDesc/': {
    hint: 'predesigned level (wire it into Dungeon.json Levels+Graph)',
    content: levelTemplate()
  }
};

function levelTemplate() {
  const w = 15, h = 15;
  const rows = [];
  for (let y = 0; y < h; y++) {
    const row = [];
    for (let x = 0; x < w; x++) {
      row.push(y === 0 || y === h - 1 || x === 0 || x === w - 1 ? 4 : 1);
    }
    rows.push(row);
  }
  // entrance bottom center, exit top center (terrain 7/8 under them)
  rows[13][7] = 7;
  rows[1][7] = 8;
  const level = {
    entrance: [7, 13],
    height: h,
    map: [].concat(...rows),
    multiexit: [[7, 1]],
    tiles: 'tilesets/tiles_sewers_xyz.png',
    width: w
  };
  return JSON.stringify(level, null, 1) + '\n';
}

export function templateFor(path) {
  for (const prefix of Object.keys(TEMPLATES)) {
    if (path.startsWith(prefix)) {
      return TEMPLATES[prefix];
    }
  }
  return null;
}
