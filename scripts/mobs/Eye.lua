--[[
  Eye - demon gaze (batch 17c-2a, was actors/mobs/Eye.java).
  Deathgaze: any enemy on the magic ray is attackable - the gaze has no
  range cap (script canAttack replaces the range+LOS check). The zap
  beams every char on the ray via the 17a beamStrike; the primary target
  takes the zap hit plus the beam hit (java quirk kept).
  viewDistance = level view + 1 rides the spawn hook.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    canAttack = function(self, enemy)
        local enemyPos = enemy:getPos()
        RPD.Ballistica:cast(self:getPos(), enemyPos, true, false)
        local trace = RPD.Ballistica.trace
        for i = 2, RPD.Ballistica.distance do
            if trace[i] == enemyPos then
                return true
            end
        end
        return false
    end,

    zapProc = function(self, enemy, damage)
        RPD.CharUtils:beamStrike(self, enemy, self:getPos(), "Eye_Kill", 2)
        return damage
    end,

    spawn = function(self, level)
        self:setViewDistance(level:getViewDistance() + 1)
    end,
}
