---
--- FrozenCarpaccio: mystery meat, frozen - mostly good news
--- (was items/food/FrozenCarpaccio.java).
---
local RPD  = require "scripts/lib/commonClasses"
local food = require "scripts/lib/food"

return food.init{
    desc = function()
        return {
            image  = 116, -- ItemSpriteSheet.CARPACCIO
            energy = 120, -- Hunger.STARVING - Hunger.HUNGRY
            price  = 10,
        }
    end,
    onExecute = function(self, item, chr, action)
        if action ~= RPD.Actions.eat then
            return
        end

        local roll = RPD.Random:Int(5)
        if roll == 0 then
            RPD.GLog:i(RPD.textById("FrozenCarpaccio_Info1"))
            RPD.affectBuff(chr, "Invisibility", 15)
        elseif roll == 1 then
            RPD.GLog:i(RPD.textById("FrozenCarpaccio_Info2"))
            local barkskin = RPD.affectBuff(chr, "Barkskin")
            local lvl = math.floor(chr:ht() / 4) -- java int division
            if barkskin:level() < lvl then
                barkskin:level(lvl)
            end
        elseif roll == 2 then
            RPD.GLog:i(RPD.textById("FrozenCarpaccio_Info3"))
            RPD.removeBuff(chr, "Poison")
            RPD.removeBuff(chr, "Cripple")
            RPD.removeBuff(chr, "Weakness")
            RPD.removeBuff(chr, "Bleeding")
        elseif roll == 3 then
            RPD.GLog:i(RPD.textById("FrozenCarpaccio_Info4"))
            chr:heal(chr:hp() + math.floor(chr:ht() / 4), item)
        end
    end,
    burn = function(self, item, cell) return "MysteryMeat" end,
}
