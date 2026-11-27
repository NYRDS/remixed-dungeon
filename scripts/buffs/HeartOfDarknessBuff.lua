---
--- HeartOfDarknessBuff: a Spirit of Pain spawns on every defended hit (was
--- guts.HeartOfDarkness.HeartOfDarknessBuff).
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 35, -- BuffIndicator.DARKVEIL
            name          = "DarkVeilBuff_Name",
            info          = "DarkVeilBuff_Info",
        }
    end,

    defenceProc = function(self, buff, enemy, damage)
        local defender = buff.target
        local spiritPos = defender:level():getEmptyCellNextTo(defender:getPos())

        if defender:level():cellValid(spiritPos) then
            local spirit = RPD.MobFactory:mobByName("SpiritOfPain")
            spirit:setPos(spiritPos)
            RPD.Mob:makePet(spirit, defender:getId())
            defender:level():spawnMob(spirit, 0, defender:getPos())
        end
        return damage
    end,
}
