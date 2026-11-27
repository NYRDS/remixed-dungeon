---
--- Multiplicity: clones the wearer on hits (was armor/glyphs/Multiplicity.java).
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance  = 1,
            glowing = { color = 0xCCAA88 },
        }
    end,

    defenceProc = function(self, glyph, armor, attacker, defender, damage)
        local level = math.max(0, armor:level())

        if RPD.Random:Int(math.floor(level / 2) + 6) >= 5 then

            local imgCell = attacker:level():getEmptyCellNextTo(defender:getPos())
            if attacker:level():cellValid(imgCell) then
                if defender:getEntityKind() == "Hero" then
                    local img = defender:makeClone()
                    RPD.CharUtils:appear(img, imgCell)
                    defender:damage(RPD.Random:IntRange(1, math.floor(defender:ht() / 6)), glyph)
                else
                    defender:split(imgCell, damage)
                end
            end

        end

        return damage
    end,
}
