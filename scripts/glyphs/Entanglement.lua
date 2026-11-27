---
--- Entanglement: roots the wearer and raises the Earthroot armor pool (was
--- armor/glyphs/Entanglement.java).
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance  = 1,
            glowing = { color = 0x448822 },
        }
    end,

    defenceProc = function(self, glyph, armor, attacker, defender, damage)
        local level = math.max(0, armor:level())

        if RPD.Random:Int(4) == 0 then
            RPD.Buffs.Buff:prolong(defender, "Roots", 5 - math.floor(level / 5))
            -- Armor pool is a script buff; keep the raise-only semantics of the
            -- old Earthroot.Armor.level(int) override
            local pool = RPD.Buffs.Buff:affect(defender, "Armor")
            local wantLevel = 5 * (level + 1)
            if pool:level() < wantLevel then
                pool:level(wantLevel)
            end
            RPD.Sfx.CellEmitter:bottom(defender:getPos()):start(RPD.Sfx.EarthParticle.FACTORY, 0.05, 8)
            RPD.shakeCamera(1, 0.4)
        end
        return damage
    end,
}
