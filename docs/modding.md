# Remixed Dungeon Modding Guide

## Overview

Remixed Dungeon features a comprehensive modding system that allows developers to extend and customize the game without modifying the core Java code. The system uses two complementary approaches:

1. **Lua Scripting**: Dynamic behavior customization through Lua scripts
2. **JSON Configuration**: Static property definition through JSON files

These systems work together to provide maximum flexibility while maintaining compatibility with the base game.

## Table of Contents

1. [Scripting Architecture](#scripting-architecture)
2. [Mob Scripting](#mob-scripting)
3. [Custom AI Scripting](#custom-ai-scripting)
4. [Item Scripting](#item-scripting)
5. [Spell Scripting](#spell-scripting)
6. [Trap Scripting](#trap-scripting)
7. [Level Object Scripting](#level-object-scripting)
8. [Buff Scripting](#buff-scripting)
9. [Actor Scripting](#actor-scripting)
10. [Quest Scripting](#quest-scripting)
11. [JSON Configuration Files](#json-configuration-files)
12. [Common Patterns](#common-patterns)
13. [API Reference](#api-reference)
14. [Best Practices](#best-practices)

## Scripting Architecture

### Lua-Java Integration

The integration between Lua and Java is handled through:
- **LuaScript.java**: The main interface between Java game objects and Lua scripts
- **LuaEngine.java**: The core Lua execution environment

### Script Loading Mechanism

Scripts are loaded in two ways:
1. **Module Scripts**: Shared scripts loaded once and cached (using `require`)
2. **Instance Scripts**: Unique script instances for each object (using `dofile`)

### RPD Library

The `commonClasses.lua` file provides the `RPD` library, which exposes Java classes and methods to Lua scripts.

## Mob Scripting

### Purpose
Customize mob behavior, combat abilities, and interactions.

### Library
`mob.lua`

### Key Methods
- `onSpawn(mob, level)`: Called when mob is created
- `onDie(mob, cause)`: Called when mob dies
- `onAttackProc(mob, enemy, damage)`: Called after successful attack
- `onDefenceProc(mob, enemy, damage)`: Called after successful defense
- `onZapProc(mob, enemy, damage)`: Called after successful ranged attack
- `onZapMiss(mob, enemy)`: Called when ranged attack misses
- `onInteract(mob, chr)`: Called when player interacts with mob
- `onMove(mob, cell)`: Called when mob moves
- `onAct(mob)`: Called each turn for AI processing
- `onDamage(mob, dmg, src)`: Called when mob takes damage
- `onSelectCell(mob)`: Called when mob needs to select a cell
- `actionsList(mob, hero)`: Define custom actions available for the mob
- `executeAction(mob, hero, action)`: Execute a custom action
- `priceSell(mob, item, defaultPrice)`: Modify item selling price
- `priceBuy(mob, item, defaultPrice)`: Modify item buying price

### Data Management Methods
- `saveData()`: Serialize custom data for save games
- `loadData(_, str)`: Deserialize custom data from save games
- `storeData(data)`: Store temporary data
- `restoreData()`: Retrieve temporary data

### Example
```lua
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    attackProc = function(self, enemy, dmg)
        if math.random() > 0.25 then
            RPD.affectBuff(enemy, RPD.Buffs.Poison, 1)
        end
        return dmg
    end
}
```

## Custom AI Scripting

### Purpose
Create custom AI behaviors for mobs that go beyond the built-in AI states.

### Library
`ai.lua`

### Key Methods
- `act(self, ai, me)`: Called each turn for AI decision making
- `gotDamage(self, ai, me, src, dmg)`: Called when AI-controlled mob takes damage
- `status(self, ai, me)`: Return status text for the AI

### Example (BlackCat AI)
```lua
local RPD = require "scripts/lib/commonClasses"
local ai = require "scripts/lib/ai"

return ai.init{
    act = function(self, ai, me)
        local level = RPD.Dungeon.level
        
        -- Look for food items and interact with them
        local heaps = level:allHeaps()
        local iterator = heaps:iterator()
        
        while iterator:hasNext() do
            local heap = iterator:next()
            local itemPos = heap.pos
            
            if level.fieldOfView[itemPos] then -- visible heap
                local item = heap:peek()
                if item:getEntityKind() == "FriedFish" then
                    if RPD.Actor:findChar(itemPos) then
                        RPD.Wands.wandOfTelekinesis:mobWandUse(me, itemPos)
                    else
                        RPD.blinkTo(me, itemPos)
                    end
                    break
                end
            end
        end
        
        me:spend(1)
    end,
    
    status = function(self, ai, me)
        return "BlackCat_AiStatus"
    end
}
```

To use custom AI, set it in the mob's spawn method:
```lua
spawn = function(me, level)
    RPD.setAi(me, "BlackCat")  -- Name of the AI script file
end
```

## Item Scripting

### Purpose
Create custom items with unique behaviors.

### Library
`item.lua`

### Key Methods
- `desc()`: Define item properties (name, image, price, etc.)
- `execute(self, item, hero, action)`: Handle item actions (eating, equipping, etc.)
- `actions(self, item, hero)`: Define available actions for the item
- `onThrow(self, item, cell, thrower)`: Handle throwing behavior
- `burn(self, item, cell)`: Handle fire effects
- `freeze(self, item, cell)`: Handle freezing effects
- `poison(self, item, cell)`: Handle poison effects
- `act(self, item)`: Called each turn for active items
- `cellSelected(self, item, action, cell)`: Handle cell selection
- `activate(self, item, hero)`: Handle item activation (equipping)
- `deactivate(self, item, hero)`: Handle item deactivation (unequipping)
- `onPickUp(self, item, chr)`: Handle item pickup

### Data Management Methods
- `saveData()`: Serialize custom data for save games
- `loadData(_, str)`: Deserialize custom data from save games
- `storeData(data)`: Store temporary data
- `restoreData()`: Retrieve temporary data

### Example
```lua
local RPD = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"

return item.init{
    desc = function()
        return {
            image = 1,
            imageFile = "items/mastery_items.png",
            name = "TenguLiver_Name",
            info = "TenguLiver_Info",
            defaultAction = "Food_ACEat",
            price = 0
        }
    end,
    actions = function() 
        return {RPD.Actions.eat} 
    end,
    execute = function(self, item, hero, action)
        if action == RPD.Actions.eat then
            local wnd = luajava.newInstance(RPD.Objects.Ui.WndChooseWay, 
                hero, item, hero:getSubClassByName("GUARDIAN"), 
                hero:getSubClassByName("WITCHDOCTOR"))
            RPD.GameScene:show(wnd)
        end
    end,
    onPickUp = function(self, item, chr)
        RPD.Badges:validateMastery(chr:getHeroClass())
    end
}
```

## Spell Scripting

### Purpose
Create custom spells for magic systems.

### Library
`spell.lua`

### Key Methods
- `desc()`: Define spell properties (name, image, cost, etc.)
- `cast(self, spell, chr)`: Handle self-targeted spell casting
- `castOnCell(self, spell, chr, cell)`: Handle cell-targeted spell casting
- `castOnChar(self, spell, caster, target)`: Handle character-targeted spell casting

### Example
```lua
local RPD = require "scripts/lib/commonClasses"
local spell = require "scripts/lib/spell"

return spell.init{
    desc = function()
        return {
            image = 2,
            imageFile = "spellsIcons/witchcraft.png",
            name = "Heal_Name",
            info = "Heal_Info",
            magicAffinity = "Witchcraft",
            targetingType = "char",
            level = 2,
            castTime = 1,
            spellCost = 5,
            cooldown = 2
        }
    end,
    castOnChar = function(self, spell, caster, target)
        if target then
            local heal = target:ht() / 5. * caster:skillLevel()
            target:heal(math.max(1, heal), caster)
        end
        return true
    end
}
```

## Trap Scripting

### Purpose
Create custom traps with unique effects.

### Library
`trap.lua`

### Key Methods
- `trigger(self, cell, char)`: Handle trap activation
- `setData(self, data)`: Set trap data

### Example
```lua
local RPD = require "scripts/lib/commonClasses"
local trap = require "scripts/lib/trap"

return trap.init(
    function(cell, char, data)
        local mobs = {"Rat", "Gnoll"}
        local items = {"Dagger", "RatHide"}
        local objects = {"Barrel", "Sorrowmoss"}
        
        local level = RPD.Dungeon.level
        local pos = level:getEmptyCellNextTo(cell)
        local roll = math.random()
        
        if roll < 0.33 then
            local item = RPD.item(items[math.random(1, #items)], 1)
            item:upgrade()
            level:drop(item, pos)
        elseif roll < 0.66 then
            local mob = RPD.MobFactory:mobByName(mobs[math.random(1, #mobs)])
            mob:setPos(pos)
            level:spawnMob(mob)
        else
            RPD.levelObject(objects[math.random(1, #objects)], pos)
        end
    end
)
```

## Level Object Scripting

### Purpose
Create interactive level objects (chests, machines, etc.).

### Library
`object.lua`

### Key Methods
- `init(self, object, level, data)`: Initialize object when added to level
- `bump(self, object, presser)`: Handle character bumping into object
- `interact(self, object, char)`: Handle player interaction
- `burn(self, object, cell)`: Handle fire effects
- `freeze(self, object, cell)`: Handle freezing effects
- `actions(self, object, hero)`: Define available actions
- `addedToScene(self, object)`: Handle object being added to scene
- `textureFile(self, object, level)`: Define texture file
- `image(self, object, level)`: Define image index
- `interactive(self, object)`: Define if object is interactive
- `affectItems(self, object)`: Define if object affects items

### Example
```lua
local RPD = require "scripts/lib/commonClasses"
local object = require "scripts/lib/object"

return object.init{
    init = function(self, object, level, data)
        local pos = object:getPos()
        if level:blobAmountAt(RPD.Blobs.Alchemy, pos) > 0 then
            return
        end
        RPD.placeBlob(RPD.Blobs.Alchemy, pos, 1, level)
    end,
    bump = function(self, object, presser)
        local pos = object:getPos()
        if not presser.alchemyClass and presser.price then
            RPD.ItemUtils:throwItemAway(pos)
            return
        end
        RPD.Blobs.Alchemy:transmute(pos)
    end,
    interact = function(self, object, hero)
        local alchemyPot = luajava.bindClass("com.watabou.pixeldungeon.levels.features.AlchemyPot")
        alchemyPot:operate(hero, object:getPos())
        return true
    end
}
```

## Buff Scripting

### Purpose
Create custom character buffs/debuffs.

### Library
`buff.lua`

### Key Methods
- `desc()`: Define buff properties (name, icon, etc.)
- `attachTo(self, buff, target)`: Handle buff being applied to character
- `detach(self, buff)`: Handle buff being removed
- `act(self, buff)`: Called each turn for ongoing effects
- `setProperty(self, key, value)`: Set buff property
- `getProperty(self, key)`: Get buff property

### Example
```lua
local RPD = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc = function()
        return {
            icon = 46,
            name = "CloakBuff_Name",
            info = "CloakBuff_Info"
        }
    end,
    act = function(self, buff)
        buff:detach()
    end
}
```

## Actor Scripting

### Purpose
Create custom actors that can be added to the game's actor system.

### Library
`actor.lua`

### Key Methods
- `act()`: Called each turn for actor processing
- `actionTime()`: Define how long actions take
- `activate()`: Handle actor activation

### Example
```lua
local RPD = require "scripts/lib/commonClasses"
local actor = require "scripts/lib/actor"

return actor.init{
    act = function()
        local levelSize = RPD.Dungeon.level:getLength()
        local cell = math.random(levelSize) - 1
        if not RPD.Dungeon.level.solid[cell] then
            RPD.placeBlob(RPD.Blobs.Fire, cell, 10)
        end
        return true
    end,
    actionTime = function()
        return 1
    end,
    activate = function()
        local wnd = RPD.new(RPD.Objects.Ui.WndStory, "It gonna be hot here...")
        RPD.GameScene:show(wnd)
    end
}
```

## Quest Scripting

### Purpose
Manage quest states and conditions.

### Library
`quest.lua`

### Key Methods
- `give(name, chr, conditions)`: Start a quest for a character
- `complete(name)`: Mark a quest as completed
- `state(name, state)`: Get/set quest state
- `mobDied(mob, cause)`: Handle mob death for kill-based quests
- `isGiven(name)`: Check if quest is given
- `isCompleted(name)`: Check if quest is completed
- `debug(val)`: Enable/disable debug mode

### Example Usage
```lua
local quest = require "scripts/lib/quest"

-- Start a quest
quest.give("rat_hunt", hero, {kills = {"Rat"}})

-- Check if quest is completed
if quest.isCompleted("rat_hunt") then
    -- Reward player
end
```

## Item Selection

### Purpose
Allow Lua scripts to prompt the player to select an item from their backpack. This feature enables interactive gameplay where players can choose specific items to interact with through spells, NPC interactions, or other game mechanics.

### Library
`itemSelector.lua`

### Key Methods
- `selectItem(callback, mode, title, selector)`: Show item selection window with custom filtering
- `selectAnyItem(callback, title, selector)`: Select any item (equivalent to `selectItem` with `RPD.BackpackMode.ALL`)
- `selectUnidentifiedItem(callback, title, selector)`: Select unidentified items only
- `selectUpgradeableItem(callback, title, selector)`: Select upgradeable items only
- `selectWeapon(callback, title, selector)`: Select weapons only
- `selectArmor(callback, title, selector)`: Select armor only
- `selectWand(callback, title, selector)`: Select wands only
- `selectSeed(callback, title, selector)`: Select seeds only
- `selectArrow(callback, title, selector)`: Select arrows only

### Parameters
- `callback`: Function to call when an item is selected. The callback receives two parameters:
  - `item`: The selected item object, or `nil` if no item was selected
  - `selector`: The character that selected the item (usually the hero)
- `mode`: Selection mode that filters which items are shown (optional, defaults to `RPD.BackpackMode.ALL`)
- `title`: Window title displayed to the player (optional, defaults to "Select an item")
- `selector`: Character that is selecting the item (optional, defaults to `RPD.Dungeon.hero`)

### Available Selection Modes
- `RPD.BackpackMode.ALL`: Show all items
- `RPD.BackpackMode.UNIDENTIFED`: Show only unidentified items
- `RPD.BackpackMode.UPGRADEABLE`: Show only upgradeable items
- `RPD.BackpackMode.WEAPON`: Show only weapons
- `RPD.BackpackMode.ARMOR`: Show only armor
- `RPD.BackpackMode.WAND`: Show only wands
- `RPD.BackpackMode.SEED`: Show only seeds
- `RPD.BackpackMode.ARROWS`: Show only arrows

### Example Usage in Spells
```lua
local RPD = require "scripts/lib/commonClasses"
local itemSelector = require "scripts/lib/itemSelector"

-- Select any item from backpack
itemSelector.selectItem(function(item, selector)
    if item then
        RPD.glog("Selected item: " .. item:name())
        -- Perform actions with the selected item
        -- For example, identify it
        item:identify()
    else
        RPD.glog("No item selected")
    end
end, RPD.BackpackMode.ALL, "Choose an item to identify")

-- Select a weapon to upgrade
itemSelector.selectWeapon(function(item, selector)
    if item then
        RPD.glog("Upgrading " .. item:name() .. "!")
        -- Upgrade the weapon
        item:upgrade()
        item:level(item:level() + 1)
    end
end, "Choose a weapon to upgrade")
```

### Example Usage in NPC Interactions
```lua
local RPD = require "scripts/lib/commonClasses"
local itemSelector = require "scripts/lib/itemSelector"
local mob = require "scripts/lib/mob"

return mob.init({
    spawn = function(self, level)
        RPD.setAi(self, "NpcDefault")
    end,

    interact = function(self, chr)
        RPD.glog("Hello! I can help you identify items.")
        
        -- Show item selection window for unidentified items only
        itemSelector.selectUnidentifiedItem(function(item, selector)
            if item then
                RPD.glog("I've identified your " .. item:name() .. "!")
                item:identify()
                -- Play a visual effect
                RPD.zapEffect(self:getPos(), chr:getPos(), "Identification")
            else
                RPD.glog("You didn't select any item.")
            end
        end, "Select an item for me to identify")
    end
})
```

### Advanced Example: Curse Item Spell
Here's a complete example of how to create a spell that allows players to curse an item:

```lua
local RPD = require "scripts/lib/commonClasses"
local itemSelector = require "scripts/lib/itemSelector"
local spell = require "scripts/lib/spell"

return spell.init{
    desc = function()
        return {
            image = 0,
            imageFile = "spellsIcons/necromancy.png",
            name = "Curse Item",
            info = "Select an item from your backpack to curse it.",
            magicAffinity = "Necromancy",
            targetingType = "self",
            level = 1,
            castTime = 0,
            spellCost = 3,
            cooldown = 5
        }
    end,
    
    cast = function(self, spell, chr)
        -- Show item selection window
        itemSelector.selectItem(function(item, selector)
            if item then
                -- Check if the item is already cursed
                if item:isCursed() then
                    RPD.glog("That item is already cursed!")
                else
                    -- Curse the selected item
                    item:setCursed(true)
                    item:setCursedKnown(true)
                    RPD.glog("You have cursed your " .. item:name() .. "!")
                    
                    -- Apply a visual effect
                    RPD.zapEffect(chr:getPos(), chr:getPos(), "ShadowParticle")
                    
                    -- Play a sound effect
                    RPD.playSound("snd_cursed.mp3")
                end
            else
                RPD.glog("You decided not to curse anything.")
            end
        end, RPD.BackpackMode.ALL, "Select an item to curse")
        
        return true
    end
}
```

### Test Spell and Debug Configuration
A test spell called "CurseItem" is included in the debug configuration to demonstrate the item selection functionality. This spell can be found in `scripts/spells/CurseItem.lua` and is automatically added to the player's inventory when running in debug mode. The spell allows the player to select any item from their backpack and curse it.

To add this spell to a hero in debug mode, the following configuration is used in `initHeroesDebug.json`:

```json
{
  "common": {
    "items": [
      {
        "kind": "SpellBook",
        "identified": true,
        "spell": "CurseItem"
      }
    ]
  }
}
```

### Best Practices
1. Always validate that an item was selected before attempting to use it (check if `item` is not `nil`)
2. Provide clear titles to help players understand what they're selecting
3. Use appropriate selection modes to filter items and improve user experience
4. Give feedback to the player about what happened after they selected an item
5. Consider adding visual or audio effects to enhance the user experience
6. Handle edge cases such as already cursed, identified, or upgraded items

## JSON Configuration Files

### Purpose
JSON configuration files provide a declarative way to define game entities and their properties without writing Lua scripts. These files are used for mobs, levels, sprites, effects, and other game elements.

### Mob Configuration Files

Mob configuration files are located in `mobsDesc/` directory and define mob properties such as stats, behavior, and appearance.

#### Example (Bee.json):
```json
{
   "defenseSkill"  :10,
   "attackSkill"   :14,
   "exp"           :2,
   "maxLvl"        :10,
   "dmgMin"        :1,
   "dmgMax"        :3,
   "dr"            :1,
   "baseSpeed"     :1.5,
   "attackDelay"   :1,
   "ht"            :35,
   "viewDistance"  :4,
   "lootChance"    :0,
   "name"          :"Bee_Name",
   "name_objective":"Bee_Name_Objective",
   "description"   :"Bee_Desc",
   "gender"        :"Bee_Gender",
   "spriteDesc"    :"spritesDesc/Bee.json",
   "scriptFile"    :"scripts/mobs/Stinger",
   "walkingType"   :"NORMAL",
   "aiState"       :"Wandering",
   "canBePet"      : true,
   "flying"        : true,
   "friendly"      : false
}
```

#### Key Properties:
- `defenseSkill`: Mob's defense skill level
- `attackSkill`: Mob's attack skill level
- `exp`: Experience points awarded for killing this mob
- `maxLvl`: Maximum level this mob can reach
- `dmgMin`/`dmgMax`: Minimum and maximum damage dealt
- `dr`: Damage reduction
- `baseSpeed`: Base movement speed
- `attackDelay`: Delay between attacks
- `ht`: Health points
- `viewDistance`: How far the mob can see
- `lootChance`: Chance to drop loot
- `spriteDesc`: Path to sprite definition file
- `scriptFile`: Optional Lua script for custom behavior
- `walkingType`: Movement type (NORMAL, WALL, etc.)
- `aiState`: Initial AI state
- `canBePet`: Whether mob can be tamed as a pet
- `flying`: Whether mob can fly over obstacles
- `friendly`: Whether mob is initially friendly

### Sprite Configuration Files

Sprite configuration files are located in `spritesDesc/` directory and define how mobs and items are visually represented.

#### Example (Bee.json):
```json
{
  "texture" : "mobs/bee.png",
  "width"  : 16,
  "height" : 16,
  "idle"   : { "fps" : 12,  "looped" : true,     "frames" : [0,1,1,0,2,2] },
  "run"    : { "fps" : 15, "looped" : true,     "frames" : [0,1,1,0,2,2] },
  "attack" : { "fps" : 20, "looped" : false,    "frames" : [3,4,5,6] },
  "die"    : { "fps" : 20, "looped" : false,    "frames" : [7,8,9,10] },
  "bloodColor": "0xffd500",
  
  "eventHandlers": {
    "onComplete": [
      {
        "animation": "idle",
        "actions": [
          {
            "action": "emitParticles",
            "particleType": "Speck.WOOL",
            "count": 3
          },
          {
            "action": "playSound",
            "sound": "snd_evoke",
            "volume": 0.2
          }
        ]
      }
    ]
  }
}
```

#### Key Properties:
- `texture`: Path to the sprite texture file
- `width`/`height`: Dimensions of each frame
- `idle`/`run`/`attack`/`die`: Animation definitions
- `fps`: Frames per second for the animation
- `looped`: Whether the animation loops
- `frames`: Array of frame indices to use
- `bloodColor`: Color of blood particles when mob is damaged
- `eventHandlers`: Defines actions to execute when certain events occur
  - `onComplete`: Actions to execute when an animation completes
    - `animation`: The name of the animation that triggers this action
    - `actions`: List of actions to perform when the animation completes
      - `emitParticles`: Emit particle effects
        - `particleType`: Type of particles to emit (Speck.HEALING, Speck.STAR, etc.)
        - `count`: Number of particles to emit
      - `splash`: Create a splash effect
        - `color`: Color of the splash effect
        - `count`: Number of splash particles
      - `playSound`: Play a sound effect
        - `sound`: Name of the sound file to play
        - `volume`: Volume level (0.0 to 1.0)
      - `ripple`: Create a water ripple effect
        - No additional parameters needed
      - `cameraShake`: Shake the camera
        - `intensity`: Strength of the shake (default: 4)
        - `duration`: Duration of the shake in seconds (default: 0.2)
      - `killAndErase`: Remove the sprite immediately
        - No additional parameters needed
- `extras`: Additional animations beyond the standard ones
  - Format: `"animationName": { "fps": X, "looped": true/false, "frames": [...] }`
- `particleEmitters`: Define particle emitters that persist with the sprite
  - `type`: Type of emitter ("Emitter")
  - `particleType`: Type of particles to emit
  - `position`: Position offset for the emitter (x, y)
  - `autoKill`: Whether the emitter automatically stops when animation stops
- `alpha`: Set transparency level (0.0 = fully transparent, 1.0 = fully opaque)
- `blendMode`: Set OpenGL blending mode
  - `srcAlphaOne`: Blending mode that uses alpha as source and one as destination
  - `srcAlphaOneMinusAlpha`: Blending mode that uses alpha as source and one minus alpha as destination

### Level Configuration Files

Level configuration files define the structure of game levels, including level progression and connections.

#### Example (Dungeon.json - levels section):
```json
{
   "Levels":{
      "0":{"kind":"SewerLevel", "depth":0, "size":[0,0]},
      "1":{"kind":"SewerLevel", "depth":1, "size":[24,24], "music":"ost_sewer"},
      "5":{"kind":"SewerBossLevel", "depth":5, "size":[32,32], "music":"ost_boss_1_ambient"}
   }
}
```

#### Level Types:
- `SewerLevel`: Standard sewer level
- `SewerBossLevel`: Sewer boss level
- `PrisonLevel`: Standard prison level
- `PredesignedLevel`: Custom designed level from a JSON file
- And many others for different areas of the dungeon

### Level Object Configuration Files

Level object configuration files define interactive objects like chests, wells, and statues.

#### Example (pot.json):
```json
{
  "kind":"CustomObject",
  "script": "pot",
  "layer" : 0,
  "appearance": {
    "desc": {
      "desc": "Level_TileDescAlchemy",
      "name": "Level_TileAlchemy",
      "nonPassable" : true
    },
    "sprite":{
      "textureFile": "levelObjects/objects.png",
      "imageIndex" : 0
    }
  }
}
```

### Effect Configuration Files

Effect configuration files define visual effects like particle animations.

#### Example (cauldron_effect.json):
```json
{
  "texture" : "effects/cauldron_effect.png",
  "width"  : 16,
  "height" : 16,
  "anim"   : { "fps" : 15,  "looped" : true,    "frames" : [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15] }
}
```

### Hero Initialization Files

Hero initialization files define starting equipment and stats for different character classes.

#### Example (initHeroes.json):
```json
{
  "WARRIOR": {
    "weapon": {
      "kind": "ShortSword",
      "identified": true
    },
    "str": 11,
    "magicAffinity": "Combat"
  }
}
```

#### SpellBook Configuration

To give a hero a spellbook with a specific spell, use the following format:

```json
{
  "common": {
    "items": [
      {
        "kind": "SpellBook",
        "identified": true,
        "spell": "CurseItem"
      }
    ]
  }
}
```

Note that the `spell` field should contain just the name of the spell file without the path or `.lua` extension. The game will automatically look for the spell in the `scripts/spells/` directory.

##### Spell Categories

Spells are organized into categories in the `CustomSpellsList.lua` file. When creating new spells, you should add them to an appropriate category:

```lua
spells["Necromancy"] = {"RaiseDead","Exhumation", "DarkSacrifice","Possess"}
spells["Common"] = {"TownPortal","Heal","RaiseDead","Cloak","Calm","Charm"}
spells["Combat"] = {"DieHard","Dash","BodyArmor","Smash"}
spells["Rogue"] = {"Cloak","Backstab","KunaiThrow","Haste"}
spells["Witchcraft"] = {"Roar","LightningBolt","Heal","Order"}
spells["Huntress"] = {"Calm","Charm","ShootInEye","SummonBeast"}
spells["Elf"] = {"MagicArrow","Sprout","HideInGrass","NatureArmor"}
spells["Priest"] = {"Heal","Calm","Charm","Order"}
spells["PlagueDoctor"] = {"Heal","Calm","Charm","Order"}
spells["Dev"] = {"TestSpell", "CurseItem"}
```

The "Dev" category is specifically for development and testing spells like "CurseItem" that are not meant for regular gameplay.

### Treasury Files

Treasury files define what items can be found in shops and their probabilities.

#### Example (TownShopTreasury.json):
```json
{
  "FOOD": {
    "categoryChance": 10,
    "OverpricedRation" : 1,
    "FriedFish" : 0.05
  }
}
```

### Usage Patterns

JSON configurations are used in combination with Lua scripts:
1. **Pure JSON**: Simple mobs/objects with no custom behavior
2. **JSON + Lua**: Complex entities that use JSON for static properties and Lua for dynamic behavior
3. **Predesigned Levels**: Custom level maps defined entirely in JSON with embedded mob and object placements

## Common Patterns

### Data Persistence
All script types support data storage for maintaining state:
```lua
-- Store data
self:storeData({counter = 5})

-- Retrieve data
local data = self:restoreData()
```

### Random Effects
Many scripts use probability for varied outcomes:
```lua
if math.random() > 0.25 then
    -- Apply effect
end
```

### Resource Management
Scripts can create and manipulate game objects:
```lua
-- Spawn mob
local mob = RPD.MobFactory:mobByName("Rat")
mob:setPos(pos)
RPD.Dungeon.level:spawnMob(mob)

-- Create item
local item = RPD.createItem("Dagger")
```

### UI Integration
Scripts can show windows and interact with players:
```lua
local wnd = luajava.newInstance(RPD.Objects.Ui.WndChooseWay, hero, item, option1, option2)
RPD.GameScene:show(wnd)
```

## API Reference

### RPD Library Components

#### Game Systems
- `RPD.Dungeon`: Access to the current game state
- `RPD.GameScene`: Scene management and UI functions
- `RPD.MobFactory`: Mob creation and management
- `RPD.ItemFactory`: Item creation and management
- `RPD.Buffs`: Various buff classes for affecting characters
- `RPD.Blobs`: Environmental effects like gases and fires
- `RPD.Terrain`: Terrain type definitions

#### Utility Functions
- `RPD.affectBuff(chr, buffClass, duration)`: Apply a buff to a character
- `RPD.permanentBuff(chr, buffClass)`: Apply a permanent buff to a character
- `RPD.removeBuff(chr, buffClass)`: Remove a buff from a character
- `RPD.spawnMob(mobClass, cell, mobDesc)`: Create and spawn a new mob
- `RPD.createItem(itemClass, itemDesc)`: Create a new item
- `RPD.glog(text, ...)`: Log messages to the game log
- `RPD.playSound(sound)`: Play audio effects
- `RPD.setAi(mob, state)`: Change a mob's AI state
- `RPD.blinkTo(mob, target)`: Teleport mob to target location

#### UI and Effects
- `RPD.showQuestWindow(chr, text_id)`: Display quest-related dialogue
- `RPD.chooseOption(handler, title, text, ...)`: Present options to the player
- `RPD.zapEffect(from, to, zapEffect)`: Create visual effects for attacks
- `RPD.speckEffectFactory(particleType, evolutionType)`: Create particle effects

## Best Practices

1. **Use the Provided Libraries**: Leverage the RPD library rather than trying to access Java classes directly
2. **Handle Errors Gracefully**: Use optional method calls when behavior is not critical
3. **Store Data Appropriately**: Use the data storage methods for persistent state
4. **Follow Naming Conventions**: Use consistent naming for script files and methods
5. **Test Thoroughly**: Lua errors can break game functionality, so test scripts extensively
6. **Document Your Scripts**: Add comments to explain complex behaviors
7. **Use Conditional Logic**: Check conditions before applying effects
8. **Respect Game Balance**: Consider the impact of your mods on game difficulty

This scripting system provides a powerful yet accessible way to extend Remixed Dungeon with custom content, allowing modders to create complex behaviors while maintaining compatibility with the base game.