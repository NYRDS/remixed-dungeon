--[[
  Tengu (batch 17d-1, was actors/mobs/Tengu.java).
  Prison boss: while the enemy is visible Tengu jumps instead of walking
  (getCloser hook), and every 5th attack next to the hero is a jump too
  (dodge, doAttack hook). Jumps re-arm up to 4 random traps into poison
  traps and reveal them; with no free cell to land on Tengu falls back to
  a potion-style 10% heal.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"
local JUMP_DELAY = 5

-- java Level.adjacent is a raw cell-diff check (rows can "wrap"); port it
-- verbatim instead of a proper chebyshev
local function adjacent(level, a, b)
    local diff = math.abs(a - b)
    local W = level:getWidth()
    return diff == 1 or diff == W or diff == W + 1 or diff == W - 1
end

local function rearmTraps(level)
    local traps = {}
    for _, obj in pairs(level:getLevelObjects()) do
        if obj:isTrap() then
            traps[#traps + 1] = obj
        end
    end

    for _ = 1, 4 do
        if #traps == 0 then
            break
        end
        local trap = traps[math.random(#traps)]
        trap:reactivate("PoisonTrap", 1)
        RPD.ScrollOfMagicMapping:discover(trap:getPos())
    end
end

local function jump(self)
    local data = mob.restoreData(self)
    data.timeToJump = JUMP_DELAY

    local level = RPD.Dungeon.level
    rearmTraps(level)

    local enemyPos = self:getEnemy():getPos()
    local candidates = {}
    for i = 0, level:getLength() - 1 do
        if level.fieldOfView[i + 1]
                and level.passable[i + 1]
                and not adjacent(level, i, enemyPos)
                and RPD.Actor:findChar(i) == nil then
            candidates[#candidates + 1] = i
        end
    end

    if #candidates == 0 then
        -- java fell back to PotionOfHealing.heal(this, 0.1f)
        self:heal(math.floor(self:ht() * 0.1), self)
        self:detachBuff("Poison")
        self:detachBuff("Cripple")
        self:detachBuff("Weakness")
        self:detachBuff("Bleeding")
        self:spend(1 / self:speed())
        return
    end

    local newPos = candidates[math.random(#candidates)]
    self:move(newPos)

    if RPD.CharUtils:isVisible(self) then
        RPD.Sfx.CellEmitter:get(newPos):burst(RPD.Sfx.Speck:factory(RPD.Sfx.Speck.WOOL), 6)
        RPD.playSound("snd_puff")
    end

    self:spend(1 / self:speed())
end

return mob.init{
    spawn = function(self, level)
        local heroClass = RPD.Dungeon.hero:getHeroClass():name()
        local bag = self:getBelongings()

        if heroClass ~= "NECROMANCER" and heroClass ~= "GNOLL" and heroClass ~= "DOCTOR"
                and not bag:getItem("TomeOfMastery") then
            self:collect(RPD.item("TomeOfMastery"))
        end

        if heroClass == "GNOLL" and not bag:getItem("TenguLiver") then
            self:collect(RPD.item("TenguLiver"))
        end
    end,

    getCloser = function(self, target, ignorePets)
        local level = RPD.Dungeon.level
        if level.fieldOfView[target + 1] then
            -- refund doStepTo's pre-charge; jump() spends the real cost
            self:spend(-1 / self:speed())
            jump(self)
            return true
        end
        return nil
    end,

    doAttack = function(self, enemy)
        local data = mob.restoreData(self)
        data.timeToJump = (data.timeToJump or JUMP_DELAY) - 1
        if data.timeToJump <= 0
                and adjacent(RPD.Dungeon.level, self:getPos(), enemy:getPos()) then
            jump(self)
            return true
        end
        return nil
    end,

    notice = function(self)
        local heroClass = RPD.Dungeon.hero:getHeroClass()
        local id = "Tengu_Info2"
        if heroClass:getGender() == 2 then
            id = "Tengu_Info3"
        end
        self:yell(RPD.textById(id):format(heroClass:title()))
    end,

    die = function(self, cause)
        RPD.CharUtils:validateBossSlain("BOSS_SLAIN_2")
        self:say(RPD.textById("Tengu_Info1"))
    end,
}
