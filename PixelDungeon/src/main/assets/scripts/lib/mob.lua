--
-- User: mike
-- Date: 23.11.2017
-- Time: 21:00
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local quest = require"scripts/lib/quest"

local mob = {}

mob.__index = mob

mob.init = function(desc)
    local ret = {}

    for k,v in pairs(desc) do
        ret[k] = v
    end

    setmetatable(ret, mob)

    return ret
end

mob.onDie = function(self,mob,cause)
    quest.mobDied(mob, cause)
    return not not (self.die and self.die(mob, cause))
end

mob.onInteract = function(self,mob,chr)
    if self.interact == nil then
        return false
    end
    self.interact(mob, chr)
    return true
end

mob.onMove = function(self,mob,cell)
    return not not (self.move and self.move(mob, cell))
end

mob.onDamage = function(self,mob,dmg,src)
    return not not (self.damage and self.damage(mob, dmg, src))
end

mob.onSpawn = function(self,mob,level)
    return not not (self.spawn and self.spawn(mob,level))
end

return mob
