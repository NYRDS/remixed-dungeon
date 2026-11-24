---
--- ScrollOfLullaby: puts every visible mob to sleep (was items/scrolls/ScrollOfLullaby.java).
---
local RPD    = require "scripts/lib/commonClasses"
local scroll = require "scripts/lib/scroll"

return scroll.init{
    desc = function()
        return { price = 50 }
    end,
    doRead = function(self, item, reader)
        reader:getSprite():centerEmitter():start(RPD.speckEffectFactory(RPD.Sfx.Speck.NOTE, 0), 0.3, 5)
        RPD.playSound("snd_lullaby")

        local level = reader:level()
        local count = 0
        local affected = nil
        for _, mob in pairs(level:getMobs()) do
            if level.fieldOfView[mob:getPos() + 1] then
                RPD.affectBuff(mob, "Sleep")
                if mob:hasBuff("Sleep") then
                    affected = mob
                    count = count + 1
                end
            end
        end

        if count == 0 then
            RPD.glog(RPD.textById("ScrollOfLullaby_Info1"))
        elseif count == 1 then
            RPD.glog(RPD.format(RPD.textById("ScrollOfLullaby_Info2"), affected:getName()))
        else
            RPD.glog(RPD.textById("ScrollOfLullaby_Info3"))
        end
        scroll.identifyTail(self, item, reader)
    end,
}
