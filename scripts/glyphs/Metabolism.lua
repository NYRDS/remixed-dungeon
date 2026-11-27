---
--- Metabolism: converts hunger into healing on hits (was
--- armor/glyphs/Metabolism.java).
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance  = 1,
            glowing = { color = 0xCC0000 },
        }
    end,

    defenceProc = function(self, glyph, armor, attacker, defender, damage)
        local level = math.max(0, armor:level())
        if RPD.Random:Int(math.floor(level / 2) + 5) >= 4 then

            local healing = math.min(defender:ht() - defender:hp(), RPD.Random:Int(1, math.floor(defender:ht() / 5)))
            if healing > 0 then
                if not defender:isStarving() then
                    -- was -Hunger.STARVING / 10 (STARVING = 400)
                    defender:hunger():satisfy(-40)
                    defender:heal(healing, glyph)
                    defender:showStatus(RPD.CharSprite.POSITIVE, tostring(healing))
                end
            end
        end

        return damage
    end,
}
