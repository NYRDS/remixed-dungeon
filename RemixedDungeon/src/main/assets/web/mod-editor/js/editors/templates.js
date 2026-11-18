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
  'scripts/plants/': {
    hint: 'plant behavior (scripts/lib/plant; sprite frame goes to plantsDesc/<Name>.json)',
    content: `local RPD   = require "scripts/lib/commonClasses"
local plant = require "scripts/lib/plant"

return plant.init{
    effect = function(self, plantObject, pos, presser, activator)
        -- the char (or null) that stepped on the seed
        if presser ~= nil and RPD.CharUtils:isChar(presser) then
            -- e.g. RPD.CharUtils:buff(presser, RPD.Buffs.Poison, 5)
        end
    end
}
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
  'scripts/quests/': {
    hint: 'quest-giver npc (scripts/lib/quest; give on interact, count kills, reward + complete) - modeled on scripts/npc/QuestGiverDemo.lua',
    content: `local RPD   = require "scripts/lib/commonClasses"
local mob   = require "scripts/lib/mob"
local quest = require "scripts/lib/quest"

-- quest state survives save/load via game storage; the name is free-form
local questName = "My Quest"
local targetKind = "BlackRat" -- mob kind to hunt (mobsDesc/<Kind>.json)
local killsNeeded = 5

return mob.init({
    interact = function(self, chr)
        if not quest.isGiven(questName) then
            self:say("Kill " .. targetKind .. "s and a reward follows!")
            quest.give(questName, chr, {kills = {targetKind}})
            return
        end

        if quest.isCompleted(questName) then
            self:say("Thanks again!")
            return
        end

        -- kills are counted per kind by quest.mobDied on every mob death
        local kills = quest.state(questName).kills[targetKind] or 0
        if kills < killsNeeded then
            self:say(kills .. " killed so far, keep going!")
        else
            self:say("Great work! Here is your reward!")
            RPD.Dungeon.level:drop(RPD.ItemFactory:itemByName("Gold"), chr:getPos())
            quest.complete(questName)
        end
    end,

    spawn = function(self, level)
        level:setCompassTarget(self:getPos())
    end
})
`
  },
  'levelsDesc/': {
    hint: 'predesigned level (wire it into Dungeon.json Levels+Graph)',
    content: levelTemplate()
  },
  'mobsDesc/': {
    hint: 'mob descriptor (stats + spriteDesc; strings keys derive from the file name)',
    // every authored key derives from the file base name - a hardcoded MyMob
    // shipped raw keys and a scriptFile validation error on every fresh mob
    // (bd 53c); scriptless mobs are valid, so no scriptFile at all
    dynamic: path => {
      const kind = path.split('/').pop().replace(/\.json$/i, '');
      return JSON.stringify({
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
        name: kind + '_Name',
        name_objective: kind + '_Name_Objective',
        description: kind + '_Desc',
        spriteDesc: 'spritesDesc/' + kind + '.json',
        walkingType: 'NORMAL',
        aiState: 'Wandering',
        canBePet: false,
        flying: false,
        friendly: false
      }, null, 3) + '\n';
    }
  },
  'spritesDesc/': {
    hint: 'sprite descriptor (texture + animations; pair with a png sheet)',
    dynamic: path => {
      const kind = path.split('/').pop().replace(/\.json$/i, '');
      return JSON.stringify({
        texture: 'mobs/' + snakeCase(kind) + '.png',
        width: 16,
        height: 16,
        idle: { fps: 2, looped: true, frames: [0, 1] },
        run: { fps: 14, looped: true, frames: [2, 3] },
        attack: { fps: 11, looped: false, frames: [4, 5, 6] },
        die: { fps: 11, looped: false, frames: [7, 8, 9] }
      }, null, 3) + '\n';
    }
  },
  'hero/spritesDesc/': {
    hint: 'hero sprite descriptor (same sheet shape as spritesDesc/)',
    dynamic: path => TEMPLATES['spritesDesc/'].dynamic(path)
  },
  'levelObjects/': {
    hint: 'level object descriptor (pair with scripts/objects/)',
    dynamic: path => {
      const kind = path.split('/').pop().replace(/\.json$/i, '');
      return JSON.stringify({
        kind: kind,
        script: snakeCase(kind),
        layer: 0,
        appearance: {
          desc: { name: kind + '_Name', desc: kind + '_Desc', nonPassable: true },
          sprite: { textureFile: 'levelObjects/objects.png', imageIndex: 0 }
        }
      }, null, 3) + '\n';
    }
  },
  'effects/': {
    hint: 'animated effect (texture + frame size + anim)',
    dynamic: path => {
      const kind = path.split('/').pop().replace(/\.json$/i, '');
      return JSON.stringify({
        texture: 'effects/' + snakeCase(kind) + '.png',
        width: 16,
        height: 16,
        anim: { fps: 15, looped: true, frames: [0, 1, 2, 3] }
      }, null, 3) + '\n';
    }
  },
  'plantsDesc/': {
    hint: 'plant sprite-frame override (behavior goes to scripts/plants/)',
    content: JSON.stringify({ imageIndex: 0 }, null, 3) + '\n'
  }
};

// treasury files wear a levelsDesc/ address but are loot tables, not maps
const TREASURY_TEMPLATE = {
  hint: 'loot table (v1: categories + settings)',
  content: JSON.stringify({
    version: 1,
    settings: {
      decayFactor: 1,
      probabilities: { POTION: 50, GOLD: 50 }
    },
    categories: {
      POTION: { PotionOfHealing: 1 },
      GOLD: { Gold: 1 }
    }
  }, null, 3) + '\n'
};

const BESTIARY_TEMPLATE = {
  hint: 'spawn table (level kind -> depth -> mob weights)',
  content: JSON.stringify({
    SewerLevel: {
      any: { Rat: 1 }
    }
  }, null, 3) + '\n'
};

// CamelKind -> camel_kind, matching the builtin texture naming convention
function snakeCase(kind) {
  return kind.replace(/([a-z0-9])([A-Z])/g, '$1_$2').toLowerCase();
}

// shape: AlchemyRecipes.loadRecipesFromJson - {recipes:[{input:[{name,count}], outputs:[{name,count}]}]}
const ALCHEMY_TEMPLATE = {
  hint: 'alchemy pot recipes (ingredient kinds + counts -> product kinds)',
  content: JSON.stringify({
    recipes: [
      {
        input: [
          { name: 'RottenMeat', count: 3 },
          { name: 'VileEssence', count: 1 }
        ],
        outputs: [
          { name: 'PotionOfToxicGas', count: 1 }
        ]
      }
    ]
  }, null, 3) + '\n'
};

// per-item sprite map - starts empty; the form adds {kind: {file, index}} rows
const ITEM_SPRITES_TEMPLATE = {
  hint: 'per-item sprite overrides (kind -> {file, index, fliesStraight, fliesFastRotating})',
  content: '{}\n'
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
  // name-aware picks first: these files share a directory with level maps
  // but are structured tables
  if (/^levelsDesc\/.*[Tt]reasury\.json$/.test(path)) { return TREASURY_TEMPLATE; }
  if (/^levelsDesc\/Bestiary\.json$/.test(path)) { return BESTIARY_TEMPLATE; }
  if (path === 'scripts/alchemy_recipes.json') { return ALCHEMY_TEMPLATE; }
  // exact-name picks BEFORE prefix matching: these wear a spritesDesc/ address
  // but are not sheet descriptors (the prefix template would seed animations)
  if (path === 'spritesDesc/items.json') { return ITEM_SPRITES_TEMPLATE; }
  if (path === 'hero/spritesDesc/items.json') { return ITEM_SPRITES_TEMPLATE; }
  for (const prefix of Object.keys(TEMPLATES)) {
    if (path.startsWith(prefix)) {
      const t = TEMPLATES[prefix];
      // name-derived templates (bd 53c): content is a function of the path
      return t.dynamic ? { hint: t.hint, content: t.dynamic(path) } : t;
    }
  }
  return null;
}
