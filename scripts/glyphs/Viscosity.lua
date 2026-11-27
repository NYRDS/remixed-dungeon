---
--- Viscosity: defers the damage taken into a slow drain (was
--- armor/glyphs/Viscosity.java). The DeferedDamage buff itself lives in
--- scripts/buffs/DeferedDamage.lua.
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance  = 1,
            glowing = { color = 0x8844CC },
        }
    end,

    defenceProc = function(self, glyph, armor, attacker, defender, damage)

        if damage == 0 then
            return 0
        end

        local level = math.max(0, armor:level())

        if RPD.Random:Int(level + 7) >= 6 then

            local debuff = defender:buff("DeferedDamage")
            if not debuff then
                debuff = RPD.Buffs.Buff:affect(defender, "DeferedDamage")
                -- java attachTo postponed the first drain tick by Actor.TICK
                debuff:postpone(1)
            end
            debuff:level(debuff:level() + damage)

            defender:showStatus_a(RPD.CharSprite.WARNING, RPD.textById("Viscosity_Status"), damage)

            return 0
        end

        return damage
    end,
}
