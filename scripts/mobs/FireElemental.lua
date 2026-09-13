local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java FireElemental: flame cannot hurt it (Burning heals it instead of
-- attaching), Frost burns it. Half of its hits reignite the target.
return mob.init{
    attackProc = function(self, enemy, dmg)
        if enemy ~= nil and math.random(2) == 1 then
            -- java: affect + reignite(ch) == attach with duration(ch)
            RPD.Buffs.Buff:affect(enemy, "Burning", RPD.Buffs.Burning:duration(enemy))
        end
        return dmg
    end,

    addBuff = function(self, buff)
        local kind = buff:getEntityKind()
        if kind == "Burning" then
            self:heal(RPD.Random:NormalIntRange(1, self:ht() * 4), buff)
            return true
        end
        if kind == "Frost" then
            self:damage(RPD.Random:NormalIntRange(1, math.floor(self:ht() * 2 / 3)), buff)
            return true
        end
        return false
    end,
}
