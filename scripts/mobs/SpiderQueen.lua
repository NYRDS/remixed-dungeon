--[[
  SpiderQueen - spiders lair boss (batch 17c-2a,
  was com.nyrds.pixeldungeon.mobs.spiders.SpiderQueen.java).
  Lays a SpiderEgg about every 21 turns, poisons on hit, below half hp
  she refuses melee and kites away while the target is close. The 1/3
  crystal/charm/armor carry-gear roll rides the spawn hook, one-shot
  guarded (restore re-runs onSpawn). isBoss json carries the die-flow
  and the SkeletonKey.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    act = function(self)
        if math.random(0, 20) == 0 then
            RPD.CharUtils:spawnOnNextCell(self, "SpiderEgg",
                math.floor(100 * RPD.GameLoop:getDifficultyFactor()))
        end
    end,

    attackProc = function(self, enemy, dmg)
        if enemy ~= nil and math.random(2) == 1 then
            RPD.Buffs.Buff:affect(enemy, "Poison",
                math.random(7, 9) * RPD.Buffs.Poison:durationFactor(enemy))
        end
        return dmg
    end,

    canAttack = function(self, enemy)
        return self:hp() > self:ht() / 2 and self:distance(enemy) == 1
    end,

    getCloser = function(self, target, ignorePets)
        if self:hp() < self:ht() / 2
                and self:getState():getTag() == "HUNTING"
                and RPD.Dungeon.level:distance(self:getPos(), target) < 5 then
            return self:getFurther(target)
        end
        return false
    end,

    spawn = function(self, level)
        local data = mob.restoreData(self)
        if data.gearGranted then
            return
        end
        data.gearGranted = true

        local dice = math.random()
        if dice < 0.33 then
            self:collect(RPD.ItemFactory:itemByName("ChaosCrystal"))
        elseif dice < 0.66 then
            self:collect(RPD.ItemFactory:itemByName("SpiderCharm"))
        else
            self:collect(RPD.ItemFactory:itemByName("SpiderArmor"))
        end
    end,

    die = function(self, cause)
        RPD.CharUtils:validateBossSlain("SPIDER_QUEEN_SLAIN")
    end,
}
