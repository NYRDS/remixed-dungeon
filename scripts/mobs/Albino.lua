local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    act = function(self)
        -- albino is a rat: the rat skull's ratter aura panics it too
        -- (inherited from java Rat.canAttack before batch 6)
        local enemy = self:getEnemy()

        if enemy ~= nil and enemy:hasBuff("artifactBuffRatterAura") then
            RPD.setAi(self, "Fleeing")
            if not self:hasBuff("Terror") then
                RPD.affectBuff(self, "Terror", 10)
            end
        end
    end,

    attackProc = function(self, enemy, dmg)
        -- 1/2 chance to inflict bleeding as strong as the hit
        if enemy ~= nil and math.random(2) == 1 then
            local bleeding = RPD.Buffs.Buff:affect(enemy, "Bleeding")
            bleeding:level(dmg)
        end
        return dmg
    end
}
