---
--- RatKingAuraBuff: rats the wearer strikes become pets (was
--- common.RatKingCrown.RatKingAuraBuff).
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 34, -- BuffIndicator.RATTNESS
            name          = "RatKingCrownBuff_Name",
            info          = "RatKingCrownBuff_Info",
        }
    end,

    attackProc = function(self, buff, defender, damage)
        local attacker = buff.target
        if defender:getEntityKind() == "Rat" and attacker:buffLevel(buff:getEntityKind()) > 0 then
            RPD.Mob:makePet(defender, attacker:getId())
        end
        return damage
    end,
}
