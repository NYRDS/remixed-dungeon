---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by mike.
--- DateTime: 12/23/20 10:01 PM
---

local RPD = require "scripts.lib.commonClasses"
local lru = require "scripts.lib.lru"

local explorationCache = lru.new(10);

local ai = {}

local function handleWindow(hero)
    local activeWindow = RPD.RemixedDungeon:scene():getWindow(0)

    if not activeWindow then
        return false
    end

    local wndClass = tostring(activeWindow:getClass())

    if wndClass:match('CellSelector') then
        --RPD.debug("cell selector: %s", wndClass)
        activeWindow = RPD.RemixedDungeon:scene():getWindow(1)
        if not activeWindow then
            return false
        end
        wndClass = tostring(activeWindow:getClass())
    end

    RPD.debug("wnd: %s", wndClass)

    if wndClass:match('WndChar') then
        local target = activeWindow:getTarget()
        local action = RPD.CharUtils:randomAction(target, hero)
        RPD.CharUtils:execute(target, hero, action)
        activeWindow:hide()
        return true
    end

    if wndClass:match('WndStepOnTrap') then
        activeWindow:onSelect(0)
        activeWindow:hide()
        return true
    end

    if wndClass:match('WndItem') then
        activeWindow:onSelect(0)
        activeWindow:hide()
        return true
    end

    if wndClass:match('WndChasmJump') then
        if math.random() < 0.05 then
            activeWindow:onSelect(0)
        else
            activeWindow:onSelect(1)
        end
        return true
    end

    if wndClass:match('Potion') then
        if math.random() < 0.5 then
            activeWindow:onSelect(0)
        else
            activeWindow:onSelect(1)
        end
        activeWindow:hide()
        return true
    end

    if wndClass:match('CellSelectorToast') then
        cell = hero:level():randomDestination()
        RPD.debug("wnd toast: %s %d", wndClass, cell)

        RPD.GameScene:handleCell(cell)
        hero:readyAndIdle()

        return true
    end

    if wndClass:match('WndQuest') then
        activeWindow:hide()
        return true
    end

    if wndClass:match("WndShopOptions") then
        activeWindow:hide()
        return true
    end

    if wndClass:match('WndBag') then
        activeWindow:hide()
        return true
    end

    if wndClass:match('WndStory') then
        activeWindow:hide()
        return true
    end

    if wndClass:match('com.watabou.pixeldungeon.scenes.DefaultCellListener') then
        activeWindow:hide()
        return false
    end

    if wndClass:match('CellSelector') then
        RPD.GameScene:handleCell(hero:level():randomDestination())
        return true
    end

    RPD.debug("unmatched wnd: %s", wndClass)

    activeWindow:hide()

    return false

end

local function handleItem(hero, item, ignoreAction)

    if item:getEntityKind() == "Amulet" then
        return
    end

    local actions = item:actions_l(hero)

    if #actions > 0 then
        local action = actions[math.random(#actions)]
        if action ~= ignoreAction then
            item:execute(hero, action)
        end
    end
end

ai.step = function()
    local hero = RPD.Dungeon.hero

    if not hero:isReady() then
        return
    end

    local heroPos = hero:getPos()

    if handleWindow(hero) then
        return
    end

    local level = hero:level()

    if hero:buffLevel('Blindness') > 0 then
        local cell = level:getEmptyCellNextTo(heroPos)
        if level:cellValid(cell) then
            hero:handle(cell)
        else
            hero:rest(false)
        end
        return
    end

    if hero:buffLevel('Charm') == 0 then
        local enemyPos = hero:getNearestEnemy():getPos()
        if level:cellValid(enemyPos) and level:adjacent(enemyPos, heroPos) then

            if hero:getSkillPoints() > 0 and math.random() < 0.25 then
                RPD.SpellFactory:getRandomSpell():castOnRandomTarget(hero)
                return
            end

            hero:handle(enemyPos)
            return
        end
    end

    if hero:buffLevel('Roots') > 0 or math.random() < 0.025 then
        hero:search(true)
        return
    end

    if not hero:getBelongings():isBackpackFull() then
        local heapPos = level:getNearestVisibleHeapPosition(heroPos)

        if level:cellValid(heapPos) and heapPos ~= heroPos and not explorationCache:get(heapPos) then
            explorationCache:set(heapPos, true)
            hero:handle(heapPos)
            return
        end
    end

    if hero:getSkillPoints() > 0 and math.random() < 0.01 then
        RPD.SpellFactory:getRandomSpell():castOnRandomTarget(hero)
        return
    end

    local enemy = hero:getNearestEnemy()
    if enemy:valid() then
        hero:handle(enemy:getPos())
        return
    end

    local objectPos = level:getNearestVisibleLevelObject(heroPos)

    if level:cellValid(objectPos) then
        local objectKind = level:getTopLevelObject(objectPos):getEntityKind()

        if not explorationCache:get(objectPos) then
            explorationCache:set(objectPos, true)
            hero:handle(objectPos)
            return
        end

        if objectKind == 'Barrel' and not hero:getBelongings():isBackpackEmpty() and objectPos ~= heroPos then
            hero:getBelongings():randomUnequipped():cast(hero, objectPos)
            return
        end
    end

    if not hero:getBelongings():isBackpackEmpty() and math.random() < 0.05 then
        handleItem(hero, hero:getBelongings():randomUnequipped(), RPD.Actions.drop)
        return
    end

    if math.random() < 0.01 then
        handleItem(hero, hero:getBelongings():randomEquipped(), RPD.Actions.throw)
        return
    end
    --[[
        local exitCell = level:getRandomVisibleTerrainCell(RPD.Terrain.EXIT)

        if level:cellValid(exitCell) and not level:getTopLevelObject(exitCell) then
            hero:handle(exitCell)
            return
        end

        exitCell = level:getRandomVisibleTerrainCell(RPD.Terrain.LOCKED_EXIT)
        if level:cellValid(exitCell) and not level:getTopLevelObject(exitCell) and hero:getItem("SkeletonKey"):valid() then
            hero:handle(exitCell)
            return
        end
    ]]
    local doorCell = level:getRandomVisibleTerrainCell(RPD.Terrain.DOOR)

    if level:cellValid(doorCell) and not level:isCellVisited(doorCell) then
        hero:handle(doorCell)
        return
    end

    doorCell = level:getRandomVisibleTerrainCell(RPD.Terrain.LOCKED_DOOR)

    if level:cellValid(doorCell) and hero:getItem("IronKey"):valid() and not explorationCache:get(doorCell) then
        hero:handle(doorCell)
        explorationCache:set(doorCell, true)
        return
    end

    local cell = level:randomTestDestination()
    if not level:cellValid(cell) or cell == heroPos or cell == level:getEntrance() or explorationCache:get(cell) then
        cell = level:randomDestination()
    end

    explorationCache:set(cell, true)

    hero:handle(cell)
end

ai.selectCell = function(self)
    RPD.GameScene:handleCell(RPD.Dungeon.level:randomDestination())
end

return ai