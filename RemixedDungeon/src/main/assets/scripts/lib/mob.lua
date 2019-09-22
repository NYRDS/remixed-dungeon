--
-- User: mike
-- Date: 23.11.2017
-- Time: 21:00
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local quest = require"scripts/lib/quest"

local serpent = require "scripts/lib/serpent"

local mob = {}

local knownMobs = {}
setmetatable(knownMobs, { __mode = 'vk' })

mob.__index = mob

mob.init = function(desc)
    local ret = {}

    for k,v in pairs(desc) do
        ret[k] = v
    end

    setmetatable(ret, mob)

    ret.data = {}

    return ret
end

local onDieCallbacks = {}

mob.installOnDieCallback = function(callback)
    onDieCallbacks[callback] = true
end

function mob.saveData(self, _)
    return serpent.dump(self.data or {})
end

function mob.loadData(self, _, str)
    local _,data = serpent.load(str)
    self.data = data or {}
end

mob.storeData = function(self, data)
    knownMobs[self].data = data or {}
end

mob.restoreData = function(self)
    return knownMobs[self].data or {}
end

mob.onDie = function(self,mob,cause)
    quest.mobDied(mob, cause)

    for k, _ in pairs(onDieCallbacks) do
        k(mob,cause)
    end

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

mob.onZapProc = function(self,mob, enemy, damage)
    if self.zapProc == nil then
        return damage
    end
    return self.zapProc(mob, enemy, damage)
end

mob.fillStats = function(self,mob)
    knownMobs[mob] = self
    return not not (self.stats and self.stats(mob))
end

return mob
