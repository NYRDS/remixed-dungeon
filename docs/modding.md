# Remixed Dungeon Modding Guide

## Overview

Remixed Dungeon features a comprehensive modding system built on Lua scripting that allows developers to extend and customize the game without modifying the core Java code. The scripting system provides APIs for customizing mobs, items, spells, traps, level objects, buffs, and more.

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
11. [Common Patterns](#common-patterns)
12. [API Reference](#api-reference)
13. [Best Practices](#best-practices)

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
- `spawn(mob, level)`: Called when mob is created
- `die(mob, cause)`: Called when mob dies
- `attackProc(mob, enemy, damage)`: Called after successful attack
- `defenceProc(mob, enemy, damage)`: Called after successful defense
- `zapProc(mob, enemy, damage)`: Called after successful ranged attack
- `zapMiss(mob, enemy)`: Called when ranged attack misses
- `interact(mob, chr)`: Called when player interacts with mob
- `move(mob, cell)`: Called when mob moves
- `act(mob)`: Called each turn for AI processing
- `damage(mob, dmg, src)`: Called when mob takes damage
- `selectCell(mob)`: Called when mob needs to select a cell
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