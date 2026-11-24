---
--- ScrollOfTerror: terrifies every visible mob (was items/scrolls/ScrollOfTerror.java).
---
local RPD    = require "scripts/lib/commonClasses"
local scroll = require "scripts/lib/scroll"

return scroll.init{
    desc = function()
        return { price = 50 }
    end,
    doRead = function(self, item, reader)
        RPD.new(RPD.Sfx.Flare, 5, 32):show(reader:getSprite(), 2)
        RPD.playSound("snd_read")
        RPD.CharUtils:dispelInvisibility(reader)

        local level = reader:level()
        local count = 0
        local affected = nil
        for _, mob in pairs(level:getMobs()) do
            if level.fieldOfView[mob:getPos() + 1] and mob:getOwnerId() ~= reader:getId() then
                local terror = RPD.affectBuff(mob, "Terror", 10)
                terror:setSource(reader)
                count = count + 1
                affected = mob
            end
        end

        if count == 0 then
            RPD.glog(RPD.textById("ScrollOfTerror_Info1"))
        elseif count == 1 then
            RPD.glog(RPD.format(RPD.textById("ScrollOfTerror_Info2"), affected:getName()))
        else
            RPD.glog(RPD.textById("ScrollOfTerror_Info3"))
        end
        scroll.identifyTail(self, item, reader)
    end,
}
