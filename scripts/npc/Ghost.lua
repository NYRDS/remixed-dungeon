--[[
    mob lua migration batch 16c-1: replaces java Ghost + WndSadGhost + WndSadGhostNecro
    (placed by SewerLevel d2+; quest state lives in lib/quest game storage under "sadGhost",
    per-mob persuade/introduced state in restoreData)
]]

local RPD   = require "scripts/lib/commonClasses"
local quest = require "scripts/lib/quest"
local mob   = require "scripts/lib/mob"

local QUEST = "sadGhost"

local function qstate()
    return quest.state(QUEST) or {}
end

local function questItemKind(alternative)
    if alternative then
        return "RatSkull"
    end
    return "DriedRose"
end

-- java Ghost.Quest.makeReward: level treasury best-of-4, weapon excludes
-- missiles, both identified (identify happens here on the returned item)
local function makeReward(alternative, weapon)
    local treasury = RPD.Treasury:getLevelTreasury()

    local reward
    if weapon then
        reward = treasury:bestWeapon(4)
    else
        reward = treasury:bestOf("ARMOR", 4)
    end

    reward:identify()
    return reward
end

-- WndSadGhost port: reward item is generated only when a button is pressed
-- (java kept them in Quest statics - lua state must stay serpent-safe)
local function showRewardWindow(self, chr, alternative, item)
    RPD.chooseOption(function(index)
        if index ~= 0 and index ~= 1 then
            return
        end

        if item then
            item:removeItemFrom(chr)
        end

        chr:collectAnimated(makeReward(alternative, index == 0))

        self:say(RPD.textById("WndSadGhost_Farewell"))
        self:remove()

        quest.complete(QUEST)
        RPD.Journal:remove(RPD.textById("Journal_Ghost"))
    end,
    self:getName(),
    RPD.textById(alternative and "WndSadGhost_Rat" or "WndSadGhost_Rose"),
    RPD.textById("WndSadGhost_Wepon"),
    RPD.textById("WndSadGhost_Armor"))
end

-- Ghost.Quest.process port: rat/gnoll/crab deaths drive both quest branches
mob.installOnDieCallback(function(dead, cause)
    local kind = dead:getEntityKind()

    if kind ~= RPD.MobFactory.RAT and kind ~= RPD.MobFactory.GNOLL and kind ~= RPD.MobFactory.CRAB then
        return
    end

    local st = qstate()

    if not (st.spawned and st.given and not st.processed and not st.completed and st.depth == RPD.Dungeon.depth) then
        return
    end

    if st.alternative then
        -- rat quest: first qualifying kill spawns the FetidRat carrier
        local rat = RPD.MobFactory:mobByName(RPD.MobFactory.FETID_RAT)
        local pos = rat:respawnCell(RPD.Dungeon.level)
        if RPD.Dungeon.level:cellValid(pos) then
            rat:setPos(pos)
            RPD.Dungeon.level:spawnMob(rat)
            st.processed = true
            quest.state(QUEST, st)
        end
    else
        -- rose quest: DriedRose drops after 1-in-left2kill kills (init 8)
        local left2kill = st.left2kill or 8
        if math.random(left2kill) == 1 then
            RPD.Dungeon.level:animatedDrop(RPD.item("DriedRose"), dead:getPos())
            st.processed = true
        else
            st.left2kill = left2kill - 1
        end
        quest.state(QUEST, st)
    end
end)

return mob.init({
    act = function(self)
        -- java act: when quest given, ghost drifts toward the hero 10% of ticks
        local st = qstate()
        if st.given and math.random() < 0.1 then
            self:beckon(RPD.Dungeon.hero:getPos())
        end
    end,

    interact = function(self, chr)
        local data = mob.restoreData(self)

        if chr:getHeroClass():getEntityKind() == "NECROMANCER" and not data.introduced then
            data.introduced = true
            mob.storeData(self, data)

            RPD.chooseOption(function(index)
                data.persuade = (index == 0)
                mob.storeData(self, data)

                if data.persuade then
                    RPD.glog(RPD.textById("WndSadGhostNecro_Persuaded"))
                end
            end,
            RPD.textById("Necromancy_Title"),
            RPD.textById("WndSadGhostNecro_Text"),
            RPD.textById("WndSadGhostNecro_Yes"),
            RPD.textById("WndSadGhostNecro_No"))

            return true
        end

        local st = qstate()

        RPD.playSound("snd_ghost")

        if data.persuade or st.given then
            local item = nil
            if data.persuade then
                -- persuade fabricates the quest item, bypassing the fetch
                item = RPD.item(questItemKind(st.alternative))
            else
                item = chr:getBelongings():getItem(questItemKind(st.alternative))
            end

            if item ~= nil then
                showRewardWindow(self, chr, st.alternative, item)
            else
                RPD.showQuestWindow(self, st.alternative and "Ghost_Rat2" or "Ghost_Rose2")
                RPD.CharUtils:teleportRandomForce(self)
            end
        else
            RPD.showQuestWindow(self, st.alternative and "Ghost_Rat1" or "Ghost_Rose1")
            st.given = true
            quest.state(QUEST, st)
            RPD.Journal:add(RPD.textById("Journal_Ghost"))
        end

        return true
    end
})
