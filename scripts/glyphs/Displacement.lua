---
--- Displacement: teleports the wearer out of trouble on hits (was
--- armor/glyphs/Displacement.java).
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance  = 1,
            glowing = { color = 0x66AAFF },
        }
    end,

    defenceProc = function(self, glyph, armor, attacker, defender, damage)
        if RPD.Dungeon:bossLevel() then
            return damage
        end

        local nTries = (armor:level() < 0 and 1 or armor:level() + 1) * 5
        for _ = 1, nTries do
            local pos = RPD.Random:Int(RPD.Dungeon.level:getLength())
            if RPD.Dungeon:isCellVisible(pos) and RPD.Dungeon.level.passable[pos + 1] and not RPD.Actor:findChar(pos) then
                RPD.CharUtils:appear(defender, pos)
                RPD.Dungeon.level:press(pos, defender)
                defender:observe()
                break
            end
        end

        return damage
    end,
}
