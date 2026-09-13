local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- the rat skull's ratter aura panics rats: they flee and Terror is applied
-- once on first sight of the aura bearer (java Rat.canAttack before batch 6)
return mob.init{
    act = function(self)
        local enemy = self:getEnemy()

        if enemy ~= nil and enemy:hasBuff("artifactBuffRatterAura") then
            RPD.setAi(self, "Fleeing")
            if not self:hasBuff("Terror") then
                RPD.affectBuff(self, "Terror", 10)
            end
        end
    end
}
