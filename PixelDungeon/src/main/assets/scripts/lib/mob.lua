--
-- User: mike
-- Date: 23.11.2017
-- Time: 21:00
-- This file is part of Remixed Pixel Dungeon.
--

local quest = require"scripts/lib/quest"

local serpent = require "scripts/lib/serpent"

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

mob.storeData = function(chr, data)
    chr:setData(serpent.dump(data))
end

mob.restoreData = function(chr)
    local data = chr:getData()
    if data == nil then
        return {}
    end

    local _, data = serpent.load(data)
    return data or {}
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

mob.onDefenceProc = function(self,mob, enemy, damage)
    if self.defenceProc == nil then
        return damage
    end
    return self.defenceProc(mob, enemy, damage)
end

mob.onAttackProc = function(self,mob, enemy, damage)
    if self.attackProc == nil then
        return damage
    end
    return self.attackProc(mob, enemy, damage)
end

mob.fillStats = function(self,mob)
    return not not (self.stats and self.stats(mob))
end

return mob
