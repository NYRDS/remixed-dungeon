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
  'scripts/spells/': {
    hint: 'custom spell (scripts/lib/spell)',
    content: `local RPD   = require "scripts/lib/commonClasses"
local spell = require "scripts/lib/spell"

return spell.init{
    desc  = function()
        return {
            image         = 0,
            imageFile     = "spellsIcons/common.png",
            name          = "MySpell_Name",
            info          = "MySpell_Info",
            magicAffinity = "Common",
            targetingType = "self",
            level         = 1,
            castTime      = 1,
            spellCost     = 5,
            cooldown      = 10
        }
    end,

    cast = function(self, spell, chr)
        RPD.glog("Nothing happens... yet.")
        return true
    end
}
`
  },
  'scripts/traps/': {
    hint: 'custom trap (scripts/lib/trap)',
    content: `local RPD  = require "scripts/lib/commonClasses"
local trap = require "scripts/lib/trap"

return trap.init(
    function(cell, char, data)
        local level = RPD.Dungeon.level
        local pos = level:getEmptyCellNextTo(cell)
        RPD.placeBlob(RPD.Blobs.Fire, pos, 5)
    end
)
`
  },
  'scripts/objects/': {
    hint: 'level object behavior (scripts/lib/object)',
    content: `local RPD    = require "scripts/lib/commonClasses"
local object = require "scripts/lib/object"

return object.init{
    bump = function(self, object, presser)
        RPD.glog("Bumped!")
        return true
    end
}
`
  },
  'scripts/npc/': {
    hint: 'npc behavior (scripts/lib/mob + RPD.setAi "NpcDefault")',
    content: `local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    spawn = function(self, level)
        RPD.setAi(self, "NpcDefault")
    end,

    interact = function(self, chr)
        RPD.glog("Hello, traveler!")
    end
}
`
  },
  'scripts/actors/': {
    hint: 'custom actor (scripts/lib/actor)',
    content: `local RPD   = require "scripts/lib/commonClasses"
local actor = require "scripts/lib/actor"

return actor.init{
    act = function()
        return true
    end,
    actionTime = function()
        return 1
    end
}
`
  },
  'levelsDesc/': {
    hint: 'predesigned level (wire it into Dungeon.json Levels+Graph)',
    content: levelTemplate()
  },
  'mobsDesc/': {
    hint: 'mob descriptor (stats + spriteDesc + optional scriptFile)',
    content: JSON.stringify({
      defenseSkill: 10,
      attackSkill: 14,
      exp: 2,
      maxLvl: 10,
      dmgMin: 1,
      dmgMax: 3,
      dr: 1,
      baseSpeed: 1,
      attackDelay: 1,
      ht: 25,
      viewDistance: 6,
      lootChance: 0,
      name: 'MyMob_Name',
      name_objective: 'MyMob_Name_Objective',
      description: 'MyMob_Desc',
      gender: 'MyMob_Gender',
      spriteDesc: 'spritesDesc/MyMob.json',
      scriptFile: 'scripts/mobs/MyMob',
      walkingType: 'NORMAL',
      aiState: 'Wandering',
      canBePet: false,
      flying: false,
      friendly: false
    }, null, 3) + '\n'
  },
  'spritesDesc/': {
    hint: 'sprite descriptor (texture + animations; pair with a png sheet)',
    content: JSON.stringify({
      texture: 'mobs/my_mob.png',
      width: 16,
      height: 16,
      idle: { fps: 2, looped: true, frames: [0, 1] },
      run: { fps: 14, looped: true, frames: [2, 3] },
      attack: { fps: 11, looped: false, frames: [4, 5, 6] },
      die: { fps: 11, looped: false, frames: [7, 8, 9] }
    }, null, 3) + '\n'
  },
  'levelObjects/': {
    hint: 'level object descriptor (pair with scripts/objects/)',
    content: JSON.stringify({
      kind: 'CustomObject',
      script: 'my_object',
      layer: 0,
      appearance: {
        desc: { name: 'MyObject_Name', desc: 'MyObject_Desc', nonPassable: true },
        sprite: { textureFile: 'levelObjects/objects.png', imageIndex: 0 }
      }
    }, null, 3) + '\n'
  },
  'effects/': {
    hint: 'animated effect (texture + frame size + anim)',
    content: JSON.stringify({
      texture: 'effects/my_effect.png',
      width: 16,
      height: 16,
      anim: { fps: 15, looped: true, frames: [0, 1, 2, 3] }
    }, null, 3) + '\n'
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
