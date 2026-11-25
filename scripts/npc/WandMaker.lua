--[[
    mob lua migration batch 16c-2: replaces java WandMaker + WndWandmaker
    (placed by PrisonLevel d7+; quest state in lib/quest game storage under
    "wandmaker". The Rotberry plant/Seed split out of the java class and
    stay java - plants are out of migration scope)
]]

local RPD   = require "scripts/lib/commonClasses"
local quest = require "scripts/lib/quest"
local mob   = require "scripts/lib/mob"

local QUEST = "wandmaker"

local function qstate()
    return quest.state(QUEST) or {}
end

local function questItemKind(alternative)
    if alternative then
        return "CorpseDust"
    end
    return "Rotberry.Seed"
end

local function questItemName(alternative)
    if alternative then
        return "WandMaker_Dust"
    end
    return "WandMaker_Berry"
end

-- java WandMaker.makeBattleWand/makeNonBattleWand: one of five wands,
-- randomized then upgraded once. Generated at reward time, like java.
local function makeWand(battle)
    local kinds
    if battle then
        kinds = { "WandOfAvalanche", "WandOfDisintegration", "WandOfFirebolt", "WandOfLightning", "WandOfPoison" }
    else
        kinds = { "WandOfAmok", "WandOfBlink", "WandOfRegrowth", "WandOfSlowness", "WandOfTelekinesis" }
    end

    local wand = RPD.item(kinds[math.random(#kinds)])
    wand:random()
    wand:upgrade()
    return wand
end

-- WandMaker.Quest.placeItem port: dust goes into a hidden skeleton heap
-- (or a fresh one on a free cell), berry seeds itself on a free cell
local function placeItem(alternative)
    local lvl = RPD.Dungeon.level

    if alternative then
        local candidates = {}
        local heaps = lvl:allHeaps()
        for i = 0, heaps:size() - 1 do
            local heap = heaps:get(i)
            if heap.type:name() == "SKELETON" and not RPD.Dungeon:isCellVisible(heap:getPos()) then
                table.insert(candidates, heap)
            end
        end

        if #candidates > 0 then
            candidates[math.random(#candidates)]:drop(RPD.item("CorpseDust"))
        else
            local pos = lvl:randomRespawnCell()
            while lvl:getHeap(pos) ~= nil do
                pos = lvl:randomRespawnCell()
            end
            RPD.ItemUtils:dropAt("CorpseDust", pos, "SKELETON")
        end
    else
        local pos = lvl:randomRespawnCell()
        while lvl:getHeap(pos) ~= nil do
            pos = lvl:randomRespawnCell()
        end
        lvl:plant(RPD.item("Rotberry.Seed"), pos)
    end
end

return mob.init({
    interact = function(self, chr)
        local st = qstate()

        if st.given then
            local item = chr:getBelongings():getItem(questItemKind(st.alternative))

            if item ~= nil then
                RPD.chooseOption(function(index)
                    if index ~= 0 and index ~= 1 then
                        return
                    end

                    item:removeItemFrom(chr)

                    local reward = makeWand(index == 0)
                    reward:identify()
                    chr:collectAnimated(reward)

                    self:say(RPD.textById("WndWandmaker_Farawell"):format(chr:className()))
                    self:remove()

                    quest.complete(QUEST)
                    RPD.Journal:remove(RPD.textById("Journal_Wandmaker"))
                end,
                self:getName(),
                RPD.textById("WndWandmaker_Message"),
                RPD.textById("WndWandmaker_Battle"),
                RPD.textById("WndWandmaker_NonBattle"))
            else
                local reminder = RPD.textById(questItemName(st.alternative) .. "2"):format(chr:className())
                RPD.showQuestWindow(self, reminder)
            end
        else
            RPD.showQuestWindow(self, questItemName(st.alternative) .. "1")
            st.given = true
            quest.state(QUEST, st)

            placeItem(st.alternative)

            RPD.Journal:add(RPD.textById("Journal_Wandmaker"))
        end

        return true
    end
})
