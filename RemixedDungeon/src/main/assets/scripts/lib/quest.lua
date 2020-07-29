--
-- User: mike
-- Date: 25.11.2017
-- Time: 22:57
-- This file is part of Remixed Pixel Dungeon.
--

local storage = require "scripts/lib/storage"

local RPD = require "scripts/lib/commonClasses"
--[[
    kills.kind = {questName1, questName2, guestName3}
 ]]


local conditionsList = {}
conditionsList.kills = {}


local function questDataIndex(name)
    return "__quest_"..name
end

local function storeConditions()
    storage.gamePut("__quest__conditionsList", conditionsList)
end

local function getConditions()
    conditionsList = storage.gameGet("__quest__conditionsList") or conditionsList
    return conditionsList
end


local quest = {}

quest.debug = function(val)
    getConditions().debug = val
    storeConditions()
end

quest.state = function(name,state)
    if state then
        storage.gamePut(questDataIndex(name),state)
    end
    return storage.gameGet(questDataIndex(name))
end

--[[
-- .kills.BlackRat = 5
-- ]]
quest.give = function(name, chr, conditions)
    conditions = conditions or {}

    quest.state(name, {hero = chr, conditions = conditions, kills = {}})

    getConditions()

    if conditions.kills then
        for _,kind in pairs(conditions.kills) do
            conditionsList.kills[kind] = conditionsList.kills[kind] or {}
            conditionsList.kills[kind][name] = true
        end
    end

    storeConditions()
end

quest.isGiven = function(name)
    return quest.state(name) ~= nil
end

quest.isCompleted = function(name)
    local state = quest.state(name) or {}
    return state.completed or false
end

quest.complete = function(name)
    local state = quest.state(name)
    state.completed = true
    quest.state(name,state)

    getConditions()

    for _, quests in pairs(conditionsList.kills) do
        quests[name] = nil
    end

    storeConditions()
end

quest.mobDied = function(mob,cause)
    local kind = mob:getMobClassName()

    getConditions()

    local affectedQuests = conditionsList.kills[kind] or {}

    for questName,_ in pairs(affectedQuests) do
        local state = quest.state(questName)
        state.kills = state.kills or {}
        state.kills[kind] = (state.kills[kind] or 0) + 1

        if conditionsList.debug then
            RPD.glog("quest: %s kills %s %d", questName, kind, state.kills[kind])
        end

        quest.state(questName, state)
    end
end

return quest