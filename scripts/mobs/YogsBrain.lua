local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java YogsBrain: ranged-only kiter (same shape as Scorpio) shooting
-- lightning bolts with no base zap damage; being hurt draws every mob to it
-- and spawns a Nightmare nearby.
local function beckonAll(self)
    local mobs = RPD.Dungeon.level:getMobs()
    for i = 1, #mobs do
        mobs[i]:beckon(self:getPos())
    end
end

return mob.init{
    act = function(self)
        -- retreat unless the enemy is in a clean ranged line; resume hunting
        -- when one opens up (the Hunting/Fleeing pair replaces the java
        -- Hunting-always-getFurther inversion)
        local enemy = self:getEnemy()
        if enemy == nil then
            return
        end
        local tag = self:getState():getTag()
        if tag == "HUNTING" then
            if not self.enemySeen or not RPD.CharUtils:canDoOnlyRangedAttack(self, enemy) then
                RPD.setAi(self, "Fleeing")
            end
        elseif tag == "FLEEING" then
            if self.enemySeen and RPD.CharUtils:canDoOnlyRangedAttack(self, enemy) then
                RPD.setAi(self, "Hunting")
            end
        end
    end,
    zapProc = function(self, enemy, dmg)
        if enemy ~= nil then
            RPD.CharUtils:lightningProc(self, enemy:getPos(), dmg)
        end
        return 0
    end,
    damage = function(self, dmg, src)
        beckonAll(self)
        local spawn = RPD.CharUtils:spawnOnNextCell(self, "Nightmare",
                math.floor(10 * RPD.GameLoop:getDifficultyFactor()))
        if spawn:valid() then
            RPD.playSound("snd_cursed")
        end
    end
}
