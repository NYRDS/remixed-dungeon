--[[
  KingPedestal (batch 17d-4, scripts/ai state for the King boss, replaces
  King.java's getCloser override + zap-summon flow).
  Active while the king can still raise servants: he walks to the nearest
  free pedestal (never the one used for the previous summoning) and, once
  there, calls up an undead army - 50% plain Undead, otherwise undead
  versions of Monk/Warlock/Golem/Senior (green tint, no XP). The army cap
  grows as the king loses hp. When the cap is reached the state hands the
  turn back to Hunting: Mob.act re-dispatches same-turn state switches,
  so no time is lost on the transition.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"
local ai = require "scripts/lib/ai"
local WandOfBlink = luajava.bindClass("com.watabou.pixeldungeon.items.wands.WandOfBlink")

local MAX_ARMY_SIZE = 5

-- java Level.adjacent is a raw cell-diff check (rows can "wrap"); port it
-- verbatim instead of a proper chebyshev
local function adjacent(level, a, b)
    local diff = math.abs(a - b)
    local W = level:getWidth()
    return diff == 1 or diff == W or diff == W + 1 or diff == W - 1
end

-- nearest pedestal, java side: min path distance over top level objects,
-- random tie-break, lastPedestal excluded (Level.nearestLevelObject)
local function nearestPedestal(self, data)
    return RPD.Dungeon.level:nearestLevelObject(self:getPos(), "pedestal", data.lastPedestal or -1)
end

local function countServants()
    local count = 0
    for _, m in pairs(RPD.Dungeon.level:getMobs()) do
        if m:isUndead() and not m:isPet() then
            count = count + 1
        end
    end
    return count
end

local function maxArmySize(self)
    -- java: (int)(1 + 5 * (ht-hp)/ht * difficultyFactor), integer division
    -- of 5*(ht-hp) by ht happens before the float multiply
    local missing = self:ht() - self:hp()
    local factor = RPD.GameLoop:getDifficultyFactor()
    return math.floor(1 + MAX_ARMY_SIZE * math.floor(missing / self:ht()) * factor)
end

local function canTryToSummon(self, data)
    if not data.targetPedestal or data.targetPedestal < 0 then
        return false
    end

    if countServants() >= maxArmySize(self) then
        return false
    end

    local occupant = RPD.Actor:findChar(data.targetPedestal)
    return occupant == self or occupant == nil
end

local function summon(self, data)
    data.lastPedestal = data.targetPedestal

    self:getSprite():centerEmitter():start(RPD.Sfx.Speck:factory(RPD.Sfx.Speck.SCREAM), 0.4, 2)
    RPD.playSound("snd_challenge")

    local level = RPD.Dungeon.level
    local toSummon = maxArmySize(self) - countServants()

    for _ = 1, toSummon do
        local pos = level:getEmptyCellNextTo(data.lastPedestal)

        if level:cellValid(pos) then
            local kind = "Undead"
            if math.random(2) == 1 then
                kind = ({ "Monk", "Warlock", "Golem", "Senior" })[math.random(4)]
            end

            local servant = RPD.MobFactory:mobByName(kind)
            WandOfBlink:appear(servant, pos)

            -- plain Undead kind is born undead (json); raised city mobs
            -- get the flag, no XP and the deathly green tint
            if kind ~= "Undead" then
                servant:setUndead(true)
                servant:setExpForKill(0)
                RPD.setAi(servant, "Hunting")
                servant:getSprite():tint(0x225522, 0.5)
                local flare = luajava.newInstance("com.watabou.pixeldungeon.effects.Flare", 3, 32)
                flare:color(0x000000, false):show(servant:getSprite(), 2)
            end
        end
    end

    self:yell(RPD.textById("King_Info2"))
end

return ai.init({
    act = function(self, ai, me)
        local data = mob.restoreData(me)

        if not canTryToSummon(me, data) then
            -- cap reached (or no free pedestal): hand the turn to Hunting -
            -- spending nothing here lets Mob.act continue into the new
            -- state within the same turn
            RPD.setAi(me, "Hunting")
            return
        end

        -- summoning must not suppress melee
        local enemy = me:getEnemy()
        if enemy and enemy:valid()
                and adjacent(RPD.Dungeon.level, me:getPos(), enemy:getPos()) then
            me:doAttack(enemy)
            return
        end

        if me:getPos() == data.targetPedestal then
            summon(me, data)
            -- next walks must head for a different pedestal
            data.targetPedestal = nearestPedestal(me, data)
            me:spend(1 / me:speed())
            return
        end

        me:doStepTo(data.targetPedestal)
    end,

    status = function(self, ai, me)
        return RPD.format("Mob_StaHuntingStatus", me:getName())
    end,

    -- helpers shared with scripts/mobs/King.lua
    nearestPedestal = nearestPedestal,
    canTryToSummon = canTryToSummon,
})
