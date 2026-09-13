local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

local BLINK_DELAY = 5

-- java Succubus (batch 8): charms on 1/3 hits (OneWayLove redirects the
-- charm to the succubus itself), blinks in from range every 5 turns instead
-- of walking - a free action (spend refund cancels doStepTo's pre-charge).
return mob.init{
    spawn = function(self, level)
        self:setViewDistance(level:getViewDistance() + 1)
    end,

    attackProc = function(self, enemy, dmg)
        if enemy ~= nil and math.random(3) == 1 then
            local target = enemy
            if enemy:hasBuff("OneWayLoveBuff") then
                target = self
            end
            local duration = RPD.Buffs.Charm:durationFactor(target) * math.random(2, 5)
            RPD.affectBuff(target, "Charm", duration)
        end
        return dmg
    end,

    getCloser = function(self, target, ignorePets)
        if self:isPet() then
            return false -- pets use the regular pathfind
        end

        local data = mob.restoreData(self)
        local level = RPD.Dungeon.level

        -- luaj arrays are 1-based: fieldOfView[target + 1] == java fieldOfView[target]
        if level.fieldOfView[target + 1]
                and level:distance(self:getPos(), target) > 2
                and (data.delay or 0) <= 0 then

            local tgtCell = target
            if not level:isCellNonOccupied(target) then
                tgtCell = level:getSafeCellNextTo(target)
            end
            RPD.CharUtils:blinkTo(self, tgtCell)
            data.delay = BLINK_DELAY
            self:spend(-1 / self:speed()) -- refund doStepTo's pre-charge
            return true
        else
            data.delay = (data.delay or 0) - 1
            return false -- regular pathfind
        end
    end
}
