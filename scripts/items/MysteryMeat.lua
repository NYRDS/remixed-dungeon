---
--- MysteryMeat: strange meat from strange animals - mostly bad news
--- (was items/food/MysteryMeat.java).
---
local RPD  = require "scripts/lib/commonClasses"
local food = require "scripts/lib/food"

return food.init{
    desc = function()
        return {
            image   = 113, -- ItemSpriteSheet.MEAT
            energy  = 120, -- Hunger.STARVING - Hunger.HUNGRY
            message = "MysteryMeat_Message",
            price   = 5,
        }
    end,
    onExecute = function(self, item, chr, action)
        if action ~= RPD.Actions.eat then
            return
        end

        local roll = RPD.Random:Int(5)
        if roll == 0 then
            RPD.GLog:w(RPD.textById("MysteryMeat_Info1"), {})
            RPD.CharUtils:ignite(chr)
        elseif roll == 1 then
            RPD.GLog:w(RPD.textById("MysteryMeat_Info2"), {})
            RPD.Buffs.Buff:prolong(chr, "Roots", RPD.CharUtils:durationFactor(chr) * 10)
        elseif roll == 2 then
            RPD.GLog:w(RPD.textById("MysteryMeat_Info3"), {})
            RPD.affectBuff(chr, "Poison", RPD.CharUtils:durationFactor(chr) * chr:ht() / 5)
        elseif roll == 3 then
            RPD.GLog:w(RPD.textById("MysteryMeat_Info4"), {})
            RPD.Buffs.Buff:prolong(chr, "Slow", RPD.CharUtils:durationFactor(chr) * 10)
        end
    end,
    burn   = function(self, item, cell) return "ChargrilledMeat" end,
    freeze = function(self, item, cell) return "FrozenCarpaccio" end,
    poison = function(self, item, cell) return "RottenMeat" end,
}
