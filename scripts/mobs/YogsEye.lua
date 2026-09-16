--[[
  YogsEye - the Old God proper (batch 17d-2, was mobs/guts/YogsEye.java).
  Sleeps until the hero enters its arena; gazes along the magic ray cast
  from the organ above (the cell one width-row up), beaming every char on
  it. Being hurt draws every surviving Yog organ to the eye and halves the
  incoming damage per organ (live shift - in java the isBoss scan counted
  nothing, the organs were never bosses); larvae pour in while the cap
  allows. isBoss json carries the die-flow and the SkeletonKey; the battle
  music keys ride the same json. The eye's own spawn hook places 2-3
  distinct organs (HallsBossLevel just places the eye now) - the level
  argument is creation-safe, the organsSpawned flag survives saves via
  script data.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

local organKinds = { "RottingFist", "BurningFist", "YogsBrain", "YogsHeart", "YogsTeeth" }

-- nearest passable char-free cell to fromPos, java Level.getNearestTerrain
-- semantics: all cells at the minimal path distance, random pick among them
local function nearestFreeCell(level, fromPos)
    local best, bestD = nil, nil
    for i = 0, level:getLength() - 1 do
        if level.passable[i + 1] and not RPD.Actor:findChar(i) then
            local d = level:distance(fromPos, i)
            if bestD == nil or d < bestD then
                best, bestD = { i }, d
            elseif d == bestD then
                best[#best + 1] = i
            end
        end
    end
    if best == nil then
        return -1
    end
    return best[math.random(#best)]
end

return mob.init{
    canAttack = function(self, enemy)
        local level = RPD.Dungeon.level
        local enemyPos = enemy:getPos()
        -- the gaze originates one row above the eye's cell
        RPD.Ballistica:cast(self:getPos() - level:getWidth(), enemyPos, true, false)
        local trace = RPD.Ballistica.trace
        for i = 2, RPD.Ballistica.distance do
            if trace[i] == enemyPos then
                return true
            end
        end
        return false
    end,

    zapProc = function(self, enemy, damage)
        RPD.CharUtils:beamStrike(self, enemy,
                self:getPos() - RPD.Dungeon.level:getWidth(), "Eye_Kill", 3)
        return damage
    end,

    damage = function(self, dmg, src)
        local level = RPD.Dungeon.level
        local mobs = level:getMobs()
        local shift = 0
        for i = 1, #mobs do
            local m = mobs[i]
            local kind = m:getEntityKind()
            if kind == "RottingFist" or kind == "BurningFist"
                    or kind == "YogsBrain" or kind == "YogsHeart"
                    or kind == "YogsTeeth" then
                m:beckon(self:getPos())
                shift = shift + 1
            end
        end

        RPD.CharUtils:spawnOnNextCell(self, "Larva",
                math.floor(10 * RPD.GameLoop:getDifficultyFactor()))

        if shift > 0 then
            return math.floor(dmg / 2 ^ shift)
        end
        return dmg
    end,

    die = function(self, cause)
        RPD.CharUtils:validateBossSlain("YOG_SLAIN")
        self:yell(RPD.textById("Yog_Info1"))
    end,

    notice = function(self)
        self:yell(RPD.textById("Yog_Info2"))
    end,

    spawn = function(self, level)
        local data = mob.restoreData(self)
        if data.organsSpawned then
            return
        end
        data.organsSpawned = true

        local count = RPD.GameLoop:getDifficulty() > 2 and 3 or 2
        local chosen = {}
        local picked = 0
        while picked < count do
            local kind = organKinds[math.random(#organKinds)]
            if not chosen[kind] then
                chosen[kind] = true
                picked = picked + 1
            end
        end

        for kind in pairs(chosen) do
            local organ = RPD.MobFactory:mobByName(kind)
            local pos = nearestFreeCell(level, self:getPos())
            if pos >= 0 then
                organ:setPos(pos)
                level:spawnMob(organ)
            end
        end
    end,
}
