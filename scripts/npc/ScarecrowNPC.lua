--[[
    mob lua migration batch 16c-3: replaces java ScarecrowNPC (halloween candy
    quest, placed by SewerLevel d2 during the halloween event; quest state in
    lib/quest game storage under "scarecrow", spawned recorded by
    QuestBridge.trySpawn from the SewerLevel shim). Kill counting happens in
    the onDie callback below - the java Mob.processQuestKills switch is gone.
]]

local RPD   = require "scripts/lib/commonClasses"
local quest = require "scripts/lib/quest"
local mob   = require "scripts/lib/mob"

local QUEST = "scarecrow"

local function qstate()
    return quest.state(QUEST) or {}
end

-- java ScarecrowNPC.Quest.process: every rat/gnoll kill counts, a Candy
-- drops at the kill spot every 5th, 25 kills mark the quest processed
mob.installOnDieCallback(function(dead, cause)
    local kind = dead:getEntityKind()

    if kind ~= RPD.MobFactory.RAT and kind ~= RPD.MobFactory.GNOLL then
        return
    end

    local st = qstate()

    if not (st.spawned and st.given and not st.processed and not st.completed) then
        return
    end

    st.killed = (st.killed or 0) + 1

    if st.killed % 5 == 0 then
        RPD.Dungeon.level:animatedDrop(RPD.item("Candy"), dead:getPos())
    end

    if st.killed >= 25 then
        st.processed = true
    end

    quest.state(QUEST, st)
end)

return mob.init({
    interact = function(self, chr)
        self:getSprite():turnTo(self:getPos(), chr:getPos())

        local st = qstate()

        if st.completed then
            self:remove()
            return true
        end

        if st.given then
            local candy = chr:getBelongings():getItem("Candy")

            if candy ~= nil and candy:quantity() == 5 then
                candy:removeItemFrom(chr)

                local reward = RPD.item("PumpkinPie")
                reward:quantity(5)
                chr:collectAnimated(reward)

                quest.complete(QUEST)
                RPD.showQuestWindow(self, "ScarecrowNPC_Quest_End")
            else
                RPD.showQuestWindow(self, "ScarecrowNPC_Quest_Reminder")
                RPD.CharUtils:teleportRandomForce(self)
            end
        else
            -- 2 = Utils.FEMININE (java constant, not worth a binding)
            if chr:getGender() == 2 then
                RPD.showQuestWindow(self, "ScarecrowNPC_Quest_Start_Female")
            else
                RPD.showQuestWindow(self, "ScarecrowNPC_Quest_Start_Male")
            end

            st.given = true
            quest.state(QUEST, st)

            RPD.Journal:add(RPD.textById("Journal_ScarecrowNPC"))
        end
        return true
    end
})
