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
  }
};

export function templateFor(path) {
  for (const prefix of Object.keys(TEMPLATES)) {
    if (path.startsWith(prefix)) {
      return TEMPLATES[prefix];
    }
  }
  return null;
}
