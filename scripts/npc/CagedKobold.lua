--[[
    mob lua migration batch 16c-3: replaces java CagedKobold (IceKey fetch on
    IceCavesLevel d18 exit room; quest state in lib/quest game storage under
    "cagedKobold", spawned recorded by QuestBridge.trySpawn from the
    IceCavesLevel shim)
]]

local RPD   = require "scripts/lib/commonClasses"
local quest = require "scripts/lib/quest"
local mob   = require "scripts/lib/mob"

local QUEST = "cagedKobold"

local function qstate()
    return quest.state(QUEST) or {}
end

-- NPC.exchangeItem port: hero hands over item, gets reward animated
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
            return true
        end

        if st.given then
            if exchangeItem(chr, "IceKey", "CandleOfMindVision") then
                quest.complete(QUEST)
                RPD.showQuestWindow(self, "CagedKobold_Quest_End")

                local speck = RPD.Sfx.Speck:factory(RPD.Sfx.Speck.LIGHT)
                RPD.Sfx.CellEmitter:get(self:getPos()):start(speck, 0.2, 3)
                self:getSprite():killAndErase()
                self:remove()
            else
                self:say(RPD.textById("CagedKobold_Message" .. math.random(1, 3)))
            end
        else
            RPD.showQuestWindow(self, "CagedKobold_Intro")

            st.given = true
            quest.state(QUEST, st)

            RPD.Journal:add(RPD.textById("Journal_Caged_Kobold"))
        end
        return true
    end
})
