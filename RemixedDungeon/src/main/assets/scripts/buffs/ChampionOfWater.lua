---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by mike.
--- DateTime: 1/4/24 11:53 PM
---

local buff = require "scripts/lib/buff"

local RPD  = require "scripts/lib/commonClasses"

return buff.init{
    desc  = function ()
        return {
            icon          = -1,
            name          = "ChampionOfWaterBuff_Name",
            info          = "ChampionOfWaterBuff_Info",
        }
    end,


    attachTo = function(self, buff, target)
        self.data.activated = self.data.activated or false

        if target:hasBuff(buff:getEntityKind()) then
            return false
        end

        target:setGlowing(0x5555AA, 1.5)

        if not self.data.activated then
            self.data.activated = true
        end

        return true
    end,

    defenceSkillBonus = function(self, buff)
        return buff.target:lvl() * 1.25
    end
}