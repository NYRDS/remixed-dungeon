--[[
  Deathling - necromancy summon (batch 17c-2b,
  was com.nyrds.pixeldungeon.mobs.common.Deathling.java).
  A fallen hero's soul serving the summoner; its stats scale off the
  owner hero (lvl + skillLevel^2) and refresh every act. The first act
  after summoning fills the new hp total (firstAct, script data - rides
  the serpent save round-trip like Goo's pumpedUp). Summoned by the
  SummonDeathling spell, which makePet()s it.
  Accepted delta: the java ARTIFACT/LEFT_ARTIFACT equipment-slot override
  is dropped (pets are never equipped by the game).
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

local HEALTH = 4

local function modifier(self)
    local owner = self:getOwner()
    if owner == nil or not owner:valid() then
        return 0
    end
    return owner:lvl() + owner:skillLevel() * owner:skillLevel()
end

return mob.init{
    act = function(self)
        local m = modifier(self)
        self:ht(HEALTH + m)

        local data = mob.restoreData(self)
        if not data.firstActDone then
            data.firstActDone = true
            self:heal(self:ht(), self)
        end
    end,

    defenseSkill = function(self, enemy)
        return 1 + modifier(self)
    end,

    attackSkill = function(self, target)
        return 4 + modifier(self)
    end,

    damageRoll = function(self)
        return math.random(1, 4 + modifier(self))
    end,

    dr = function(self)
        return modifier(self)
    end,

    spawn = function(self, level)
        self:setSkillLevel(3)
    end,
}
