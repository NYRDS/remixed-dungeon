--
-- User: mike
-- Date: 25.11.2017
-- Time: 22:57
-- This file is part of Remixed Pixel Dungeon.
--

local storage = require "scripts/lib/storage"

--[[
    kills.kind = {questName1, questName2, guestName3}
 ]]


local condintionsList = {}
condintionsList.kills = {}

condintionsList = storage.gameGet("__quest__condintionsList") or condintionsList


local function questDataIndex(name)
    return "__quest_"..name
end


local quest = {}

quest.state = function(name,state)
    if state ~= nil then
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

    if conditions.kills ~= nil then
        for _,kind in pairs(conditions.kills) do
            condintionsList.kills[kind] = condintionsList.kills[kind] or {}
            table.insert(condintionsList.kills[kind], name)
        end
    end

    storage.gamePut("__quest__condintionsList",condintionsList)

end

quest.isGiven = function(name)
    return quest.state(name) ~= nil
end

quest.isCompleted = function(name)
    local state = quest.state(name)
    return state.completed
end

quest.complete = function(name)
    local state = quest.state(name)
    state.completed = true
    quest.state(name,state)
end

quest.mobDied = function(mob,cause)
    local kind = mob:getMobClassName()

    local affectedQuests = condintionsList.kills[kind] or {}

    for _,questName in pairs(affectedQuests) do
        local state = quest.state(questName)
        state.kills[kind] = (state.kills[kind] or 0) + 1
        quest.state(questName, state)
    end

end

return quest