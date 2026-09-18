local RPD = require "scripts/lib/commonClasses"
local trap = require "scripts/lib/trap"

-- java GrippingTrap: crushes the foot - bleeding scaled by depth vs defence,
-- cripple, wound visual. Wound-only if triggered with nobody on the cell.
return trap.init(
    function (cell, char, data)
        if char ~= nil then
            local damage = math.max(0, (RPD.Dungeon.depth + 3) - math.floor(char:defenceRoll(char) / 2))
            local bleeding = RPD.Buffs.Buff:affect(char, "Bleeding")
            bleeding:level(damage)
            RPD.Buffs.Buff:prolong(char, "Cripple", 10)
            RPD.Sfx.Wound:hit(char)
        else
            RPD.Sfx.Wound:hit(cell)
        end
    end
)
