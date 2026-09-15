--[[
    mob lua migration batch 16c-2: replaces java Imp + WndImp
    (placed by CityLevel d17+; quest state in lib/quest game storage under "imp",
    kills counted here via onDie callback - dwarf tokens from monk/golem deaths)
]]

local RPD   = require "scripts/lib/commonClasses"
local quest = require "scripts/lib/quest"
local mob   = require "scripts/lib/mob"

local QUEST = "imp"

local function qstate()
    return quest.state(QUEST) or {}
end

local function tokensNeeded(alternative)
    -- java: quantity >= 8 always passes, >= 6 only for the golem variant
    if alternative then
        return 8
    end
    return 6
end

-- java Imp.Quest.spawn reward roll: level-treasury ring, never cursed,
-- upgraded twice, re-cursed. Generated lazily at reward time (java kept it
-- in a static field; lua quest state must stay serpent-safe)
local function makeReward()
    local treasury = RPD.Treasury:getLevelTreasury()

    local ring = treasury:random("RING")
    while ring.cursed do
        ring = treasury:random("RING")
    end

    ring:upgrade(2)
    ring.cursed = true
    return ring
end

-- Imp.Quest.process port: monk/golem deaths drop dwarf tokens,
-- palace servants and raised undead do not count
mob.installOnDieCallback(function(dead, cause)
    local kind = dead:getEntityKind()

    if kind ~= RPD.MobFactory.GOLEM and kind ~= RPD.MobFactory.MONK then
        return
    end

    if dead.undead or RPD.Dungeon.level:levelKind() == "CityBossLevel" then
        return
    end

    local st = qstate()

    if not (st.spawned and st.given and not st.completed) then
        return
    end

    RPD.item("DwarfToken"):doDrop(dead)
end)

return mob.init({
    act = function(self)
        local data = mob.restoreData(self)
        local st = qstate()

        if not st.given and RPD.CharUtils:isVisible(self) then
            if not data.seenBefore then
                self:say(RPD.textById("Imp_Hey"):format(RPD.Dungeon.hero:className()))
            end
            data.seenBefore = true
        else
            data.seenBefore = false
        end
        -- seenBefore is transient, like the java field - no storeData here
    end,

    interact = function(self, chr)
        local st = qstate()

        if st.given then
            local tokens = chr:getBelongings():getItem("DwarfToken")
            local need = tokensNeeded(st.alternative)

            if tokens ~= nil and tokens:quantity() >= need then
                RPD.chooseOption(function(index)
                    if index ~= 0 then
                        return
                    end

                    tokens:detachAll(chr:getBelongings().backpack)

                    local reward = makeReward()
                    reward:identify()
                    chr:collectAnimated(reward)

                    self:say(RPD.textById("Imp_Cya"):format(RPD.Dungeon.hero:className()))
                    self:remove()

                    quest.complete(QUEST)
                    RPD.Journal:remove(RPD.textById("Journal_Imp"))
                end,
                self:getName(),
                RPD.textById("WndImp_Message"),
                RPD.textById("WndImp_Reward"))
            else
                local reminder = RPD.textById(st.alternative and "Imp_Monks2" or "Imp_Golems2"):format(chr:className())
                RPD.showQuestWindow(self, reminder)
            end
        else
            RPD.showQuestWindow(self, st.alternative and "Imp_Monks1" or "Imp_Golems1")
            st.given = true
            st.completed = false
            quest.state(QUEST, st)
            RPD.Journal:add(RPD.textById("Journal_Imp"))
        end

        return true
    end
})
