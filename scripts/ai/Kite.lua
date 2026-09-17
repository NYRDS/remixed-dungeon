--[[
  Kite (generic ranged-distance AI state, batch 17d-4 follow-up).
  Replaces the seven hand-rolled act/getCloser kite policies (Scorpio,
  Acidic, AirElemental, ShamanElder, SpiderMind, SpiderMindAmber,
  SpiderQueen): the mob attacks while in position and backs off when not.

  The stock Hunting/Fleeing state objects provide the actual turn -
  approach+attack / retreat - and this state re-asserts itself after each
  delegated turn, so it owns the policy while buff-driven states (Terror,
  amok) still win for their duration. Same-turn handoffs are bounded by
  Mob.act's state-loop cap.

  Per-mob tuning rides the mob script data (set in stats, survives saves):
    kiteRanged        - retreat when the enemy is unseen or not in a clean
                        ranged line (canDoOnlyRangedAttack gate)
    kiteMinDist       - retreat while the enemy is closer than this (cells)
    kiteNeverApproach - retreat whenever the enemy is seen but cannot be
                        attacked (buff-bots that never close in)
    kiteBelowHp       - only kite while hp is below this fraction of max
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"
local ai = require "scripts/lib/ai"

local hunting = RPD.MobAi:getStateByTag("Hunting")
local fleeing = RPD.MobAi:getStateByTag("Fleeing")

local function shouldRetreat(me, enemy, data)
    if data.kiteBelowHp and me:hp() >= me:ht() * data.kiteBelowHp then
        return false
    end

    if data.kiteRanged then
        return not me.enemySeen or not RPD.CharUtils:canDoOnlyRangedAttack(me, enemy)
    end

    if data.kiteNeverApproach then
        return me.enemySeen and not me:canAttack(enemy)
    end

    if data.kiteMinDist then
        return RPD.Dungeon.level:distance(me:getPos(), enemy:getPos()) < data.kiteMinDist
    end

    return false
end

return ai.init({
    act = function(self, ai, me)
        local data = mob.restoreData(me)
        local enemy = me:getEnemy()

        if enemy ~= nil and enemy:valid() and shouldRetreat(me, enemy, data) then
            fleeing:act(me)
        else
            hunting:act(me)
        end

        -- the delegated state may have spent the turn or handed off
        -- (Wandering fallback): take the policy back for the next one
        RPD.setAi(me, "Kite")
    end,

    status = function(self, ai, me)
        return RPD.format("Mob_StaHuntingStatus", me:getName())
    end,
})
