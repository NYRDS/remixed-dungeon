--[[
    mob lua migration batch 16c-3: replaces java AzuterronNPC (heart fetch at
    the d27 shop; quest state in lib/quest game storage under "azuterron").
    Once the quest completes the mob falls back to the shared Shopkeeper.lua
    shop interact, like the java super.interact() did
]]

local RPD       = require "scripts/lib/commonClasses"
local quest     = require "scripts/lib/quest"
local mob       = require "scripts/lib/mob"
local shopkeeper = require "scripts/npc/Shopkeeper"

local QUEST = "azuterron"

local function qstate()
    return quest.state(QUEST) or {}
end

local function exchangeItem(chr, itemKind, rewardKind)
    local item = chr:getItem(itemKind)

    if not item or not item:valid() then
        return false
    end

    item:removeItemFrom(chr)
    chr:collectAnimated(RPD.item(rewardKind))
    return true
end

return mob.init({
    interact = function(self, chr)
        self:getSprite():turnTo(self:getPos(), chr:getPos())

        local st = qstate()

        if st.completed then
            return shopkeeper.interact(self, chr)
        end

        if st.given then
            if exchangeItem(chr, "HeartOfDarkness", "PotionOfMight") then
                quest.complete(QUEST)
                RPD.showQuestWindow(self, "AzuterronNPC_Quest_End")
            else
                RPD.showQuestWindow(self, "AzuterronNPC_Quest_Reminder")
            end
        else
            RPD.showQuestWindow(self, "AzuterronNPC_Quest_Start")

            st.given = true
            quest.state(QUEST, st)

            RPD.Journal:add(RPD.textById("Journal_Azuterron"))

            -- java Quest.process: a TreacherousSpirit appears nearby
            local level = RPD.Dungeon.level
            local pos = self:respawnCell(level)

            if level:cellValid(pos) then
                local spirit = RPD.MobFactory:mobByName(RPD.MobFactory.TREACHEROUS_SPIRIT)
                spirit:setPos(pos)
                level:spawnMob(spirit)
            end
        end
        return true
    end
})
